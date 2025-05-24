import React from 'react'
import { connect } from 'react-redux'


class SmallBreadcrumbs extends React.Component {
   render() {
      return (
         <ol className="breadcrumb">
            {
               this.props.items.map((it, idx) => (
                  <li key={it + idx}>{it}</li>
               ))
            }
         </ol>
      )
   }
}


const mapStateToProps = (state, ownProps) => {
   const { navigation, routing } = state;
   const route = routing.locationBeforeTransitions.pathname;

   const items = [];
   var route_parts = route.split('/');
   var path = "";

   route_parts.forEach(element => {
      if (element) {
         path += "/" + element;
   
         const titleReducer = (chain, it) => {
            if (it.route == path) {
               chain.push(it.breadcrumb)
            } else if (it.items) {
               it.items.reduce(titleReducer, chain);
            }
            return chain
         };
   
         const item = navigation.items.reduce(titleReducer, []);
         if(item.length) {
            items.push(...item);
         }
      }
   });

   return { items }
};


export default connect(mapStateToProps)(SmallBreadcrumbs)