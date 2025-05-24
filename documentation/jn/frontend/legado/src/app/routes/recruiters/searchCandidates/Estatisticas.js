import React, { Component } from 'react';
import JnAjax from '../../../services/JnAjax';
import getRegioes from '../../../jncomponents/JnRegion';
import formatarDinheiro from '../../../jncomponents/JnFormatCurrency';
import { withRouter } from 'react-router';
import { Alert } from 'react-bootstrap';
import { smallBox } from '../../../components/utils/actions/MessageActions';
import JnAlert from '../../../jncomponents/JnAlert';

const msgDesempenho = 'Cada índice desses é obtido a partir da comparação entre os números do último mês e a média do ano corrente. Números absurdos podem representar começo de ano (2020 é o nosso primeiro ano), poucos currículos para o termo ou poucas pesquisas para o termo. Já a ausência de números na coluna, representa que é a primeira vez que o número pôde ser calculado, as vezes porque é a primeira vez na história do sistema que o termo está sendo pesquisado, por isso não houve número anterior para fazer comparação.';

const regioes = getRegioes();


class Estatisticas extends Component {
    state = {
        mes : new Date().getMonth() + 1,
        buscandoDados : false,
        ordenador: 'pesquisas',
        selecionados : [],
        estatisticas: [],
        erro: false,
        ddd: '0'
    };

    meses =[
        {
            value : 12,
            label: 'Dezembro' 
        },
        {
            value : 11,
            label: 'Novembro' 
        },
        {
            value : 10,
            label: 'Outubro' 
        },
        {
            value : 9,
            label: 'Setembro' 
        },
        {
            value : 8,
            label: 'Agosto' 
        },
        {
            value : 7,
            label: 'Julho' 
        },
        {
            value : 6,
            label: 'Junho' 
        },
        {
            value : 5,
            label: 'Maio' 
        },

        {
            value : 4,
            label: 'Abril' 
        },

        {
            value : 3,
            label: 'Março' 
        },
        {
            value : 2,
            label: 'Fevereiro' 
        },
        {
            value : 1,
            label: 'Janeiro' 
        },


    ];


    alert = null;

    ordenadores = [];

    constructor() {
        super();

        this.ordenadores['ranking'] = true;
        this.ordenadores['pesquisas'] = false;
        this.ordenadores['candidatos'] = false;
        this.ordenadores['experiencia'] = false;
        this.ordenadores['demanda'] = false;
        this.ordenadores['pj'] = false;
        this.ordenadores['clt'] = false;
        this.ordenadores['experiencia'] = false;

        this.ordenadores['variacaoPj'] = false;
        this.ordenadores['variacaoClt'] = false;
        this.ordenadores['variacaoTotal'] = false;
        this.ordenadores['variacaoDemanda'] = false;
        this.ordenadores['variacaoOcorrencias'] = false;

    }

    componentDidMount(){
        const ordenador  = this.props.ordenador || this.props.location.query.ordenador || this.state.ordenador;
        const ddd  = this.props.ddd || this.props.location.query.ddd || this.state.ddd;
        const sels = this.props.selecionados || [];
        const selecionados = [...sels];
        const message = selecionados.length ? 'Selecionados: ' + selecionados.join(', ')  + '. (Clique aqui para finalizar a seleção)': ''; 
        this.setState({selecionados, message});
        this.recarregarEstatisticas(ddd);
        this.reordenarEstatisticas(ordenador);
    }

    reordenarEstatisticas = (ordenador) => {
        const estatisticas = this.state.estatisticas;
        estatisticas.sort((a, b) => this.getOrdenacao(a, b, ordenador));
        this.setState({ ordenador, estatisticas });
    }

