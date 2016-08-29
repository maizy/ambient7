import React from 'react';
import { Link } from 'react-router';

class DeviceItem extends React.PureComponent {
  render() {
    const deviceId = this.props.deviceId;
    return (
     <li>
       <Link to={`/devices/${deviceId}`}>Device: {deviceId}</Link>
     </li>
   );
  }
}

export default DeviceItem;
