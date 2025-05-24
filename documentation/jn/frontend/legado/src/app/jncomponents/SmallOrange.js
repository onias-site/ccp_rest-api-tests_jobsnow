import React from 'react';
export default class SmallOrange extends React.Component {


    render(){
        return(
        <small style={{ color: "chocolate", fontSize: "10px" }}>{this.props.message}</small>

        );
    }

}