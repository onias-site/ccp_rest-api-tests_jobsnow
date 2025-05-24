import {createStore, combineReducers,  applyMiddleware} from 'redux'
import thunk from 'redux-thunk'
import {routerReducer} from 'react-router-redux'

import navigationReducer from '../components/navigation/navigationReducer'
import {eventsReducer} from '../components/calendar'
import authReducer from '../routes/auth/authReducer'

export const rootReducer = combineReducers(
  {
    routing: routerReducer,
    navigation: navigationReducer,
    auth: authReducer,
    events: eventsReducer,
  }
);

const store =  createStore(rootReducer,
  applyMiddleware(
    thunk
  )
);

export default store;