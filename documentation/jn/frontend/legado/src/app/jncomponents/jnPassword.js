
import React from 'react';

export default class JnPassword extends React.Component {

    state = {
        show: false
    }

    render() {
      const eye = !this.state.show ? "" : "-slash";
      return <section>

            <label className="input"> <i className={"icon-append fa fa-eye" + eye} onClick={() => this.setState({ show: !this.state.show })} />
                <input type={this.state.show ? "text" : "password"} name="password" data-smart-validate-input="" data-required=""
                    data-minlength="3" data-maxnlength="20"
                    data-message="Por favor inserir valor"
                    value={this.props.value}
                    onChange={event => this.props.onChange && this.props.onChange(event)} />

                <b className="tooltip tooltip-top-right">
                    <i className={`fa fa-eye${eye} txt-color-teal`} />
                    Clique aqui para {!eye?"exibir":"ocultar"} o valor digitado
                            </b>

            </label>
        </section>
    }
}