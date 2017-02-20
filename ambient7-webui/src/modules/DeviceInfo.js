/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import { checkIsSuccessfull, buildApiUrl, toImmutableJs } from '../utils/rest';
import appOpts from '../AppOpts';
import LoaderOrError from '../modules/LoaderOrError';

const apiUrl = buildApiUrl(appOpts);

class DeviceInfo extends React.Component {
  constructor() {
    super();
    this.state = {
      deviceInfo: null,
      error: null,
      loading: true,
    };
  }

  componentDidMount() {
    fetch(apiUrl(`/devices/${encodeURIComponent(this.props.deviceId)}`))
      .then(checkIsSuccessfull)
      .then(toImmutableJs)
      .then(data => this.setState({ deviceInfo: data, loading: false }))
      .catch(error => this.setState({
        error: `Unable to load device info: ${error}`,
        loading: false,
      }));
  }

  render() {
    let tags = null;
    if (this.state.deviceInfo) {
      const backendTagsData = this.state.deviceInfo.get('agent').get('tags');
      tags = backendTagsData.size > 0
        ? (
          <dl>
            <dt>Tags</dt>
            <dd>
              <ul className="list-unstyled">
                {
                  backendTagsData
                    .map((tag, i) => {
                      const name = tag.get('name');
                      const val = tag.get('value');
                      return (<li key={`tag-${i}`}>{name}: {val}</li>);
                    })
                }
              </ul>
            </dd>
          </dl>
        )
        : null;
    }
    return (
      <div className="device-info">
        <h4>Device</h4>
        <dl>
          <dt>Id</dt>
          <dd>{this.props.deviceId}</dd>
        </dl>
        {tags}
        <LoaderOrError
          errorMessage={this.state.error}
          loading={this.state.loading}
        />
      </div>
    );
  }
}


DeviceInfo.propTypes = {
  deviceId: React.PropTypes.string,
  tags: ImmutablePropTypes.orderedMap,
};

export default DeviceInfo;
