import { Chips } from 'primereact/chips';
import React, { Component } from 'react';
import gif from './jobsnow.gif';
import Alert from '../../../jncomponents/JnAlert';

export default class InputDeRequisitos extends Component {

    state = {
        requisitos: []
    }

    alertTermos = null;

    componentDidUpdate(prevProps) {
        if (!this.props.requisitos) {
            this.setState({ requisitos: [] });
            return;
        }

        if (JSON.stringify(prevProps.requisitos) == JSON.stringify(this.props.requisitos)) {
            return;
        }

        this.setState({ requisitos: this.props.requisitos });
    }



    render() {


        return (
            <div>

                <div className="row">
                    { <article className="col-xs-12 col-sm-1 col-md-1 col-lg-1 text-align-left">
                        <a href="#" title="Como funciona?" onClick={(e) => { e.preventDefault(); this.exibirHelpTermos() }} style={{ color: "blue", fontSize: "8px" }}>Clique em mim!</a>
                    </article>}
                    <article className="col-xs-12 col-sm-6 col-md-6 col-lg-6 text-align-left">
                        <strong style={{ color: "#7FBCEC" }}>
                            {this.props.nomeTermo}

                        </strong>

                    </article>
                    <div className="col col-sm-4">

                    </div>
                </div>
                <Chips tooltipOptions={{ position: 'bottom' }} tooltip={this.props.descricaoTermo + ". Ao terminar digitaçào de palavra (ou frase), pressione a tecla enter"} onRemove={e => this.removerRequisito(e)} value={this.state.requisitos} onAdd={(e) => this.adicionarRequisito(e)} />
                <strong style={{ color: "black", fontSize: "12px" }}>[Ao terminar digitaçào de palavra (ou frase), pressione a tecla enter. Você pode digitar mais de uma palavra (ou frase)]</strong>

                <div className="row">
                    <Alert ref={(el) => this.alertTermos = el} >
                        <br/>
                        <img src={gif} />

                    </Alert>
                </div>
            </div>
        );


    }

    exibirHelpTermos = () => {

        this.alertTermos.exibir('Como funciona?', this.props.descricaoTermo + '. Listamos perfis que contenham dentro do seu conteúdo os termos digitados (Busca extata), não fazemos interpretação de texto e nem buscamos pelo nome da profissão, os perfis poderão ser escolhidos por região metropolitana, tempo de experiência e pretensões salariais, à  medida que os termos forem sendo digitados. Veja abaixo a nossa ilustraçào de 30 segundos:');
    }

    removerRequisito = (e) => {
        const requisitos = this.state.requisitos;

        requisitos.forEach((value, index) => {
            if (value == e.value) {
                requisitos.splice(index, 1);
                return;
            }
        });
        this.setState({ requisitos });
        const requisito = requisitos[requisitos.length - 1];
        this.props.getRequisitos && this.props.getRequisitos(requisitos, requisito);

    }

    adicionarRequisito = (e) => {

        let requisito = e.value;
        if (!requisito) {
            this.props.exibirErro && this.props.exibirErro('O requisito deve ter mais que dois caracteres');
            return;
        }


        if (requisito.trim().length < 2) {
            this.props.exibirErro && this.props.exibirErro('O requisito deve ter mais que dois caracteres');
            return;
        }
        if (this.state.requisitos.includes(requisito)) {
            this.props.exibirErro && this.props.exibirErro('Você já mencionou este requisito');
            return;
        }
       
        const requisitos = this.state.requisitos;


        if(requisito.toLowerCase().includes(' e ')){
            const array = requisito.toLowerCase().split(' e ');      
        
            array.forEach(x => {
                requisitos.push(x);

            });
            this.setState({ requisitos });
            this.props.getRequisitos && this.props.getRequisitos(requisitos, requisito);
            return;
        }
  

        if(requisito.includes(',')){
            const array = requisito.split(',');      
        
            array.forEach(x => {
                requisitos.push(x);

            });
            this.setState({ requisitos });
            this.props.getRequisitos && this.props.getRequisitos(requisitos, requisito);
            return;
        }
       
        requisitos.push(requisito);
        this.setState({ requisitos });
        this.props.getRequisitos && this.props.getRequisitos(requisitos, requisito);
    }


}