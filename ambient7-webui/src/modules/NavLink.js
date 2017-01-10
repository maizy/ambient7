/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import React from 'react';
import { Link } from 'react-router';

export default function NavLink(props) {
  return <Link {...props} activeClassName="active" />;
}
