import React from 'react'

import 'react-fontawesome';

export default class JnContainerCheckBox extends React.Component {
    state = {
        checked: true
    }

    componentDidMount() {
        this.setState({ checked: this.props.checked })
    }

    componentDidUpdate(prevProps){
        if(prevProps.checked != this.props.checked){
            this.setState({ checked: this.props.checked })
        }
    }

    onChange = () =>{
        this.setState({checked : false});
        this.props.onChange && this.props.onChange();
    }

    onLinkClick = (e) =>{
        e && e.preventDefault && e.preventDefault();
        this.props.onLinkClick && this.props.onLinkClick();
    }

    render() {
        if (this.state.checked) {
            return (
                <div>
                    <br />
                    <label className="labelCkx">
                        <input type="checkbox" checked={this.state.checked} onChange={this.onChange} />
                                &nbsp;&nbsp;&nbsp;{this.props.label}</label>
                               <br/>
                                <a href="#" onClick={this.onLinkClick}>
                                    <small style={{ color: "chocolate", fontSize: "10px" }}>
                                        {this.props.labelLink}
                                    </small>
                                </a>
                </div>

            );
        }
        return (
                this.props.children
        )
    }
}