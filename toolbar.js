/**
 * @flow
 */
'use strict';

import React from 'react';
import {
    AppRegistry,
    DeviceEventEmitter,
    Image,
    Linking,
    NativeModules,
    PanResponder,
    Text,
    ToolbarAndroid,
    View,
} from 'react-native';
import DialogAndroid from 'react-native-dialogs';

import StatelessComponent from './StatelessComponent';
import store from './store';
import * as t from './types';
import {IList} from './types-collections';

export const TOOLBAR_HEIGHT = 56;

type ToolbarProps = {
    definitionNames: IList<string>,
    recognizeNumbers: boolean,
}
export class Toolbar extends StatelessComponent<ToolbarProps> {
    actions: Array<any>;

    constructor() {
        super();
        this.actions = [
            {
                title: 'Lambda palette',
                icon: require('./img/lambda.png'),
                show: 'always',
                onPress() {
                    store.dispatch(t.ToggleLambdaPalette.make());
                }
            },
            {
                title: 'Definition palette',
                icon: require('./img/definition.png'),
                show: 'always',
                onPress() {
                    store.dispatch(t.ToggleDefinitionPalette.make());
                }
            },
            {
                title: 'Delete definition',
                onPress: this.handleDeleteDefinition.bind(this),
            },
            {
                title: 'Automatically recognize numbers',
                onPress() {
                    // TODO.
                }
            },
            {
                title: 'View demo video',
                onPress() {
                    // TODO: Show the video in the app itself instead of going to
                    // YouTube.
                    Linking.openURL('https://www.youtube.com/watch?v=0OzpqDDniDs');
                }
            },
            {
                title: 'Create lambda',
                onPress: this.handleCreateLambda.bind(this),
            },
            {
                title: 'Create definition',
                onPress: this.handleCreateDefinition.bind(this),
            },
            {
                title: 'Dev: Show options',
                onPress() {
                    NativeModules.DeveloperSupportModule.showDevOptionsDialog();
                }
            },
            {
                title: 'Dev: Refresh JS',
                onPress() {
                    NativeModules.DeveloperSupportModule.reloadJs();
                }
            },
        ];
    }

    handleActionSelected(position: number) {
        this.actions[position].onPress();
    }

    handleDeleteDefinition() {
        const dialog = new DialogAndroid();
        dialog.set({
            title: 'Choose a definition to delete',
            items: this.props.definitionNames.toArray(),
            itemsCallback: (index, item) => {
                store.dispatch(t.DeleteDefinition.make(item));
            },
            negativeText: 'Cancel'
        });
        dialog.show();
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
        return <ToolbarAndroid
            title="Lambda Calculus Playground"
            actions={this.actions}
            onActionSelected={this.handleActionSelected.bind(this)}
            onStartShouldSetResponderCapture={() => true}
            style={{
                elevation: 4,
                height: TOOLBAR_HEIGHT,
                backgroundColor: '#e9eaed',
                position: 'absolute',
                left: 0,
                right: 0,
                top: 0,
            }}
        />
    }
}