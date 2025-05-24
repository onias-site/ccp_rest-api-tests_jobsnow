import Opcao from './JnOption';
import React, { Component } from 'react';

export default class Opcoes extends Component {

    render() {
        return (
            <div className="card">
                <ul className="list-group list-group-flush">
                    {
                        this.props.opcoes.map((opcao, idx) => (
                            <Opcao key={idx} option={opcao} onClick={(e) => 

                                this.props.onClick && this.props.onClick(e)
                            
                            } />
                        ))
                    }
                </ul>

            </div>
        )
    }
}