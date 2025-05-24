import JnAjax from '../../../services/JnAjax';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import Alert from '../../../jncomponents/JnAlert';
import { InputTextarea } from 'primereact/inputtextarea';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Calendar } from 'primereact/calendar';
import { Fieldset } from 'primereact/fieldset';
import exibirMenssagem from '../../../jncomponents/ExibirMensagem';

import React from 'react';

const tiposVagas = [
    {
        label: "T.I (Tecnologia em Informática)",
        value: 1
    },
    {
        label: "RH, DP, R&S e afins",
        value: 2
    },
    {
        label: "Vagas que requerem ensino superior, curso técnico ou certificação",
        value: 3
    },
    {
        label: "Vagas operacionais",
        value: 4
    }
];



const formattedDate = (d) => {

    let month = String(d.getMonth() + 1);
    let day = String(d.getDate());
    const year = String(d.getFullYear());

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return `${day}/${month}/${year}`;
}
const getRegistros = (registros) => {

    registros.forEach((element, idx) => {
        element.tipo = tiposVagas.filter(x => x.value == element.tipoVaga)[0].label;
        element.data = element.dataDeExpiracao ? formattedDate(new Date(element.dataDeExpiracao)) : '';
        element.indice = (idx + 1) + ' ';
    });

    return registros;
}


export default class ListaDeVagas extends React.Component {
    locale = {
        firstDayOfWeek: 1,
        dayNames: ["domingo", "segunda feira", "terça feira", "quarta feira", "quinta feira", "sexta feira", "sábado"],
        dayNamesShort: ["dom", "seg", "ter", "qua", "qui", "sex", "sáb"],
        dayNamesMin: ["D", "S", "T", "Q", "Q", "S", "S"],
        monthNames: ["janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"],
        monthNamesShort: ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"],
        today: 'Hoje',
        clear: 'Limpar',
        dateFormat: 'dd/mm/yy',
        weekHeader: 'Sm'
     };

    state = {
        registros: [],
        panelCollapsed: true
    }
    formatos = {
        pdf: 'application/pdf',
        doc: 'application/msword',
        docx: 'application/msword'

    };

    modal = null;

    getRegistro = (element) => {

        element.tipo = tiposVagas.filter(x => x.value == element.tipoVaga)[0].label;
        element.dataDeInclusaoFormatada = formattedDate(new Date());
        element.data = formattedDate(new Date(element.dataDeExpiracao));
        element.indice = (this.state.registros.length + 1) + ' ';
        return element;
    };

    redirecionarParaLogin = (detail) => {

        sessionStorage.removeItem('sessao');
        const queryParameters = `?email=${this.props.location.query.email || ''}&mensagem=${detail}`;
        window.location.href = '#/login' + queryParameters;
    };

    aoSalvar = (vaga) => {

        if (!window.confirm('Confirma inclusão?')) {
            return;
        }


        const callbacks = {};
        const token = JnAjax.getToken();
        callbacks[200] = novaVaga => {
            const registro = this.getRegistro(novaVaga);
            const registros = this.state.registros.concat(registro);
            this.setState({ registros, panelCollapsed: registros.length });
        };

        callbacks[401] = () => this.redirecionarParaLogin('requerLogin');
        callbacks[403] = () => this.redirecionarParaLogin('rhRequerido');
        callbacks[422] = msg => exibirMenssagem(msg);
        callbacks[409] = () => exibirMenssagem('Esta vaga já foi cadastrada');
        callbacks[400] = () => exibirMenssagem('Sua vaga deve ser maior que 10 caracteres');
        callbacks[413] = () => exibirMenssagem('Sua vaga deve ser menor que 2500 caracteres, devido a limitações de envio do telegram');
        callbacks[404] = () => exibirMenssagem(`Essa vaga não possui palavras chaves para a vaga do tipo '${vaga.tipo || tiposVagas.filter(x => x.value == vaga.tipoVaga)[0].label}'`, 8000);
        callbacks['onUnexpectedHttpStatus'] = () => exibirMenssagem('Ocorreu um erro ao salvar esta vaga, por favor, notifique onias@ccpjobsnow.com', 8000);

        JnAjax.doAnAjaxRequest(`vagas/${token.email}/ativas`, callbacks, 'POST', vaga);
    }

    excluir = (vaga) => {
        if (!window.confirm('Confirma exclusão?')) {
            return;
        }
        const callbacks = {};
        const token = JnAjax.getToken();
        callbacks[401] = () => this.redirecionarParaLogin('requerLogin');
        callbacks[403] = () => this.redirecionarParaLogin('rhRequerido');
        callbacks[200] = () => {
            const registros = this.state.registros.filter(x => x.id != vaga.id);
            this.setState({ registros, panelCollapsed: registros.length });
        };
        callbacks['onUnexpectedHttpStatus'] = () => exibirMenssagem('Ocorreu um erro ao excluir esta vaga, por favor, notifique onias@ccpjobsnow.com', 8000);

        JnAjax.doAnAjaxRequest(`vagas/${token.email}/${vaga.id}`, callbacks, 'DELETE');

    }


