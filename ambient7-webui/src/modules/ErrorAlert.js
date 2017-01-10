/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import React from 'react';

function ErrorAlert(props) {
  return (
    <div className="alert alert-danger" role="alert">
      {props.message}
    </div>
  );
}

ErrorAlert.propTypes = {
  message: React.PropTypes.string,
};

export default ErrorAlert;
