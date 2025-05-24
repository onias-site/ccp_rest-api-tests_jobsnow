import React, { Component } from 'react';



export default class Opcao extends Component {


    executarOnClick = (e) =>{
        e && e.preventDefault();
        this.props.onClick && this.props.onClick(this.props.option.key);
    }



    render() {
        return (

            <a key={this.props.option.key} href="#" onClick={this.executarOnClick}>
                <li className="list-group-item ">
                    {this.props.option.value}
                </li>
            </a>
        );
    }

}
