import React from 'react';
import JnAjax from '../../services/JnAjax';
import { Chart } from 'primereact/chart';
import { Dropdown } from 'primereact/dropdown';
import { Calendar } from 'primereact/calendar';

export default class PieChart extends React.Component {


   state = {
      table: '', field: '', language: 'pt', chartData: {}, tables: [], fields: [], allLabels: []
      , lineChart: {}
      , de: new Date(1569888056000)
      , ate: new Date(new Date().getTime() + (1000 * 60 * 60 * 24))
   }
   locale = {
      firstDayOfWeek: 1,
      dayNames: ["domingo", "segunda feira", "terça feira", "quarta feira", "quinta feira", "sexta feira", "sábado"],
      dayNamesShort: ["dom", "seg", "ter", "qua", "qui", "sex", "sáb"],
      dayNamesMin: ["D", "S", "T", "Q", "Q", "S", "S"],
      monthNames: ["janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"],
      monthNamesShort: ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"],
      today: 'Hoje',
      clear: 'Limpar',
      dateFormat: 'dd/mm/yy',
      weekHeader: 'Sm'
   };

   loadComboboxes = (callback) => {
      const callBacks = {};

      callBacks[200] = allLabels => this.handleLabels(allLabels, callback);

      JnAjax.doAnAjaxRequest(`/estatisticas/allLabels`, callBacks, 'GET');

   }

   handleLabels = (allLabels, callback) => {

      const tables = allLabels.map(x => {
         return { label: x[this.state.language], value: x.id }
      });

      const table = (tables[0] || {}).id;
      const fields = this.getFields(tables[0]);

      this.setState({ tables, fields, allLabels, table });
      callback && callback();

   }


   getFields = (table) => {
      const fields = ((table && table.fields) || []).map(y => {
         return { label: y[this.state.language], value: y.value }
      });

      return fields;
   }

   chooseOneTable = (tableId, field) => {
      const tables = this.state.allLabels.filter(x => x.id == tableId);

      const table = (tables[0] || {}).id;
      const fields = this.getFields(tables[0]);

      this.setState({ fields, table, field, chartData: {} });

      const url = `/estatisticas/${tableId || this.props.location.query.tabela}/dataDeInclusao`

      const callBacks = {};

      callBacks[200] = lineChart => {
         lineChart.datasets[0].label = `Registros diários de '${this.state.allLabels.filter(x => x.id === tableId)[0].pt}'`;

         this.setState({ lineChart });
      };

      JnAjax.doAnAjaxRequest(url, callBacks, 'GET');

   }

   handleResult = (chartData) => {
      const tabela = this.state.table || this.props.location.query.tabela;
      const table = this.state.allLabels.filter(t => t.id == tabela)[0];
      if (!table) {
         console.error('Nao encontrou tabela ' + tabela);
         return {};
      }

      const campo = this.state.field || this.props.location.query.campo;
      const field = table.fields.filter(f => f.value == campo)[0];
      if (!field) {
         console.error('Nao encontrou campo ' + campo);
         return {};
      }

      const values = field.values || [];

      chartData.labels = (chartData.labels || []).map((value, idx) => this.translateInCurrentLanguage(values, value) + this.getPercentLabel(chartData, idx));
      return chartData;
   }

   getPercentLabel = (chartData, idx) => {
      const percent = chartData && chartData.percents && chartData.percents[idx];

      const percentLabel = percent ? ' (' + percent + '%)' : '';
      return percentLabel;
   }

   translateInCurrentLanguage = (values, value, idx) => {
      const found = values.filter(x => x.value == value)[0];
      if (!found) {
         return value;
      }

      const label = found[this.state.language];
      if (!label) {
         return value;
      }


      return label;
   }

   loadChart = (field) => {
      const callBacks = {};

      const table = this.state.table || this.props.location.query.tabela;

      callBacks[200] = chartData => this.setState({ chartData: this.handleResult(chartData), table }, this.chooseOneTable(table, field), window.location.href = `#/estatisticas/simples?tabela=${table}&campo=${field}`);

      JnAjax.doAnAjaxRequest(`/estatisticas/${this.state.table || this.props.location.query.tabela}/${field}/${this.state.de.getTime()}/${this.state.ate.getTime()}`, callBacks, 'GET');
   }

   componentDidMount() {

      this.loadComboboxes(this.props.location.query.tabela && this.props.location.query.campo && (() => this.loadChart(this.props.location.query.campo)));

   }

   render() {
      const options = {
         title: {
            display: true,
            text: this.state.chartData.total ? 'Total: ' + this.state.chartData.total : '',
            fontSize: 16
         },
         legend: {
            position: 'bottom'
         }

      };
      return (
         <div className="card">
            <div className="row" style={{ padding: "20px" }}>
               <div className="col col-md-3">
                  <label>Tipo de informação: </label><br />
                  <Dropdown value={this.state.table} options={this.state.tables} style={{ width: "300px" }} onChange={e => this.chooseOneTable(e.value)} disabled />

               </div>
               <div className="col col-md-3">
                  <label>O que você quer saber: </label><br />
                  <Dropdown value={this.state.field} options={this.state.fields} style={{ width: "300px" }} onChange={e => this.setState({ field: e.value }, this.loadChart(e.value))} />
               </div>
               <div className="col col-md-3">
                  <label>Início do período: </label><br />
                  <Calendar value={this.state.de} onChange={(e) => {
                     this.setState({ de: e.value }, this.loadChart(this.state.field))
                  }
                  } dateFormat="dd/mm/yy" readOnlyInput locale={this.locale} />
               </div>
               <div className="col col-md-3">
                  <label>Final do período: </label><br />
                  <Calendar value={this.state.ate} onChange={(e) => {
                     this.setState({ ate: e.value }, this.loadChart(this.state.field))
                  }
                  } dateFormat="dd/mm/yy" readOnlyInput locale={this.locale} />
               </div>
            </div>
            <table width="100%">
               <tr>
               <td width="55%" style={{padding:"20px"}}>
                     <Chart type="line" data={this.state.lineChart} options={{options}} />
                  </td>
                  <td width="45%">
                     <Chart type="pie" data={this.state.chartData} options={options} />
                  </td>
               </tr>
            </table>

         </div>
      );
   }
}

