import { Tree } from 'primereact/tree';
import InputDeRequisitos from '../../recruiters/searchCandidates/InputDeRequisitos';
import JnAlert from '../../../jncomponents/JnAlert';

import JnAjax from '../../../services/JnAjax';
import getRegioes from '../../../jncomponents/JnRegion';
const regioes = getRegioes();
import formatarDinheiro from '../../../jncomponents/JnFormatCurrency';
import Profissionais from './Profissionais';
import PubSub from 'pubsub-js';
import exibirMensagem from '../../../jncomponents/ExibirMensagem';
import { Messages } from 'primereact/messages';
import React from 'react';

export default class ArvoreDDDs extends React.Component {
    state = {
        must: [],
        ddds: [],
        total: 0,
        nodes: [],
        should: [],
        loading: false
    };

    exibirErro = (errorMessage) => {
        this.messagesFiltros.show({ severity: 'error', life: 100000, summary: 'Erro!', detail: errorMessage });
    }

    atualizarEstatisticas = (response, parameters) => {
        PubSub.publish('atualizouTermosDaPesquisa', parameters);
        let nodes = response.ddds;
        nodes.forEach(x => {
            if(!x.ddd){
                 x.ddd = 10;
            }
            x.leaf = false;
            x.teste = true;
            x.children = [];
            x.children.push({ label: '', key: x.ddd + '1', ddd: x.ddd });
            x.icon = "pi pi-fw pi-folder";
            x.key = x.ddd;
            const msgExperiencia = x.experiencia ? `Experiência Média: ${new Date().getFullYear() - x.experiencia} Anos` : '';
            x.label = `${regioes[x.ddd]} -  ${x.total} Currículos. Pretensões salariais médias: [PJ: ${formatarDinheiro(x.pj)}  CLT: ${formatarDinheiro(x.clt)}  Bitcoin: ${formatarDinheiro(x.bitcoin)}] ${msgExperiencia}`;
        });


        nodes = nodes.filter(x => x.ddd >= 0);
        nodes.sort((a, b) => {

            return a.ddd - b.ddd;
        });

        const total = response.total;
        const loading = false;
        const should = this.state.should.length || (this.props.location.query.requisitosDesejaveis || []).length;
        const must = this.state.must.length || (this.props.location.query.requisitosObrigatorios || []).length; 
        const totalDeParametrosPesquisados = must + should;
        const message = !total? 'Não encontramos currículos para os termos pesquisados, sua pesquisa foi salva e nossa equipe já sabe o que você está procurando!' : totalDeParametrosPesquisados ? `Encontramos ${total} currículos com base nos termos que você digitou acima` : `Estamos listando todos os nossos ${total} currículos, sugerimos que você preencha (e dê enter) algum dos campos acima para ter um resultado mais assertivo!`;
        this.setState({ nodes, loading, total, message, ...parameters });

        response.erro && this.exibirErro(response.erro);
        response.candidate && this.executarDownloadDoCurriculo(response.candidate);

        const termoCorrente = parameters.termoCorrente;
        const sinonimos = (response.sinonimos && response.sinonimos[termoCorrente] && response.sinonimos[termoCorrente].join(', '));
        if (termoCorrente && termoCorrente.toUpperCase() != sinonimos) {
            const fraseSinonimos = `Exibindo resultados para o termo "${termoCorrente}" e também para os seguintes termos associados a este termo: ${sinonimos}`;
            exibirMensagem(fraseSinonimos, 5000);
        }
        this.executarAcao();

    }

    nodeTemplate = (node) => {
        if (node.teste) {
            return (
                <div>{node.label}</div>
            )
        }
        const nodes = this.state.nodes;

        const index = nodes.findIndex(x => x.key == node.ddd);

        return (
            <Profissionais ddd={node.ddd} key={node.ddd + '1'} total={nodes[index].total} must={this.state.must} should={this.state.should} />
        );
    }

    changeIcon = (event, icon) => {
        const nodes = this.state.nodes;
        const node = event.node;
        const index = nodes.findIndex(x => x.key == event.node.key);
        nodes[index] = node;
        node.icon = icon;
        this.setState({ nodes });

    }

    executarAcao = () => {

        if (!this.props.location.query.acao) {
            return;
        }

        if (this.props.location.query.acao == 'Descadastrar') {
            if (!this.props.location.query.email) {
                return;
            }
            if (!window.confirm('Confirmar descadastramento do seu e-mail?')) {
                return;
            }
            const callbacks = {};
            callbacks[404] = () => { this.jnAlert && this.jnAlert.exibir('Erro!', 'O recrutador não foi encontrado na base de dados') };
            callbacks[401] = () => { this.jnAlert && this.jnAlert.exibir('Erro!', 'O token para inativação, presente na url não confere com o banco de dados') };
            callbacks[200] = () => { this.jnAlert && this.jnAlert.exibir('Sucesso!', 'A partir de agora você não mais receberá nossos e-mails com currículos') };
            JnAjax.doAnAjaxRequest(`recrutadores/${this.props.location.query.email}/inativacao/${this.props.location.query.tokenToInactivate}`, callbacks, 'DELETE');
            return;
        }

    }

