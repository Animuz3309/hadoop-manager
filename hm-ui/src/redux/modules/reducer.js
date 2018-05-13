import { combineReducers } from 'redux';
import multireducer from 'multireducer';
import { routerReducer } from 'react-router-redux';
import {reducer as reduxAsyncConnect} from 'redux-async-connect';

import auth from './auth/auth';
import {reducer as form} from 'redux-form';
import menuLeft from './menuLeft/menuLeft';
import errors from './errors/errors';

export default combineReducers({
  routing: routerReducer,
  reduxAsyncConnect,
  auth,
  form,
  menuLeft,
  // Begin models data
  // End models data
  errors
});
