import React from 'react'
import ReactDOM from 'react-dom'
import {Provider} from 'react-redux'
import {syncHistoryWithStore} from 'react-router-redux'
import {Router, hashHistory} from 'react-router'

import store from './store/configureStore'
import './buscarCurriculos.css';
import './formCandidato.css';

import 'primereact/resources/themes/nova-light/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';

const history = syncHistoryWithStore(hashHistory, store);

const routes = {

  path: '/',
  // indexRoute: { onEnter: (nextState, replace) => replace('/login?path='+encodeURIComponent(nextState.location.pathname)) },
  indexRoute: { onEnter: (nextState, replace) => replace('/login') },
  childRoutes: [
    require('./routes/recruiters').default,
    require('./routes/candidates').default,
    require('./routes/stats').default,
    require('./routes/auth').default,
  ]
};


ReactDOM.render((
  <Provider store={store}>
    <Router
      history={history}
      routes={routes} 
    />
 
  </Provider>
), document.getElementById('smartadmin-root'));
