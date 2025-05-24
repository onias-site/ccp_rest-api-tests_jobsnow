import $ from "jquery";

export default class JnAjax {

    static PRODUCAO = 'producao';
    static DESENVOLVIMENTO = 'desenvolvimento';
    
    static SAME = 'same';

     static deafultEnviroment = JnAjax.PRODUCAO;
//    static deafultEnviroment = JnAjax.DESENVOLVIMENTO;

    static doAnAjaxRequest(uri, callbacks = {}, verb = 'HEAD', requestBody = {},  headers = {}, enviroment = JnAjax.deafultEnviroment, contentType = 'application/json', dataType = 'json') {
        const url = JnAjax.getUrlBackEnd(enviroment);
        JnAjax.ajaxLoading(true);
        const token = this.getToken();
        if(!headers){
            headers = {};
        }

        if (token && token.token) {
            headers['token'] = token.token;
            headers['email'] = token.email;
        }

        $.ajax({
            data: JSON.stringify(requestBody),
            headers,
            url: `${url}/${uri}`,
            contentType,
            type: verb,
            dataType,

            complete: (a) => {
                try {
                    JnAjax.ajaxLoading(false);

                    callbacks[401] = callbacks[401] || JnAjax.getHandler401();

                    callbacks[420] = callbacks[420] || JnAjax.getHandler420();
                   
                    callbacks[403] = callbacks[403] || JnAjax.getHandler403(uri);

                    callbacks[410] = callbacks[410] || JnAjax.getHandler410(uri);

                    const callback = callbacks[a.status] || callbacks['onUnexpectedHttpStatus'] || ((responseBody, httpStatus) => console.log('resposta' + responseBody, 'status imprevisto: ' + httpStatus));
                    
                    const afterHttpRequest = callbacks['afterHttpRequest'] || (() => { });

                    const responseBody = JnAjax.parseToObject(a.responseText);

                    callback(responseBody, a.status);
                    afterHttpRequest(responseBody, a.status);
                } catch (error) {
                    console.error(error);
                }
            }
        });
    }
  
   static getHandler401 = () =>{

        let resultado = () => {
            const token = JnAjax.getToken();
            
            sessionStorage.removeItem('sessao');
            const queryParameters = token.email ? `?email=${token.email}&mensagem=sessaoExpirada` : '?mensagem=acessoNegado';
            window.location.href = '#/login' + queryParameters;
        };

        return resultado;
    
    }

    static getHandler420 = () =>{

        let resultado = () => {
            window.location.href = '#/login?mensagem=foraDoHorario';
        };

        return resultado;
    
    }


    static getHandler403 = (uri) =>{

        let resultado = (reason) => {
            const email = uri.split('/')[1];
            if(!email){
                return;
            }

            sessionStorage.removeItem('sessao');
            const queryParameters = `?email=${email}&msgType=danger&msgValue=${reason}` ;
            window.location.href = '#/tokenToSetPassword' + queryParameters;
        };

        return resultado;
    }

    static getHandler410 = (uri) =>{

        let resultado = () => {
            const email = uri.split('/')[1];
            if(!email){
                return;
            }
 
            sessionStorage.removeItem('sessao');
            window.location.href = `#/blockedToken?email=${email}`;
            
        };

        return resultado;
    }

  static getEmailFromUrl = () => {
    alert(window.location.href);
    const x = window.location.href.split('?')[1];
      if(!x){
        return '';
      }

      const array = x.split('&');

      const email = array.filter(y=> y.startsWith('email='));
      if(!email){
          return '';
      }
      return email;
    }
  
    static parseToObject = res => {
        try {
            res = JSON.parse(res);
            return res;
        } catch (error) {
            return res;
        }
    }


    static ajaxLoading(x) {
        document.getElementById('cover').style.display = x ? 'block' : 'none';
    }

    static getUrlBackEnd(enviroment = JnAjax.deafultEnviroment) {
        const urls = {
            same: '',
            desenvolvimento: 'http://localhost:8080',
            producao: 'https://ccpjobsnow.com'
        };
        return urls[enviroment] || enviroment;
    }


    static getToken() {
        const sessao = sessionStorage.getItem('sessao');

        try {
            const sessaoObj = JSON.parse(sessao);
            return { ...sessaoObj };
        } catch (error) {
            return {};
        }

    }
   
}

