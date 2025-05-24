import React from 'react';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';

export default class JnAlert extends React.Component {
    state = { showDialog: false }

    exibir = (titulo, mensagem) => {
        this.setState({ titulo, mensagem, showDialog: true });
    }

    render() {
        const footer = (
            <div>
                <Button label={this.props.labelBotao || "OK"} icon="pi pi-check"  onClick={(e) => { e.preventDefault(); this.props.onClick && this.props.onClick();  this.setState({ showDialog: false }) }} />

            </div>
        );
        return (
            <Dialog style={{top:"0px"}} width={this.props.width || ""} height={this.props.height || ""}  maximizable={false} closeOnEscape={true} modal={true} header={this.state.titulo + ' (pressione a tecla esc para fechar)'} visible={this.state.showDialog} style={{ width: '50vw' }} footer={footer} onHide={() => 
                {

                    this.props.onClose && this.props.onClose();
                    this.setState({ showDialog: false });
                }
            
            }>
                {this.state.mensagem}
                {
                    !this.props.children ? null :
                        <div>
                            {this.props.children}

                        </div>
                }
            </Dialog>

        )
    }
}
