import React from 'react';
import Container from '../modules/Container';
import Jumbotron from '../modules/Jumbotron';
import Devices from '../modules/Devices';

export default function Home() {
  return (
    <div>
      <Jumbotron header="Ambient7">
        <p>Tools for home climate monitoring</p>
      </Jumbotron>
      <Container>
        <Devices />
      </Container>
    </div>
  );
}