    executarDownloadDoCurriculo = (candidato, recruiter) => {

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

    }


    login = (cvKey, tokenToLogin, recruiter) => {

        const callbacks = {};

        callbacks[401] = () => this.redirecionarParaLogin('requerLogin');
        callbacks[403] = () => this.redirecionarParaLogin('rhRequerido');
        callbacks[200] = token => this.entrarNoSistema(token, cvKey, recruiter);
        callbacks['afterHttpRequest'] = () => {
            this.props.router.push('/recrutadores/vagas');
            this.refresh();
        };
        const requestBody = { tokenToLogin };

        JnAjax.doAnAjaxRequest(`login/${recruiter.trim()}/recruiter`, callbacks, 'POST', requestBody);
        return true;
    }

    entrarNoSistema = (token, cvKey, recruiter) => {

        const uri = 'recrutadores/vagas';
        const email = recruiter;
        const acesso = 299;

        const sessao = { email, token, uri, acesso };

        sessionStorage.setItem('sessao', JSON.stringify(sessao));

        const callbacks = {};

        callbacks[200] = candidato => this.executarDownloadDoCurriculo(candidato, recruiter);
        callbacks[404] = msg => exibirMenssagem(msg.content.message);
        callbacks[400] = msg => exibirMenssagem(msg.content.message);
        callbacks[401] = msg => exibirMenssagem(msg);

        JnAjax.doAnAjaxRequest(`curriculos/${recruiter}/${cvKey}/arquivo`, callbacks, 'GET');

    }

    refresh = () => {
        const callbacks = {};
        callbacks[401] = () => this.redirecionarParaLogin('requerLogin');
        callbacks[403] = () => this.redirecionarParaLogin('rhRequerido');
        callbacks[200] = registros => this.setState({ registros: getRegistros(registros), panelCollapsed: registros.length });
        const token = JnAjax.getToken();
        if (!token) {
            return true;
        }
        callbacks['onUnexpectedHttpStatus'] = () => exibirMenssagem('Ocorreu um erro ao listar vagas, por favor, notifique onias@ccpjobsnow.com', 8000);
        JnAjax.doAnAjaxRequest(`vagas/${token.email}/ativas`, callbacks, 'GET');
        return true;
    }

    getText = (rowData, field) => {

        return <a href="#" title={rowData[field]} onClick={e => { e && e.preventDefault(); alert(rowData[field]) }}>{rowData[field] && rowData[field].substring(0, 100)}</a>;
    }

    getBotoesDeAcoes = (rowData) => {
        return (
            <div className="row">
                <div className="col col-md-6">
                    <Button icon="pi pi-times" label="Remover" onClick={() => this.excluir(rowData)} className="p-button-danger" />
                </div>
                <div className="col col-md-12">
                    <br />
                </div>
                {
                    rowData.tipoVaga == 4 ? null :
                        <div className="col col-md-6">
                            <Button icon="pi pi-download" label="Ver Currículos" className="p-button-warning" onClick={() => window.open('#/recrutadores/redirect?idPesquisa=' + (rowData.id || rowData.idVaga))} />
                        </div>

                }
            </div>
        );
    }

    getGrupos = x => {
        const gruposDeVagas = x.gruposDeVagas || [];

        if (!gruposDeVagas.length) {
            return <div>Atualize esta página para ver</div>
        }

        return <div>
            {
                gruposDeVagas.map((y, idx) => <div className="row"><a href={y.link} target="_blank">{`${idx + 1}: Grupo com ${y.membros} membros`}</a></div>)
            }
        </div>
    }

    componentDidMount() {
        const { cvKey, loginToken, recruiter } = this.props.location.query;
        (loginToken && recruiter && this.login(cvKey, loginToken, recruiter)) || this.refresh();
    }

