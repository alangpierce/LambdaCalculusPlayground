/**
 * The "Delete bar" is the area at the top of the screen where you can drag
 * expressions to remove them.
 *
 * @flow
 */

import React from 'react';
import shallowCompare from 'react-addons-shallow-compare';
import {
    Text,
} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';

import StatelessComponent from './StatelessComponent';
import {TOOLBAR_HEIGHT} from './toolbar';
import {TrackedView} from './TrackedViews';
import * as t from './types';

type DeleteBarProps = {
    isDeleteBarHighlighted: boolean,
    isDraggingExpression: boolean,
}
export default class DeleteBar extends StatelessComponent<DeleteBarProps> {
    shouldComponentUpdate(nextProps: DeleteBarProps, nextState: {}) {
        return shallowCompare(this, nextProps, nextState);
    }

    render() {
        const {isDeleteBarHighlighted, isDraggingExpression} = this.props;
        const text = isDraggingExpression ? 'Remove' : 'Hide';
        return <TrackedView
            viewKey={t.DeleteBarKey.make()}
            style={{
                backgroundColor: isDeleteBarHighlighted ? '#AA0000' : '#888888',
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
        </TrackedView>;
    }
}