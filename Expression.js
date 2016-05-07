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
import {TrackedText, TrackedView} from './TrackedViews'

import type {
    DisplayExpression,
    DisplayLambda,
    DisplayFuncCall,
    DisplayVariable,
    DisplayReference,
    ViewKey,
} from './types'

import * as t from './types'

// This is the type returned by RelativeImageStub.
type AssetId = number;

type ExpressionPropTypes = {
    expr: DisplayExpression,
}
class Expression extends StatelessComponent<ExpressionPropTypes> {
    render() {
        console.log("called 2");
        const {expr} = this.props;
        return t.matchDisplayExpression(expr, {
            displayLambda: (expr) => <Lambda expr={expr} />,
            displayFuncCall: (expr) => <FuncCall expr={expr} />,
            displayVariable: (expr) => <Variable expr={expr} />,
            displayReference: (expr) => <Reference expr={expr} />,
        });
    }
}

type LambdaPropTypes = {
    expr: DisplayLambda,
}
class Lambda extends StatelessComponent<LambdaPropTypes> {
    render() {
        const {exprKey, varKey, emptyBodyKey, varName, body} = this.props.expr;
        var bodyElement;
        if (body != null) {
            bodyElement = <Expression expr={body} />;
        } else {
            bodyElement = <EmptyBody viewKey={emptyBodyKey} />;
        }
        return <ExprContainer viewKey={exprKey}>
            <ExprText>λ</ExprText>
            <ExprText viewKey={varKey}>{varName}</ExprText>
            <Bracket source={require('./img/left_bracket.png')}/>
            {bodyElement}
            <Bracket source={require('./img/right_bracket.png')}/>
        </ExprContainer>;
    }
}

type FuncCallPropTypes = {
    expr: DisplayFuncCall,
}
class FuncCall extends StatelessComponent<FuncCallPropTypes> {
    render() {
        const {exprKey, func, arg} = this.props.expr;
        return <ExprContainer viewKey={exprKey}>
            <Expression expr={func} />
            <Bracket source={require('./img/left_paren.png')}/>
            <Expression expr={arg} />
            <Bracket source={require('./img/right_paren.png')}/>
        </ExprContainer>;
    }
}

type VariablePropTypes = {
    expr: DisplayVariable,
}
class Variable extends StatelessComponent<VariablePropTypes> {
    render() {
        const {exprKey, varName} = this.props.expr;
        return <ExprContainer viewKey={exprKey}>
            <ExprText>{varName}</ExprText>
        </ExprContainer>;
    }
}

type ReferencePropTypes = {
    expr: DisplayReference,
}
class Reference extends StatelessComponent<ReferencePropTypes> {
    render() {
        const {exprKey, defName} = this.props.expr;
        return <ExprContainer viewKey={exprKey}>
            <ExprText>{defName}</ExprText>
        </ExprContainer>
    }
}

type ExprContainerPropTypes = {
    children: any,
    viewKey: ?ViewKey,
}
class ExprContainer extends StatelessComponent<ExprContainerPropTypes> {
    render() {
        const {children, viewKey} = this.props;
        return <TrackedView viewKey={viewKey} style={{
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
        </TrackedView>
    }
}

type ExprTextPropTypes = {
    children: any,
    viewKey: ?ViewKey,
}
class ExprText extends StatelessComponent<ExprTextPropTypes> {
    render() {
        const {children, viewKey} = this.props;
        return <TrackedText viewKey={viewKey} style={{
                paddingLeft: 6,
                paddingRight: 6,
                fontSize: 28,
                color: "black",
                textAlign: "center",
                textAlignVertical: "center",
            }}>
            {children}
        </TrackedText>;
    }
}

type EmptyBodyPropTypes = {
    viewKey: ?ViewKey,
}
class EmptyBody extends StatelessComponent<EmptyBodyPropTypes> {
    render() {
        return <TrackedView viewKey={this.props.viewKey} style={{
            backgroundColor: "#FFBBBB",
            padding: 2,
            width: 20,
            height: 40,
            margin: 1,
            alignSelf: "center",
        }}>
        </TrackedView>;
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