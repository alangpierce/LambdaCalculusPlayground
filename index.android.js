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
    DragData,
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
            position: 'absolute',
            transform: [
                {translateX: canvasX},
                {translateY: canvasY},
            ],
        }}>
            <Expression expr={expr}
                        path={t.newExprPath(exprId, new Immutable.List())}/>
        </View>;
    }
}

type DraggedExpressionPropTypes = {
    expr: UserExpression,
    pos: CanvasPoint,
}
class DraggedExpression
extends StatelessComponent<DraggedExpressionPropTypes> {
    render() {
        const {expr, pos: {canvasX, canvasY}} = this.props;
        return <View style={{
            position: 'absolute',
            transform: [
                {translateX: canvasX},
                {translateY: canvasY},
                {scaleX: 1.1},
                {scaleY: 1.1},
            ],

        }}>
            <Expression expr={expr}/>
        </View>;
    }
}

type PlaygroundCanvasProps = {
    screenExpressions: Immutable.Map<number, ScreenExpression>,
    activeDrags: Immutable.Map<number, DragData>
};

class PlaygroundCanvasView extends SimpleComponent<PlaygroundCanvasProps, {}> {
    _responderMethods: any;

    componentWillMount() {
        DeviceEventEmitter.addListener('createLambda', (varName) => {
            store.dispatch(t.newAddExpression(
                newScreenExpression(
                    newUserLambda(varName, null),
                    newCanvasPoint(100, 100))
            ));
        });
        this._responderMethods = this.getResponderMethods();
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
        const {screenExpressions, activeDrags} = this.props;
        const exprNodes = Array.from(screenExpressions)
            .map(([exprId, screenExpression]) => {
                return <TopLevelExpression
                    exprId={exprId}
                    expr={screenExpression.expr}
                    pos={screenExpression.pos}
                    key={"expr" + exprId}
                />
            });
        const dragNodes = Array.from(activeDrags)
            .map(([fingerId, dragData]) => {
                return <DraggedExpression
                    expr={dragData.screenExpr.expr}
                    pos={dragData.screenExpr.pos}
                    key={"drag" + fingerId}
                />;
            });
        return <View {...this._responderMethods} style={{
            backgroundColor: 'gray',
            flex: 1,
        }}>
            {exprNodes}
            {dragNodes}
        </View>;
    }
}

const ConnectedPlaygroundCanvasView =
    connect(
        ({screenExpressions, activeDrags}) => ({
            screenExpressions,
            activeDrags
        })
    )(PlaygroundCanvasView);

class PlaygroundCanvas extends SimpleComponent<{}, {}> {
    render() {
        return <Provider store={store}>
            <ConnectedPlaygroundCanvasView />
        </Provider>
    }
}

React.AppRegistry.registerComponent('PlaygroundCanvas', () => PlaygroundCanvas);
