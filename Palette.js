/**
 * @flow
 */

import React, {
    DrawerLayoutAndroid,
    View,
} from 'react-native';

import {PALLETE_VAR_NAMES} from './constants';
import {Expression} from './Expression';
import SimpleComponent from './SimpleComponent';
import * as t from './types';

export default class Palette extends SimpleComponent<{}, {}> {
    render() {
        const displayExprs = PALLETE_VAR_NAMES.map(varName => t.DisplayLambda.make(
            t.PaletteLambdaKey.make(varName),
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
                <Expression expr={expr}/>
            </View>)}
        </View>;
    }
}