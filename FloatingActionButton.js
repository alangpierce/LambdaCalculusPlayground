/**
 * @flow
 */
import React from 'react';
import {
    Image,
    TouchableWithoutFeedback,
    View,
} from 'react-native';

import StatelessComponent from './StatelessComponent';

type AssetId = number;

/**
 * Custom-built FAB since none of the existing FABs seem to work the way I want.
 *
 * TODO: Consolidate with ExecuteButton.js
 */
type ExecuteButtonPropTypes = {
    onPress: () => void,
    source: AssetId,
    style: any,
};
class FloatingActionButton extends StatelessComponent<ExecuteButtonPropTypes> {
    render() {
        const {onPress, source} = this.props;
        return <TouchableWithoutFeedback onPress={onPress}>
            <View style={{
                width: 56,
                height: 56,
                backgroundColor: '#00AA00',
                borderRadius: 28,
                alignItems: 'center',
                justifyContent: 'center',
                ...this.props.style,
            }}>
                <Image
                    source={source}
                    style={{
                        width: 24,
                        height: 24,
                        tintColor: 'white',
                        resizeMode: 'contain',
                    }}
                />
            </View>
        </TouchableWithoutFeedback>;
    }
}

export default FloatingActionButton;