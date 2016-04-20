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

import StatelessComponent from './StatelessComponent'
import SimpleComponent from './SimpleComponent'

import type {
    ExpressionType,
    LambdaType,
    FuncCallType,
    VariableType,
    ReferenceType
} from './ExpressionType'

// This is the type returned by RelativeImageStub.
type AssetId = number;

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

type ExpressionPropTypes = {
    expr: ExpressionType,
}
class Expression extends StatelessComponent<ExpressionPropTypes> {
    render() {
        const {expr} = this.props;
        switch (expr.type) {
            case 'lambda':
                return <Lambda expr={expr}/>;
            case 'funcCall':
                return <FuncCall expr={expr}/>;
            case 'variable':
                return <Variable expr={expr}/>;
            case 'reference':
                return <Reference expr={expr}/>;
        }
    }
}

type LambdaPropTypes = {
    expr: LambdaType,
}
class Lambda extends StatelessComponent<LambdaPropTypes> {
    render() {
        const {expr} = this.props;
        var body;
        if (expr.body != null) {
            body = <Expression expr={expr.body}/>;
        } else {
            body = <EmptyBody/>;
        }
        return <ExprContainer>
            <ExprText>λ</ExprText>
            <ExprText>{expr.varName}</ExprText>
            <Bracket source={require('./img/left_bracket.png')}/>
            {body}
            <Bracket source={require('./img/right_bracket.png')}/>
        </ExprContainer>;
    }
}

type FuncCallPropTypes = {
    expr: FuncCallType,
}
class FuncCall extends StatelessComponent<FuncCallPropTypes> {
    render() {
        const {expr} = this.props;
        return <ExprContainer>
            <Expression expr={expr.func}/>
            <Bracket source={require('./img/left_paren.png')}/>
            <Expression expr={expr.arg}/>
            <Bracket source={require('./img/right_paren.png')}/>
        </ExprContainer>;
    }
}

type VariablePropTypes = {
    expr: VariableType,
}
class Variable extends StatelessComponent<VariablePropTypes> {
    render() {
        const {expr} = this.props;
        return <ExprContainer>
            <ExprText>{expr.varName}</ExprText>
        </ExprContainer>
    }
}

type ReferencePropTypes = {
    expr: ReferenceType,
}
class Reference extends StatelessComponent<ReferencePropTypes> {
    render() {
        const {expr} = this.props;
        return <ExprContainer>
            <ExprText>{expr.defName}</ExprText>
        </ExprContainer>
    }
}

type ExprContainerPropTypes = {
    children: any,
}
class ExprContainer extends StatelessComponent<ExprContainerPropTypes> {
    render() {
        const {children} = this.props;
        return <View style={{
            flexDirection: 'row',
            backgroundColor: "white",
            elevation: 5,
            paddingTop: 1,
            alignItems: "center",
            paddingBottom: 1,
            paddingLeft: 2,
            paddingRight: 2,
            marginTop: 1,
            marginBottom: 1,
            marginLeft: 2,
            marginRight: 2,
        }}>
            {children}
        </View>
    }
}

type ExprTextPropTypes = {
    children: any,
}
class ExprText extends StatelessComponent<ExprTextPropTypes> {
    render() {
        const {children} = this.props;
        return <Text style={{
                paddingLeft: 6,
                paddingRight: 6,
                fontSize: 28,
                color: "black",
                textAlign: "center",
                textAlignVertical: "center",
            }}>
            {children}
        </Text>;
    }
}

class EmptyBody extends StatelessComponent<{}> {
    render() {
        return <View style={{
            backgroundColor: "#FFBBBB",
            padding: 2,
            width: 20,
            height: 40,
            margin: 1,
            alignSelf: "center",
        }}>
        </View>;
    }
}

type BracketPropTypes = {
    source: AssetId,
}
class Bracket extends StatelessComponent<BracketPropTypes> {
    render() {
        // We want the width of the bracket to be fixed, but for the height to
        // match the available space. We can accomplish this by wrapping it in a
        // vertical flexbox and setting flex to 1, then setting the height of
        // the image itself to 0. This causes the flexbox to use the enclosing
        // height, and the image is stretched to 100%.
        const {source} = this.props;
        return <View style={{flexDirection: "column", alignSelf: "stretch"}}>
            <Image source={source} style={{
            width: 6,
            height: 0,
            resizeMode: "stretch",
            flex: 1,
            marginTop: 0.5,
            marginBottom: 0.5,
        }}/>
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
