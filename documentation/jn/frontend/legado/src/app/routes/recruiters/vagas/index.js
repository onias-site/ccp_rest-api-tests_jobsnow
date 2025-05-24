
export default {
  component: require('../../../components/common/Layout').default,

  childRoutes: [
    {
      path: 'vagas',
      getComponent(nextState, cb){
        System.import('./ListaDeVagas').then((m)=> {
          cb(null, m.default)
        })
      }
    }
  ]
};
