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
