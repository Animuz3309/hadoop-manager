import {ACTIONS} from './actions';
const initialState = {
  loading: false,
  loadingError: null
};
export default function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case ACTIONS.LOAD_IMAGES:
      return {
        ...state,
        loading: true,
        loadingError: null
      };
    case ACTIONS.LOAD_IMAGES_SUCCESS:
      return {
        ...state,
        loading: false
      };
    case ACTIONS.LOAD_IMAGES_FAIL:
      return {
        ...state,
        loading: false,
        loadingError: "Cannot load images"
      };
    default:
      return state;
  }
}
