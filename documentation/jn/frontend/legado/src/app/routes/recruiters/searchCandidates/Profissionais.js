import React from 'react';

import JnAjax from '../../../services/JnAjax';
import getRegioes from '../../../jncomponents/JnRegion';
const regioes = getRegioes();
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import formatarDinheiro from '../../../jncomponents/JnFormatCurrency';
import JnAlert from '../../../jncomponents/JnAlert';
import { Dropdown } from 'primereact/dropdown';
import 'react-fontawesome';
import { Checkbox } from 'primereact/checkbox';
import PubSub from 'pubsub-js';
import './profissionais.css';
import { withRouter } from 'react-router';
import exibirMensagem from '../../../jncomponents/ExibirMensagem';


class Profissionais extends React.Component {
    formatos = {
        pdf: 'application/pdf',
        doc: 'application/msword',
        docx: 'application/msword'

    };

    ordenacoes = [
        {
            label: "Pretensões PJ mais baixas",
            value: "pretensaoPj"
        },
        {
            label: "Pretensões CLT mais baixas",
            value: "pretensaoClt"
        },
        {
            label: "Mais recentes",
            value: "data"
        },
        {
            label: "Candidatos mais experientes",
            value: "experiencia"
        }
    ];

    tiposVagas = [
        {
            label: "Indiferente...",
            value: 'valorNumericoInvalido'
        },
        {
            label: "T.I (Tecnologia em Informática)",
            value: 1
        },
        {
            label: "RH, DP, R&S e afins",
            value: 2
        },
        {
            label: "Vagas de nível superior",
            value: 3
        },
        {
            label: "Vagas Operacionais",
            value: 4
        }
    ];



    state = {
        sortField: '',
        sortType: 'asc',
        filtrarPorPcd: false,
        tipoVaga: '',
        profissionais: [],
        page: 0,
        rows: 50,
        must: [],
        should: [],
        mudanca: false,
        first: 0
    }
    curriculoEmTexto = (rowData) => {
        return <a href="#" onClick={e => this.abrirCurriculoEmModoTexto(e, rowData)}>Ver este currículo em modo texto</a>;
    }

    abrirCurriculoEmModoTexto = (e, rowData) => {
        e && e.preventDefault();
        const email = JnAjax.getToken().email;

        if (!email) {
            // console.log(this.props.location);

            const delimitador = this.props.location.search ? '&' : '?';
            const nextUrl = encodeURIComponent(this.props.location.pathname + this.props.location.search + delimitador + `cvTexto=${JnAjax.getUrlBackEnd(JnAjax.deafultEnviroment)}/curriculos/${email}/${rowData.pseudonimo}/texto`);
            // alert(nextUrl); 
            this.props.router.push(`/login?nextUrl=${nextUrl}&mensagem=msgBaixarCurriculos`);

            return;
        }


        window.open(`${JnAjax.getUrlBackEnd(JnAjax.deafultEnviroment)}/curriculos/${email}/${rowData.pseudonimo}/texto`);
        this.salvarVisualizacaoDoCurriculo(rowData);
    }

    curriculoEmArquivo = rowData => {

        return <a onClick={e => this.baixarCurriculo(e, rowData)} href="#">Ver este currículo em modo arquivo</a>;
    }

