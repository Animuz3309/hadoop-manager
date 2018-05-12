import { combineReducers } from 'redux';
import multireducer from 'multireducer';
import { routerReducer } from 'react-router-redux';
import {reducer as reduxAsyncConnect} from 'redux-async-connect';

import auth from './auth/auth';
export default combineReducers({
  routing: routerReducer,
  reduxAsyncConnect,
  auth
});
