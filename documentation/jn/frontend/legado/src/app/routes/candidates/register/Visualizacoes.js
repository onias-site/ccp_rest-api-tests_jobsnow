import JnAjax from '../../../services/JnAjax';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';

import React from 'react';

export default class Visualizacoes extends React.Component {
  state = {
    visualizacoes: []
  }

  componentDidMount() {
        const events = {};
        events[200] = visualizacoes => this.setState({visualizacoes});

        JnAjax.doAnAjaxRequest(`/candidatos/${JnAjax.getToken().email}/visualizacoes`, events, "GET");
  }

  render() {
    return (
      <DataTable value={this.state.visualizacoes}>
        <Column field='consultoria' header='Consultoria'/>
        <Column field='total' header='Visualizações'/>
      </DataTable>
    )
  }
}