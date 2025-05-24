import React from 'react'

import NavMenuItem from './NavMenuItem'

export default function SmartMenuList(props) {

  const {items, ...p} = props;

  return (
    <ul {...p} >
      {items.map((item, idx) => {
        return <NavMenuItem item={item} key={item._id}/>
      })}
    </ul>
  )
}
