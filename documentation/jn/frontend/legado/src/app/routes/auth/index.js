import React from 'react';
import HtmlRender from '../../components/utils/HtmlRender'

export default {
  childRoutes: [
    {
      path: 'lock',
      getComponent(nextState, cb){
        System.import('./containers/LockedScreen').then((m)=> {
          cb(null, m.default)
        })
      }
    },
    {
      path: 'login',
      getComponent(nextState, cb){
        System.import('./containers/Login').then((m)=> {
          cb(null, m.default)
        })
      }
    },
    {
      path: 'tokenToSetPassword',
      getComponent(nextState, cb){
        System.import('./containers/TokenToSetPassword').then((m)=> {
          cb(null, m.default)
        })
      }
    },
    {
      path: 'blockedToken',
      getComponent(nextState, cb){
        System.import('./containers/BlockedToken').then((m)=> {
          cb(null, m.default)
        })
      }
    },
  
    {
      path: 'validatePassword',
      getComponent(nextState, cb){
        System.import('./containers/ValidatePassword').then((m)=> {
          cb(null, m.default)
        })
      }
    },
    {
      path: 'register',
      getComponent(nextState, cb){
        System.import('./containers/Register').then((m)=> {
          cb(null, m.default)
        })
      }
    }
  ]
};
