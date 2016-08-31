import React from 'react';
import 'whatwg-fetch';
import { checkIsSuccessfull, toImmutableJs, buildApiUrl } from '../utils/rest';

import DeviceItem from './DeviceItem';
import ErrorAlert from '../modules/ErrorAlert';
import appOpts from '../AppOpts';

const apiUrl = buildApiUrl(appOpts);

class Devices extends React.Component {
  constructor() {
    super();
    this.state = {
      devices: null,
      loading: true,
      error: null,
    };
  }

  componentDidMount() {
    fetch(apiUrl('/devices'))
      .then(checkIsSuccessfull)
      .then(toImmutableJs)
      .then(data => this.setState({ devices: data, loading: false }))
      .catch(error => this.setState({
        error: `Unable to load devices: ${error}`,
        loading: false,
      }));
  }

  render() {
    const hasError = this.state.error !== null;
    const error =
      hasError
      ? <ErrorAlert message={this.state.error} />
      : null;

    const loading =
      this.state.loading
      ? <div className="loading">Loading...</div>
      : null;

    const co2Devices =
      this.state.devices === null
      ? loading
      : this.state.devices
          .get('co2')
          .map(d => <DeviceItem key={`device-${d.get('id')}`} deviceId={d.get('id')} />);


    return (
      <div>
        <h1>Co2 devices</h1>
        <ul>{co2Devices}</ul>
        {error}
      </div>
    );
  }
}

export default Devices;
