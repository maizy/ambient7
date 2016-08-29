import React from 'react';
import { render } from 'react-dom';
import { Router, Route, hashHistory } from 'react-router';

import App from './App';
import Devices from './components/Devices';
import Device from './components/Device';
import About from './components/About';


window.React = React;

// const opts = window.Ambient7Opts;

render(
    <Router history={hashHistory}>
      <Route path="/" component={App}>
        <Route path="/about" component={About} />
        <Route path="/devices" component={Devices} />
        <Route path="/devices/:deviceId" component={Device} />
      </Route>
    </Router>,
    document.getElementById('content')
);
