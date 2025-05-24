
export default {
  component: require('../../../components/common/Layout').default,

  childRoutes: [
    {
      path: 'cadastro',
      getComponent(nextState, cb){
        System.import('./FormCandidato').then((m)=> {
          cb(null, m.default)
        })
      }
    },
    {
      path: 'visualizacoes',
      getComponent(nextState, cb){
        System.import('./Visualizacoes').then((m)=> {
          cb(null, m.default)
        })
      }
    }
   
  ]
};
