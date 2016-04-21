/**
 * Top-level Android app.
 *
 * @flow
 */
'use strict';

import React, {
    Image,
    Text,
    View,
    DeviceEventEmitter,
} from 'react-native';
import {connect, Provider} from 'react-redux';

import {addExpression} from './actions'
import './DebugGlobals'
import Expression from './Expression'
import SimpleComponent from './SimpleComponent'
import StatelessComponent from './StatelessComponent'
import store from './store'

import type {UserExpression, ScreenExpression} from './ExpressionType'

type TopLevelExpressionPropTypes = {
    x: number,
    y: number,
    expr: UserExpression,
}
class TopLevelExpression
        extends StatelessComponent<TopLevelExpressionPropTypes> {
    render() {
        const {x, y, expr} = this.props;
        return <View style={{
            left: x,
            top: y,
            position: "absolute",
        }}>
            <Expression expr={expr}/>
        </View>;
    }
}

type PlaygroundCanvasProps = {
    screenExpressions: Map<number, ScreenExpression>;
};

class PlaygroundCanvasView extends SimpleComponent<PlaygroundCanvasProps, {}> {
    componentWillMount() {
        DeviceEventEmitter.addListener('refreshState', (state) => {
            console.log("Ignoring refreshState command. Consider removing.");
        });
    }

    render() {
        const {screenExpressions} = this.props;
        const exprNodes = Array.from(screenExpressions)
            .map(([exprId, screenExpression]) => {
                return <TopLevelExpression
                    expr={screenExpression.expr}
                    x={screenExpression.x}
                    y={screenExpression.y}
                    key={exprId}
                />
            });
        return <View>
            {exprNodes}
        </View>;
    }
}

const ConnectedPlaygroundCanvasView =
    connect(state => state)(PlaygroundCanvasView);

class PlaygroundCanvas extends SimpleComponent<{}, {}> {
    render() {
        return <Provider store={store}>
            <ConnectedPlaygroundCanvasView />
        </Provider>
    }
}

React.AppRegistry.registerComponent('PlaygroundCanvas', () => PlaygroundCanvas);
