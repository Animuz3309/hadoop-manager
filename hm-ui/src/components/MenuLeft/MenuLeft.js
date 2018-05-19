import React, {Component, PropTypes} from 'react';
import {Link, browserHistory} from 'react-router';
import {Alert} from 'react-bootstrap';
import {toggle} from "../../redux/modules/menuLeft/menuLeft";
import {logout} from "../../redux/modules/auth/auth";
import {connect} from "react-redux";
import {getCurrentUser} from "../../redux/modules/users/users";
import _ from "lodash";
import {Dialog} from "../index";
import {connectWebsocketEventsListener} from '../common/EventListener/EventListener';

//todo add event listener "ws"
@connect(
  state => ({
    toggled: state.menuLeft.toggled,
    user: state.auth.user,
    users: state.users
  }),
  {toggle, getCurrentUser, logout}
)
export default class MenuLeft extends Component {
  static propTypes = {
    toggled: PropTypes.bool,
    toggle: PropTypes.func.isRequired,
    user: PropTypes.object,
    users: PropTypes.object,
    getCurrentUser: PropTypes.func,
    logout: PropTypes.func
  };

  static contextTypes = {
    store: PropTypes.object
  };

  handleLogout = (event) => {
    event.preventDefault();
    this.props.logout();
    window.location.assign('/login');
  };

  componentWillMount() {
    const {getCurrentUser} = this.props;
    getCurrentUser().then(() => {
      const {users} = this.props;
      if (!users.currentUser.credentialsNonExpired) {
        this.showPasswordChange("You need to change the present password to continue.");
      }
    });
  }

  componentDidMount() {
    this.checkSideBarCollapsed();
    connectWebsocketEventsListener(this.context.store);
  }

  showPasswordChange(title) {
    this.setState({
      actionDialog: (
        <Dialog
          title={title}
          onHide={this.onHideDialog.bind(this)}
          okTitle="Ok"
          hideCancel
          onSubmit={() => {
            browserHistory.push('/my_account');
            this.onHideDialog();
          }}
          show
        >
          <Alert bsStyle="info" className="margin-top-20">Click "OK" to change password</Alert>
        </Dialog>
      )
    });
  }

  onHideDialog() {
    const {getCurrentUser} = this.props;
    getCurrentUser().then(()=> {
      const {users} = this.props;
      if (!users.currentUser.credentialsNonExpired && window.location.pathname !== '/my_account') {
        this.setState({
          actionDialog: undefined
        });
        this.showPasswordChange("You need to change the preset password to continue.");
      } else {
        this.setState({
          actionDialog: undefined
        });
      }
    });

    this.setState({
      actionDialog: undefined
    });
  }

