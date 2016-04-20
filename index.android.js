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

import Expression from './Expression'
import StatelessComponent from './StatelessComponent'
import SimpleComponent from './SimpleComponent'

import type {ExpressionType} from './ExpressionType'

type TopLevelExpressionPropTypes = {
    x: number,
    y: number,
    expr: ExpressionType,
}
class TopLevelExpression extends StatelessComponent<TopLevelExpressionPropTypes> {
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

type ScreenExpression = {
    expr: ExpressionType,
    x: number,
    y: number,
    exprId: number,
};

type PlaygroundCanvasProps = {};
type PlaygroundCanvasState = {
    screenExpressions: Array<ScreenExpression>;
};

class PlaygroundCanvas extends SimpleComponent<
        PlaygroundCanvasProps, PlaygroundCanvasState> {
    constructor(props: PlaygroundCanvasProps) {
        super(props);
        this.state = {
            screenExpressions: [],
        };
    }

    componentWillMount() {
        DeviceEventEmitter.addListener('refreshState', (state) => {
            this.setState(state);
        })
    }

    render() {
        const {screenExpressions} = this.state;
        const exprNodes = screenExpressions.map((screenExpression) => {
            return <TopLevelExpression
                expr={screenExpression.expr}
                x={screenExpression.x}
                y={screenExpression.y}
                key={screenExpression.exprId}
            />
        });
        return <View>
            {exprNodes}
        </View>;
    }
}

React.AppRegistry.registerComponent('PlaygroundCanvas', () => PlaygroundCanvas);
