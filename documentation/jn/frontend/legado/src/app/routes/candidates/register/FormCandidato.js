
import ComboRegiaoMetropolitana from '../../../jncomponents/ComboRegiaoMetropolitana';
import JnAjax from '../../../services/JnAjax';

import Alert from '../../../jncomponents/JnAlert';
import JnContainerCheckBox from '../../../jncomponents/JnContainerCheckBox';
import AlertBitcoin from '../../../jncomponents/JnAlert';
import JnDialog from '../../../jncomponents/JnMultiButtonDialog';
import exibirMensagem from '../../../jncomponents/ExibirMensagem';

import 'react-fontawesome';
import { Messages } from 'primereact/messages';
import { Checkbox } from 'primereact/checkbox';
import { InputTextarea } from 'primereact/inputtextarea';
import './style.css';
import SmallOrange from '../../../jncomponents/SmallOrange';
import { AutoComplete } from 'primereact/autocomplete';

const aviso = 'Por favor, ao enviar dados para qualquer site da internet (Esse aqui, por exemplo), certifique-se que NÃO enviou seu endereço residencial completo (No máximo o bairro e cidade já bastam para o recrutador!) e jamais forneça números de documentos pessoais como CPF, RG e outros. Seus dados (Assim como em qualquer plataforma da internet, Linkedin por exemplo) estarão visíveis a qualquer pessoa na internet e não nos responsabilizaremos por dados que VOCÊ fornece, portanto, pense a respeito e seja responsável! Veja um exemplo de golpe no vídeo abaixo:';
const msgObservacoes = 'Este é um campo de preenchimento opcional. Aqui você escreve suas exigências para trocar de emprego, caso esteja empregado e confortável onde trabalha (Exemplo de exigências: cidade e bairro onde trabalhar, horário, ambiente de trabalho que gostaria para trabalhar, e outras que você imaginar), alguma restrição para trabalhar ou qualquer outra coisa não mencionada neste texto. Cabe ressaltar que informações sobre pretensões salariais, disponibilidade para início, negociabilidade de pretensões salariais e homeoffice não precisam ser mecionadas aqui, pois já foram informadas em outros campos acima neste mesmo formulário!';
const msgBitcoin = 'O bitcoin (por copiar a escassez do ouro, porém sendo ainda mais escasso) não é suscetível à desvalorização mensal (as vezes, diária) que as moedas emitidas por casa de moeda são, essas emissões as vezes são feitas para financiar guerras, outras vezes, luxos, outras vezes gastos ineficientes. A inflação da moeda significa o seu empobrecimento, a sua perda de poder de compra, por isso é importante você ir para meios alternativos de reserva de valor, como o bitcoin ou o ouro são. Caso se interesse em se proteger financeiramente, instale o telegram no celular, clique no link do convite abaixo para entrar no grupo de comércio de criptomoedas e lá pergunte às centenas de pessoas já presentes neste grupo como começar com bitcoin:';
const msgInexperientes = 'Não adianta apenas estar cursando uma faculdade ou curso... você tem que ter em seu currículo as tecnologias ou ferramentas que os recrutadores mais estão buscando nesse momento, para isso, acompanhe as vagas que são postadas diariamente nos grupos de vagas ou linkedin para sua profissão e aprenda sobre elas no youtube ou em plataformas baratas de ensino da atualidade, tais como udacity, udemy, alura e tantas outras.'
const titleAlertInexperientes = 'Como conseguir a primeira oportunidade?';
import React, { Component } from 'react';
export default class FormCandidatos extends Component {
    state = {
        ddd: 11,
        pcd: false,
        tipoVaga: 1,
        observacao: '',
        experiencia: 0,
        mudanca: false,
        homeoffice: false,
        naoAvalioPj: true,
        disponibilidade: 0,
        naoAvalioCLT: true,
        novoCandidato: true,
        profissaoDesejada: '',
        empresasFiltradas: [],
        naoAvalioBitcoin: true,
        empresasParaNaoEnviar: [],
        pretensaoNegociavel: true,  
        naoPossuoExperiencia: true
    }
    alertBitcoin = null;
    alert = null;

