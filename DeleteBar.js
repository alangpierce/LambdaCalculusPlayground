/**
 * The "Delete bar" is the area at the top of the screen where you can drag
 * expressions to remove them.
 *
 * @flow
 */

import React from 'react';
import {
    Text,
} from 'react-native';
import StatelessComponent from './StatelessComponent';

type DeleteBarProps = {
    isDraggingExpression: boolean,
}
export default class DeleteBar extends StatelessComponent<DeleteBarProps> {
    render() {
        const {isDraggingExpression} = this.props;
        const text = isDraggingExpression ? 'Remove' : 'Hide';
        return <Text>{text}</Text>;
    }
}