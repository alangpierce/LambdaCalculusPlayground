/**
 * @flow
 */
'use strict';

import React from 'react';
import {
    ToastAndroid,
    View,
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
                    const error = checkDefNameErrors(varName);
                    if (error != null) {
                        ToastAndroid.show(error, ToastAndroid.LONG);
                    } else {
                        store.dispatch(t.AddExpression.make(
                            t.CanvasExpression.make(
                                t.UserLambda.make(varName, null),
                                t.CanvasPoint.make(100, 100))
                        ));
                    }
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
                    const error = checkDefNameErrors(defName);
                    if (error != null) {
                        ToastAndroid.show(error, ToastAndroid.LONG)
                    } else {
                        store.dispatch(t.PlaceDefinition.make(
                            defName,
                            t.ScreenPoint.make(100, 100),
                        ));
                    }
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

const isLowerCase = (letter: string): boolean => {
    return letter !== letter.toUpperCase();
};

/**
 * Returns an error message if the variable name is invalid, or null if the name
 * is valid.
 */
const checkVarNameErrors = (varName: string): ?string => {
    if (varName.length > 8) {
        return 'Variable names can only be up to 8 letters long.';
    }
    for (const c of varName) {
        if (!isLowerCase(c)) {
            return 'Variable names can only contain lower-case letters.';
        }
    }
    return null;
};

/**
 * Returns an error message if the definition name is invalid, or null if the
 * name is valid.
 */
const checkDefNameErrors = (defName: string): ?string => {
    if (defName.length > 8) {
        return 'Definition names can only be up to 8 letters long.';
    }
    for (const c of defName) {
        if (isLowerCase(c)) {
            return 'Definition names can only contain capital letters and symbols.';
        }
    }
    return null;
};