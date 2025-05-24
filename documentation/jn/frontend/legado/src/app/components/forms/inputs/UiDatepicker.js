import React from 'react';
import ReactDOM from 'react-dom';

export default class UiDatepicker extends React.Component {

   input = null;

   componentDidMount() {
      const onSelectCallbacks = [];
      const props = this.props;
      const element = $(this.input);

      if (props.minRestrict) {
         onSelectCallbacks.push((selectedDate) => {
            $(props.minRestrict).datepicker('option', 'minDate', selectedDate);
         });
      }
      if (props.maxRestrict) {
         onSelectCallbacks.push((selectedDate) => {
            $(props.maxRestrict).datepicker('option', 'maxDate', selectedDate);
         });
      }

      if (props.onSelect) {
         onSelectCallbacks.push(props.onSelect);
      }

      //Let others know about changes to the data field
      onSelectCallbacks.push((selectedDate) => {
         element.triggerHandler("change");

         const form = element.closest('form');

         if (typeof form.bootstrapValidator == 'function') {
            try {
               form.bootstrapValidator('revalidateField', element);
            } catch (e) {
               console.log(e.message)
            }
         }


      });

      const options = {
         prevText: '<i class="fa fa-chevron-left"></i>',
         nextText: '<i class="fa fa-chevron-right"></i>',
         monthNames: ["Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"],
         dayNamesShort: ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab"],
         dayNamesMin: ["Do", "Se", "Te", "Qa", "Qi", "Se", "Sa"],
         onSelect: (selectedDate) => {
            onSelectCallbacks.forEach((cb) => {
               cb(cb, selectedDate)
            })
         }
      };

      if (props.numberOfMonths) options.numberOfMonths = props.numberOfMonths;

      if (props.dateFormat) options.dateFormat = props.dateFormat;

      if (props.defaultDate) options.defaultDate = props.defaultDate;

      if (props.changeMonth) options.changeMonth = props.changeMonth;

      element.datepicker(options);

      // if (this.container) {
      //    let widget = element.datepicker("widget");
      //    $(ReactDOM.findDOMNode(this.container)).append(widget);

      //    element.datepicker("option", "beforeShow", (input, inst) => {
      //       setTimeout(() => {
      //          var offsets = $(this.container).offset();
      //          // var top = offsets.top;
      //          console.log(offsets);
      //          // console.log(inst.dpDiv.css('top'));

      //          inst.dpDiv.css({
      //             position: 'absolute',
      //             top: 30,
      //             left: 0,
      //          });
      //       });
      //    });
      // }
   }

   render() {
      const {
         minRestrict, maxRestrict, changesMonth,
         numberOfMonths, dateFormat, defaultDate, changeMonth,
         ...props
      } = { ...this.props };

      if (this.props.inline) {
         return (
            <div ref={i => this.input = i} />
         )
      }

      return (
         <input type="text" {...props} ref={i => this.input = i} />
      )

      // tentativa de conseguir exibi-lo no modal sem sucesso
      // return (
      //    <div ref={i => this.container = i}>
      //       <input type="text" {...props} ref={i => this.input = i} />
      //    </div>
      // )
   }
}