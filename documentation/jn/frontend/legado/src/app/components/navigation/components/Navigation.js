/**
 * Created by griga on 11/24/15.
 */

import React from 'react'

import NavMenu from './NavMenu'

import MinifyMenu from './MinifyMenu'



import { withRouter } from 'react-router';


class Navigation extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <aside id="left-panel">
        {/* <LoginInfo router={this.props.router} /> */}
        <nav>
          {
            <NavMenu
              openedSign={'<i class="fa fa-minus-square-o"></i>'}
              closedSign={'<i class="fa fa-plus-square-o"></i>'}
            />}
        </nav>
        <MinifyMenu />
      </aside>
    )
  }
}

export default withRouter(Navigation)