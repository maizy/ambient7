/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import React from 'react';

function Container(props) {
  return (
    <div className="container">
      <div className="row">
        {props.children}
      </div>
    </div>
  );
}

Container.propTypes = {
  children: React.PropTypes.node,
};

export default Container;
