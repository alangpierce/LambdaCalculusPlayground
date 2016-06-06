/**
 * Top-level Android app.
 *
 * @flow
 */
'use strict';

import React from 'react';
import {
    AppRegistry,
    DeviceEventEmitter,
    Image,
    Linking,
    NativeModules,
    PanResponder,
    Text,
    ToolbarAndroid,
    View,
} from 'react-native';
import {connect, Provider} from 'react-redux';

import './DebugGlobals';
import ExecuteButton from './ExecuteButton';
import {Definition, Expression} from './Expression';
import generateDisplayState from './generateDisplayState';
import Palette from './Palette';
import SimpleComponent from './SimpleComponent';
import StatelessComponent from './StatelessComponent';
import store from './store';
import * as t from './types';

import type {
    DisplayState,
    MeasureRequest,
    ScreenDefinition,
    ScreenExpression,
    ScreenPoint,
} from './types'
import {IMap, ISet} from './types-collections'

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

type TopLevelDefinitionPropTypes = {
    screenDef: ScreenDefinition,
}
class TopLevelDefinition extends StatelessComponent<TopLevelDefinitionPropTypes> {
    render() {
        const {
            defName, expr, pos: {screenX, screenY}, defKey, refKey,
            emptyBodyKey, shouldHighlightEmptyBody, isDragging
        } = this.props.screenDef;
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
            <Definition
                defName={defName}
                defKey={defKey}
                emptyBodyKey={emptyBodyKey}
                refKey={refKey}
                shouldHighlightEmptyBody={shouldHighlightEmptyBody}
                expr={expr} />
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

type ToolbarProps = {
    recognizeNumbers: boolean,
}
class Toolbar extends StatelessComponent<ToolbarProps> {
    constructor() {
        super();
    }

    static actions = [
        {
            title: 'Lambda palette',
            onPress() {
                store.dispatch(t.ToggleLambdaPalette.make());
            }
        },
        {
            title: 'Definition palette',
            onPress() {
                store.dispatch(t.ToggleDefinitionPalette.make());
            }
        },
        {
            title: 'Delete definition',
            onPress() {
                // TODO.
            }
        },
        {
            title: 'Automatically recognize numbers',
            onPress() {
                // TODO.
            }
        },
        {
            title: 'View demo video',
            onPress() {
                // TODO: Show the video in the app itself instead of going to
                // YouTube.
                Linking.openURL('https://www.youtube.com/watch?v=0OzpqDDniDs');
            }
        },
        {
            title: 'Create lambda',
            onPress() {
                // TODO.
            }
        },
        {
            title: 'Create definition',
            onPress() {
                // TODO.
            }
        },
        {
            title: 'Dev: Show options',
            onPress() {
                NativeModules.DeveloperSupportModule.showDevOptionsDialog();
            }
        },
        {
            title: 'Dev: Refresh JS',
            onPress() {
                NativeModules.DeveloperSupportModule.reloadJs();
            }
        },
    ];

    handleActionSelected(position) {
        Toolbar.actions[position].onPress();
    }

    render() {
        return <ToolbarAndroid
            title="Lambda Calculus Playground"
            actions={Toolbar.actions}
            onActionSelected={this.handleActionSelected.bind(this)}
            style={{
            elevation: 4,
            height: 56,
            backgroundColor: '#e9eaed',
            position: 'absolute',
            left: 0,
            right: 0,
            top: 0,
        }}
        />
    }
};

type PlaygroundCanvasProps = {
    displayState: DisplayState,
};
class PlaygroundCanvasView extends SimpleComponent<PlaygroundCanvasProps, {}> {
    _responderMethods: any;

    componentWillMount() {
        DeviceEventEmitter.addListener('createLambda', (varName) => {
            store.dispatch(t.AddExpression.make(
                t.CanvasExpression.make(
                    t.UserLambda.make(varName, null),
                    t.CanvasPoint.make(100, 100))
            ));
        });
        DeviceEventEmitter.addListener('createDefinition', (defName) => {
            store.dispatch(t.PlaceDefinition.make(
                defName,
                t.ScreenPoint.make(100, 100),
            ));
        });
        DeviceEventEmitter.addListener('toggleLambdaPalette', () => {
            store.dispatch(t.ToggleLambdaPalette.make());
        });
        DeviceEventEmitter.addListener('toggleDefinitionPalette', () => {
            store.dispatch(t.ToggleDefinitionPalette.make());
        });
        DeviceEventEmitter.addListener('viewDemoVideo', () => {
            // TODO: Show the video in the app itself instead of going to YouTube.
            Linking.openURL('https://www.youtube.com/watch?v=0OzpqDDniDs');
        });
        this._responderMethods = this.getResponderMethods();
    }

    getResponderMethods() {
        let lastTouches: IMap<number, ScreenPoint> = IMap.make();
        const processEvent = ({nativeEvent: {touches}}) => {
            const newTouches = IMap.make(touches.map((touch) =>
                [touch.identifier, t.ScreenPoint.make(touch.pageX, touch.pageY)]
            ));

            const fingers = ISet.make(lastTouches.keys())
                .union(newTouches.keys());
            for (const fingerId of fingers) {
                const beforePoint = lastTouches.get(fingerId);
                const afterPoint = newTouches.get(fingerId);
                if (beforePoint && afterPoint) {
                    store.dispatch(t.FingerMove.make(fingerId, afterPoint));
                } else if (afterPoint) {
                    store.dispatch(t.FingerDown.make(fingerId, afterPoint));
                } else if (beforePoint) {
                    store.dispatch(t.FingerUp.make(fingerId, beforePoint));
                }
            }
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
        const {
            screenExpressions, screenDefinitions, paletteState, measureRequests
        } = this.props.displayState;
        const measureHandlers = measureRequests.map((measureRequest, i) =>
            <MeasureHandler
                measureRequest={measureRequest}
                key={"measure" + i} />);
        const exprNodes = screenExpressions.map((screenExpr) =>
            <TopLevelExpression
                screenExpr={screenExpr}
                key={screenExpr.key}
            />
        );
        const definitionNodes = screenDefinitions.map((screenDef) =>
            <TopLevelDefinition
                screenDef={screenDef}
                key={screenDef.key}
            />
        );

        return <View {...this._responderMethods} style={{
                backgroundColor: '#AAAAAA',
                flex: 1,
            }}>
            <Toolbar />
            {measureHandlers}
            {exprNodes}
            {definitionNodes}
            <Palette displayState={paletteState} />
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

AppRegistry.registerComponent('PlaygroundCanvas', () => PlaygroundCanvas);
