/**
 * @flow
 */

import * as Immutable from 'immutable'

export type Updater<T> = (t: T) => T;

/**
 * Construct a class with properly-named fields.
 */
export const buildValueClass = (fieldNames: Array<string>): any => {
    const defaults: any = {};
    for (const name of fieldNames) {
        defaults[name] = undefined;
    }
    const resultConstructor = Immutable.Record(defaults);
    const resultClass = resultConstructor.prototype;
    for (const name of fieldNames) {
        const upperName = name[0].toUpperCase() + name.slice(1);
        resultClass['with' + upperName] = function(newVal) {
            return this.set(name, newVal);
        };
        resultClass['update' + upperName] = function(updater) {
            return this.set(name, updater(this[name]));
        };
    }
    return resultConstructor;
};

export const buildUnionCaseClass = (
        caseName: string, fieldNames: Array<string>): any => {
    const defaults: any = {};
    for (const name of fieldNames) {
        defaults[name] = undefined;
    }
    defaults.type = undefined;
    const resultConstructor = Immutable.Record(defaults);
    const resultClass = resultConstructor.prototype;
    for (const name of fieldNames) {
        const upperName = name[0].toUpperCase() + name.slice(1);
        resultClass['with' + upperName] = function(newVal) {
            return this.set(name, newVal);
        };
        resultClass['update' + upperName] = function(updater) {
            return this.set(name, updater(this[name]));
        };
    }
    return resultConstructor;
};