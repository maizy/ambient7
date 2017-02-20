/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ImmutableJs from 'immutable';

class AppOpts {
  constructor(rawOpts) {
    const opts = ImmutableJs.fromJS(rawOpts);
    this.apiBaseUrl = opts.get('apiBase', '/');
  }
}

// TODO: is there any better way to get app opts?
export default new AppOpts(window.Ambient7Opts);
