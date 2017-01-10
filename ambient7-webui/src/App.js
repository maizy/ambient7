/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import React from 'react';
import Navigation from './modules/Navigation';

function App(props) {
  return (
      <div>
        <Navigation />
        {props.children}
      </div>
  );
}

App.propTypes = {
  children: React.PropTypes.element,
};

export default App;
