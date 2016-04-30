/**
 * View code for expressions.
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
import {registerView} from './ViewTracker'

import type {
    ExprPath,
    PathComponent,
    UserExpression,
    UserLambda,
    UserFuncCall,
    UserVariable,
    UserReference
} from './types'

import * as t from './types'

// This is the type returned by RelativeImageStub.
type AssetId = number;

const stepPath = (exprPath: ExprPath, component: PathComponent): ExprPath => {
    return exprPath.withPathSteps(exprPath.pathSteps.push(component));
};

type ExpressionPropTypes = {
    expr: UserExpression,
    path: ExprPath,
}
class Expression extends StatelessComponent<ExpressionPropTypes> {
    render() {
        const {expr, path} = this.props;
        return t.matchUserExpression(expr, {
            userLambda: (expr) => <Lambda expr={expr} path={path} />,
            userFuncCall: (expr) => <FuncCall expr={expr} path={path} />,
            userVariable: (expr) => <Variable expr={expr} path={path} />,
            userReference: (expr) => <Reference expr={expr} path={path} />,
        });
    }
}

type LambdaPropTypes = {
    expr: UserLambda,
    path: ExprPath,
}
class Lambda extends StatelessComponent<LambdaPropTypes> {
    render() {
        const {expr, path} = this.props;
        var body;
        if (expr.body != null) {
            body = <Expression expr={expr.body}
                               path={stepPath(path, 'body')} />;
        } else {
            body = <EmptyBody path={stepPath(path, 'body')} />;
        }
        return <ExprContainer path={path}>
            <ExprText>λ</ExprText>
            <ExprText>{expr.varName}</ExprText>
            <Bracket source={require('./img/left_bracket.png')}/>
            {body}
            <Bracket source={require('./img/right_bracket.png')}/>
        </ExprContainer>;
    }
}

type FuncCallPropTypes = {
    expr: UserFuncCall,
    path: ExprPath,
}
class FuncCall extends StatelessComponent<FuncCallPropTypes> {
    render() {
        const {expr, path} = this.props;
        return <ExprContainer path={path}>
            <Expression expr={expr.func} path={stepPath(path, 'func')} />
            <Bracket source={require('./img/left_paren.png')}/>
            <Expression expr={expr.arg} path={stepPath(path, 'arg')} />
            <Bracket source={require('./img/right_paren.png')}/>
        </ExprContainer>;
    }
}

type VariablePropTypes = {
    expr: UserVariable,
    path: ExprPath,
}
class Variable extends StatelessComponent<VariablePropTypes> {
    render() {
        const {expr, path} = this.props;
        return <ExprContainer path={path}>
            <ExprText>{expr.varName}</ExprText>
        </ExprContainer>
    }
}

type ReferencePropTypes = {
    expr: UserReference,
    path: ExprPath,
}
class Reference extends StatelessComponent<ReferencePropTypes> {
    render() {
        const {expr, path} = this.props;
        return <ExprContainer path={path}>
            <ExprText>{expr.defName}</ExprText>
        </ExprContainer>
    }
}

type ExprContainerPropTypes = {
    children: any,
    path: ExprPath,
}
class ExprContainer extends StatelessComponent<ExprContainerPropTypes> {
    componentDidMount() {
        registerView(this.props.path, this.refs.viewRef);
    }

    render() {
        const {children} = this.props;
        return <View ref="viewRef" style={{
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

type EmptyBodyPropTypes = {
    path: ExprPath,
}
class EmptyBody extends StatelessComponent<EmptyBodyPropTypes> {
    componentDidMount() {
        registerView(this.props.path, this.refs.viewRef);
    }

    render() {
        const {path} = this.props;
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

export default Expression;