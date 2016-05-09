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
import ExecuteButton from './ExecuteButton'
import Expression from './Expression'
import generateDisplayState from './generateDisplayState'
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
    DisplayState,
    MeasureRequest,
    ScreenExpression,
    ScreenPoint,
} from './types'

type TopLevelExpressionPropTypes = {
    screenExpr: ScreenExpression,
}
type TopLevelExpressionState = {
    measuredSize: ?{
        width: number,
        height: number,
    }
}
class TopLevelExpression
        extends SimpleComponent<TopLevelExpressionPropTypes, TopLevelExpressionState> {
    constructor(props) {
        super(props);
        this.state = {
            measuredSize: null,
        }
    }

    handleLayout({nativeEvent: {layout: {width, height}}}) {
        this.setState({
            measuredSize: {width, height},
        });
    }

    render() {
        const {
            expr, pos: {screenX, screenY}, isDragging, executeHandler
        } = this.props.screenExpr;
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

        let executeButton = null;
        if (executeHandler && this.state.measuredSize != null) {
            const {width, height} = this.state.measuredSize;
            executeButton = <ExecuteButton onPress={executeHandler} style={{
                elevation: 5,
                position: 'absolute',
                transform: [
                    {translateX: screenX + width - 20 + 8},
                    {translateY: screenY + height - 20 + 8},
                ],
            }}/>
        }

        return <View>
            <View
                onLayout={this.handleLayout.bind(this)}
                style={{
                    position: 'absolute',
                    transform,
            }}>
                <Expression expr={expr} />
            </View>
            {executeButton}
        </View>;
    }
}

/**
 * Component used for taking measurements of expressions so we know where to
 * position them when they're placed for real.
 */
type MeasureHandlerPropTypes = {
    measureRequest: MeasureRequest,
}
class MeasureHandler extends StatelessComponent<MeasureHandlerPropTypes> {
    handleLayout({nativeEvent: {layout: {width, height}}}) {
        this.props.measureRequest.resultHandler(width, height);
    }

    render() {
        const {expr} = this.props.measureRequest;
        return <View onLayout={this.handleLayout.bind(this)} style={{
            position: 'absolute',
            opacity: 0,
        }}>
            <Expression expr={expr} />
        </View>;
    }
}

type PlaygroundCanvasProps = {
    displayState: DisplayState,
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
        DeviceEventEmitter.addListener('createDefinition', (defName) => {
            console.log('Created definition ' + defName);
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
        const {screenExpressions, measureRequests} = this.props.displayState;
        const measureHandlers = measureRequests.map((measureRequest, i) =>
            <MeasureHandler
                measureRequest={measureRequest}
                key={"measure" + i} />);
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
            {measureHandlers}
            {nodes}
        </View>;
    }
}

const ConnectedPlaygroundCanvasView =
    connect((state) => ({
            displayState: generateDisplayState(state),
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
