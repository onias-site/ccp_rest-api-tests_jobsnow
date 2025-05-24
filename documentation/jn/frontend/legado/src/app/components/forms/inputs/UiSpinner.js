import React from 'react'

export default class UiSpinner extends React.Component {
  componentDidMount() {
    let options = {};
    let props = this.props;
    if (props.spinnerType == 'decimal') {
      options = {
        step: 0.01,
        numberFormat: "n"
      };
    } else if (props.spinnerType == 'currency') {
      options = {
        min: 5,
        max: 2500,
        step: 25,
        start: 1000,
        numberFormat: "C"
      };
    }

    if(props.min) {
      options.min = props.min;
    }

    if(props.max) {
      options.max = props.max;
    }

    options.change = ( event, ui ) => {
      this.props.onChange && this.props.onChange(event);
    }

    $(this.refs.input).spinner(options);

  }

  value = () =>{
    return $(this.refs.input).spinner('value');
  }

  render() {
    const {spinnerType, ...props} =  {...this.props};
    return <input type="number" ref="input" {...props}  />
  }
}