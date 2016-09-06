import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Immutable from 'immutable';

class DeviceInfo extends React.Component {
  constructor() {
    super();
    this.state = {
      deviceInfo: null,
      error: false,
      loading: true,
    };
  }

  componentDidMount() {
    // FIXME: tmp
    this.setState({
      loading: false,
      error: false,
      deviceInfo: {
        tags: Immutable.OrderedMap([['some', 'tag'], ['altitude', '200m']]),
      },
    });
  }

  render() {
    let tags = null;
    if (this.state.deviceInfo) {
      tags = this.state.deviceInfo.tags.size > 0
        ? (
          <div>
            <li>Tags</li>
            <ul>
              {this.state.deviceInfo.tags.entrySeq().map(t => <li>{t[0]}: {t[1]}</li>)}
            </ul>
          </div>
        )
        : null;
    }
    return (
      <div>
        <h4>Device</h4>
        <ul>
          <li><span>Id</span>: {this.props.deviceId}</li>
          {tags}
        </ul>
      </div>
    );
  }
}


DeviceInfo.propTypes = {
  deviceId: React.PropTypes.string,
  tags: ImmutablePropTypes.orderedMap,
};

export default DeviceInfo;
