import {ACTIONS} from './actions';
import _ from 'lodash';

//todo more user actions
export default function reducer(state = {}, action = {}) {
  switch (action.type) {
    case ACTIONS.GET_CURRENT_USER:
      return {
        ...state,
        loadingCurrentUser: true
      };
    case ACTIONS.GET_CURRENT_USER_FAIL:
      return {
        ...state,
        loadingCurrentUser: false
      };
    case ACTIONS.GET_CURRENT_USER_SUCCESS:
      let user = _.get(action.result, 'username', 'undefined');
      let role = _.get(action.result, 'roles[0].name', '');
      let credentialsState = _.get(action.result, 'credentialsNonExpired', true);
      return {
        ...state,
        loadingCurrentUser: false,
        currentUser: {
          name: user,
          role: role,
          credentialsNonExpired: credentialsState
        }
      };
    default:
      return state;
  }
}

export function getCurrentUser() {
  return {
    types: [ACTIONS.GET_CURRENT_USER, ACTIONS.GET_CURRENT_USER_SUCCESS, ACTIONS.GET_CURRENT_USER_FAIL],
    promise: (client) => client.get('/api/users/current/')
  };
}
