
import React from 'react';
import JnAjax from '../../../services/JnAjax';

export default class Redirect extends React.Component {

    carregarPesquisa = (pesquisa) => {
        let url = 'https://ccpjobsnow.com/#/recrutadores/buscaDeCandidatos/?x=x';

        if(pesquisa.requisitosObrigatorios &&  pesquisa.requisitosObrigatorios.join){
            url += ('&requisitosObrigatorios=' + pesquisa.requisitosObrigatorios.filter(x => x.length > 1).map(x => encodeURIComponent(x)).join(','));
        }

        if(pesquisa.requisitosDesejaveis &&  pesquisa.requisitosDesejaveis.join){
            url += ('&requisitosDesejaveis=' + pesquisa.requisitosDesejaveis.filter(x => x.length > 1).map(x => encodeURIComponent(x)).join(','));
        }

        if(pesquisa.ddd){
            url += ('&ddd=' + pesquisa.ddd);
        }

        window.location.href = url;
    }

    componentDidMount() {
        if(this.props.location.query.idPesquisa){

            const callbacks = {};

            callbacks[200] = pesquisa => this.carregarPesquisa(pesquisa);
            callbacks[404] = () => window.location.href = window.location.href.split('#')[0]+ '#/recrutadores/curriculos?idVaga=' + this.props.location.query.idPesquisa;
            callbacks['onUnexpectedHttpStatus'] = () => window.location.href = 'https://ccpjobsnow.com/#/recrutadores/buscaDeCandidatos';

            JnAjax.doAnAjaxRequest(`curriculos/pesquisas/${this.props.location.query.idPesquisa}`, callbacks, 'GET');

            return;
        }

    }


    render() {
        return null;
    }
}
