/**
 * @flow
 */

import React from 'react';
import {
    DrawerLayoutAndroid,
    ScrollView,
    Text,
    View,
} from 'react-native';

import {Expression} from './Expression';
import SimpleComponent from './SimpleComponent';
import * as t from './types';
import {PaletteDisplayState} from './types';
import {TOOLBAR_HEIGHT} from './toolbar';


type PalettePropTypes = {
    displayState: PaletteDisplayState,
};
export default class Palette extends SimpleComponent<PalettePropTypes, {}> {
    render() {
        const {activePalette, lambdas, definitions} = this.props.displayState;
        if (activePalette === 'none') {
            return null;
        }
        let viewContents;
        if (activePalette === 'lambda') {
            viewContents = lambdas.map(varName =>
                <View key={`lambda-${varName}`} style={{margin: 10}}>
                    <Expression expr={t.DisplayLambda.make(
                        t.PaletteLambdaKey.make(varName),
                        false,
                        null,
                        null,
                        false,
                        varName,
                        null,
                    )} />
                </View>
            );
        } else {
            viewContents = [<Text key='definitionText' style={{
                fontSize: 24,
                margin: 16,
                color: 'black',
                fontWeight: 'bold',
                textDecorationLine: 'underline',
            }}>
                Definitions
            </Text>];
            viewContents.push(definitions.map(defName =>
                <View key={`def-${defName}`} style={{margin: 10}}>
                    <Expression expr={t.DisplayReference.make(
                        t.PaletteReferenceKey.make(defName),
                        false,
                        false,
                        defName,
                    )} />
                </View>
            ));
        }

        return <ScrollView
            onStartShouldSetResponder={() => {
                console.log('got here');
                return true;
            }}
            style={{
                backgroundColor: '#E6CEA3',
                position: 'absolute',
                right: 0,
                top: TOOLBAR_HEIGHT,
                bottom: 0,
            }}
            contentContainerStyle={{
                alignItems: 'center',
            }}
        >
            {viewContents}
        </ScrollView>;
    }
}