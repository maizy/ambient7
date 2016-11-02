import React from 'react';
import { Link } from 'react-router';

function DeviceItem(props) {
  const deviceId = props.deviceId;
  return (
    <li>
      <Link to={`/devices/${deviceId}`}>Device: {deviceId}</Link>
    </li>
  );
}

DeviceItem.propTypes = {
  deviceId: React.PropTypes.string,
};

export default DeviceItem;
