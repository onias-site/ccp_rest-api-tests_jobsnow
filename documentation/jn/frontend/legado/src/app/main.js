window.jQuery = window.$ =  require("jquery");
window.moment = require('moment');
window._ = require('lodash');

import 'core-js/es6/array'
import 'core-js/es6/promise'
import 'core-js/es6/object'

import 'jquery-ui-npm/jquery-ui.min.js'


require("bootstrap")//x

require.ensure([], ()=>{
  require('./app');

})

