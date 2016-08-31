import React from 'react';
import Container from '../modules/Container';

class Device extends React.PureComponent {
  render() {
    return (
      <Container>
        <h1>{this.props.params.deviceId}</h1>
        <p>TODO ...</p>
      </Container>
    );
  }
}

export default Device;