  render() {
    const {toggled, toggle, users} = this.props;
    let role = _.get(this.props, 'users.currentUser.role', '');

    return (
      <aside id="menu-left" className="al-sidebar">
        <div className="clearfix">
          <Link to="/dashboard" className="al-logo clearfix">
            <img src="/logo-white.png" title="Hadoop Manager Tool"/>
            <span className="product-name">HM Tool</span>
          </Link>
        </div>
        <ul className="al-sidebar-list">
          <li className="al-sidebar-list-item" title="Dashboard">
            <Link to="/dashboard" className="al-sidebar-list-link">
              <i className="fa fa-tachometer"/>
              <span>Dashboard</span>
            </Link>
          </li>
          <li className="al-sidebar-list-item" title="Clusters">
            <Link to="/clusters" className="al-sidebar-list-link">
              <i className="fa fa-object-group fa-fw"/>
              <span>Clusters</span>
            </Link>
          </li>

          <li className="al-sidebar-list-item" title="Containers">
            <Link to="/clusters/all" className="al-sidebar-list-link">
              <i className="fa fa-square-o"/>
              <span>Containers</span>
            </Link>
          </li>

          <li className="al-sidebar-list-item" title="Nodes">
            <Link to="/nodes" className="al-sidebar-list-link">
              <i className="fa fa-server fa-fw"/>
              <span>Nodes</span>
            </Link>
          </li>

          <li className="al-sidebar-list-item" title="Images">
            <Link to="/images" className="al-sidebar-list-link">
              <i className="fa fa-file-o fa-fw"/>
              <span>Images</span>
            </Link>
          </li>

          <li className="al-sidebar-list-item" title="Registries">
            <Link to="/registries" className="al-sidebar-list-link">
              <i className="fa fa-list fa-fw"/>
              <span>Registries</span>
            </Link>
          </li>

          <li className="al-sidebar-list-item" title="Jobs">
            <Link to="/jobs" className="al-sidebar-list-link">
              <i className="fa fa-cogs fa-fw"/>
              <span>Jobs</span>
            </Link>
          </li>

          <li className="al-sidebar-list-item" title="Change password">
            <Link className="al-sidebar-list-link" to="/my_account">
              <i className="fa fa-id-badge fa-fw"/>
              <span>My Account</span>
            </Link>
          </li>

          {role === 'ROLE_ADMIN' && (
            <li className="al-sidebar-list-item with-sub-menu" title="Admin">
              <Link className="al-sidebar-list-link" onClick={()=>this.showSubBlock('adminSublist')}>
                <i className="fa fa-briefcase fa-fw"/>
                <span>Admin</span>
                <b id="adminSublistAngle" className="fa fa-angle-down"/>
              </Link>
              <ul className="al-sidebar-sublist shown-sublist hidden-sublist" id="adminSublist">
                <li className="ba-sidebar-sublist-item" title="Users">
                  <Link to="/users" className="al-sidebar-list-link">
                    <span>Users</span>
                  </Link>
                </li>
                <li className="al-sidebar-list-item" title="Settings">
                  <Link to="/settings" className="al-sidebar-list-link">
                    <span>Settings</span>
                  </Link>
                </li>
                <li className="al-sidebar-list-item" title="Add Node">
                  <Link to="/agent" className="al-sidebar-list-link">
                    <span>Add Node</span>
                  </Link>
                </li>
              </ul>
            </li>
          )}

          <li className="al-sidebar-list-item" title="Sign Out">
            <Link className="al-sidebar-list-link" onClick={this.handleLogout}>
              <i className="fa fa-sign-out fa-fw"/>
              <span>Sign out</span>
            </Link>
          </li>

          <li id="expandIcon" className="al-sidebar-list-item" title="Expand">
            <Link to="#" className="al-sidebar-list-link" onClick = {this.expand}>
              <i className="fa fa-chevron-right fa-2x" data-direction="right" />
            </Link>
          </li>
        </ul>

        {(this.state && this.state.actionDialog) && (
          <div>
            {this.state.actionDialog}
          </div>
        )}
      </aside>
    );
  }

  showSubBlock(id) {
    this.checkSideBarCollapsed();
    let $subBlock = $("#" + id);
    let $angle = $('#' + id + 'Angle');
    if ($angle.hasClass('fa-angle-down')) {
      $angle.removeClass('fa-angle-down').addClass('fa-angle-up');
    } else {
      $angle.removeClass('fa-angle-up').addClass('fa-angle-down');
    }
    $subBlock.slideToggle(300);
  }

  checkSideBarCollapsed() {
    let $sidebar = $('#menu-left');
    if ($sidebar.width() < 160) {
      $sidebar.addClass('sidebar-collapsed');
    }
  }

  expand(e) {
    e.preventDefault();
    let $iconLi = $("#expandIcon");
    let $icon = $iconLi.find("i");
    let arrowDirection = $icon.attr("data-direction");
    let $mainContent = $(".al-main");
    let $footer = $(".al-footer-main");
    let $sidebar = $(".al-sidebar");

    if (arrowDirection === 'right') {
      $sidebar.addClass("sidebar-expanded").removeClass("sidebar-collapsed");
      $icon.removeClass("fa-chevron-right").addClass("fa-chevron-left");
      $mainContent.addClass("extra-margin-content");
      $footer.addClass('extra-margin-footer');
      $iconLi.attr("title", "Collapse");
      $icon.attr("data-direction", "left");
    } else {
      $sidebar.removeClass("sidebar-expanded").addClass("sidebar-collapsed");
      $icon.removeClass("fa-chevron-left").addClass("fa-chevron-right");
      $mainContent.removeClass("extra-margin-content");
      $footer.removeClass('extra-margin-footer');
      $iconLi.attr("title", "Expand");
      $icon.attr("data-direction", "right");
    }
  }
}
