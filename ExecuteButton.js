/**
 * @flow
 */
import React, {
    TouchableWithoutFeedback,
    View,
} from 'react-native'
import StatelessComponent from './StatelessComponent'

/**
 * Custom-built FAB since none of the existing FABs seem to work the way I want.
 */
type ExecuteButtonPropTypes = {
    onPress: any,
    style: any,
};
class ExecuteButton extends StatelessComponent<ExecuteButtonPropTypes> {
    render() {
        // TODO: Figure out why TouchableOpacity isn't working here.
        // TODO: Use TouchableNativeFeedback on Android to get a nice ripple
        // effect, or use some other FAB, once it works without looking weird.
        // See https://github.com/facebook/react-native/issues/5203 .
        return <TouchableWithoutFeedback>
            <View style={{
                width: 40,
                height: 40,
                backgroundColor: '#00AA00',
                borderRadius: 20,
                ...this.props.style,
            }}
            />
        </TouchableWithoutFeedback>;
    }
}

export default ExecuteButton;