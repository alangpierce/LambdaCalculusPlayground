/**
 * Simple wrapper that avoids having to specify props twice when default props
 * are not used.
 *
 * @flow
 */
import React from 'react'

export default class SimpleComponent<Props, State>
        extends React.Component<Props, Props, State> {
    state: State;
    static defaultProps: Props;
}