
import './LockedScreen.css'

import React from 'react'
export default class LockedScreen extends React.Component {
  render() {
    return (

      <div>
      <ModalSelecionarUmEmail 
      buttonConfirmLabel = 'Confirmar' buttonCloseLabel='Cancelar'  
      buttonClose={true} buttonConfirm={true}
      ref={e => this.modalSelecionarUmEmail = e}
      title = 'Escolher um email para o pré-candidato'     
      onConfirm={this.salvarEmailSelecionado} 
      onClose = {() => {}}
      >
      <form className="smart-form client-form" style={{ paddingTop: "20px", width: "90%" }} >                
          <div>
              {this.state.message ? <Alert bsStyle={this.state.messageType}><p>{this.state.message}</p></Alert> : null}
          </div>
          <div className="row" style={{ paddingLeft: "20px" }} >
              <div className="col col-md-12"><span>Nome do Arquivo: {this.state.nomeArquivoImportado}</span>    <br /></div>
          </div>
          <div className="row" style={{ paddingLeft: "20px" }} >
              <div className="col col-md-12"><span>Data da Importação: {this.state.dataArquivoImportado}</span>    <br /></div>
          </div>
          <br/>        
          <div className="row" style={{ paddingLeft: "20px" }} >
              <div className="col col-md-12">
                      <label className="label">Selecione um Email para o pré-candidato:</label>
                      
                      <select className="form-control"
      
                          name="emailPreCandidato" 
                          value={this.state.emailPreCandidato}
                          onChange={e => {
                              const nome = e.target.value;
                              this.setState({ emailPreCandidato : nome });
                          }} >
                             <option value={0}>Selecione um...</option>
                          {
                              this.state.emailsPreCandidato.map(x => (
                                  <option key={x} value={x}>{x}</option>
                              ))
                          }
                      </select>
                       
              </div>
          </div>
      </form>                
      </ModalSelecionarUmEmail>
      </div>      
      

    )
  }
}