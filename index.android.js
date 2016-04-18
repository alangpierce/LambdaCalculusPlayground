'use strict';

import React, {
    Image,
    Text,
    View,
    DeviceEventEmitter,
} from 'react-native';

const TopLevelExpression = ({expr, x, y}) => {
    return <View style={{
            left: x,
            top: y,
            position: "absolute",
        }}>
        <Expression expr={expr}/>
    </View>;
};

const Expression = ({expr}) => {
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
};

const Lambda = ({expr}) => {
    var body;
    if (expr.body != null) {
        body = <Expression expr={expr.body}/>;
    } else {
        body = <EmptyBody/>;
    }
    return <ExprContainer>
        <ExprText>λ</ExprText>
        <ExprText>{expr.varName}</ExprText>
        <ExprText>[</ExprText>
        {body}
        <ExprText>]</ExprText>
    </ExprContainer>;
};

const FuncCall = ({expr}) => {
    return <ExprContainer>
        <Expression expr={expr.func}/>
        <Image source={require('image!drawable_left_paren')}
               style={{backgroundColor: "white", tintColor: "black", overlayColor: "green"}}/>
        <Expression expr={expr.arg}/>
        <Image source={require('image!drawable_right_paren')}
               style={{backgroundColor: "black"}}/>
    </ExprContainer>;
};

const Variable = ({expr}) => {
    return <ExprContainer>
        <ExprText>{expr.varName}</ExprText>
    </ExprContainer>
};

const Reference = ({expr}) => {
    return <ExprContainer>
        <ExprText>{expr.defName}</ExprText>
    </ExprContainer>
};

const ExprContainer = ({children}) => {
    return <View style={{
            flexDirection: 'row',
            backgroundColor: "white",
            elevation: 5,
            padding: 2,
            justifyContent: "center",
        }}>
        {children}
    </View>
};

const ExprText = ({children}) => {
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
};

const EmptyBody = () => {
    return <View style={{
            backgroundColor: "#FFBBBB",
            padding: 2,
            width: 20,
        }}>
    </View>
};

class PlaygroundCanvas extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            screenExpressions: [{
                x: 50,
                y: 50,
                expr: {
                    type: 'lambda',
                    varName: 'x',
                    body: {
                        type: "funcCall",
                        func: {
                            type: "variable",
                            varName: "x",
                        },
                        arg: {
                            type: "variable",
                            varName: "x",
                        },
                    },
                },
                exprId: 1,
            }]
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
var styles = React.StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
    },
    hello: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
});

React.AppRegistry.registerComponent('PlaygroundCanvas', () => PlaygroundCanvas);
