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

import type {
    CanvasPoint,
    UserExpression,
    ScreenExpression
} from './types'

type TopLevelExpressionPropTypes = {
    expr: UserExpression,
    pos: CanvasPoint,
}
class TopLevelExpression
        extends StatelessComponent<TopLevelExpressionPropTypes> {
    render() {
        const {expr, pos: {canvasX, canvasY}} = this.props;
        return <View style={{
            left: canvasX,
            top: canvasY,
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
        DeviceEventEmitter.addListener('createLambda', (varName) => {
            store.dispatch(addExpression({
                expr: {
                    type: 'lambda',
                    varName,
                    body: null,
                },
                pos: {
                    canvasX: 100,
                    canvasY: 100,
                },
            }));
        });
    }

    render() {
        const {screenExpressions} = this.props;
        const exprNodes = Array.from(screenExpressions)
            .map(([exprId, screenExpression]) => {
                return <TopLevelExpression
                    expr={screenExpression.expr}
                    pos={screenExpression.pos}
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