    formatos = {
        pdf: 'application/pdf',
        doc: 'application/msword',
        docx: 'application/msword'

    };

    alert = null;

    executarDownloadDoCurriculo = (candidato) => {
        if (!candidato) {
            return;
        }
        const link = document.createElement("a");

        link.download = candidato.arquivo;

        const lastIndexOf = candidato.arquivo.lastIndexOf('.') + 1;

        const extension = candidato.arquivo.substring(lastIndexOf);

        const type = this.formatos[extension];
        let data = `data:${type};base64,${candidato.curriculo}`;

        if (candidato.curriculo.startsWith('data:')) {
            data = candidato.curriculo;
        }

        link.href = data;

        link.click();
        const parameters = this.getParametersFromUrl();

        const must = parameters.must || this.state.must;
        const should = parameters.should || this.state.should;

        window.location.href = (`#/recrutadores/buscaDeCandidatos?requisitosObrigatorios=${must.filter(x => x.length > 1).map(x => encodeURIComponent(x)).join(',')}&requisitosDesejaveis=${should.filter(x => x.length > 1).map(x => encodeURIComponent(x)).join(',')}&termoCorrente=${parameters.termoCorrente?encodeURIComponent(parameters.termoCorrente) : ''}&email=${this.props.location.query.recruiter || ''}`);
    }

    atualizarArvore = (parameters) => {

        const should = parameters.should || this.state.should;
        const must = parameters.must || this.state.must;
        const termoCorrente = parameters.termoCorrente || this.state.termoCorrente;
        const callbacks = [];
        const recruiter = this.props.location.query.recruiter || '';
        const cvKey = this.props.location.query.cvKey || '';

        this.setState({ loading: true });
        callbacks['200'] = response => this.atualizarEstatisticas(response, parameters, recruiter);

        JnAjax.doAnAjaxRequest(`curriculos/pesquisa?recruiter=${recruiter}&cvKey=${cvKey}`, callbacks, 'POST', { should, must, termoCorrente });


    }

    reescreverUrl = (parameters) => {
        const must = parameters.must || this.state.must;
        const should = parameters.should || this.state.should;

        window.location.href = (`#/recrutadores/buscaDeCandidatos?requisitosObrigatorios=${must.filter(x => x.length > 1).map(x => encodeURIComponent(x)).join(',')}&requisitosDesejaveis=${should.filter(x => x.length > 1).map(x => encodeURIComponent(x)).join(',')}&termoCorrente=${parameters.termoCorrente?encodeURIComponent(parameters.termoCorrente) : ''}`);

        this.atualizarArvore(parameters);
    }

    getParametersFromUrl = () => {
        const parameters = {
            must: !this.props.location.query.requisitosObrigatorios ? [] : this.props.location.query.requisitosObrigatorios.split(',').filter(x => x.trim().length > 1),
            should: !this.props.location.query.requisitosDesejaveis ? [] : this.props.location.query.requisitosDesejaveis.split(',').filter(x => x.trim().length > 1),
            termoCorrente: this.props.location.query.termoCorrente || ''
        }

        return parameters;
    }

    componentDidMount() {
      
        const parameters = this.getParametersFromUrl();
        if(this.props.location.query.cvTexto){
            window.open(this.props.location.query.cvTexto);
        }
        this.atualizarArvore(parameters);
    }

    render() {
        return (
            <div>
                <div id="content" style={{ paddingLeft: "10px", paddingRight: "10px", paddingTop: "5px" }}>
                    <Messages ref={(el) => this.messagesFiltros = el} closable={false} />
                    <JnAlert ref={(el) => this.jnAlert = el} />
                    <InputDeRequisitos requisitos={this.state.must} getRequisitos={(must, termoCorrente) => this.reescreverUrl({ must, termoCorrente })} exibirErro={this.exibirErro} nomeTermo="Os currículos DEVERÃO conter:" descricaoTermo="Todos estes requisitos devem estar presentes nos currículos encontrados" />
                    <br />
                    <br />
                    <InputDeRequisitos requisitos={this.state.should} getRequisitos={(should, termoCorrente) => this.reescreverUrl({ should, termoCorrente })} exibirErro={this.exibirErro} nomeTermo="Os currículos PODERÃO conter:" descricaoTermo="Ao menos um destes requisitos devem estar presentes nos currículos encontrados" />
                    <br />
                    <br />
                    {this.state.message && 
                    <div className="p-messages p-component p-messages-info p-messages-enter-done"><div className="p-messages-wrapper"><span className="p-messages-icon pi  pi-info-circle"></span><ul><li><span className="p-messages-summary">{this.state.message}</span><span className="p-messages-detail"></span></li></ul></div></div>
                    }
                    <br />
                    <br />
                    <Tree style={{ width: "100%" }} value={this.state.nodes} nodeTemplate={this.nodeTemplate} onExpand={e => this.changeIcon(e, 'pi pi-fw pi-folder-open')}
                        onCollapse={e => this.changeIcon(e, 'pi pi-fw pi-folder')}
                        loading={this.state.loading} />
                </div>
            </div>
        )
    }
}
