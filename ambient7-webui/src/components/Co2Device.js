/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import React from 'react';
import moment from 'moment';
import DatePicker from 'react-datepicker';
import URLSearchParams from 'url-search-params';

import appOpts from '../AppOpts';
import { checkIsSuccessfull, buildApiUrl, toImmutableJs } from '../utils/rest';
import Container from '../modules/Container';
import LastDaysCo2Levels from '../modules/LastDaysCo2Levels';
import DeviceInfo from '../modules/DeviceInfo';

const apiUrl = buildApiUrl(appOpts);

class Co2Device extends React.Component {

  constructor() {
    super();
    this.state = {
      reportUntil: moment(),
      reportDays: 7,
      reportByDay: null,
      error: false,
      loading: true,
    };

    // prebind
    [
      'reloadLevels',
      'handleLevelsUntilDateChange',
      'handleLevelsDaysChange',
    ].map((f) => (this[f] = this[f].bind(this)));
  }

  componentDidMount() {
    this.reloadLevels();
  }

  setCo2LevelData(data) {
    const chartData = data.get('items').reduce(
      (acc, value) => {
        acc.high.push(value.get('high_level'));
        acc.medium.push(value.get('medium_level'));
        acc.low.push(value.get('low_level'));
        acc.unknown.push(value.get('unknown_level'));
        return acc;
      },
      { high: [], medium: [], low: [], unknown: [] }
    );
    this.setState({
      reportByDay: {
        from: data.get('from').get('local_iso8601'),
        daysTotal: data.get('duration').get('days'),
        data: chartData,
      },
    });
  }

  reloadLevels() {
    const days = this.state.reportDays ? this.state.reportDays : 7;
    const from = this.state.reportUntil.clone().subtract(days - 1, 'days');

    // use separate object because URLSearchParams isn't supported in all browsers
    const params = new URLSearchParams();
    params.append('from', from.format('YYYY-MM-DD'));
    params.append('days', days.toString());
    params.append('device_id', this.props.params.deviceId);

    const url = apiUrl(`/co2_report/by_day?${params}`);

    fetch(url)
      .then(checkIsSuccessfull)
      .then(toImmutableJs)
      .then(data => this.setCo2LevelData(data))
      .catch(error => this.setState({
        error: `Unable to load device data: ${error}`,
        loading: false,
      }));
  }

  handleLevelsUntilDateChange(date) {
    this.setState({ reportUntil: date }, this.reloadLevels);
  }

  handleLevelsDaysChange(event) {
    const value = event.target.value;
    this.setState(
      {
        reportDays: value !== '' ? Number.parseInt(value, 10) : null,
      },
      this.reloadLevels
    );
  }

  render() {
    const dateFormat = 'ddd DD MMM \'YY';

    let co2Levels = null;

    const report = this.state.reportByDay;
    if (report !== null) {
      co2Levels = (
        <LastDaysCo2Levels
          from={moment(report.from)}
          daysTotal={report.daysTotal}
          dateFormat={dateFormat}
          reportData={report.data}
        />
      );
    }

    return (
      <Container>
        <div className="col-sm-8 co2-levels-report">
          <h2>Levels by day</h2>
          <div className="params">
            <form className="form-inline">

              <div className="form-group date-picker">
                <label htmlFor="co2-report-until">Until&nbsp;</label>
                  <DatePicker
                    customInput={<input type="text" />}
                    selected={this.state.reportUntil}
                    onChange={this.handleLevelsUntilDateChange}
                    className="form-control"
                    id="co2-report-until"
                    dateFormat="DD.MM.YYYY"
                  />
              </div>

              <div className="form-group days-picker">
                {/* FIXME: why I need there nbsp hack? */}
                <label htmlFor="co2-report-days">&nbsp;Days&nbsp;</label>
                <input
                  type="text"
                  className="form-control"
                  id="co2-report-days"
                  value={ this.state.reportDays ? this.state.reportDays : '' }
                  onChange={ this.handleLevelsDaysChange }
                />
              </div>

            </form>
          </div>
          {co2Levels}
        </div>
        <div className="col-sm-3 col-sm-offset-1 sidebar">
          <div className="sidebar-module sidebar-module-inset">
            <DeviceInfo deviceId={this.props.params.deviceId} />
          </div>
        </div>
      </Container>
    );
  }
}

Co2Device.propTypes = {
  params: React.PropTypes.object,
};

export default Co2Device;
