const _ACTIONS = {
  GET_CURRENT_USER: 'GET_CURRENT_USER',
  GET_CURRENT_USER_SUCCESS: 'GET_CURRENT_USER_SUCCESS',
  GET_CURRENT_USER_FAIL: 'GET_CURRENT_USER_FAIL',
  //todo more user actions
};

Object.keys(_ACTIONS).forEach((key) => {
  _ACTIONS[key] = 'users/' + _ACTIONS[key];
});

export const ACTIONS = _ACTIONS;
