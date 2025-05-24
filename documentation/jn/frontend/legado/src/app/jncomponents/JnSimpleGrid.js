
import JnAjax from '../services/JnAjax'
import React from 'react';
import { Dropdown } from 'primereact/dropdown';

export default class JnSimpleGrid extends React.Component {
    state = {
        labels: []
        , campos: []
        , size: 10
        , page: 1
        , registros: []
        , numeros: []
    }

    quantidades = [
        {
            label: "10",
            value : 10
        },
        {
            label: "20",
            value : 20
        },
        {
            label: "50",
            value : 50
        },
        {
            label: "100",
            value : 100
        }
    ];
 
    recarregarGrids = (size, page) =>{

        const from  = ((page - 1) * size);

        const parametrosDaUrl = this.props.httpMethod? '' : `?from=${from}&size=${size}`;

        const callBacks = [];
        callBacks[this.props.httpStatus || 200] = (resultado) => this.carregarRegistros(resultado, size, page);
        JnAjax.doAnAjaxRequest(`${this.props.url}${parametrosDaUrl}`, callBacks, this.props.httpMethod || 'GET', {from, size});

    }

    carregarRegistros = (resultado, size, page) =>{
       
        const registros = (this.props.extrairRegistros && this.props.extrairRegistros(resultado).hits) || resultado;
        const length = (this.props.extrairRegistros && this.props.extrairRegistros(resultado).total) || resultado.length;
        const quantidadeDePaginas = Math.ceil(length / size);

        const numeros = [];
        for(let k = 0; k < quantidadeDePaginas; k++){
            numeros.push({label:k+1, value: k+1});
        }
        this.setState({registros, numeros, page, size});
    }

    componentDidUpdate(prevProps){
        if(this.props.url != prevProps.url){
            this.recarregarGrids(this.state.size, 1);
        }
    }

    componentDidMount() {
    
        const size  = this.props.size || this.state.size;
        const page  = this.props.page || this.state.page;
        this.recarregarGrids(size, page);
        const labels = [];
        const campos = [];
        for (const campo in this.props.colunas) {
            const label = this.props.colunas[campo];

            labels.push(label);
            campos.push(campo);
        }
 
        this.setState({ labels, campos, page});
    }

    reloadData = () => {
        const size  = this.props.size || this.state.size;
        const page  = this.props.page || this.state.page;
        this.recarregarGrids(size, page);

        this.setState({ page, size });
    }

    getTr = (registro, idx) => {
        return (
            <tr key={idx} >
                {
                    this.state.campos.map(campo => <td key={campo}>{(registro[campo] && registro[campo].funcao && registro[campo].funcao()) || registro[campo]}</td>)
                }
                {
                    this.props.getColunaDasAcoes && this.props.getColunaDasAcoes(registro)
                }

            </tr>);

    }

    getPaginacao = () => {
        
        if(!this.props.paginar){
            return null;
        }

        return <tr className= "row">
                    <td className = "col col-md-4">
                        Registros Por página: <Dropdown value={this.state.size} options={this.quantidades} style={{width:"300px"}} onChange={e => 
                            this.setState({size : e.value}, this.recarregarGrids(e.value, this.state.page))} placeholder="Quantidade de registros por página..." />
                       
                    </td>
                    <td className = "col col-md-4">
                       Ir para a página:  <Dropdown value = {this.state.page} options = {this.state.numeros} style={{width:"300px"}} onChange = {e => this.setState({page: e.value}, this.recarregarGrids(this.state.size, e.value))} placeholder = "ir para a página..."/>
                    </td>    
                    <td className = "col col-md-4">
                            {this.props.children}
                    </td>    
                </tr>;
    }

    render() {
        return (
            <div style = {{padding:"30px"}}>
                {
                    this.getPaginacao()
                }
                <div className = "row">&nbsp;</div>
                <table id="customers" >
                    <tr>
                        {
                            this.state.labels.map((label, idx) => <th key={idx} role="col">{label}</th>)
                        }
                    </tr>

                    {
                       this.props.getRow && this.state.registros.map((registro,  idx) => this.props.getRow(registro, idx + ((this.state.page - 1) * this.state.size)))  || this.state.registros.map((registro, idx) => this.getTr(registro, idx))
                    }
                    
                </table>
            </div>
        )
    }

}
