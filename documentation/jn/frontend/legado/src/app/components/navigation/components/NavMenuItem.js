import React from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router'
import Msg from '../../i18n/Msg'
import JnAjax from '../../../services/JnAjax';
import ModalLogout from '../../../jncomponents/JnModal';
import ModalChangePassword from '../../../jncomponents/JnModal';
import JnDialog from '../../../jncomponents/JnMultiButtonDialog';
import { withRouter } from 'react-router';
import { smallBox } from '../../../components/utils/actions/MessageActions'


import SmartMenuList from './NavMenuList'

class SmartMenuItem extends React.Component {

   modalDeleteCandidate = null;
   modalChangePassword = null;
   modalLogout = null;
   modalBug = null;

   static contextTypes = {
      router: PropTypes.object.isRequired
   };
   state = {
      permissoes:[]
   }

   componentDidMount(){
      if(this.getPermissoes().length > 0){
         this.setState({permissoes: this.getPermissoes()});
         return;
      }
  }
   
   getPermissoes = () => {
      try {
         const str = sessionStorage.getItem('permissoes');
         if(!str){
            return [];
         }
         const permissoes = JSON.parse(str);
         return permissoes;
      } catch (error) {
         console.error(error);
         return [];
      }
   }

   render() {
      const item = this.props.item;
      const sessao = JnAjax.getToken();

      if (item.hidden) {
         return null;
      }

      if (item.permission) {
         const permissions = this.state.permissoes;

         try {

            if (!permissions || !permissions.includes(item.permission)) {
               return null;
            }

         } catch (error) {

         }
      }

      if (item.internal) {
         if (!sessao || !sessao.token) {
            return null;
         }
      }
      
      if(item.recruiter && sessao.acesso != 299){
         return null;
      }

      if (item.external) {
         if (sessao && sessao.token) {
            return null;
         }
      }
      const title = !item.parent ?
         <span className="menu-item-parent"><Msg phrase={item.title} /></span> :
         <Msg phrase={item.title} />;

      const badge = item.badge ? <span className={item.badge.class}>{item.badge.label || ''}</span> : null;
      const childItems = item.items ? <SmartMenuList items={item.items} /> : null;

      const icon = item.icon ? (
         item.counter ? <i className={item.icon}><em>{item.counter}</em></i> : <i className={item.icon} />
      ) : null;


      const liClassName = (item.route && this.context.router.isActive(item.route)) ? 'active' : '';
      const credentials = (item.route && item.route.indexOf('?') != -1) ? '' :  '?email=' + ((sessao && sessao.email) || this.props.location.query.email || '');

      const link = item.route ?
         <Link to={item.route + credentials} title={item.title} activeClassName="active" onClick = {() => 
            {
               location.href= (location.href.split('#')[0] +'#' + item.route + credentials)
               location.reload();
            }
            }
         
         >
            {icon} {title} {badge}

         </Link> :
         <a href={item.href || '#'} onClick={(e) => this.clickMenu(e, item)} title={item.title}>
            {icon} {title} {badge}
         </a>;


      return <div><li className={liClassName}>{link}{childItems}</li>

         <ModalChangePassword buttonClose={true} buttonConfirm={true} ref={e => this.modalChangePassword = e}
            onConfirm={this.executeChangePassword}
         >
            <h1>Deseja trocar a senha?</h1>
         </ModalChangePassword>

         <ModalLogout buttonClose={true} buttonConfirm={true} ref={e => this.modalLogout = e}
            onConfirm={this.executeLogout}
         >
            <h1>Deseja encerrar sessão?</h1>
         </ModalLogout>

         <JnDialog ref={e => this.modalDeleteCandidate = e} buttons={this.getCandidateOptions()} style={{ width: "100%" }}>
            <h1>Deseja de fato excluir seu currículo desta plataforma?</h1>
         </JnDialog>
      </div>
   }


   getCandidateOptions = () => {
      return [
         {
            label: "Sim, quero mesmo excluir!",
            tooltip: "Ao escolher esta opção seus dados serão excluídos permanentemente e se futuramente quiser usar nossos serviços, terá de fazer um novo cadastro",
            event: () => this.deleteCandidate()
         },
         {
            label: "Apenas torne-me invisível",
            tooltip: "Os recrutadores não poderão mais encontrar o seu currículo, enquanto você não voltar atrás nessa opção escolhida",
            event: () => this.inactivateCandidate()
         },
         {
            label: "Cancelar",
            event: () => this.modalDeleteCandidate.close()
         }

      ];
   }

   inactivateCandidate = () => {

      const sessao = JnAjax.getToken();
      const url = `candidatos/${sessao.email}/0`;
      const httpResponses = {};
      this.modalDeleteCandidate && this.modalDeleteCandidate.close();
      httpResponses[200] = () => this.showMessage('A partir de agora, os recrutadores não poderão mais encontrar o seu currículo, a não ser que você reabilite esta opção');
      JnAjax.doAnAjaxRequest(url, httpResponses, 'DELETE');
   }

 
   clickMenu = (e, item) => {
      e && e.preventDefault && e.preventDefault();
      this[item.event] && this[item.event]();
   }

   logout = () => {
      this.modalLogout && this.modalLogout.show && this.modalLogout.show();
   }

   changePassword = () => {
      this.modalChangePassword && this.modalChangePassword.show && this.modalChangePassword.show();
   }

   executeLogout = (e) => {
      e && e.preventDefault && e.preventDefault();
      const sessao = JnAjax.getToken();
      sessionStorage.removeItem('sessao');
      this.props.router.push(`/login?email=${sessao.email}&mensagem=sairDoSistema`);
      JnAjax.doAnAjaxRequest(`login/${sessao.email}/logout`, {}, 'HEAD', {}, {token: sessao.token});

   }

   executeChangePassword = (e) => {
      e && e.preventDefault && e.preventDefault();
      const sessao = JnAjax.getToken();
      sessionStorage.removeItem('sessao');
      this.props.router.push(`/tokenToSetPassword?email=${sessao.email}&msgType=info&msgValue=changePassword`);
      JnAjax.doAnAjaxRequest(`login/${sessao.email}/logout`, {}, 'HEAD', {}, {token: sessao.token});
   }

   openDialogToDeleteCandidate = () => {
      this.modalDeleteCandidate.show();
   }


   showMessage = (title) => {
      smallBox({
         title,
         content: "<br/><br/>",
         color: "#296191",
         timeout: 8000,
         icon: "fa fa-bell swing animated"
      });

   }
}

export default withRouter(SmartMenuItem)