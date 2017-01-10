/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import React from 'react';
import { render } from 'react-dom';
import { Router, Route, IndexRoute, hashHistory } from 'react-router';

import App from './App';
import Co2Device from './components/Co2Device';
import About from './components/About';
import Home from './components/Home';


window.React = React;

render(
    <Router history={hashHistory}>
      <Route path="/" component={App}>
        <IndexRoute component={Home} />
        <Route path="/about" component={About} />
        <Route path="/devices/:deviceId" component={Co2Device} />
      </Route>
    </Router>,
    document.getElementById('content')
);
