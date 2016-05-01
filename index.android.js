/**
 * Top-level Android app.
 *
 * @flow
 */
'use strict';

import * as Immutable from 'immutable'
import React, {
    DeviceEventEmitter,
    Image,
    NativeModules,
    PanResponder,
    Text,
    View,
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
    ScreenPoint,
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

    getResponderMethods() {
        let lastTouches: Immutable.Map<number, ScreenPoint> =
            new Immutable.Map();
        const processEvent = ({nativeEvent: {touches}}) => {
            const newTouches = new Immutable.Map(touches.map((touch) =>
                [touch.identifier, t.newScreenPoint(touch.pageX, touch.pageY)]
            ));

            const fingers = Immutable.Set(lastTouches.keys())
                .union(newTouches.keys());
            fingers.forEach((fingerId) => {
                const beforePoint = lastTouches.get(fingerId);
                const afterPoint = newTouches.get(fingerId);
                if (beforePoint && afterPoint) {
                    store.dispatch(t.newFingerMove(fingerId, afterPoint));
                } else if (afterPoint) {
                    store.dispatch(t.newFingerDown(fingerId, afterPoint));
                } else if (beforePoint) {
                    store.dispatch(t.newFingerUp(fingerId, beforePoint));
                }
            });

            lastTouches = newTouches;
        };

        // The different callbacks don't let us distinguish individual fingers;
        // we need to look at the event data directly.
        return {
            onStartShouldSetResponder: (event) => true,
            onResponderGrant: processEvent,
            onResponderMove: processEvent,
            onResponderRelease: processEvent,
            onResponderTerminate: processEvent,
        }
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
        return <View {...this.getResponderMethods()} style={{
            backgroundColor: 'gray',
            flex: 1,
        }}>
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
