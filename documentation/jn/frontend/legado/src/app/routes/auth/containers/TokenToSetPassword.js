import React from 'react';
import UiValidate from '../../../components/forms/validation/UiValidate';
import { Alert } from 'react-bootstrap';
import JnAjax from '../../../services/JnAjax';
import { withRouter } from 'react-router';
import ModalTempoDeEnvio from '../../../jncomponents/JnModal';
import ModalPerguntaSpam from '../../../jncomponents/JnModal';
import ModalFirstAccess from './ModalFirstAccess';
import JnAlert from '../../../jncomponents/JnAlert';
import Password from '../../../jncomponents/jnPassword';


class TokenToSetPassword extends React.Component {

   state = {
      token: '',
      message: ''
   };

   modalTempoDeEnvio = null;
   modalPerguntaSpam = null;
   alert = null;
 
   messages = {};
  
   exibirMensagem = (message, messageType) => {

      if(!message){
         return;
      }
      const msg = this.translateMessage(message);
      message = message || msg;
      messageType = messageType || this.props.location.query.msgType;
      this.setState({ message, messageType});
   }

   translateMessage = () => {
      const message = this.props.location.query.msgValue;
      
      if(!message){
         return;
      }

      if ('blockedByInvalidToken' == this.props.location.query.msgValue) {
         return `Você errou seu token, se errar três vezes terá o token bloqueado!`;
      }

      if ('blockedByIncorrectPassword' == message) {
         return `O usuário pertencente ao e-mail ${this.props.location.query.email} está bloqueado por errar varias vezes a senha, informe um token e crie nova senha para que possamos efetuar o desbloqueio`;
      }

      if ('newUser' == message) {
         return `Informe o token recebido no e-mail ${this.props.location.query.email} e uma senha para que possa prosseguir para o seu primeiro acesso.`;
      }
      if ('changePassword' == message) {
         return `Informe o token recebido no e-mail ${this.props.location.query.email} para que possamos e uma nova senha.`;
      }

      if ('passwordResend' == message) {
         return `Este token já foi enviado ao e-mail ${this.props.location.query.email} nessas últimas 24 horas`;
      }

      if ('forgotPassword' == message) {
         return `Digite o token enviado ao e-mail ${this.props.location.query.email} para que voce possa resetar sua senha. Este token lhe foi enviado na primeira vez que você acessou ao sistema.`;
      }

      if ('lostTokenMoreThanOnce' == message) {
         return `Você já fez esse tipo de requisição. Por favor contacte o suporte (11) 96605-8642 via whatsapp ou telegram.`;
      }

   }


   componentDidMount() {
      const callbacks = {};
      callbacks[202] = () => this.setState({message: 'Neste momento ou nas ultimas 24 horas, enviamos seu token ao e-mail informado, favor verificar caixas de entrada ou spam (lixo eletrônico)'}) 
      JnAjax.doAnAjaxRequest(`login/${this.props.location.query.email.trim()}/token`, callbacks, 'POST');
      this.exibirMensagem();
      const token = this.props.location.query.token;
      this.setState({token});
   }

   abrirModal = (e) =>{
      e && e.preventDefault && e.preventDefault();

      this.modalTempoDeEnvio.show();

   }
   irParaCadastro = {};

   modalFirstAccess = null;
 
   messages = {};

   constructor(props){
      super(props);
      this.irParaCadastro[298] = () => this.entrarNoSistema(`candidato/cadastro?email=${this.props.location.query.email}`, this.state.token);
      this.irParaCadastro[299] = () => this.entrarNoSistema('recrutadores/buscaDeCandidatos', this.state.token);      
      this.exibirMensagem();
   }

   savePassword = (e) =>{
      e && e.preventDefault && e.preventDefault();
      const callBacks = {};
      callBacks[400]= () => this.exibirMensagem(`O e-mail ${this.props.location.query.email} está inválido!`, 'danger');
      callBacks[403]= (errorMessage) => this.exibirMensagem(this.translateMessage(errorMessage),'danger');     
      callBacks[422]= (errorMessage) => this.exibirMensagem(errorMessage,'danger');
      callBacks[201]= (loginHash) => this.abrirModalFirstAccess(loginHash);
      callBacks[298]= (loginHash) => this.entrarNoSistema(`candidato/cadastro?email=${this.props.location.query.email}`, loginHash, 298);
      callBacks[299]= (loginHash) => this.entrarNoSistema('recrutadores/buscaDeCandidatos', loginHash, 299);
      callBacks[404]= () => this.props.router.push(`/login?email=${this.props.location.query.email}&mensagem=usuarioNaoCadastrado`);
      
      const {password, password2} = this.state;
  
      if(!password){
         this.exibirMensagem(`A senha está sem caracteres!`, 'danger');
         return;
      }
      if(!password2){
         this.exibirMensagem(`A confirmação de senha está sem caracteres!`, 'danger');
         return;
      }
  
      const token = this.state.token.trim();
      const requestBody = {password, password2, token};
      JnAjax.doAnAjaxRequest(`senha/${this.props.location.query.email.trim()}`, callBacks, 'POST', requestBody);

   }
   
