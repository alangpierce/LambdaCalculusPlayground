/**
 * @flow
 */
'use strict';

import React from 'react';
import {
    Linking,
    NativeModules,
    ToolbarAndroid,
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