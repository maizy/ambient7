import React from 'react';
import { Bar } from 'react-chartjs-2';

function LastDaysCo2Levels(props) {
  const days = Array(props.daysTotal).fill(0).map((_, i) => props.from.clone().add(i, 'days'));
  const chartData = {
    labels: days.map(e => e.format(props.dateFormat)),
    datasets: [
      {
        label: 'Low level',
        backgroundColor: 'rgba(13,255,0,0.5)',
        data: props.reportData.low,
      },

      {
        label: 'Medium level',
        backgroundColor: 'rgba(255,187,0,0.5)',
        data: props.reportData.medium,
      },

      {
        label: 'High level',
        backgroundColor: 'rgba(255,0,0,0.5)',
        data: props.reportData.high,
      },
      {
        label: 'Unknown level',
        backgroundColor: 'rgba(20,20,20,0.5)',
        data: props.reportData.unknown,
      },
    ],
  };

  const chartOpts = {
    title: {
      display: false,
    },
    tooltips: { mode: 'label' },
    scales: {
      xAxes: [{ stacked: true }],
      yAxes: [{ stacked: true }],
    },
    responsive: true,
    maintainAspectRatio: false,
  };

  return (
    <div>
      <div>
        <ul>Days: <span>{ props.daysTotal ? props.daysTotal : null }</span></ul>
      </div>
      <div className="co2-chart">
        <Bar data={chartData}
          options={chartOpts}
          height={400}
        />
      </div>
    </div>
  );
}

LastDaysCo2Levels.propTypes = {
  reportData: React.PropTypes.object,
  from: React.PropTypes.object,
  daysTotal: React.PropTypes.number,
  dateFormat: React.PropTypes.string,
};

LastDaysCo2Levels.defaultProps = {
  dateFormat: 'ddd DD MMM `YY',
};

export default LastDaysCo2Levels;
