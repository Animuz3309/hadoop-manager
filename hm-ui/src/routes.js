import React from 'react';
import {IndexRoute, Route} from 'react-router';
import {
  App,
  Dashboard,
  ClustersPanel,
  NodesPanel,
  Login,
  LoginSuccess,
  NotFound,
  SettingsPanel,
  AgentPanel,
} from 'containers';

export default (store) => {
  const requireLogin = (nextState, replace, cb) => {
    function checkNotAuth() {
      const { auth: { user }} = store.getState();
      if (!user) {
        replace('/login');
      }
      cb();
    }

    checkNotAuth();
  };

  const redirectLogin = (nextState, replace, cb) => {
    let redirect = '/dashboard';
    if (window && window.location.search) {
      let search = window.location.search.match(/\?back=(.+)/);
      if (search && search[1]) {
        redirect = search[1];
      }
    }

    function checkAuth() {
      const { auth: { user }} = store.getState();
      if (user) {
        replace(redirect);
      }
      cb();
    }
    console.log('redirect: ', redirect);
    checkAuth();
  };

  return (
		<Route name="Home" path="/" component={App}>
      <Route onEnter={requireLogin}>
        <IndexRoute name="Dashboard" component={Dashboard}/>

        <Route name="Dashboard" path="dashboard" component={Dashboard}/>
        <Route name="Login Successful" path="loginSuccess" component={LoginSuccess}/>
        <Route name="Clusters" path="clusters" component={ClustersPanel}/>
        <Route name="Nodes" path="nodes" component={NodesPanel}/>
        <Route name="Nodes" path="clusters/:name/nodes" component={NodesPanel}/>
        <Route name="Settings" path="settings" component={SettingsPanel}/>
        <Route name="Agent" path="agent" component={AgentPanel}/>
      </Route>

			{ /* Public Routes */ }
			<Route onEnter={redirectLogin}>
				<Route name="Login" path="login" component={Login}/>
			</Route>
      <Route name="Not Found" path="*" component={NotFound} status={404}/>
		</Route>
	);
};
