import SockJS from 'sockjs-client';
import { Stomp } from 'stompjs/lib/stomp.min.js';
import _ from 'lodash';

import config from '../../../config';
import { ACTIONS } from '../../../redux/modules/events/actions';

// todo jobs event listener
export function connectWebsocketEventsListener(store) {
  let url = config.eventServer;

  if (!url.startsWith('http')) {
    url = `http://${url}`;
  }

  let state = store.getState();
  let token;

  if (state && state.auth && state.auth.token) {
    url = `${url}?token=${state.auth.token.key}`;
  }

  let ws = new SockJS(url);
  let stompClient = Stomp.over(ws);

  let stompHeaders = {
    command: 'CONNECT',
    header: {
      'accept-version': '1.1,1.0',
      'heart-beat': '10000,10000',
      'client-id': config.app.title
    },
    body: ''
  };

  stompClient.debug = false;

  stompClient.connect(stompHeaders, (connectFrame) => {
    stompClient.send('/app/subscriptions/available');

    stompClient.subscribe('/user/queue/subscriptions/get', (message) => {
      console.log('Current subscription: ', message.body);
    });

    stompClient.subscribe('/user/queue/subscriptions/available', (message) => {
      console.log('Available channels: ', message.body);
    });

    stompClient.subscribe('/user/queue/*', (message) => {
      if (message.headers && message.body) {
        let destination = _.get(message.headers, 'destination', '').match(/[^/]+.$/g);
        let key = destination && destination[0] ? destination[0] : null;
        switch (key) {
          case 'bus.hm.errors-stats':
            store.dispatch({
              type: ACTIONS.NEW_STAT_EVENT,
              topic: key,
              event: JSON.parse(message.body)
            });
            return;
          default:
            return;
        }
      }
    });
    let yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    let defaultChannels = [
      {
        source: 'hm.cluman.errors-stats',
        historyCount: 7,
        historySince: yesterday
      }
    ];
    stompClient.send('/app/subscriptions/add', {}, JSON.stringify(defaultChannels));
  }, (error) => {
  });
}
