import React, { Component } from 'react';
import JnAjax from '../../../services/JnAjax';
import { withRouter } from 'react-router';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Fieldset } from 'primereact/fieldset';

class Curriculos extends Component {
    state = {
        vaga: { profissionais: [], outrosGruposDoTelegram: [] }
        , collapseProfissionais: false
        , outrosGruposDoTelegram: []
        , collapseDescricao: false
        , collapseGrupos: false
        , first: 0
        , last: 10
        , loading: false
    }


    getVagaComProfissionais = (pagina, first = 0) => {
        this.setState({ loading: true, first, last: first + 10 })
        const callbacks = {};
        callbacks[200] = vaga => this.setState({ vaga });
        callbacks['afterHttpRequest'] = () => this.setState({ loading: false })
        JnAjax.doAnAjaxRequest(`estatisticas/${this.props.location.query.idVaga}/profissionais/paginas/${pagina}`, callbacks, 'GET');
    }
    componentDidMount() {
        this.getVagaComProfissionais(1);
    }

    getRanking = profissional => {
        return (
            <div style={{ fontSize: 10 }}>
                <p>{profissional.ranking}º Lugar</p>
                <small style={{ color: "red" }}>
                    <p>{profissional.afinidadeComEstaVaga} Pontos</p>
                    <p>{profissional.porcentagemAcima}% Acima da média</p>
                </small>

            </div>

        );
    }

    getPretensoes = profissional => {
        return (
            <Linhas colunas={{
                pj: 'PJ:',
                clt: 'CLT:',
                btc: 'Bitcoin:',
                pretensaoNegociavel: 'Negocia?',
            }}
                profissional={profissional}
            />
        );
    }
    getOutrosDados = profissional => {
        return (
            <Linhas colunas={{
                disponibilidade: 'Disponibilidade em dias:',
                pcd: 'PCD:',
                regiao: ''
            }}
                profissional={profissional}
            />

        );
    }

    getCurriculo = profissional => {
        const classeCss = profissional.jaVisualizado ? "jaVisualizado" : "naoVisualizado";
        return (
            <div className="row" style={{ fontSize: 10 }}>
                <div className="col col-md-12">Inclusão: {profissional.dataDeInclusaoFormatada}</div>
                <div className="col col-md-12">Alteração: {profissional.dataDeAlteracaoFormatada}</div>
                <div className="col col-md-12">Cadastro: {profissional.cadastro}</div>
                <div className="col col-md-12"><a target="_blank" href={`https://ccpjobsnow.com/#/recrutadores/buscaDeCandidatos?cvKey=${profissional.pseudonimo}&recruiter=${this.state.vaga.recruiter}`}>Currículo em arquivo</a></div>
                <div className="col col-md-12"><a target="_blank" href={`https://ccpjobsnow.com/curriculos/${this.state.vaga.recruiter}/${profissional.pseudonimo}/texto`} >Currículo em texto</a></div>
                <div className={`col col-md-12 ${classeCss}`}><strong >{profissional.jaVisualizado ? 'Currículo visualizado em ' : ''}{profissional.dataDaUltimaVisualizacao}</strong></div>
            </div>

        );
    }

