import React from 'react';
import NavLink from './NavLink';

export default function Navigation() {
  return (
    <nav className="navbar navbar-inverse navbar-fixed-top">
      <div className="container">
        <div className="navbar-header">
          <NavLink className="navbar-brand" to="/">Ambient7</NavLink>
        </div>
        <div id="navbar">
          <ul className="nav navbar-nav">
            <li><NavLink to="/">Home</NavLink></li>
            <li><NavLink to="/about">About</NavLink></li>
          </ul>
        </div>
      </div>
    </nav>
  );
}
