import React from 'react'

import 'script-loader!bootstrap-timepicker/js/bootstrap-timepicker.min.js'

export default class Timepicker extends React.Component {
  componentDidMount() {
    $(this.refs.input).timepicker({
      showMeridian: false,
      minuteStep: 1,
      maxHours: 24,
      explicitMode: true
    }).on('hide.timepicker', (e) => {
      if (this.props.onChange) {
        this.props.onChange(e.time.value);
      }
    });
  }

  render() {
    return (
      <input type="text" {...this.props} ref="input" />
    )
  }
}