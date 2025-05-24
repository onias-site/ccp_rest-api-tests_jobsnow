
export default {
  component: require('../../components/common/Layout').default,

  childRoutes: [
    {
      path: 'estatisticas/simples',
      getComponent(nextState, cb){
        System.import('./PieChart').then((m)=> {
          cb(null, m.default)
        })
      }
    }
   
  ]
};
