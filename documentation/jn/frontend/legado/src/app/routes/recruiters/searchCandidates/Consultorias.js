import React, { Component } from 'react';
import Grid from '../../../jncomponents/JnSimpleGrid';
export default class Cosnultorias extends Component {

    render(){
        return (
            <Grid colunas = {{
                colocacao: '#',
                key: 'Consultoria ou Empresa'
                ,value: 'Recrutadores que recebem seu currículo sempre que você altera'
            }} url = "estatisticas/consultorias"/>
        ) ;     
    } 

}