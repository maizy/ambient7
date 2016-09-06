import React from 'react';

import ErrorAlert from '../modules/ErrorAlert';

function LoaderOrError(props) {
  const hasError = !!props.errorMessage;
  const error =
    hasError
    ? <ErrorAlert message={props.errorMessage} />
    : null;

  const loading =
    props.loading
    ? <div className="loading">Loading...</div>
    : null;

  return (error || loading)
    ? <div>{loading}{error}</div>
    : null;
}

LoaderOrError.propTypes = {
  errorMessage: React.PropTypes.string,
  loading: React.PropTypes.bool,
};

export default LoaderOrError;