    getDemaisDados = rowData => {
        const experiencia = !rowData.experiencia ? " Não informada " : (new Date().getFullYear() - rowData.experiencia) > 1 ? (new Date().getFullYear() - rowData.experiencia) + " Anos " : "1 ano";

        const tipoProfissao = ["T.I (Tecnologia em Informática)", "RH, DP, R&S e afins", "Vagas de nível superior", "Vagas Operacionais"][rowData.tipoVaga > 0 ? rowData.tipoVaga - 1 : 1];

        return (
            <div>
                {
                    rowData.tipoVaga &&
                    <div className="row">
                        <div className="col col-md-12">Tipo da profissão:  {tipoProfissao}</div>
                    </div>
                }
                <div className="row">
                    &nbsp;
                </div>
                {
                    rowData.ultimaProfissao &&
                    <div className="row" >
                        <div className="col col-md-12" title={rowData.ultimaProfissao}>Última profissão:  {rowData.ultimaProfissao}</div>
                    </div>
                }
                <div className="row">
                    &nbsp;
                </div>
                {
                    rowData.profissaoDesejada &&
                    <div className="row" style={{}}>
                        {
                            <div className="col col-md-12">Cargo desejado:   {rowData.profissaoDesejada}</div>
                        }
                    </div>
                }
                <div className="row">
                    &nbsp;
                </div>
                <div className="row">
                    <div className="col col-md-12">Experiência (Anos):   {experiencia}</div>
                </div>
                <div className="row">
                    &nbsp;
                </div>
                {
                    rowData.pretensaoPj > 0 &&
                    <div className="row">
                        <div className="col col-md-12">PJ: {formatarDinheiro(rowData.pretensaoPj)}</div>
                    </div>
                }
                <div className="row">
                    &nbsp;
                </div>
                {
                    rowData.pretensaoClt > 0 &&
                    <div className="row">
                        <div className="col col-md-12">CLT: {formatarDinheiro(rowData.pretensaoClt)}</div>
                    </div>
                }
                <div className="row">
                    &nbsp;
                </div>
                {
                    rowData.bitcoin > 0 &&
                    <div className="row">
                        <div className="col col-md-12">Bitcoin (Valor equivalente em reais): {formatarDinheiro(rowData.bitcoin)}</div>
                    </div>
                }
                <div className="row">
                    &nbsp;
                </div>
                {
                    rowData.disponibilidade > 0 &&
                    <div className="row">
                        <div className="col col-md-12">Disponibilidade de : {formatarDinheiro(rowData.disponibilidade)} Dia(s)</div>
                    </div>
                }
                <div className="row">
                    &nbsp;
                </div>
                {
                    rowData.pretensaoNegociavel != undefined &&
                    <div className="row">
                        <div className="col col-md-12">{rowData.pretensaoNegociavel ? 'Pretensões negociáveis' : 'Pretensões não negociáveis'}</div>
                    </div>
                }
                <div className="row">
                    &nbsp;
                </div>
                {
                    rowData.pcd &&
                    <div className="row">
                        <div className="col col-md-12">Pessoa portadora de necessidades especiais</div>
                    </div>

                }
                <div className="row">
                    &nbsp;
                </div>
                {
                    (rowData.homeoffice || !rowData.ddd) ?
                        <div className="row">
                            <div className="col col-md-12">Este perfil apenas avalia propostas homeoffice.</div>
                        </div>
                        :
                        <div className="row">
                            <div className="col col-md-12">{` ${regioes[rowData.ddd]} ${rowData.mudanca ? ' (Esta pessoa avalia oportunidades em outras regiões além do seu domícilio)' : ''}`}</div>
                        </div>

                }
                <div className="row">
                    &nbsp;
                </div>
                {
                    rowData.observacao &&
                    <div className="row">
                        <div className="col col-md-12">{this.getTextoLongo(rowData.observacao, 10)}</div>
                    </div>

                }
            </div>

        );
    }

    getTextoLongo = (str) => {
        return <div>


            <a href="#" title={str} onClick={e => {
                e && e.preventDefault();
                alert(str);
            }}>
                &nbsp;[Exibir Observações]
            </a>
        </div>
    }

    getEspecialidades = rowData => {
        return (
            <div>
                <div className="row">
                    {
                        rowData.requisitos && rowData.requisitos.length > 0
                            ?
                            rowData.requisitos.slice(0, 50).map((e, idx) => <div className="col col-md-12">{(idx + 1) + ': ' + e}</div>)
                            :
                            <div className="col col-md-12">-</div>
                    }
                </div>

            </div>
        );
    }


    getTextoDaVisualizacao = (pseudonimo) => {

        let visualizacao = localStorage.getItem(pseudonimo);

        if (!visualizacao) {
            return "";
        }

        try {
            visualizacao = JSON.parse(visualizacao);
            const x = <div className="row">
                <div className="col col-md-12">
                    Sua última visualização foi em {visualizacao.data}
                </div>
                <div className="col col-md-12">
                    Este currículo pertence a {visualizacao.email}
                </div>
            </div>
            return x;
        } catch (error) {
            return "";
        }

    }

    executarDownloadDoCurriculo = (cv, profissional) => {

        const link = document.createElement("a");

        link.download = cv.arquivo;

        const lastIndexOf = cv.arquivo.lastIndexOf('.') + 1;

        const extension = cv.arquivo.substring(lastIndexOf);

        const type = this.formatos[extension];
        let data = `data:${type};base64,${cv.curriculo}`;

        if (cv.curriculo.startsWith('data:')) {
            data = cv.curriculo;
        }

        this.setState({ type, data });

        link.href = data;

        link.click();

        this.salvarVisualizacaoDoCurriculo(profissional);
    }

