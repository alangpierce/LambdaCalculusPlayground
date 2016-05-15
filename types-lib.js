/**
 * @flow
 */

import * as Immutable from 'immutable'

export type Updater<T> = (t: T) => T;

const registeredConstructors = {};

/**
 * A "lens", as defined here, is a path down an existing object with the ability
 * to do an immutable replacement deep in the object tree. All generated value
 * types know how to create a type-specific lens to assist in navigating through
 * them, and for other types, we
 */
export class Lens<T, Result> {
    value: T;
    replace: (t: T) => Result;

    constructor(value: T, replace: (t: T) => Result) {
        this.value = value;
        this.replace = replace;
    }

    update(updater: Updater<T>): Result {
        return this.replace(updater(this.value));
    }
}

export const makeLens = function<T, Result>(
        value: T, replace: (t: T) => Result): Lens<T, Result> {
    if (typeof value.makeLens === 'function') {
        return value.makeLens(replace);
    }
    return new Lens(value, replace);
};

/**
 * Construct a class with properly-named fields.
 */
export const buildValueClass = (
        className: string, fieldNames: Array<string>): any => {
    class CustomLens extends Lens {}
    const customLensPrototype: any = CustomLens.prototype;
    for (const name of fieldNames) {
        customLensPrototype[name] = function() {
            const replaceChild = newChildVal =>
                this.replace(this.value.set(name, newChildVal));
            return makeLens(this.value[name], replaceChild);
        };
    }

    const defaults: any = {};
    for (const name of fieldNames) {
        defaults[name] = undefined;
    }
    class ValueClass extends Immutable.Record(defaults) {
        static make(...args) {
            const constructorArg = {};
            for (let i = 0; i < fieldNames.length; i++) {
                constructorArg[fieldNames[i]] = args[i];
            }
            return new ValueClass(constructorArg);
        }
        serialize() {
            const result = {};
            result.__SERIALIZED_CLASS = className;
            for (const name of fieldNames) {
                result[name] = serialize(this[name]);
            }
            return result;
        }
        lens() {
            return this.makeLens(newValue => newValue);
        }
        makeLens(replace) {
            return new CustomLens(this, replace);
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
        static make(...args) {
            const constructorArg = {};
            constructorArg.type = caseName;
            for (let i = 0; i < fieldNames.length; i++) {
                constructorArg[fieldNames[i]] = args[i];
            }
            return new UnionCaseClass(constructorArg);
        }
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