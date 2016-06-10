/**
 * The "Delete bar" is the area at the top of the screen where you can drag
 * expressions to remove them.
 *
 * @flow
 */

import React from 'react';
import {
    Text,
    View,
} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';

import StatelessComponent from './StatelessComponent';
import {TOOLBAR_HEIGHT} from './toolbar';

type DeleteBarProps = {
    isDraggingExpression: boolean,
}
export default class DeleteBar extends StatelessComponent<DeleteBarProps> {
    render() {
        const {isDraggingExpression} = this.props;
        const text = isDraggingExpression ? 'Remove' : 'Hide';
        return <View
            style={{
                backgroundColor: '#888888',
                position: 'absolute',
                left: 0,
                right: 0,
                top: 0,
                height: TOOLBAR_HEIGHT,
                flexDirection: 'row',
                alignItems: 'center',
                justifyContent: 'center',
            }}
        >
            <Icon name='clear' size={24} color='white' />
            <Text
                style={{
                    color: 'white',
                    fontSize: 18,
                }}
            >
                {text}
            </Text>
        </View>;
    }
}