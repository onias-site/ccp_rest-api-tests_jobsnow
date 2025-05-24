import React from 'react'

import 'script-loader!ion-rangeslider/js/ion.rangeSlider.min.js';
import './IonSlider.css';

export default class IonSlider extends React.Component {

  componentDidMount() {

    let options = {
      extra_classes: "irs--big"
    };

    if (this.props.prettify) {
      options.prettify = this.props.prettify;
    }

    if (this.props.onChange) {
      options.onChange = this.props.onChange;
    }

    $(this.input).ionRangeSlider(options);
  }

  render() {
    let props = { ...this.props };
    delete props.prettify;
    delete props.onChange;

    return (
      <span className="irs--big">
        <input type="text" {...props}
          ref={i => this.input = i}
          data-force-edges={true}
          data-hide-min-max={true}
          data-hide-from-to={false}
        />
      </span>
    )
  }
}
