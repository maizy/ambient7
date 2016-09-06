import React from 'react';
import 'whatwg-fetch';
import { checkIsSuccessfull, toImmutableJs, buildApiUrl } from '../utils/rest';

import DeviceItem from './DeviceItem';
import LoaderOrError from '../modules/LoaderOrError';
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
    const co2Devices =
      this.state.devices !== null
      ? (
          <ul>{ this.state.devices
            .get('co2')
            .map(d => <DeviceItem key={`device-${d.get('id')}`} deviceId={d.get('id')} />)
          }</ul>
        )
      : null;

    return (
      <div>
        <h1>Co2 devices</h1>
        <LoaderOrError
          errorMessage={this.state.error}
          loading={this.state.loading}
        />
        {co2Devices}
      </div>
    );
  }
}

export default Devices;
