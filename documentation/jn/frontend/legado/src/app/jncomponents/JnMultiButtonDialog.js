import React from 'react';
import { Modal } from 'react-bootstrap';

import { Button } from 'primereact/button';
export default class JnMultiButtonDialog extends React.Component {

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


   handleClose = () => {
      this.setState({ show: false });
      this.props.onClose && this.props.onClose();
   }

   render() {
      const length = this.props.buttons.length ? this.props.buttons.length : 1;
      const dividendo = 12 / length;
      return (
         <Modal dialogClassName="dialogDeleteCandidate" show={this.state.show} onHide={this.handleClose}>
            <Modal.Header closeButton>
               <Modal.Title>{this.props.title}</Modal.Title>
            </Modal.Header>

            <Modal.Body>
               {this.props.children}
            </Modal.Body>

            {
               this.props.buttons && this.props.buttons.length &&
               (<Modal.Footer>
                  <div className="row">

                     {
                        this.props.buttons.map((item, idx) => (
                           <div className= {`col col-md-${dividendo}`} style = {{display: "inline-block"}} key={idx}>
                              <a href="#" onClick={(e) => e && e.preventDefault()} title={item.tooltip}>
                                 <Button style={{width: "100%"}}   type="button" onClick={item.event} label={item.label} className="btn btn-default" ></Button></a>
                           </div>
                        ))
                     }
                  </div>
               </Modal.Footer>)
            }

         </Modal>
      );
   }

}