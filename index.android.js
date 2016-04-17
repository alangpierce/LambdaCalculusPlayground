'use strict';

import React, {
    Text,
    View,
    DeviceEventEmitter,
} from 'react-native';

const TopLevelExpression = ({expr, x, y}) => {
    return <View style={{
            left: x,
            top: y,
            // TODO: This looks correct visually, but the actual view bounds are
            // 100% wide. Maybe that could cause problems later?
            alignSelf: "flex-start",
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
    }
};

const Lambda = ({expr}) => {
    return <ExprContainer>
        <ExprText>λ</ExprText>
        <ExprText>{expr.varName}</ExprText>
        <ExprText>[</ExprText>
        <Expression expr={expr.body}/>
        <ExprText>]</ExprText>
    </ExprContainer>;
};

const FuncCall = ({expr}) => {
    return <ExprContainer>
        <Expression expr={expr.func}/>
        <ExprText>(</ExprText>
        <Expression expr={expr.arg}/>
        <ExprText>)</ExprText>
    </ExprContainer>
};

const Variable = ({expr}) => {
    return <ExprContainer>
        <ExprText>{expr.varName}</ExprText>
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

class PlaygroundCanvas extends React.Component {
    constructor(props) {
        super(props);
        console.log("Called constructor");
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
                id: 1,
            }]
        };
    }

    componentWillMount() {
        DeviceEventEmitter.addListener('refreshState', (e) => {
            console.log("Got event " + e);
        })
    }

    render() {
        console.log("State is " + this.state);
        const {screenExpressions} = this.state;
        const exprNodes = screenExpressions.map((screenExpression) => {
            return <TopLevelExpression
                expr={screenExpression.expr}
                x={screenExpression.x}
                y={screenExpression.y}
                key={screenExpression.id}
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