    getExperiencia = profissional => {
        return (
            <Linhas colunas={{
                experiencia: 'Anos de experiência: ',
                ultimaProfissao: 'Último cargo: ',
                profissaoDesejada: 'Cargo desejado: '
            }}
                profissional={profissional}
            />

        );
    }
    getPalavras = profissional => {
        return (
            <div className="row" style={{ fontSize: 10 }}>
                <Termos limite={profissional.termosEncontrados.length > 10 ? 3 : 2} titulo="Palavras encontradas" tipoTermo="termosEncontrados" profissional={profissional} />
                <div className="col col-md-12">&nbsp;</div>
                <Termos limite={2} titulo="Palavras NÂO encontradas" tipoTermo="termosNaoEncontrados" profissional={profissional} />
                <div className="col col-md-12">&nbsp;</div>
                <Termos limite={2} titulo="Palavras Extras" tipoTermo="termosExtras" profissional={profissional} />
            </div>
        );
    }
    render() {
        return (
            <div style={{ padding: "20px" }}>
                <Fieldset legend={`Detalhes desta vaga`} toggleable collapsed={this.state.collapseDescricao} onToggle={(e) => this.setState({ collapseDescricao: e.value })}>
                    <table id="customers">
                        <tr>
                            <td>Data de Inclusão:</td>
                            <td>{this.state.vaga.dataDeInclusaoFormatada}</td>
                            <td>Expira em:</td>
                            <td>{this.state.vaga.dataDeExpiracaoFormatada}</td>
                            <td>Contato:</td>
                            <td>{this.state.vaga.contato}</td>
                        </tr>
                        <tr>
                            <td>Descrição:</td>
                            <td colSpan="5">{this.state.vaga.vaga}</td>
                        </tr>
                        <tr>
                            <td>Palavras chaves:</td>
                            <td colSpan="2">{this.state.vaga.requisitos && this.state.vaga.requisitos.join(', ')}</td>
                            <td>Palavras Extras:</td>
                            <td colSpan="2">{this.state.vaga.sinonimos && this.state.vaga.sinonimos.join(', ')}</td>
                        </tr>
                    </table>
                </Fieldset>
                <p>&nbsp;</p>
                <DataTable value={this.state.vaga.profissionais} columnResizeMode="fit"
                    paginator rows={10} totalRecords={this.state.vaga.totalDeProfissionais}
                    paginatorPosition="both"
                    paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                    currentPageReportTemplate={`Exibindo do ${this.state.first + 1}º ao ${this.state.last}º do total de ${this.state.vaga.totalDeProfissionais} profissionais`}
                    lazy first={this.state.first}
                    onPage={parametros => this.getVagaComProfissionais(parametros.page + 1, parametros.first, parametros.last, parametros)}
                    loading={this.state.loading}

                    emptyMessage="Esta vaga não encontrou candidatos">
                    <Column header={`Média de afinidade com esta vaga: ${this.state.vaga.afinidadeMedia} pontos`} body={profissional => this.getRanking(profissional)} />
                    <Column header="Pretensões salariais" body={profissional => this.getPretensoes(profissional)} />
                    <Column header="Experiência" body={profissional => this.getExperiencia(profissional)} />
                    <Column header="Outros Dados" body={profissional => this.getOutrosDados(profissional)} />
                    <Column header="Palavras desta vaga no currículo" body={profissional => this.getPalavras(profissional)} />
                    <Column header="Currículo" body={profissional => this.getCurriculo(profissional)} />

                </DataTable>
                <p>&nbsp;</p>
                <Fieldset legend={`Grupos do telegram onde publicamos esta vaga`} toggleable collapsed={this.state.collapseGrupos} onToggle={(e) => this.setState({ collapseGrupos: e.value })}>
                    <table id="customers">
                        <tr>
                            <th>
                                #
                            </th>
                            <th>
                                Nome do Grupo (Clique no nome para entrar)
                            </th>
                            <th>
                                Total de membros
                            </th>
                        </tr>
                        {
                            (this.state.vaga.gruposDeVagas || []).map((grupo, idx) =>
                                <tr>
                                    <td>{idx + 1}</td>
                                    <td><a href={grupo.link} target="_blank">{grupo.nome}</a></td>
                                    <td>{grupo.membros}</td>
                                </tr>)
                        }
                    </table>
                </Fieldset>
                <p>&nbsp;</p>
                <Fieldset legend={`Como fazer para este sistema publicar para outras dezenas de grupos automaticamente???`} toggleable collapsed={this.state.collapsePublicarParaOutrosGrupos} onToggle={(e) => this.setState({ collapsePublicarParaOutrosGrupos: e.value })}>
                    <p>Abaixo temos uma relação de grupos de vagas que você pode entrar, mas não seria mais prático se as vagas que você cadastra aqui neste site fossem automaticamente publicadas nesses grupos abaixo? Te pouparia um extremos trabalho manual e alcançaria dezenas de milhares de pessoas numa tacada só? Não? Pois então, instale o telegram no teu celular, entre em cada um desses grupos de vagas e faça pressão para que os seus administradores aceitem <a href="https://t.me/JnSuporteBot" target="_blank">o meu bot</a> em seus grupos </p>
                    <table id="customers">
                        <tr>
                            <th>#</th>
                            <th>Grupo de vagas do telegram</th>
                        </tr>
                        {
                            this.state.vaga.outrosGruposDoTelegram.map((x, y) => <tr><td>{y + 1}</td>
                                <td><a href={x} target="_blank">{x}</a></td></tr>)
                        }
                    </table>
                    <p><small>**Por favor, <a target="_blank" href="mailto:onias@ccpjobsnow.com">nos informe</a> se algum dos links dos grupos estiver quebrado ou se tiver mais grupos para nos passar e aumentar nossa lista</small></p>
                </Fieldset>

            </div>);
    }

}
export default withRouter(Curriculos)
class Termos extends Component {

    render() {

        const termos = this.props.profissional[this.props.tipoTermo];
        if (!termos) {
            return null;
        }
        if (termos.length <= this.props.limite) {
            return null;
        }
        return (
            <div className="col col-md-12">
                <div style={{ color: 'red' }}>{this.props.titulo}:</div>  {termos.slice(0, termos.length - this.props.limite).map(x => x.termo).join(', ')}
            </div>
        );
    }
}
class Linhas extends Component {
    render() {
        let array = [];
        for (let atributo in this.props.colunas) {
            const value = this.props.profissional[atributo];
            if (!value) {
                continue;
            }
            if (value.trim() == '-') {
                continue;
            }
            const label = this.props.colunas[atributo];
            array.push({ value, label });
        }
        if (!array.length) {
            return <div className="row" style={{ fontSize: 10 }}><div className="col col-md-12">Sem dados para exibir</div></div>;
        }
        return (
            <div className="row" style={{ fontSize: 10 }}>
                {
                    array.map(x => <div><div className="col col-md-12">{x.label} {x.value}</div> <div className="col col-md-12">&nbsp;</div></div>)
                }

            </div>
        );
    }

}