    recarregarEstatisticas = (ddd = this.state.ddd, mes = this.state.mes) => {
        let restoDaUrl = '/regiao/' + ddd + '/' + mes;
        if(this.props.finalizarSelecao){
            this.setState({buscandoDados: true});
        }
        const callbacks = {};
        callbacks['onUnexpectedHttpStatus']= () => this.setState({buscandoDados: false, erro: true})
        callbacks[200] = (estatisticas) => {
            estatisticas.forEach(estatistica =>  estatistica.experiencia = Math.trunc(new Date().getFullYear() - estatistica.experiencia));
            estatisticas.sort((a, b) => this.getOrdenacao(a, b));
            this.buscandoDados = false;
            this.setState({ estatisticas, ddd, buscandoDados: false, mes })
        };

        JnAjax.doAnAjaxRequest(`estatisticas/buscas/curriculos${restoDaUrl}/`, callbacks, 'GET');

    }

    getOrdenacao = (a,b, ordenador = this.state.ordenador) =>{
        const ascOrder = this.ordenadores[ordenador];

        if(ascOrder){
            return ((a[ordenador] || 0) * 10) - ((b[ordenador] || 0) * 10);
        }
        return ((b[ordenador] || 0) * 10) - ((a[ordenador] || 0) * 10);


    }

    getTableRow = (estatistica, idx) => {
        const experiencia = estatistica.experiencia ?  estatistica.experiencia + ' Anos'  : 'Sem Dados';

        return (
            <tr key={estatistica.ranking}>
                <td>{idx}</td>
                <td><a href = "#" onClick= {e => this.addSelected(e, estatistica.term)}>{estatistica.term}</a></td>
                <td>{estatistica.pesquisas}   {this.getVariacao(estatistica, 'variacaoOcorrencias')}</td>
                <td>{estatistica.candidatos}   {this.getVariacao(estatistica, 'variacaoTotal')} </td>
                <td>{estatistica.demanda ? Math.round(estatistica.demanda * 100)/100 : '-'}  {this.getVariacao(estatistica, 'variacaoDemanda')} </td>
                <td width= "20%">{`${formatarDinheiro(estatistica.pj)} `}   {this.getVariacao(estatistica, 'variacaoPj')}</td>
                <td width= "20%">{`${formatarDinheiro(estatistica.clt)} `}   {this.getVariacao(estatistica, 'variacaoClt')}</td>
                <td>{experiencia}</td>
            </tr>
        );
    }

    getVariacao = (estatistica, nomeDoCampo) => {
        const valor = estatistica[nomeDoCampo];
        let simbolo = '';
        let color = '';
        if(Math.trunc(valor) > 0){
            simbolo = '+';
            color = 'blue';
        }

        if(Math.trunc(valor) < 0){
            color = 'red';
        }
 
        if(valor || valor == 0){
             return (<small style ={{color}}>{' [' + simbolo   + Math.trunc(valor) + '%]'}</small>);
        }
        return null;
    }

    addSelected =(e, selected) => {
        e && e.preventDefault && e.preventDefault();
    
        if(!this.props.finalizarSelecao){
            
            return;
        }
        selected = selected.toUpperCase();

        let selecionados = this.state.selecionados;
 
        if(selecionados.includes(selected)){
            selecionados.forEach((value, index) => {
                if (value == selected) {
                    selecionados.splice(index, 1);
                    const message = selecionados.length ? 'Selecionados: ' + selecionados.join(', ') + '. (Clique aqui para finalizar a seleção)': ''; 
 

                    this.setState({selecionados, message});
                    if(selecionados.length){
                        smallBox({
                            title: "Selecionados:",
                            content: `<br/>${selecionados.join(', ')}<br/>`,
                            color: "#296191",
                            timeout: 8000,
                            icon: "fa fa-bell swing animated"
                        });

                    }        
                            }
            });
            this.setState({ selecionados });
               return;
        }
        selecionados.push(selected);
        const message = selecionados.length ? 'Selecionados: ' + selecionados.join(', ') + '. (Clique aqui para finalizar a seleção)': ''; 
 

        this.setState({selecionados, message});
        smallBox({
            title: "Selecionados:",
            content: `<br/>${selecionados.join(', ')}<br/>`,
            color: "#296191",
            timeout: 8000,
            icon: "fa fa-bell swing animated"
        });

    }

    finalizarSelecao = (e) =>{
        e && e.preventDefault && e.preventDefault();
        this.props.finalizarSelecao && this.props.finalizarSelecao(this.state.selecionados);
    }
    
