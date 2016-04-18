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
        <Bracket source={require('./img/left_bracket.png')}/>
        {body}
        <Bracket source={require('./img/right_bracket.png')}/>
    </ExprContainer>;
};

const FuncCall = ({expr}) => {
    return <ExprContainer>
        <Expression expr={expr.func}/>
        <Bracket source={require('./img/left_paren.png')}/>
        <Expression expr={expr.arg}/>
        <Bracket source={require('./img/right_paren.png')}/>
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

const Bracket = ({source}) => {
    // We want the width of the bracket to be fixed, but for the height to match
    // the available space. We can accomplish this by wrapping it in a vertical
    // flexbox and setting flex to 1, then setting the height of the image
    // itself to 0. This causes the flexbox to use the enclosing height, and the
    // image is stretched to 100%.
    return <View style={{flexDirection: "column", alignSelf: "stretch"}}>
        <Image source={source} style={{
            width: 6,
            height: 0,
            resizeMode: "stretch",
            flex: 1,
        }}/>
    </View>;
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

React.AppRegistry.registerComponent('PlaygroundCanvas', () => PlaygroundCanvas);
