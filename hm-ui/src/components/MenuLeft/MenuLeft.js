import React, {Component, PropTypes} from 'react';
import {Link, browserHistory} from 'react-router';
import {Alert} from 'react-bootstrap';
import {toggle} from "../../redux/modules/menuLeft/menuLeft";
import {logout} from "../../redux/modules/auth/auth";
import {connect} from "react-redux";
