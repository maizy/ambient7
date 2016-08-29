import React from 'react';
import Immutable from 'immutable';

import DeviceItem from '../modules/DeviceItem';

class Devices extends React.PureComponent {
  render() {
    const devices = Immutable.fromJS({
      mt8057: [
        {
          id: 'main',
          agent: { name: 'main', tags: [] },
        },
        {
          id: 'not-exist',
          agent: {
            name: 'bla-bla',
            tags: [{ name: 'altitude', value: '200' }, { name: 'place', value: 'livingroom' }],
          },
        },
      ],
    });

    const mt8057Devices = devices
      .get('mt8057')
      .map(d => <DeviceItem key={`device-${d.get('id')}`} deviceId={d.get('id')} />);

    return (
      <div>
        <h3>MT8057 devices</h3>
        <ul>{mt8057Devices}</ul>
      </div>
    );
  }
}

export default Devices;