    salvarVisualizacaoDoCurriculo = (profissional) => {
        const visualizacao = { email: profissional.id, data: new Date().toLocaleString() };
        localStorage.setItem(profissional.pseudonimo, JSON.stringify(visualizacao));
        this.state.profissionais.forEach(x => {
            if (x.id != profissional.id) {
                return;
            }
            x.textoDaVisualizacao = this.getTextoDaVisualizacao(x.pseudonimo);
        });
        this.setState({ profissionais: this.state.profissionais });
    }

    baixarCurriculo = (e, rowData) => {

        e && e.preventDefault();

        const email = JnAjax.getToken().email;
        const pseudonimo = rowData.pseudonimo;

        if (!email) {
            const delimitador = this.props.location.search ? '&' : '?';
            const nextUrl = encodeURIComponent(this.props.location.pathname + this.props.location.search + delimitador + `cvKey=${pseudonimo}&recruiter=${email}`);
            this.props.router.push(`/login?nextUrl=${nextUrl}&mensagem=msgBaixarCurriculos`);
            return;
        }


        const callbacks = {};

        callbacks[200] = res => this.executarDownloadDoCurriculo(res, rowData);
        callbacks[400] = res => exibirMensagem(res.content.message, 5000);
        callbacks[404] = res => exibirMensagem(res.content.message, 5000);
        callbacks[401] = () => {
            sessionStorage.removeItem('sessao');
            this.props.router.push(`/login?mensagem=rhRequerido&email=${email}`);

        };

        JnAjax.doAnAjaxRequest(`curriculos/${email}/${pseudonimo}/arquivo`, callbacks, 'GET');

    }

    refazerPesquisa = (parameters) => {

        let { first, rows, page, sortField, sortType, tipoVaga, filtrarPorPcd, mudanca, pcd, must, should } = parameters;

        first = first || this.state.first;

        rows = rows || this.state.rows;

        page = page || this.state.page;

        sortField = sortField || this.state.sortField;

        sortType = sortType || this.state.sortType;

        tipoVaga = tipoVaga || this.state.tipoVaga;

        pcd = pcd || this.state.pcd;

        mudanca = mudanca || this.state.mudanca;

        should = should || this.state.should;

        must = must || this.state.must;

        const email = JnAjax.getToken().email || 'INVISIVEL';

        const loading = false;

        const callbacks = [];

        this.setState({ loading: true });
        callbacks['200'] = response => {
            response.profissionais.forEach((x, idx) => {
                x.textoDaVisualizacao = this.getTextoDaVisualizacao(x.pseudonimo)
                x.idx = idx + 1;
            });

            this.setState({
                profissionais: response.profissionais.filter(x => x.pseudonimo), total: response.total,
                loading, first, rows, page, sortField, sortType, tipoVaga, filtrarPorPcd, mudanca, pcd, must, should
            });

        }

        const complementoDaUrl = mudanca === 'S' ? '/mudanca' : '';


        JnAjax.doAnAjaxRequest(`curriculos/pesquisa/${this.props.ddd == 10 ? 0 : this.props.ddd}${complementoDaUrl}?page=${page}&rows=${rows}`, callbacks, 'POST', { should, must, email, tipoVaga, sortField, sortType, pcd: 'S' === pcd });
    }

    rowClass = rowData => {
        const texto = rowData.textoDaVisualizacao;
        return {
            'jaVisto': !!texto
        }
    }

    getCurriculo = rowData => {
        return <div>
            <div className="row">
                <div className="col col-md-12">
                    {this.curriculoEmArquivo(rowData)}
                </div>
            </div>
            <br />
            <div className="row">
                <div className="col col-md-12">
                    {this.curriculoEmTexto(rowData)}
                </div>
            </div>
            <br />
            {rowData.textoDaVisualizacao || 'Você ainda não viu este currículo'}
            <div className="row">
                &nbsp;
            </div>
            {
                rowData.dataDeInclusaoFormatada &&
                <div className="row">
                    <div className="col col-md-12">Data de Inclusão:  {rowData.dataDeInclusaoFormatada}</div>
                </div>

            }
            <div className="row">
                &nbsp;
            </div>
            {
                rowData.dataDeAlteracaoFormatada &&
                <div className="row">
                    <div className="col col-md-12">Alteração mais recente:  {rowData.dataDeAlteracaoFormatada}</div>
                </div>

            }
            <div className="row">
                &nbsp;
            </div>
            {
                rowData.chatId ?
                    <div className="row">
                        <div className="col col-md-12">Cadastro feito via bot do telegram</div>
                    </div>
                    :
                    <div className="row">
                        <div className="col col-md-12">Cadastro feito neste site aqui mesmo</div>
                    </div>

            }
        </div>
    }

