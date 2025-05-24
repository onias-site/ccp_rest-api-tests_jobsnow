/**
 * Created by griga on 11/17/16.
 */


import {SET_AUTH_INFO} from './authActions'

const initialState = {
  user: {
    token: "",
    name: "",
    picture: null
  },
  logged: false,
  manterConectado: false
}

export default function authReducer (state = initialState, action ){
  const _state = {...state};

  switch (action.type){
    case SET_AUTH_INFO:
      _state.user = action.user;
      _state.logged = true;
      return _state; 
    default:
      return _state;
  }
}