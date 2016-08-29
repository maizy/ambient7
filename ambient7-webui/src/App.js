import React from 'react';
import NavLink from './modules/NavLink';

class App extends React.PureComponent {
  render() {
    return (
      <div>
        <header>
          <h1><NavLink to="/">Ambient7</NavLink></h1>
        </header>
        <nav>
          <ul>
            <li><NavLink to="/devices">Devices</NavLink></li>
            <li><NavLink to="/about">About</NavLink></li>
          </ul>
        </nav>
        {this.props.children}
      </div>
    );
  }
}

export default App;
