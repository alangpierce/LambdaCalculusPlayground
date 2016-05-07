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
import generateScreenExpressions from './generateScreenExpressions'
import SimpleComponent from './SimpleComponent'
import StatelessComponent from './StatelessComponent'
import store from './store'
import {
    newCanvasPoint,
    newUserLambda,
    newCanvasExpression,
} from './types'
import * as t from './types'

import type {
    ScreenExpression,
    ScreenPoint,
} from './types'

type TopLevelExpressionPropTypes = {
    screenExpr: ScreenExpression,
}
class TopLevelExpression
extends StatelessComponent<TopLevelExpressionPropTypes> {
    render() {
        const {expr, pos: {screenX, screenY}, isDragging} = this.props.screenExpr;
        const transform: Array<any> = [
            {translateX: screenX},
            {translateY: screenY},
        ];
        if (isDragging) {
            transform.push(
                {scaleX: 1.1},
                {scaleY: 1.1},
            )
        }

        return <View style={{
            position: 'absolute',
            transform,
        }}>
            <Expression expr={expr} />
        </View>;
    }
}

type PlaygroundCanvasProps = {
    screenExpressions: Immutable.List<ScreenExpression>,
};

class PlaygroundCanvasView extends SimpleComponent<PlaygroundCanvasProps, {}> {
    _responderMethods: any;

    componentWillMount() {
        DeviceEventEmitter.addListener('createLambda', (varName) => {
            store.dispatch(t.newAddExpression(
                newCanvasExpression(
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
        const {screenExpressions} = this.props;
        const nodes = screenExpressions.map((screenExpr) => {
            return <TopLevelExpression
                screenExpr={screenExpr}
                key={screenExpr.key}
            />;
        });
        return <View {...this._responderMethods} style={{
            backgroundColor: 'gray',
            flex: 1,
        }}>
            {nodes}
        </View>;
    }
}

const ConnectedPlaygroundCanvasView =
    connect((state) => ({
            screenExpressions: generateScreenExpressions(state),
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
