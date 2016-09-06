import React from 'react';
import moment from 'moment';

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
      reportByDay: null,
      error: false,
      loading: true,
    };
  }

  componentDidMount() {
    // FIXME: tmp, use params
    const from = '2016-01-01';
    const days = 7;

    const url = apiUrl('/co2_report/by_day');
    url.searchParams.append('from', from);
    url.searchParams.append('days', days.toString());
    url.searchParams.append('device_id', this.props.params.deviceId);

    fetch(url)
      .then(checkIsSuccessfull)
      .then(toImmutableJs)
      .then(data => this._setCo2LevelData(data))
      .catch(error => this.setState({
        error: `Unable to load device data: ${error}`,
        loading: false,
      }));
  }

  _setCo2LevelData(data) {
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
        daysTotal: data.get('days_total'),
        data: chartData,
      },
    });
  }

  render() {
    const dateFormat = 'ddd DD MMM `YY';

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
        <div className="row">
          <div className="col-sm-8 blog-main">
            {co2Levels}
          </div>
          <div className="col-sm-3 col-sm-offset-1 sidebar">
            <div className="sidebar-module sidebar-module-inset">
              <DeviceInfo deviceId={this.props.params.deviceId} />
            </div>
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
