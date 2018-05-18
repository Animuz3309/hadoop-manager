import { combineReducers } from 'redux';
import multireducer from 'multireducer';
import { routerReducer } from 'react-router-redux';
import {reducer as reduxAsyncConnect} from 'redux-async-connect';

import auth from './auth/auth';
import {reducer as form} from 'redux-form';
import menuLeft from './menuLeft/menuLeft';
import clusters from './clusters/clusters';
import clustersUI from './clusters/clustersUI';
import nodes from './nodes/nodes';
import nodesUI from './nodes/nodesUI';
import registries from './registries/registries';
import registriesUI from './registries/registriesUI';
import events from './events/events';
import settings from './settings/settings';
import users from './users/users';
import errors from './errors/errors';

export default combineReducers({
  routing: routerReducer,
  reduxAsyncConnect,
  auth,
  form,
  menuLeft,
  // Begin models data
  clusters,
  nodes,
  registries,
  events,
  settings,
  users,
  // End models data
  registriesUI,
  clustersUI,
  nodesUI,
  errors
});
