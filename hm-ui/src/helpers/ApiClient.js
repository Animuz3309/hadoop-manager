import superagent from 'superagent';
import config from '../config';
import {replace} from 'react-router-redux';
import {logout} from '../redux/modules/auth/auth';

const methods = ['get', 'post', 'put', 'patch', 'del'];

function formatUrl(path) {
  const protocol = typeof(window) !== 'undefined' ? window.location.protocol : 'http:';
  const adjustPath = path[0] !== '/' ? '/' + path : path;
  if (!__API_PROXY__ || __SERVER__) {
    return protocol + '//' + config.apiHost + adjustPath;
  }
  return '/api' + adjustPath;
}

export default class ApiClient {
    constructor(req) {
      methods.forEach((method) => {
        this[method] = (path, {params, data, contentType} = {}) => new Promise((resolve, reject) => {
          const request = superagent[method](formatUrl(path));
          this.sendRequest(params, data, contentType, request);
          request.end((err, response = {}) => {
            this.handleResponse(err, response, resolve, reject);
          });
        });
      });
    }

    sendRequest(params, data, contentType, request) {
      this.setToken(request);
      if (params) {
        request.query(params);
      }
      if (data) {
        if (contentType) {
          request.set('Content-Type', contentType);
        }
        request.send(data);
      }
    }

    handleResponse(err, response, resolve, reject) {
      let {body} = response;
      if (err) {
        this._handleAuth(response);
        reject(body || err);
      } else {
        let res = body ? body : {};
        if (!(res instanceof Array)) {
          res._res = response;
        }
        resolve(res);
      }
    }

    _handleAuth(response) {
      if (!(this._store
            && response
            && (response.status === 401
                || (typeof response.status === 'undefined' && typeof response.statusCode === 'undefined')))) {
        return false;
      }
      let backLocation;
      let state = this._store.getState();
      let stateLocation = state.routing.locationBeforeTransitions;
      if (stateLocation && stateLocation.pathname) {
        backLocation = stateLocation.pathname !== '/login' ? stateLocation.pathname : '/dashboard';
      }

      this._store.dispatch(logout);

      if (backLocation && backLocation !== '') {
        window.location.assign('/login' + '?back=' + backLocation);
        return true;
      }
      this._store.dispatch(replace('/login'));
      return false;
    }

    _store;

    setStore(store) {
      this._store = store;
    }

    setToken(request) {
      let auth = this._store.getState().auth;
      if (auth && auth.token) {
        request.set('X-Auth-Token', auth.token.key);
      }
    }
}
