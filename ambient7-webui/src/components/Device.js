import React from 'react';

class Device extends React.PureComponent {
  render() {
    return (
      <div>
        <h3>device {this.props.params.deviceId}</h3>
      </div>
    );
  }
}

export default Device;
