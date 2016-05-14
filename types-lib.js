/**
 * @flow
 */

import * as Immutable from 'immutable'

export type Updater<T> = (t: T) => T;

const registeredConstructors = {};

/**
 * Construct a class with properly-named fields.
 */
export const buildValueClass = (
        className: string, fieldNames: Array<string>): any => {
    const defaults: any = {};
    for (const name of fieldNames) {
        defaults[name] = undefined;
    }
    class ValueClass extends Immutable.Record(defaults) {
        serialize() {
            const result = {};
            result.__SERIALIZED_CLASS = className;
            for (const name of fieldNames) {
                result[name] = serialize(this[name]);
            }
            return result;
        }
    }
    for (const name of fieldNames) {
        const upperName = name[0].toUpperCase() + name.slice(1);
        ValueClass.prototype['with' + upperName] = function(newVal) {
            return this.set(name, newVal);
        };
        ValueClass.prototype['update' + upperName] = function(updater) {
            return this.set(name, updater(this[name]));
        };
    }
    registeredConstructors[className] = ValueClass;
    return ValueClass;
};

export const buildUnionCaseClass = (
        caseName: string, fieldNames: Array<string>): any => {
    const defaults: any = {};
    for (const name of fieldNames) {
        defaults[name] = undefined;
    }
    defaults.type = undefined;
    class UnionCaseClass extends Immutable.Record(defaults) {
        match(visitor) {
            return visitor[caseName](this);
        }
        serialize() {
            const result = {};
            result.__SERIALIZED_CLASS = caseName;
            for (const name of fieldNames) {
                result[name] = serialize(this[name]);
            }
            result.type = this.type;
            return result;
        }
    }
    for (const name of fieldNames) {
        const upperName = name[0].toUpperCase() + name.slice(1);
        UnionCaseClass.prototype['with' + upperName] = function(newVal) {
            return this.set(name, newVal);
        };
        UnionCaseClass.prototype['update' + upperName] = function(updater) {
            return this.set(name, updater(this[name]));
        };
    }
    registeredConstructors[caseName] = UnionCaseClass;
    return UnionCaseClass;
};

const serialize = (obj: any): any => {
    if (obj == null || typeof obj !== 'object') {
        return obj;
    }
    if (typeof obj.serialize === 'function') {
        return obj.serialize();
    }
    return obj;
};

// TODO: Handle immutable maps. Currently it just doesn't serialize them.
export const deserialize = (obj: any): any => {
    if (obj == null || typeof obj !== 'object') {
        return obj;
    }
    const className = obj.__SERIALIZED_CLASS;
    if (className == null) {
        return obj;
    }
    const constructorArg = {};
    for (const name of Object.keys(obj)) {
        if (name !== '__SERIALIZED_CLASS') {
            constructorArg[name] = deserialize(obj[name]);
        }
    }
    const constructor = registeredConstructors[className];
    return new constructor(constructorArg);
};

export const serializeActionsMiddleware = (store: any) => (next: any) => (action: any) => {
    if (action != null &&
            typeof action === 'object' &&
            typeof action.serialize === 'function') {
        action = action.serialize();
    }
    return next(action);
};