    getMessage = () =>{
    
        if(this.state.erro){
            return (<Alert bsStyle='danger'>Erro ao buscar estatisticas</Alert>);
        }

        if(this.state.buscandoDados){
            return (<Alert bsStyle='danger'>A busca de dados ainda está sendo feita, por favor, pedimos que aguarde...</Alert>);
        }


        if(!this.state.estatisticas.length ){
            return (<Alert bsStyle='danger'>Não foram encontradas estatisticas para esta região metropolitana</Alert>);
        }

        if(!this.state.selecionados.length && this.props.finalizarSelecao){
            return (<Alert bsStyle='info'>Clique nas especialidades abaixo para poder selecioná-las!</Alert>);
        }

        if(!this.state.message){
            return null;
        }

        return(
        <a href = "#" onClick={e => this.finalizarSelecao(e)}><Alert bsStyle={this.state.messageType} onDismiss={() => { this.exibirMensagem() }}>
                    <p>{this.state.message}</p>
            </Alert></a>);

    }

    render() {
        const regs = [];

        for (let ddd in regioes) {
            let regiao = regioes[ddd];
            const array = regiao.split('–');
            const uf = array[1];
            if (!uf) {
                continue;
            }
            regiao = uf + ' – ' + array[0];
            const reg = { ddd, regiao }

            regs.push(reg);
        }
     
    
        

        return (
            <div style = {{padding:"30px"}}>
                <JnAlert ref={(el) => this.alert = el} />
                  {
                        this.getMessage()
                    }

                    <div className="row">
                    <div className="col col-md-4">
                        <div className="form-group">
                            <label className="control-label">Região Metropolitana</label>
                            <select className="form-control"
                                value={this.state.ddd}
                                onChange={e => this.recarregarEstatisticas(e.target.value, this.state.mes)}>
                                <option value='0'>De todo o país</option>
                                {
                                    regs.map(x => (
                                        <option key={x.ddd} value={x.ddd}>{x.regiao}</option>
                                    ))
                                }
                            </select>
                        </div>
                    </div>
                    <div className="col col-md-4">
                        <div className="form-group">
                            <label className="control-label">Ordenado por</label>
                            <select className="form-control"
                                value={this.state.ordenador}
                                onChange={e => this.reordenarEstatisticas(e.target.value)}>
                                <option value='pesquisas'>Mais Pesquisados</option>
                                <option value='candidatos'>Candidatos Encontrados</option>
                                <option value='demanda'>Oferta e Demanda</option>
                                <option value='pj'>Média PJ</option>
                                <option value='clt'>Média CLT</option>
                                <option value='experiencia'>Média de Expriência</option>
                            </select>
                        </div>
                    </div>
                    <div className="col col-md-4">
                        <div className="form-group">
                            <label className="control-label">Mês</label>
                            <select className="form-control"
                                value={this.state.mes}
                                onChange={e => this.recarregarEstatisticas(this.state.ddd, e.target.value)}>
                               {
                                    this.meses.filter(x => x.value <= (new Date().getMonth() + 1)).map(x => <option key={x.value} value = {x.value}>{x.label}</option>)
                               }
                            </select>
                        </div>
                    </div>
                </div>
                <div className="row">
                    <div className="col col-md-12">
                        <table id="customers">
                            <tr>
                                <th scope="col" >#</th>
                            <th scope="col" style={{width:"10%"}}>{this.props.labelTermo || 'Termo Pesquisado'}</th>
                                <th scope="col" >Pesquisas neste mês</th>
                                <th scope="col" >Candidatos</th>
                                <th scope="col" >Oferta X Demanda</th>
                                <th scope="col" >Média PJ</th>
                                <th scope="col" >Média CLT</th>
                                <th scope="col" >Média Experiência</th>
                            </tr>
                            {
                                this.state.estatisticas.map((estatistica, idx) => this.getTableRow(estatistica, idx + 1))

                            }
                        </table>
                    </div>
                </div>
            </div>
        )
    }
}
export default withRouter(Estatisticas)