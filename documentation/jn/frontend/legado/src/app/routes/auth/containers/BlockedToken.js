import UiValidate from '../../../components/forms/validation/UiValidate';
import { Alert } from 'react-bootstrap';
import JnAlert from '../../../jncomponents/JnAlert';
import { withRouter } from 'react-router';
import React from 'react';
import JnAjax from '../../../services/JnAjax';


class BlockedToken extends React.Component {

   state = {
      message: 'Você está nesta tela pelo fato do seu token estar bloqueado devido às várias tentativas incorretas de informá-lo. Clique abaixo para solicitar desbloqueio.'
   };

   alert = null;

   messages = {};

   exibirMensagem = (message, messageType) => {
      const msg = this.translateMessage(message);
      message = message || msg;
      messageType = messageType || this.props.location.query.msgType;
      this.setState({ message, messageType });
   }

   solicitarDesbloqueioDeToken = (e) => {

      e && e.preventDefault && e.preventDefault();
      this.alert.exibir('Suporte JobsNow', 'Por favor, entre em contato com o Onias no linkedin https://www.linkedin.com/in/onias85/ ou envie e-mail fazendo solicitação de desbloqueio pelo e-mail onias@ccpjobsnow.com');
   
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
                                 <div>
                                    {this.state.message ? <Alert bsStyle={this.state.messageType}><p>{this.state.message}</p></Alert> : null}
                                 </div>
                                 <footer>
                                    <a href="#" onClick={(e) => this.solicitarDesbloqueioDeToken(e)}>Solicitar desbloqueio de token</a>
                                 </footer>
                              </form>
                           </UiValidate>
                        </div>
                     </div>
                  </div>
                  <h1>Sejam muito bem vindos Recrutadores e Candidatos!</h1>
               </div>
            </div>

            <JnAlert ref={(el) => this.alert = el} onClick = {() =>this.props.router.push(`/login?email=${this.props.location.query.email}`)   } />

         </div>
      )
   }
}
export default withRouter(BlockedToken)

