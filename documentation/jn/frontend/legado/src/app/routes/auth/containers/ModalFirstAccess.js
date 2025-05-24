
import Modal from '../../../jncomponents/JnModal';
import Opcoes from '../../../jncomponents/JnOptions';
import JnAjax from '../../../services/JnAjax'
import React from 'react';

export default class ModalFirstAccess extends React.Component {


    modal = null;


    state = {
        mostrarComoNosConheceu: true,
        titulo: 'Como você nos conheceu?',
        comoNosConheceu: ''
    }

    opcoesDeComoNosConheceu = [
        {
            key: 1,
            value: 'POR ALGUÉM OU POR ANÚNCIO DO LINKEDIN'
        },
        {
            key: 2,
            value: 'GRUPOS DE VAGAS DO TELEGRAM'
        },
        {
            key: 3,
            value: 'INDICAÇÃO DE AMIGOS'
        },
        {
            key: 4,
            value: 'OUTROS'
        },
    ];
    opcoesDeTipoDeUsuario = [
        {
            key: 298,
            value: 'SALÁRIOS E EMPREGOS'
        },
        {
            key: 299,
            value: 'VER CURRÍCULOS'
        }
    ];

    irParaTelaDeObjetivos = (comoNosConheceu) => {
       
        this.setState({ comoNosConheceu, titulo: 'Qual é seu objetivo?', mostrarComoNosConheceu: false });
    }

    voltarParaTelaDeComoNosConheceu = () => {
        this.setState({ comoNosConheceu: '', titulo: 'Como você nos conheceu?', mostrarComoNosConheceu: true });
    }

    entrarNoSistema = (tipoDeUsuario) => {
        this.props.callBacks[tipoDeUsuario] && this.props.callBacks[tipoDeUsuario]();
        JnAjax.doAnAjaxRequest(`preRegistration/${this.props.email}/${this.state.comoNosConheceu}/${tipoDeUsuario}`, {}, 'POST');
    }

    getOpcoes = () => {
        
        if (this.state.mostrarComoNosConheceu) {
            return (
                <div>
                    <Opcoes onClick={(comoNosConheceu) => this.irParaTelaDeObjetivos(comoNosConheceu)

                    } opcoes={this.opcoesDeComoNosConheceu} />
                </div>
            );
        }
        return (
            <div>
                <br />
                <Opcoes titulo="Qual o é o seu objetivo atual?" onClick={(tipoDeUsuario) => this.entrarNoSistema(tipoDeUsuario)
                } opcoes={this.opcoesDeTipoDeUsuario} />
                <div style={{ textAlign: "right" }}>
                    <button className="btn btn-primary" onClick={() => this.voltarParaTelaDeComoNosConheceu()}>Voltar</button>

                </div>
            </div>
        );
    }

 
    show = () => {
        this.modal && this.modal.show && this.modal.show();
    }


    render() {
        return (
            <Modal
                ref={e => this.modal = e}
                title={this.state.titulo}
            >
                {this.getOpcoes()}


            </Modal>
        );

    }
}

