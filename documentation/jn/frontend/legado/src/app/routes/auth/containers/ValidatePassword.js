import React from 'react';
import UiValidate from '../../../components/forms/validation/UiValidate';
import { Alert } from 'react-bootstrap';
import JnAjax from '../../../services/JnAjax';
import { withRouter } from 'react-router';
import ModalFirstAccess from './ModalFirstAccess';
import Password from '../../../jncomponents/jnPassword';

class ValidatePassword extends React.Component {


   state = {
      password: '',
      message: ''
   };

   irParaCadastro = {};

   modalFirstAccess = null;

   constructor(){
      super();

      this.irParaCadastro[298] = () => this.entrarNoSistema(`candidato/cadastro?email=${this.props.location.query.email}`, this.state.token, 298);
      this.irParaCadastro[299] = () => this.entrarNoSistema('recrutadores/buscaDeCandidatos', this.state.token, 299);      

   }

   validatePassword = e => {
      e && e.preventDefault && e.preventDefault();
      const callBacks = {};
      callBacks[400]= () => this.exibirMensagem(`O e-mail ${this.props.location.query.email} está inválido!`, 'danger');
      callBacks[401]= () => this.exibirMensagem(`Senha inválida!`, 'danger');
      callBacks[201]= loginHash => this.abrirModalFirstAccess(loginHash);
      callBacks[298]= loginHash => this.entrarNoSistema(`candidato/cadastro?email=${this.props.location.query.email}`, loginHash, 298);
      callBacks[299]= loginHash => this.entrarNoSistema('recrutadores/buscaDeCandidatos', loginHash, 299);
      callBacks[404]= () => this.props.router.push(`/login?email=${this.props.location.query.email}&mensagem=usuarioNaoCadastrado`);
      if(!this.state.password){
         this.exibirMensagem(`A senha está sem caracteres!`, 'danger');
         return;
      }
      const password = this.state.password;
      const requestBody = {password};
      JnAjax.doAnAjaxRequest(`login/${this.props.location.query.email.trim()}`, callBacks, 'POST', requestBody);

   }
   abrirModalFirstAccess = (token) =>{
      const sessao = { email: this.props.location.query.email, token};
      sessionStorage.setItem('sessao', JSON.stringify(sessao));
      this.setState({token});
      this.modalFirstAccess && this.modalFirstAccess.show();
   }

   entrarNoSistema = (url, loginHash, acesso) => {
      this.setState({loginHash});
      
      if(this.props.location.query.nextUrl && this.props.location.query.nextUrl != 'undefined'){
         url = this.props.location.query.nextUrl.replace('undefined', this.props.location.query.email);
      }
     
      const uri = url;
      const sessao = { email: this.props.location.query.email, token: loginHash, uri, acesso};
      sessionStorage.setItem('sessao', JSON.stringify(sessao));
      this.props.router.push(url);
   }

   exibirMensagem = (message, messageType) => {
      this.setState({ message, messageType });
   }

   render() {

      return (
         <div id="extr-page">
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
                                       <label className="label">Digite a sua senha </label>

                                       <label className="input"> <i className="icon-append fa fa-user" />
                                          <Password onChange={e => this.setState({ password: e.target.value })} value={this.state.password} />
                                       </label>
                                    </section>
                                 </fieldset>
                                 <footer>
                                    <button type="submit" className="btn btn-primary"
                                       onClick={(e) => this.validatePassword(e)}>
                                       Entrar no sistema
                                    </button>
                                    <button type="button" className="btn"
                                       onClick={(e) => this.props.router.push(`/login?email=${this.props.location.query.email}`)}>
                                       Voltar
                                    </button>
                                    <br />
                                    <a href="#" onClick = { (e) => {e && e.preventDefault && e.preventDefault(); this.props.router.push(`/tokenToSetPassword?email=${this.props.location.query.email}&msgType=info&msgValue=forgotPassword`);}}>
                                       Esqueci ou quero trocar minha senha
                                    </a>
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
         </div>
      )
   }
}
export default withRouter(ValidatePassword)

