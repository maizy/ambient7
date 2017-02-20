/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import Immutable from 'immutable';
import 'whatwg-fetch';

export function checkIsSuccessfull(response) {
  if (!([200, 201, 204].find(s => s === response.status))) {
    const error = new Error(response.statusText);
    error.response = response;
    throw error;
  }
  return response;
}

export function toImmutableJs(response) {
  return response.json().then(data => Immutable.fromJS(data));
}

export function buildApiUrl(appOpts) {
  return function apiUrl(relativePath) {
    const stripedPath =
      relativePath[0] === '/'
        ? relativePath.substr(1)
        : relativePath;
    return new URL(appOpts.apiBaseUrl + stripedPath, document.URL);
  };
}