    render() {

        const token = JnAjax.getToken();
        if (!token) {
            return null;
        }
        return (
            <div style={{ padding: "20px" }}>
                <Fieldset legend={this.state.panelCollapsed ? "Expanda-me" : "Contraia-me"} toggleable collapsed={this.state.panelCollapsed} onToggle={(e) => this.setState({ panelCollapsed: e.value })}>

                    <div className="p-messages p-component p-messages-info p-messages-enter-done"><div className="p-messages-wrapper"><span className="p-messages-icon pi"></span><ul><li><span>
                        Boas vindas, profissional de recrutamento, porque cadastrar suas vagas aqui?<br /><br />
                        <br /> 1) Você receberá em seu telegram currículos aderentes à vaga cadastrada, alguns segundos depois que esses currículos forem alterados ou inseridos por seus donos;
                         <br />2) A vaga será imediatamente entregue em grupos de vagas no telegram com centenas ou milhares de pessoas, este processo será repetido nos grupos de vagas todas as madrugadas até sua data de expiração;
                         <br />3) A vaga será imediatamente entregue aos donos dos currículos aderentes no privado do telegram;
                         <br />4) Imediatamente após a gravação da sua vaga, te damos uma sugestão de lista de currículos que julgamos serem aderentes;
                    </span><span className="p-messages-detail"></span></li></ul></div></div>
                </Fieldset>
                <br />
                <div className="row">
                    <div className="col col-md-4">

                    </div>
                    <div className="col col-md-4">
                        <Button icon="pi pi-file" label="Nova Vaga" onClick={() => this.modal && this.modal.exibir({})} />
                    </div>
                    <div className="col col-md-4">
                        <ModalVaga ref={(el) => this.modal = el} aoSalvar={vaga => this.aoSalvar(vaga)} />
                    </div>
                </div>
                <br />
                <div className="row" style={{ padding: "20px" }}>
                    <div className="col col-md-12">
                        <DataTable value={this.state.registros} columnResizeMode="fit"
                            emptyMessage="Você ainda não cadastrou vagas conosco"
                        >
                            <Column header="#" body={x => this.getText(x, 'indice')} />
                            <Column header="Inclusão" body={x => this.getText(x, 'dataDeInclusaoFormatada')} />
                            <Column header="Descrição" body={x => this.getText(x, 'vaga')} />
                            <Column header="Tipo da Vaga" body={x => this.getText(x, 'tipo')} />
                            <Column header="Expira em" body={x => this.getText(x, 'data')} />
                            <Column header="Onde publicamos" body={x => this.getGrupos(x)} />
                            <Column header="Ações" body={this.getBotoesDeAcoes} />
                        </DataTable>
                    </div>
                </div>

            </div>

        );
    }

}


class ModalVaga extends React.Component {

    state = {
        vaga: '',
        id: '',
        tipoVaga: 1,
        contato: '',
        dataDeExpiracao: new Date().getTime() + (7 * 24 * 60 * 60 * 1000)
    };

    locale = {
        firstDayOfWeek: 1,
        dayNames: ["domingo", "segunda feira", "terça feira", "quarta feira", "quinta feira", "sexta feira", "sábado"],
        dayNamesShort: ["dom", "seg", "ter", "qua", "qui", "sex", "sáb"],
        dayNamesMin: ["D", "S", "T", "Q", "Q", "S", "S"],
        monthNames: ["janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"],
        monthNamesShort: ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"],
        today: 'Hoje',
        clear: 'Limpar',
        dateFormat: 'dd/mm/yy',
        weekHeader: 'Sm'
    };


    modal = null;

    resetScreen = () => {
        const state = {
            vaga: '',
            id: '',
            tipoVaga: 1,
            contato: '',
            dataDeExpiracao: new Date().getTime() + (7 * 24 * 60 * 60 * 1000)
        };

        this.setState(state);
    };

    exibir = (vaga) => {

        if (!vaga) {
            return;
        }
        this.setState({ ...vaga });
        const texto = vaga.id ? 'Editar Vaga' : 'Nova Vaga';
        !vaga.id && this.resetScreen();
        this.modal && this.modal.exibir(texto);
    }

    render() {
        return (
            <div className="row">
                <Alert ref={(el) => this.modal = el} labelBotao="Salvar" onClick={() => this.props.aoSalvar && this.props.aoSalvar(this.state) && this.resetScreen()}>
                    <br />
                    <div className="row">
                        <div className="col col-md-12">
                            <h5>Descrição da vaga (Máximo de 2.500 caracteres):</h5>
                            <InputTextarea value={this.state.vaga} onChange={e => this.setState({ vaga: e.target.value })} rows={5} cols={30} maxLength={2500} />
                        </div>
                        <div className="col col-md-12">
                            <br />
                        </div>
                        <div className="col col-md-12">
                            <label>Tipo de mão de obra: </label><br />
                            <Dropdown value={this.state.tipoVaga} options={tiposVagas} style={{ width: "730px" }} onChange={e => this.setState({ tipoVaga: e.value })} />
                        </div>
                        <div className="col col-md-12">
                            <br />
                        </div>

                        <div className="col col-md-12">
                            Contato ou link para candidatura:
                                <InputText id="in" value={this.state.contato} onChange={e => this.setState({ contato: e.target.value })} />
                        </div>
                        <div className="col col-md-12">
                            <br />
                        </div>
                        <div className="col col-md-12">
                            Expira em:<br />
                            <Calendar value={new Date(this.state.dataDeExpiracao)} onChange={e => this.setState({ dataDeExpiracao: e.value.getTime() })} dateFormat="dd/mm/yy" readOnlyInput locale={this.locale} />
                        </div>

                    </div>
                </Alert>
            </div>

        );
    }


}
