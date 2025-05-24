import getRegiao from './JnRegion';


const regioes = getRegiao();
import React, { Component } from 'react';
export default class ComboRegiaoMetropolitana extends Component {
    state = {
        ddd:0  ,
        regs : []
    }

    componentDidMount(){
        const regs = [];
        this.setState({ddd: this.props.value});
        const exclude = this.props.exclude || [];

        for (let ddd in regioes) {
            if(exclude.includes(ddd)){
                continue;
            }
            let regiao = regioes[ddd];
            const array = regiao.split('–');
            const uf = array[1];
            if (!uf) {
                continue;
            }
            regiao = uf + ' – ' + array[0];
            const reg = { ddd, regiao }
            
            regs.push(reg);
        }
        this.setState({regs});

    }

    componentDidUpdate(prevProps){
        if (this.props.value === prevProps.value) {
            return;
        }
        this.setState({ddd: this.props.value});
        const regs = [];
        const exclude = this.props.exclude || [];

        for (let ddd in regioes) {
            if(exclude.includes(ddd)){
                continue;
            }
            let regiao = regioes[ddd];
            const array = regiao.split('–');
            const uf = array[1];
            if (!uf) {
                continue;
            }
            regiao = uf + ' – ' + array[0];
            const reg = { ddd, regiao }
            
            regs.push(reg);
        }
        this.setState({regs});

    }

    render() {
        return(
            <div className={this.props.divClass || "form-group"}>
            <label className="control-label">{this.props.label}</label>
            <select className="form-control"
                value={this.state.ddd}
                onChange={e => {
                    this.props.onChange && this.props.onChange(e.target.value);
                    this.setState({ ddd: e.target.value });
                }}>
               {!this.props.required && <option value={0}>{this.props.labelSelect}</option>}                {
                    this.state.regs.map(x => (
                        <option key={x.ddd} value={x.ddd}>{x.regiao}</option>
                    ))
                }
            </select>
        </div>

        );
    }


}