    componentDidMount() {
        const email = this.props.location.query.email;

        const callbacks = {};
        callbacks['onUnexpectedHttpStatus'] = () => this.exibirMensagem('Erro ao carregar dados');
        callbacks[404] = (e) => { this.carregarNovoRegistro(e) };
        callbacks[200] = (e) => this.carregarDados(e);
        JnAjax.doAnAjaxRequest(`candidatos/${email}`, callbacks, 'GET');
    }

    carregarNovoRegistro = () => {
        const state = {
            ddd: 11,
            pcd: false,
            tipoVaga: 1,
            observacao: '',
            experiencia: 0,
            naoAvalioPj: true,
            homeoffice: false,
            naoAvalioCLT: true,
            novoCandidato: true,
            profissaoDesejada: '',
            naoAvalioBitcoin: true,
            empresasParaNaoEnviar: [],
            pretensaoNegociavel: true,  
            naoPossuoExperiencia: true,
            mudanca: false
            }
        this.setState(state);
    }

    subirCurriculo = (e) => {
        const curriculo = e.target.files[0];
        if (curriculo.size > (2 * 1024 * 1024)) {
            this.exibirMensagem('Não aceitamos currículos maiores que 2 megabytes de tamanho!', 'error');
            return;
        }
        this.messagesFiltros.clear();

        this.setState({ arquivo: curriculo.name, file: curriculo.name });
        const toBase64 = () => new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(curriculo);
            reader.onload = () => {
                resolve(reader.result);
            }
            reader.onerror = error => reject(error);
        });
        toBase64().then(curriculo => {
            this.setState({ curriculo, content: curriculo });
            this.jnDialog && this.jnDialog.show();
        });
    }
    getSaveOptions = () => {
        const buttons = [];
        const buttonSave = {
            label: 'Pode salvar, meu curículo não contem nenhum dado sigiloso',
            event: () => this.inserirDados()
        };
        buttons.push(buttonSave);

        const buttonClose = {
            label: 'Não, revisarei meu currículo e farei um novo upload a seguir',
            event: () => this.jnDialog && this.jnDialog.close()
        };

        buttons.push(buttonClose);

        return buttons;
    }

    inserirDados = () => {

        if (!this.state.status && !this.state.novoCandidato) {
            this.exibirMensagem('Para alterar dados você tem que reativar o perfil!');
            return;
        }

        this.jnDialog && this.jnDialog.close();

        const email = this.props.location.query.email;

        this.messagesFiltros.clear();

        if (!email) {
            this.exibirMensagem('Faltando e-mail na url');
            return;
        }

        if (!this.validateEmail(email)) {
            this.exibirMensagem('O e-mail na url está inválido!');
            return;
        }

        if (this.state.novoCandidato && (!this.state.arquivo || this.state.arquivo.trim().length < 5)) {
            this.exibirMensagem('Faça upload do seu currículo!');
            return;
        }

        if (!this.state.profissaoDesejada) {
            this.exibirMensagem('Preencha a profissão desejada!');
            return;
        }


        if (this.state.experiencia > 0) {
            if (this.state.experiencia > 65) {
                this.exibirMensagem('Não fazemos cadastros de tempos de experiências maiores que 65 anos!');
                return;
            }
            if (!this.state.ultimaProfissao) {
                this.exibirMensagem('Por favor, se você já tem experiência de trabalho, preencha o último cargo que você trabalhou ou trabalha atualmente.');
                return;
            }
        }

        if (this.state.experiencia < 0) {
            this.exibirMensagem('A experiencia não pode ter valor negativo!');
            return;
        }

        if (!this.state.pretensaoClt && !this.state.pretensaoPj && !this.state.bitcoin) {
            this.exibirMensagem('Preencha ao menos uma das pretensões (PJ, Bitcoin, CLT)');
            return;
        }

        if (!this.state.pretensaoClt < 0) {
            this.exibirMensagem('A pretensão CLT não pode ter valor negativo!');
            return;
        }

        if (!this.state.pretensaoPj < 0) {
            this.exibirMensagem('A pretensão PJ não pode ter valor negativo!');
            return;
        }

        if (!this.state.bitcoin < 0) {
            this.exibirMensagem('O valor em bitcoin não pode ser negativo!');
            return;
        }

        if ((this.state.pretensaoClt > 0 && (this.state.pretensaoClt > 30000 || this.state.pretensaoClt < 1000))
            || (this.state.pretensaoPj > 0 && (this.state.pretensaoPj > 60000 || this.state.pretensaoPj < 1000))
            || (this.state.bitcoin > 0 && (this.state.bitcoin > 60000 || this.state.bitcoin < 1000))) {
            this.exibirMensagem('Valores inválidos para as pretensões, cabe ressaltar que os valores são mensais e não por hora');
            return;
        }

        this.inserir(email);
    }

    inserir = (email) => {
        const url = `candidatos/${email}`;
        let {empresas, profissaoDesejada, mudanca, tipoVaga, content, file, pretensaoNegociavel, disponibilidade, pcd, homeoffice, bitcoin, observacao, empresasParaNaoEnviar, ultimaProfissao, ddd, curriculo, experiencia, pretensaoPj, pretensaoClt, arquivo } = this.state;
        pretensaoClt = !pretensaoClt ? undefined : pretensaoClt;
        pretensaoPj = !pretensaoPj ? undefined : pretensaoPj;
        experiencia = !experiencia ? undefined : experiencia;
        homeoffice = !homeoffice ? undefined : homeoffice;
        bitcoin = !bitcoin ? undefined : bitcoin;
        curriculo = curriculo || content;
        arquivo = arquivo || file;
        tipoVaga = tipoVaga || 1;
        
        const dados = {empresas, profissaoDesejada, mudanca, tipoVaga, pretensaoNegociavel, disponibilidade, pcd, bitcoin, homeoffice, observacao, empresasParaNaoEnviar, arquivo, ultimaProfissao, ddd, curriculo, experiencia, pretensaoPj, pretensaoClt};

        const callbacks = {};
        callbacks[200] = () => { this.exibirMensagem('Dados gravados com sucesso', 'info') };

        callbacks['onUnexpectedHttpStatus'] = () => this.exibirMensagem('Erro ao inserir dados');
        JnAjax.doAnAjaxRequest(url, callbacks, 'PATCH', dados);
    }


    carregarDados = (dados) => {
        try {

            let { empresas, tipoVaga, profissaoDesejada, mudanca, pcd, bitcoin, homeoffice, observacao, status, empresasParaNaoEnviar, ultimaProfissao, ddd, experiencia, pretensaoPj, pretensaoClt, disponibilidade, pretensaoNegociavel } = dados;
            const content = dados.curriculo.conteudo;
            const file = dados.curriculo.arquivo;
            
            const naoPossuoExperiencia = !experiencia || experiencia < 0;

            const naoAvalioCLT = !pretensaoClt;
            
            const naoAvalioPj = !pretensaoPj;
            const novoCandidato = false;
            const naoAvalioBitcoin = !bitcoin;
            this.setState({empresas, tipoVaga, profissaoDesejada, mudanca, file, content, disponibilidade, pretensaoNegociavel, observacao, naoAvalioBitcoin, bitcoin, homeoffice, pcd, status, empresasParaNaoEnviar, naoPossuoExperiencia, naoAvalioCLT, naoAvalioPj, ultimaProfissao, ddd, novoCandidato, experiencia, pretensaoPj, pretensaoClt });
        } catch (error) {
            console.error(error);
            this.exibirMensagem('Erro ao carregar dados', 'error');
        }
    }

    exibirMensagem = (detail) => {
            exibirMensagem(detail, 8000);
       }

    validateEmail = (email) => {
        var re = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
        return re.test(String(email).toLowerCase());
    }

    formatos = {
        pdf: 'application/pdf',
        doc: 'application/msword',
        docx: 'application/msword'

    };

    

    executarDownloadDoCurriculo = (e) => {

        e && e.preventDefault && e.preventDefault();

        const link = document.createElement("a");

        link.download = this.state.file;

        const lastIndexOf = this.state.file.lastIndexOf('.') + 1;

        const extension = this.state.file.substring(lastIndexOf);

        const type = this.formatos[extension];

        let href = `data:${type};base64,${this.state.content}`;

        if (this.state.content.startsWith('data:')) {
            href = this.state.content;
        }

        link.href = href;

        link.click();
    }

    changeCandidateStatus = () => {
        this.messagesFiltros.clear();
        const message = this.state.status == 0 ? 'Perfil reativado com sucesso, a partir de agora os recrutadores poderão ver seu currículo novamente' : 'A partir de agora, os recrutadores não poderão mais encontrar o seu currículo, a não ser que você reabilite esta opção';
        const status = this.state.status == 0 ? 1 : 0;
        this.setState({ status });
        const sessao = JnAjax.getToken();
        const url = `candidatos/${sessao.email}/${status}`;
        const httpResponses = {};
        httpResponses[200] = () => this.exibirMensagem(message, 'info');
        JnAjax.doAnAjaxRequest(url, httpResponses, 'DELETE');

    }

    getLinkBitcoin = () => {
        return (

            <a href="#" onClick={e => { e && e.preventDefault(); this.alertBitcoin.exibir('Bitcoin?', msgBitcoin) }}>Bitcoin?</a>

        );
    }

    filtrarEmpresas = (event) => {
        const httpResponses = {};
        httpResponses[200] = (empresasFiltradas) => this.setState({empresasFiltradas});
        JnAjax.doAnAjaxRequest('autocomplete/consultorias/' + event.query, httpResponses, 'GET');
    }

    render() {
        const colunasBotao = this.state.file ? "col col-md-3" : "col col-md-4";
        const statusWord = this.state.status == 0 ? 'Reativar' : 'Inativar';
        const disponibilidadeImediata = !this.state.disponibilidade || this.state.disponibilidade <= 0;
        return (
            <form style={{ paddingTop: "20px", width: "90%" }} >
                <div className="row" style={{ paddingLeft: "20px" }} >
                    <div className="col col-md-4">
                        <ComboRegiaoMetropolitana
                            labelSelect="Indiferente..."
                            required={true}
                            label="Região metropolitana onde reside"
                            onChange={ddd => this.setState({ ddd })}
                            value={this.state.ddd}
                        />
                    </div>
                    <div className="col col-md-4">
                        <br />
                        <label htmlFor="mudanca" className="labelCkx">

                            <Checkbox id="mudanca" checked={this.state.mudanca} onChange={e => this.setState({ mudanca: e.checked })} />
                            <label htmlFor="mudanca" className="p-checkbox-label">Avalio propostas em outras regiões</label>
                        </label>

                    </div>
                    <div className="col col-md-4">
                    <br />
                    <label htmlFor="homeoffice" className="labelCkx">

                        <Checkbox id="homeoffice" checked={this.state.homeoffice} onChange={e => this.setState({ homeoffice: e.checked })} />
                        <label htmlFor="homeoffice" className="p-checkbox-label"><b>SOMENTE</b> 100% homeoffice</label>
                    </label>
                </div>
            </div>
            <div className="row" style={{ paddingLeft: "20px" }} >
            <div className="col col-md-3">
                    <div className="form-group" >
                        <label className="control-label">Cargo mais recente ou atual</label>
                        <input type="text" className="form-control"
                            value={this.state.ultimaProfissao}
                            onChange={e => {
                                const ultimaProfissao = e.target.value;
                                this.setState({ ultimaProfissao });
                            }} />
                    </div>
                </div>
                <div className="col col-md-3">
                    <div className="form-group" >
                        <label className="control-label">Profissão ou função desejada</label>
                        <input type="text" className="form-control"
                            value={this.state.profissaoDesejada}
                            onChange={e => {
                                const profissaoDesejada = e.target.value;
                                this.setState({ profissaoDesejada });
                            }} />
                    </div>
                </div>
                <div className="col col-md-2">
                    <JnContainerCheckBox
                        label="Quero oportunidade para estágio, trainee ou jr"
                        onLinkClick={() => this.alert.exibir(titleAlertInexperientes, msgInexperientes)}
                        labelLink='[Como conseguir a primeira oportunidade? Clique aqui!]'
                        checked={this.state.naoPossuoExperiencia}
                        onChange={() => this.setState({ experiencia: 1, naoPossuoExperiencia: false })}>
                        <div className="form-group">
                            <label className="control-label">Anos de experiência</label>
                            <input type="text" className="form-control" style={{ width: "250px" }}
                                value={this.state.experiencia}
                                onChange={e => {
                                    const experiencia = e.target.value.replace(/\D/, '');
                                    this.setState({ experiencia });
                                }} />
                        </div>

                    </JnContainerCheckBox>
                </div>
                <div className="col col-md-1">&nbsp;</div>
                <div className="col col-md-3" >
                    <div className={"form-group"}>

                      <label className="control-label">Tipo de Vaga</label>
                    <select className="form-control" value= {this.state.tipoVaga} onChange = {e => this.setState({tipoVaga: e.target.value}) }>
                    <option value = {1}>T.I (Tecnologia em Informática)</option>
                    <option value = {2}>RH, DP, R&S e afins</option>
                    <option value = {3}>Vagas que requerem ensino superior, curso técnico ou certificação</option>
                    <option value = {4}>Vagas que não requerem comprovação de escolaridade ou qualquer escolaridade</option>
                    </select>
                    </div>
                    </div>

                </div>

            <div className="row" style={{ paddingLeft: "20px" }} >
                <div className="col col-md-3">
                    <div className="form-group">
                        <label className="control-label">Pretensão Mensal Pessoa Jurídica (PJ)</label>
                        <input type="text" className="form-control"
                            value={this.state.pretensaoPj}
                            onChange={e => {
                                const pretensaoPj = e.target.value.replace(/\D/, '');
                                this.setState({ pretensaoPj }); 
                            }} />
                        <SmallOrange message="*Apenas valores baseados em 168 horas" />
                    </div>
                </div>

                <div className="col col-md-3">
                    <div className="form-group">
                        <label className="control-label">Pretensão Mensal Carteira Assinada (CLT) </label>
                        <input type="text" className="form-control"
                            value={this.state.pretensaoClt}
                            onChange={e => {
                                const pretensaoClt = e.target.value.replace(/\D/, '');
                                this.setState({ pretensaoClt });
                            }} />
                        <SmallOrange message="*Apenas valores baseados em 168 horas" />
                    </div>
                </div>
                <div className="col col-md-3">
                    <JnContainerCheckBox
                        label="Não avalio remuneração em Bitcoin"
                        onLinkClick={() => this.alertBitcoin.exibir('Bitcoin?', msgBitcoin)}
                        labelLink='[Porque em Bitcoin? Clique aqui para saber]'
                        checked={this.state.naoAvalioBitcoin}
                        onChange={() => this.setState({ bitcoin: 0, naoAvalioBitcoin: false })}>
                        <div className="form-group">
                            <label className="control-label">Valor em  Bitcoin</label>
                            &nbsp;&nbsp;&nbsp;
                    {
                                this.getLinkBitcoin()
                            }
                            <input type="text" className="form-control"
                                value={this.state.bitcoin}
                                onChange={e => {
                                    const bitcoin = e.target.value.replace(/\D/, '');
                                    this.setState({ bitcoin });
                                }} />
                            <SmallOrange message="*Informe valor equivalente em reais" />
                        </div>

                    </JnContainerCheckBox>
                </div>
                <div className="col col-md-3">
                    <br />
                    <label htmlFor="pretensaoNegociavel" className="labelCkx">
                        <Checkbox id="pretensaoNegociavel" checked={this.state.pretensaoNegociavel} onChange={e => this.setState({ pretensaoNegociavel: e.checked })} />
                        <label htmlFor="pretensaoNegociavel" className="p-checkbox-label">Pretensões negociáveis</label>
                    </label>
                </div>
            </div>
            <div className="row" style={{ paddingLeft: "20px" }}>
                <div className="col col-md-6">
                    <br />
                    <label htmlFor="pcd" className="labelCkx">

                        <Checkbox id="pcd" checked={this.state.pcd} onChange={e => this.setState({ pcd: e.checked })} />
                        <label htmlFor="pcd" className="p-checkbox-label">
                            Tenho necessidades especiais (PCD)</label>
                    </label>

                </div>

                <div className="col col-md-3">
                    <JnContainerCheckBox
                        label="Disponibilidade Imediata"
                        checked={disponibilidadeImediata}
                        onChange={() => this.setState({ disponibilidade: 0, disponibilidadeImediata: true })}>
                        <div className="form-group">
                            <label className="control-label">Dias para disponibilidade</label>
                            <input type="text" className="form-control"
                                value={this.state.disponibilidade}
                                onChange={e => {
                                    const disponibilidade = e.target.value.replace(/\D/, '');
                                    this.setState({ disponibilidade });
                                }} />
                        </div>

                    </JnContainerCheckBox>
                </div>
                </div>
                <div className="row" style={{ paddingLeft: "20px" }}>
                <div className="col col-md-12">
                    <div className="form-group" >
                        <br/>
                        <label className="control-label">Empresas que não poderão ver o seu currículo:</label><br/>
                        <AutoComplete value={this.state.empresas} suggestions={this.state.empresasFiltradas} completeMethod={this.filtrarEmpresas}
                        minLength={2} multiple={true} onChange={(e) => this.setState({ empresas: e.value })} />
                    <strong style={{ color: "black", fontSize: "12px" }}>[Ao terminar digitação do nome da empresa, pressione a tecla enter. Você pode digitar mais de uma empresa]</strong>
                   </div>
                </div>
                </div>

                <div className="row" style={{ paddingLeft: "20px" }} >
                    <div className="col col-md-12">
                        <div className="form-group" >
                            <label className="control-label">Observações</label>
                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a href="#" onClick={e => { e && e.preventDefault(); this.alert.exibir('O que colocar neste campo?', msgObservacoes) }}>[O que colocar neste campo?]</a>

                            <InputTextarea value={this.state.observacao} onChange={(e) => this.setState({ observacao: e.target.value })} rows={2} cols={300} />
                        </div>
                    </div>

                </div>
                <div className="row" style={{ paddingLeft: "20px" }} >
                    <div className="col col-md-12">
                        <Messages ref={(el) => this.messagesFiltros = el} closable={false} />
                    </div>
                </div>
                <div className={colunasBotao} >
                    <div className="upload-btn-wrapper form-group" style={{ width: "100%" }}>
                        <button className="btn" style={{ width: "100%" }}>
                            {
                                !this.state.file ?
                                    "Anexar um currículo"
                                    :
                                    "Alterar currículo"
                            }
                        </button>
                        <input type="file" onChange={this.subirCurriculo} style={{ width: "100%" }} />
                    </div>
                 </div>
                    {
                        this.state.file &&
                        (<div className={colunasBotao} >

                            <div>
                                <button className="btn" onClick={this.executarDownloadDoCurriculo}>
                                    Visualizar currículo
    
                        </button>
                            </div>

                        </div>)
                    }

                <div className="row" style={{ paddingLeft: "20px" }} >
                    {
                        !this.state.novoCandidato &&
                        (
                            <div className={colunasBotao}>
                                <button className="btn btn-default"
                                    onClick={this.changeCandidateStatus}
                                    type="button" style={{ width: "100%" }}>
                                    <i className="fa fa-check" />&nbsp;
                        {`${statusWord} Perfil`}</button>
                            </div>
                        )
                    }

                    <div className={colunasBotao}>
                        <button className="btn btn-primary"
                            onClick={() => this.jnDialog && this.jnDialog.show()}
                            type="button" style={{ width: "100%" }}>
                            <i className="fa fa-save" />&nbsp;
                    Salvar Dados</button>
                    </div>
                </div>
                <JnDialog title="Atenção!!!" ref={(el) => this.jnDialog = el} buttons={this.getSaveOptions()} >
                    <h1>{`${aviso}`}</h1>
                    <iframe width="1000" height="315" src="https://www.youtube.com/embed/SXJ8gpjoD8A" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
                    <h1>Salvar estes dados?</h1>
                </JnDialog>

                <Alert ref={(el) => this.alert = el} />
                
                        <AlertBitcoin ref={(el) => this.alertBitcoin = el}>
                            <div>
                                <br />
                                <a href="https://t.me/+RNs2reLDgLKlWc8R" target="_blank">
                                    Grupo de comércio de bitcoin e outras criptomoedas
                        </a>
                            </div>
                        </AlertBitcoin>
           </form>
        );
    }
}