/**
 * It is an webpack entries for client
 */
import 'babel-polyfill';
import React from 'react';
import ReactDOM from 'react-dom';
import createStore from './redux/create';
import ApiClient from './helpers/ApiClient';
import io from 'socket.io-client';
import {Provider} from 'react-redux';
import { Router, browserHistory } from 'react-router';
import { ReduxAsyncConnect } from 'redux-async-connect';
import useScroll from 'scroll-behavior/lib/useStandardScroll';
import {loadFromLS as loadAuth} from 'redux/modules/auth/auth';
import {loadFromLS as loadMenuLeft} from 'redux/modules/menuLeft/menuLeft';
import {ConfirmDialog, SimpleModal} from './components/index';

import getRoutes from './routes';

const client = new ApiClient();
const history = useScroll(() => browserHistory)();
const dest = document.getElementById('content');
const store = createStore(history, client, window.__data);
client.setStore(store);

//loadAuth && loadMenuLeft should be before rendering
store.dispatch(loadAuth());
store.dispatch(loadMenuLeft());

const component = (
	<Router render={(props) =>
		<ReduxAsyncConnect {...props} helpers={{client}} filter={item => !item.deferred} />
	} history={history}>
		{getRoutes(store)}
	</Router>
);

ReactDOM.render(
	<Provider store={store} key="provider">
		{component}
	</Provider>,
	dest
);

if (process.env.NODE_ENV !== 'production') {
  window.React = React; // enable debugger
}

if (__DEVTOOLS__ && !window.devToolsExtension) {
  const DevTools = require('./containers/DevTools/DevTools');
  ReactDOM.render(
		<Provider store={store} key="provider">
			<div>
				{component}
				<DevTools />
			</div>
		</Provider>,
		dest
	);
}

(() => {
  ConfirmDialog.initJs();
  SimpleModal.initJs(store);
})();
