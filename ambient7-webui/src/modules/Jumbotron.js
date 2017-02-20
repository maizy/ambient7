/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import React from 'react';

function Jumbotron(props) {
  const header = props.header !== undefined ? <h1>{props.header}</h1> : null;
  return (
    <div className="jumbotron">
      <div className="container">
        {header}
        {props.children}
      </div>
    </div>
  );
}

Jumbotron.propTypes = {
  children: React.PropTypes.element,
  header: React.PropTypes.string,
};

export default Jumbotron;
