/**
 * Created by griga on 11/24/15.
 */


export const SET_AUTH_INFO = 'SET_AUTH_INFO'

export const loginSuccess = (user, manterConectado) => {
  return {
    type: SET_AUTH_INFO,
    user,
    manterConectado
  };

}