   abrirModalFirstAccess = (token) =>{
      this.setState({token});
      const sessao = { email: this.props.location.query.email, token};
      sessionStorage.setItem('sessao', JSON.stringify(sessao));
      this.modalFirstAccess && this.modalFirstAccess.show();
   }

   entrarNoSistema = (url, token, acesso) => {

      this.setState({token});

      if(this.props.location.query.nextUrl && this.props.location.query.nextUrl != 'undefined'){
         url = this.props.location.query.nextUrl.replace('undefined', this.props.location.query.email);
      }
      const uri = url;
      const sessao = { email: this.props.location.query.email, token, uri, acesso};
      sessionStorage.setItem('sessao', JSON.stringify(sessao));
      
      this.props.router.push(url);
   }

   requisitarToken = (e) => {

      e && e.preventDefault && e.preventDefault();
      this.alert.exibir('Suporte JobsNow', 'Por favor, entre em contato com o Onias no linkedin https://www.linkedin.com/in/onias85/ ou envie e-mail fazendo solicitação de token pelo e-mail onias@ccpjobsnow.com');
   }


   render() {
     
      return (
         <div id="extr-page">
            <ModalTempoDeEnvio buttonConfirmLabel = 'Sim' buttonCloseLabel='Não'  buttonClose={true} buttonConfirm={true} ref={e => this.modalTempoDeEnvio = e}
               onConfirm={() => {this.modalPerguntaSpam && this.modalPerguntaSpam.show(); this.modalTempoDeEnvio.close();}}
            >
               <h1>Faz mais de dois minutos que você solicitou este token?</h1>
            </ModalTempoDeEnvio>
            <ModalPerguntaSpam buttonConfirmLabel = 'Sim' buttonCloseLabel='Não' buttonClose={true} buttonConfirm={true} ref={e => this.modalPerguntaSpam = e}
               onConfirm={this.requisitarToken}
            >
               <h1>Já verificou sua pasta de spam?</h1>
            </ModalPerguntaSpam>
            <div id="main" role="main" className="animated fadeInDown">
               <div id="content" className="container">
                  <div className="row" style={{ marginRight: "0px" }}>
                     <div className="col-xs-12 col-sm-12 col-md-6 col-lg-7 hidden-xs hidden-sm">
                        <span>
                           <img src="assets/img/logo-jobs.png" // place your logo here
                              alt="NoxxonSat" style={{ width: "300px" }} />
                        </span>
                     </div>
                     <div className="col-xs-12 col-sm-12 col-md-6 col-lg-5">
                        <div className="well no-padding">
                           <UiValidate>
                              <form id="login-form" className="smart-form client-form">
                                 <fieldset>
                                    <div>
                                       {this.state.message ? <Alert bsStyle={this.state.messageType}><p>{this.state.message}</p></Alert> : null}
                                    </div>
                                    <section>
                                       <label className="label">Informe o token que te enviamos no e-mail {this.props.location.query.email} na primeira vez que você acessou<small style={{ color: "chocolate", fontSize: "9px" }}></small></label>

                                       <label className="input"> <i className="icon-append fa fa-user" />
                                          <Password onChange={e => this.setState({ token: e.target.value })} value={this.state.token} />
                                       </label>
                                    </section>
                                    <section>
                                       <label className="label">Escolha uma nova senha</label>

                                       <label className="input"> <i className="icon-append fa fa-user" />
                                          <Password onChange={e => this.setState({ password: e.target.value })} value={this.state.password} />
                                       </label>
                                    </section>
                                    <section>
                                       <label className="label">Confirme sua nova senha</label>

                                       <label className="input"> <i className="icon-append fa fa-user" />
                                          <Password onChange={e => this.setState({ password2: e.target.value })} value={this.state.password2} />
                                       </label>
                                    </section>
                                 </fieldset>
                                 <footer>
                                    <button type="submit" className="btn btn-primary"
                                       onClick={(e) => this.savePassword(e)}>
                                       Validar
                                    </button>
                                    <button type="button" className="btn"
                                       onClick={() => this.props.router.push(`/login?email=${this.props.location.query.email}`)}>
                                       Voltar
                                    </button>
                                    <br />
                                    <a href="#" onClick = { (e) => this.abrirModal(e)}>Não recebeu ou perdeu o token?</a>
                                 </footer>
                              </form>
                           </UiValidate>
                        </div>
                     </div>
                  </div>
                  <h1>Sejam muito bem vindos Recrutadores e Candidatos!</h1>
               </div>
            </div>
            <ModalFirstAccess token={this.state.loginHash} email={this.props.location.query.email} ref={e => this.modalFirstAccess = e} callBacks={this.irParaCadastro} />
            <JnAlert ref={(el) => this.alert = el} />

       </div>
      )
   }
}
export default withRouter(TokenToSetPassword)

