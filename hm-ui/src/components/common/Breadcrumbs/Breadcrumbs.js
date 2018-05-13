import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';

export default class Breadcrumbs extends Component {
  static propTypes = {
    routes: PropTypes.arrayOf(PropTypes.object).isRequired,
    params: PropTypes.object
  };

  render() {
    return (
      this.buildCrumbs()
    );
  }

  buildCrumbs() {
    let {routes, params} = this.props;
    let route = routes.pop();
    let breadcrumbs = '';
    if ($.isEmptyObject(params)) {
      breadcrumbs = (
        <ul className="breadcrumb">
          <li className="active">{route.name}</li>
        </ul>
      );
    } else {
      let activeName = params.subname ? params.subname : route.name;
      if (route.name === "Job Detailed View") {
        activeName = params.name;
        breadcrumbs = (
          <ul className="breadcrumbs">
            <li><Link to="/jobs"/>Jobs</li>
            <li className="active">{activeName}</li>
          </ul>
        );
      } else if (route.name === 'Network Detailed') {
        breadcrumbs = (
          <ul className="breadcrumbs">
            <li><Link to="/clusters">Clusters</Link></li>
            <li><Link to={"/clusters" + "/" + params.name}>{params.name}</Link></li>
            <li><Link to={"/clusters" + "/" + params.name + "/networks"}/>Networks</li>
            <li className="active">{activeName}</li>
          </ul>
        );
      } else {
        breadcrumbs = (
          <ul className="breadcrumbs">
            <li><Link to="/clusters">Clusters</Link></li>
            <li><Link to={"/clusters" + "/" + params.name}>{params.name}</Link></li>
            <li className="active">{activeName}</li>
          </ul>
        );
      }
    }
    return breadcrumbs;
  }
}
