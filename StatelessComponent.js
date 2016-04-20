/**
 * Simple wrapper class for defining stateless components. For now, we use this
 * class instead of the simpler function syntax because Flow doesn't support
 * stateless functional components yet. Ideally, we'd be able to get rid of this
 * class once this bug is fixed:
 * https://github.com/facebook/flow/issues/1081
 *
 * @flow
 */
import React from 'react-native'

export default class StatelessComponent<Props>
        extends React.Component<Props, Props, {}> {
    static defaultProps: Props;
    state: {};
}