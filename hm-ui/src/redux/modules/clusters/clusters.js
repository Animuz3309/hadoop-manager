import {ACTIONS} from './actions';
import _ from 'lodash';
import {Cluster} from '../../models/common/Cluster';

/**
 * todo docker compose and jobs
 * @param {state} state cluster state
 * @param {action} action the action to do
 */
export default function reducer(state = {}, action = {}) {
  switch (action.type) {
    case ACTIONS.LOAD_SUCCESS:
      let clusters = action.result.map(row => {
        let data = Object.assign({}, state[row.name], row);
        return new Cluster({init: data});
      });
      return _.merge({}, state, _.keyBy(clusters, 'name'));

    case ACTIONS.GET_SOURCE_SUCCESS:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          source: action.result
        }
      };

    case ACTIONS.INFORMATION_SUCCESS:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          information: action.result
        }
      };

    case ACTIONS.LOAD_CONTAINERS_SUCCESS:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          containersList: action.result.map(container => container.id)
        }
      };

    case ACTIONS.LOAD_DEFAULT_PARAMS_SUCCESS:
      let defaultParams = Object.assign({[action.image]: {}}, state[action.id].defaultParams);
      defaultParams[action.image][action.tag] = _.omit(action.result, '_res');
      return {
        ...state,
        [action.id]: {...state[action.id], defaultParams}
      };

    case ACTIONS.LOAD_NODES_SUCCESS:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          nodesList: action.result
        }
      };

    case ACTIONS.LOAD_NODES_DETAILED_SUCCESS:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          nodesListDetailed: action.result
        }
      };

    case ACTIONS.UPLOAD_COMPOSE:
      return {
        ...state,
        uploadComposeError: null
      };
    case ACTIONS.UPLOAD_COMPOSE_FAIL:
      return {
        ...state,
        uploadComposeError: action.error.message
      };

    case ACTIONS.SET_SOURCE:
      return {
        ...state,
        setSourceError: null
      };
    case ACTIONS.SET_SOURCE_FAIL:
      return {
        ...state,
        setSourceError: action.error.message
      };
    case ACTIONS.LOAD_CLUSTER_REGISTRIES_SUCCESS:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          config: {
            ...state[action.id].config,
            registries: action.result
          }
        }
      };
    case ACTIONS.GET_CLUSTER_SUCCESS:
      return {
        ...state,
        [action.id]: action.result
      };
    default:
      return state;
  }
}

//====================================================== load clusters ==============================================//

export function isLoaded(globalState) {
  return globalState.clusters && (Object.keys(globalState.clusters).length > 0);
}

export function load() {
  return {
    types: [ACTIONS.LOAD, ACTIONS.LOAD_SUCCESS, ACTIONS.LOAD_FAIL],
    promise: (client) => client.get('/api/clusters/')
  };
}

//====================================================== CRUD clusters ==============================================//

export function getCluster(clusterId) {
  return {
    types: [ACTIONS.GET_CLUSTER, ACTIONS.GET_CLUSTER_SUCCESS, ACTIONS.GET_CLUSTER_FAIL],
    id: clusterId,
    promise: (client) => client.get(`/api/clusters/${clusterId}`)
  };
}

export function create(name, data) {
  return {
    types: [ACTIONS.CREATE, ACTIONS.CREATE_SUCCESS, ACTIONS.CREATE_FAIL],
    promise: (client) => client.put(`/api/clusters/${name}`, {data: data})
  };
}

export function update(name, data) {
  return {
    types: [ACTIONS.UPDATE, ACTIONS.UPDATE_SUCCESS, ACTIONS.UPDATE_FAIL],
    promise: (client) => client.patch(`/api/clusters/${name}`, {data: data})
  };
}

export function deleteCluster(clusterId) {
  console.log('test');
  return {
    types: [ACTIONS.DELETE, ACTIONS.DELETE_SUCCESS, ACTIONS.DELETE_FAIL],
    promise: (client) => client.del(`/api/clusters/${clusterId}`)
  };
}

export function clusterInformation(clusterId) {
  return {
    types: [ACTIONS.INFORMATION, ACTIONS.INFORMATION_SUCCESS, ACTIONS.INFORMATION_FAIL],
    promise: (client) => client.get(`/api/clusters/${clusterId}/info`)
  };
}

//====================================================== load containers ==============================================//

export function loadContainers(clusterId) {
  return {
    types: [ACTIONS.LOAD_CONTAINERS, ACTIONS.LOAD_CONTAINERS_SUCCESS, ACTIONS.LOAD_CONTAINERS_FAIL],
    id: clusterId,
    promise: (client) => client.get(`/api/clusters/${clusterId}/containers`)
  };
}

//====================================================== load nodes ==============================================//

export function loadNodes(clusterId) {
  return {
    types: [ACTIONS.LOAD_NODES, ACTIONS.LOAD_NODES_SUCCESS, ACTIONS.LOAD_NODES_FAIL],
    id: clusterId,
    promise: (client) => client.get(`/api/clusters/${clusterId}/nodes`)
  };
}

export function loadNodesDetailed(clusterId) {
  return {
    types: [ACTIONS.LOAD_NODES_DETAILED, ACTIONS.LOAD_NODES_DETAILED_SUCCESS, ACTIONS.LOAD_NODES_DETAILED_FAIL],
    id: clusterId,
    promise: (client) => client.get(`/api/clusters/${clusterId}/nodes-detailed`)
  };
}

//====================================================== load registries ==============================================//

export function loadClusterRegistries(clusterId) {
  return {
    types: [ACTIONS.LOAD_CLUSTER_REGISTRIES, ACTIONS.LOAD_CLUSTER_REGISTRIES_SUCCESS, ACTIONS.LOAD_CLUSTER_REGISTRIES_FAIL],
    id: clusterId,
    promise: (client) => client.get(`/api/clusters/${clusterId}/registries`)
  };
}
