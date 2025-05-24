import React from 'react';
import { Modal } from 'react-bootstrap';


export default class MyModal extends React.Component {

   state = {
      show: false
   }

   styleModal = {
      // position:"fixed"
      // top: '150px',
   }

   myModal = null;

   show = () => {
      this.setState({ show: true });
   }

   close = () => {
      this.setState({ show: false });
      this.props.onClose && this.props.onClose();
   }

   confirmClick = (e) => {
      e && e.preventDefault && e.preventDefault();
      this.props.onConfirm && this.props.onConfirm();
      // this.setState({ show: false });
   }

   handleClose = () => {
      this.setState({ show: false });
      this.props.onClose && this.props.onClose();
   }

   render() {
      return (
         <Modal show={this.state.show} onHide={this.handleClose}  >
            <Modal.Header closeButton>
               <Modal.Title>{this.props.title}</Modal.Title>
            </Modal.Header>

            <Modal.Body>
               {this.props.children}
            </Modal.Body>

            {
               this.props.buttonCancel || this.props.buttonConfirm || this.props.buttonClose ?
                  <Modal.Footer>
                     {this.props.buttonCancel &&
                        <button type="button" className="btn btn-default" onClick={this.close} >
                           Cancelar
                        </button>
                     }
                     {this.props.buttonConfirm &&
                        <button type="button" className="btn btn-primary"
                           disabled={this.props.loading}
                           onClick={(e) => this.confirmClick(e)}>
                           {
                              this.props.loading ?
                                 <div><i className="fa fa-spinner fa-spin"></i>&nbsp;&nbsp;&nbsp;Aguarde...</div>
                                 : this.props.buttonConfirmLabel || 'Confirmar'
                           }
                        </button>
                     }
                     {this.props.buttonClose &&
                        <button type="button" className="btn btn-default" onClick={this.close} >
                           {this.props.buttonCloseLabel || 'Fechar'}
                        </button>
                     }
                  </Modal.Footer>
                  : null
            }

         </Modal>
      );
   }

}