    componentDidMount() {

        PubSub.subscribe('atualizouTermosDaPesquisa', (msg, parameters) => this.refazerPesquisa(parameters));

        const should = this.props.should || [];

        const must = this.props.must || [];

        const email = JnAjax.getToken().email || 'INVISIVEL';

        const callbacks = [];
        callbacks['200'] = response => {

            response.profissionais.forEach((x, idx) => {
                x.textoDaVisualizacao = this.getTextoDaVisualizacao(x.pseudonimo)
                x.idx = idx + 1;
            });
            this.setState({
                profissionais: response.profissionais.filter(x => x.pseudonimo),
                total: response.total, must, should
            });
        }
        JnAjax.doAnAjaxRequest(`curriculos/pesquisa/${(this.props.ddd == 10 ? 0 : this.props.ddd)}?rows=50`, callbacks, 'POST', { should, must, email });

    }
    render() {
        const headerStyle2 = { fontSize: "10px", width: "20%" };
        const headerStyle3 = { fontSize: "10px", width: "40%" };
        const headerStyle = { fontSize: "10px", width: "30%" };
        return (
            <div className="datatable-style-demo">
                <div className="row"><div className="col col-md-12"><small style={{ color: "brown" }}>**Você pode também manter essa e as outras pastas abertas e ir fazendo as filtragens nos campos acima, as listagens de currículos irão se atualizando automaticamente</small></div></div>
                <br />
                <div style={{ textAlign: 'left' }} className="row">

                    <div className="col-md-6">
                        <label>Ordenar Resultados por: </label><br />
                        <Dropdown value={this.state.sortField} options={this.ordenacoes} style={{ width: "300px" }} onChange={e => this.refazerPesquisa({ sortField: e.value })} />
                    </div>
                    <div className="col-md-6">
                        <label>Tipo de mão de obra: </label><br />
                        <Dropdown value={this.state.tipoVaga} options={this.tiposVagas} style={{ width: "300px" }} onChange={e => this.refazerPesquisa({ tipoVaga: e.value })} />
                    </div>

                </div>
                <div style={{ textAlign: 'left' }} className="row">
                    <div className="col-md-6">
                        <br />
                        <label htmlFor="pcd" className="labelCkx">
                            <Checkbox id="pcd" checked={this.state.pcd === 'S'} onChange={e => this.refazerPesquisa({ pcd: e.checked ? 'S' : 'N' })} />
                            <label htmlFor="pcd" className="p-checkbox-label">Somente Candidatos PCD</label>
                        </label>
                    </div>
                    <div className="col-md-6">
                        {this.props.ddd > 0 && <div>
                            <br />
                            <Checkbox id="mudanca" checked={this.state.mudanca === 'S'} onChange={e => this.refazerPesquisa({ mudanca: e.checked ? 'S' : 'N' })} />
                            <label htmlFor="mudanca" className="p-checkbox-label">Avaliar candidatos que aceitem trocar de Cidade/UF</label>
                            <br />
                        </div>}
                    </div>
                </div>
                <JnAlert ref={(el) => this.alert = el} />
                <DataTable style={{ width: "100%" }} resizableColumns columnResizeMode="fit"
                    value={this.state.profissionais}
                    rowClassName={this.rowClass}
                    paginator rows={50} totalRecords={this.state.total}
                    paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                    lazy first={this.state.first} 
                    onPage={this.refazerPesquisa} 
                    loading={this.state.loading}
                >
                    <Column headerStyle={headerStyle2} bodyStyle={headerStyle} header="Até 50 Principais Palavras Chaves Detectadas" body={this.getEspecialidades} />
                    <Column headerStyle={headerStyle3} bodyStyle={headerStyle} header="Dados Profissionais" body={this.getDemaisDados} />
                    <Column headerStyle={headerStyle} bodyStyle={headerStyle} header="Download de Currículo" body={this.getCurriculo} />
                    <Column bodyStyle={{ width: "5%", fontSize: "10px" }} header="#" field="idx" headerStyle={{ width: "5%", fontSize: "10px" }} />

                </DataTable>
            </div>
        );
    }
}
export default withRouter(Profissionais)