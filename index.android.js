/**
 * Top-level Android app.
 *
 * @flow
 */
'use strict';

import * as Immutable from 'immutable'
import React, {
    Image,
    Text,
    View,
    DeviceEventEmitter,
    NativeModules,
} from 'react-native';
import {connect, Provider} from 'react-redux';

import './DebugGlobals'
import Expression from './Expression'
import SimpleComponent from './SimpleComponent'
import StatelessComponent from './StatelessComponent'
import store from './store'
import {
    newCanvasPoint,
    newUserLambda,
    newScreenExpression,
} from './types'
import * as t from './types'

import type {
    CanvasPoint,
    UserExpression,
    ScreenExpression,
} from './types'

type TopLevelExpressionPropTypes = {
    exprId: number,
    expr: UserExpression,
    pos: CanvasPoint,
}
class TopLevelExpression
        extends StatelessComponent<TopLevelExpressionPropTypes> {
    render() {
        const {exprId, expr, pos: {canvasX, canvasY}} = this.props;
        return <View style={{
            left: canvasX,
            top: canvasY,
            position: 'absolute',
        }}>
            <Expression expr={expr}
                        path={t.newExprPath(exprId, new Immutable.List())}/>
        </View>;
    }
}

type PlaygroundCanvasProps = {
    screenExpressions: Immutable.Map<number, ScreenExpression>;
};

class PlaygroundCanvasView extends SimpleComponent<PlaygroundCanvasProps, {}> {
    componentWillMount() {
        DeviceEventEmitter.addListener('createLambda', (varName) => {
            store.dispatch(t.newAddExpression(
                newScreenExpression(
                    newUserLambda(varName, null),
                    newCanvasPoint(100, 100))
            ));
        });
    }

    render() {
        const {screenExpressions} = this.props;
        const exprNodes = Array.from(screenExpressions)
            .map(([exprId, screenExpression]) => {
                return <TopLevelExpression
                    exprId={exprId}
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
    connect(({screenExpressions}) => ({screenExpressions}))(PlaygroundCanvasView);

class PlaygroundCanvas extends SimpleComponent<{}, {}> {
    render() {
        return <Provider store={store}>
            <ConnectedPlaygroundCanvasView />
        </Provider>
    }
}

React.AppRegistry.registerComponent('PlaygroundCanvas', () => PlaygroundCanvas);
