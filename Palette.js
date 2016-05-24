/**
 * @flow
 */

import React, {
    DrawerLayoutAndroid,
    View,
} from 'react-native';

import {Expression} from './Expression';
import SimpleComponent from './SimpleComponent';
import * as t from './types';

export default class Palette extends SimpleComponent<{}, {}> {
    render() {
        const vars = ['x', 'y', 't', 'f', 'b', 's', 'z', 'n', 'm'];
        const displayExprs = vars.map(varName => t.DisplayLambda.make(
            null,
            false,
            null,
            null,
            false,
            varName,
            null,
        ));

        return <View style={{
            backgroundColor: '#E6CEA3',
            position: 'absolute',
            right: 0,
            alignItems: 'center',
        }}>
            {displayExprs.map(expr => <View style={{margin: 10}}>
                <Expression expr={expr} />
            </View>)}
        </View>;
    }
}