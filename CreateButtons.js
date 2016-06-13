/**
 * @flow
 */
'use strict';

import React from 'react';
import {
    View
} from 'react-native';
import DialogAndroid from 'react-native-dialogs';

import FloatingActionButton from './FloatingActionButton';
import StatelessComponent from './StatelessComponent';
import store from './store';
import * as t from './types';

type CreateButtonsProps = {
}
export default class CreateButtons extends StatelessComponent<CreateButtonsProps> {
    shouldComponentUpdate() {
        return false;
    }

    handleCreateLambda() {
        const dialog = new DialogAndroid();
        dialog.set({
            title: 'Choose a variable name',
            positiveText: 'OK',
            negativeText: 'Cancel',
            input: {
                allowEmptyInput: false,
                callback: (varName) => {
                    // TODO: Validate name.
                    store.dispatch(t.AddExpression.make(
                        t.CanvasExpression.make(
                            t.UserLambda.make(varName, null),
                            t.CanvasPoint.make(100, 100))
                    ));
                },
            }
        });
        dialog.show();
    }

    handleCreateDefinition() {
        const dialog = new DialogAndroid();
        dialog.set({
            title: 'Create or show definition',
            positiveText: 'OK',
            negativeText: 'Cancel',
            input: {
                allowEmptyInput: false,
                type: 0x00001000, // InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                callback: (defName) => {
                    // TODO: Validate name.
                    store.dispatch(t.PlaceDefinition.make(
                        defName,
                        t.ScreenPoint.make(100, 100),
                    ));
                },
            }
        });
        dialog.show();
    }

    render() {
        return <View
            style={{
                flexDirection: 'row',
                position: 'absolute',
                right: 0,
                bottom: 0,
            }}
        >
            <FloatingActionButton
                onPress={this.handleCreateLambda.bind(this)}
                source={require('./img/lambda.png')}
                style={{
                    marginRight: 24,
                    marginBottom: 24,
                }}
            />
            <FloatingActionButton
                onPress={this.handleCreateDefinition.bind(this)}
                source={require('./img/definition.png')}
                style={{
                    marginRight: 24,
                    marginBottom: 24,
                }}
            />
        </View>;
    }
};