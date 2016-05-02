/**
 * @flow
 */

import React, {
    Component,
    Text,
    View,
} from 'react-native';

import {registerView, unregisterView} from './ViewTracker'

/**
 * Higher-order component that turns a built-in View component into a component
 * that is tracked with the key specified in the viewKey prop. If the viewKey
 * prop is not specified, then no tracking is done.
 */
const createTrackedView = (ViewComponent: any) => class TrackedComponent extends Component {
    componentDidMount() {
        const {viewKey} = this.props;
        if (viewKey) {
            registerView(viewKey, this.refs.viewRef);
        }
    }

    componentWillUnmount() {
        const {viewKey} = this.props;
        if (viewKey) {
            unregisterView(viewKey, this.refs.viewRef);
        }
    }

    render() {
        const {viewKey, ...childProps} = this.props;
        return <ViewComponent ref="viewRef" {...childProps} />
    }
};

export const TrackedView = createTrackedView(View);
export const TrackedText = createTrackedView(Text);