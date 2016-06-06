/**
 * @flow
 */
import React from 'react';
import {
    Text,
    TouchableWithoutFeedback,
    View,
} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';

import StatelessComponent from './StatelessComponent';

/**
 * Custom-built FAB since none of the existing FABs seem to work the way I want.
 */
type ExecuteButtonPropTypes = {
    onPress: () => void,
    style: any,
};
class ExecuteButton extends StatelessComponent<ExecuteButtonPropTypes> {
    render() {
        // TODO: Figure out why TouchableOpacity isn't working here.
        // TODO: Use TouchableNativeFeedback on Android to get a nice ripple
        // effect, or use some other FAB, once it works without looking weird.
        // See https://github.com/facebook/react-native/issues/5203 .
        return <TouchableWithoutFeedback onPress={this.props.onPress}>
            <View style={{
                width: 40,
                height: 40,
                backgroundColor: '#00AA00',
                borderRadius: 20,
                alignItems: 'center',
                justifyContent: 'center',
                ...this.props.style,
            }}>
                <Icon name='play-arrow' size={24} color='white' />
            </View>
        </TouchableWithoutFeedback>;
    }
}

export default ExecuteButton;