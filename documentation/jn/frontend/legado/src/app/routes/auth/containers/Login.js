import React from 'react';
import { withRouter } from 'react-router';
import UiValidate from '../../../components/forms/validation/UiValidate';
import { Alert } from 'react-bootstrap';
import JnAjax from '../../../services/JnAjax';

class Login extends React.Component {


   state = {
      email:'',
      message:'',
      textoBotao: "Login"
   };
   mensagens = {
      rhRequerido: 'Esta ação requer que você seja recrutador. Envie e-mail para onias@ccpjobsnow.com solicitando seu cadastro para poder ter acesso a essa funcionalidade!',
      msgBaixarCurriculos: 'Assim que você fizer o seu login como recrutador, o currículo que você requisitou poderá ser visto por você',
      erroGenerico:'O sistema acaba de apresentar comportamento inadequado, nosso suporte já foi notificado sobre o ocorrido',
      usuarioNaoCadastrado: 'Este usuário não está cadastrado, informe e-mail logo abaixo, para poder cadastrar',
      sessaoExpirada: 'Esta sessão está expirada, favor reconectar',
      requerLogin: 'Faça login para continuar!',
      sairDoSistema:'Você saiu do sistema',
      acessoNegado: 'Acesso negado!',
      emailNaoConfere: 'A confirmação de e-mail não confere, por favor, preencha corretamente ou corrija!',
      confirmarEmail: 'Como é o seu primeiro acesso, por favor, repita novamente o e-mail que você acabou de digitar',
      foraDoHorario: 'Este acesso é permitido somente após as 8 da manhã'
   }


   componentDidMount(){
      this.setState({email: this.props.location.query.email})
      
      const mensagem = this.mensagens[this.props.location.query.mensagem];
    
      if (!mensagem) {
         this.exibirMensagem('', '');
         return;
      }
      
      this.exibirMensagem(mensagem, 'danger');
      sessionStorage.removeItem('sessao');
      
   }

   corrigirEmail = e => {
      e && e.preventDefault && e.preventDefault();
      window.location.href = window.location.href.split('#')[0]+ '#/login';
      window.location.reload();
   }

   validateLogin = e => {
      e && e.preventDefault && e.preventDefault();

      const callBacks = {};
  
      callBacks['onUnexpectedHttpStatus'] = () => this.props.router.goForward(`/login?email=${this.state.email}&mensagem=erroGenerico&nextUrl=${encodeURIComponent(this.props.location.query.nextUrl)}`);
      callBacks[201] = () => this.props.router.push(`/tokenToSetPassword?email=${this.state.email}&msgType=info&msgValue=newUser&nextUrl=${encodeURIComponent(this.props.location.query.nextUrl)}`);
      callBacks[200] = () => this.props.router.push(`/validatePassword?email=${this.state.email}&nextUrl=${encodeURIComponent(this.props.location.query.nextUrl)}`);
      callBacks[400] = () => this.exibirMensagem(`O e-mail ${this.state.email} está inválido!`, 'danger');

      const acao = this.state.acao;
      if (acao != 'confirmarEmail') {
         callBacks[404] = () => this.reconfigurarTela( { message: "confirmarEmail", messageType: "info", acao: "confirmarEmail", email: "", textoBotao: "Confirmar E-mail", confirmacaoDeEmail: this.state.email});
      
         JnAjax.doAnAjaxRequest(`login/${this.state.email.trim()}/status`, callBacks);
         return;
      }

      if (this.state.confirmacaoDeEmail.trim() != this.state.email.trim()) {
         this.reconfigurarTela({message: "emailNaoConfere", messageType: "danger", acao: "confirmarEmail", email: "", textoBotao: "Confirmar E-mail"});
         return;
      }

      this.reconfigurarTela({message: "", messageType: "", acao: "", textoBotao: "Login"});
      JnAjax.doAnAjaxRequest(`login/${this.state.email.trim()}/requisicao`, callBacks, 'POST');
   }
  
   reconfigurarTela = state => {
      state.message = this.mensagens[ state.message];
      this.setState(state);
   }

   exibirMensagem = (message = this.mensagens[this.state.message], messageType) => {

      this.setState({ message, messageType });
   }


   render() {
      //verifica se já existe um token no sessionStorage e direciona para a tela home
      const sessao = JnAjax.getToken();
      if (sessao && sessao.uri) {
         this.props.router.push('/' + sessao.uri);
         return null;
      }

  
      return (
         <div id="extr-page">
            <div id="main" role="main" className="animated fadeInDown">
               <div id="content" className="container">
                  <div className="row">
                     <div className="col-xs-12 col-sm-12 col-md-7 col-lg-8 hidden-xs hidden-sm">
                        <span>
                           <img src="assets/img/logo-jobs.png" // place your logo here
                              alt="NoxxonSat" style={{ width: "300px" }} />
                        </span>
                     </div>
                     <div className="col-xs-12 col-sm-12 col-md-5 col-lg-4">
                        <div className="well no-padding">
                           <UiValidate>
                              <form id="login-form" className="smart-form client-form" onSubmit={(e) => this.state.enviarToken ? this.enviarToken(e) : this.validarToken(e)}>
                                 <fieldset>
                                    <div>
                                       {this.state.message ? <Alert bsStyle={this.state.messageType} onDismiss={() => { this.exibirMensagem() }}><p>{this.state.message}</p></Alert> : null}
                                    </div>
                                    <section>
                                       <label className="label">Informe o seu e-mail<small style={{color:"chocolate", fontSize:"9px"}}>&nbsp;&nbsp;&nbsp;[Mesmo que seja seu primeiro acesso]</small></label>
                                       
                                       <label className="input"> <i className="icon-append fa fa-user" />
                                          <input type="text" onChange={e => this.setState({email : e.target.value})} value={this.state.email} />
                                       </label>
                                    </section>
                                 </fieldset>
                                 <footer>
                                    <button type="submit" className="btn btn-primary"
                                       onClick={(e) => this.validateLogin(e)}>
                                       {this.state.textoBotao}
                                    </button>
                                    {
                                       this.state.acao == "confirmarEmail" &&
                                       <button type="button" className="btn" onClick={e => this.corrigirEmail(e)}>
                                          Corrigir e-mail
                                       </button>
                                    }
                                 </footer>
                              </form>
                           </UiValidate>
                        </div>
                     </div>
                  </div>
                  <h1>Sejam muito bem vindos Recrutadores e Candidatos!</h1>
               </div>
            </div>
         </div>
      )
   }
}
export default withRouter(Login)