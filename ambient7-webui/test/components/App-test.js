import test from 'ava';
import React from 'react';
import TestUtils from 'react-addons-test-utils';
import App from '../../src/components/App';

const shallowRenderer = TestUtils.createRenderer();
shallowRenderer.render(<App />);
const app = shallowRenderer.getRenderOutput();

test('should have a div as container', t => {
  t.is(app.type, 'div');
});
