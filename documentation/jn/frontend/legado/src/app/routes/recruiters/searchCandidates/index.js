
export default {
  component: require('../../../components/common/Layout').default,

  childRoutes: [
    {
      path: 'buscaDeCandidatos',
      getComponent(nextState, cb){
        System.import('./ArvoreDDDs').then((m)=> {
          cb(null, m.default)
        })
      }
    },
    {
      path: 'redirect',
      getComponent(nextState, cb){
        System.import('./Redirect').then((m)=> {
          cb(null, m.default)
        })
      }
    },
    {
      path: 'estatisticas',
      getComponent(nextState, cb){
        System.import('./Estatisticas').then((m)=> {
          cb(null, m.default)
        })
      }
    },
    {
      path: 'curriculos',
      getComponent(nextState, cb){
        System.import('./Curriculos').then((m)=> {
          cb(null, m.default)
        })
      }
    },
    {
      path: 'consultorias',
      getComponent(nextState, cb){
        System.import('./Consultorias').then((m)=> {
          cb(null, m.default)
        })
      }
    }
  ]
};
