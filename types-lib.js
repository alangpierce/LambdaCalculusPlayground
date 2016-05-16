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
    if (value != null && typeof value.makeLens === 'function') {
        return value.makeLens(replace);
    }
    return new Lens(value, replace);
};

const withName = (name) => {
    return 'with' + name[0].toUpperCase() + name.slice(1);
};

const updateName = (name) => {
    return 'update' + name[0].toUpperCase() + name.slice(1);
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
                this.replace(this.value[withName(name)](newChildVal));
            return makeLens(this.value[name], replaceChild);
        };
    }

    class ValueClass {
        constructor(fields) {
            // Attach the class name for easier debugging.
            (this: any).className = className;
            for (const fieldName of fieldNames) {
                (this: any)[fieldName] = fields[fieldName];
            }
        }
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
                result[name] = serialize((this: any)[name]);
            }
            return result;
        }
        lens() {
            return this.makeLens(newValue => newValue);
        }
        makeLens(replace) {
            return new CustomLens(this, replace);
        }
        equals(other) {
            for (const name of fieldNames) {
                if (!Immutable.is((this: any)[name], other[name])) {
                    return false;
                }
            }
            return true;
        }
        hashCode() {
            return Immutable.Map(this).hashCode();
        }
        toString() {
            return Immutable.Map(this).toString();
        }
    }
    for (const name of fieldNames) {
        (ValueClass: any).prototype[withName(name)] = function(newVal) {
            const newArgs = {};
            for (const copyName of fieldNames) {
                newArgs[copyName] = this[copyName];
            }
            newArgs[name] = newVal;
            return new ValueClass(newArgs);
        };
        (ValueClass: any).prototype[updateName(name)] = function(updater) {
            return this[withName(name)](updater(this[name]));
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
    class UnionCaseClass {
        constructor(fields) {
            (this: any).type = fields.type;
            for (const fieldName of fieldNames) {
                (this: any)[fieldName] = fields[fieldName];
            }
        }
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
                result[name] = serialize((this: any)[name]);
            }
            result.type = (this: any).type;
            return result;
        }
        equals(other) {
            for (const name of fieldNames) {
                if (!Immutable.is((this: any)[name], other[name])) {
                    return false;
                }
            }
            return true;
        }
        hashCode() {
            return Immutable.Map(this).hashCode();
        }
        toString() {
            return Immutable.Map(this).toString();
        }
    }
    for (const name of fieldNames) {
        (UnionCaseClass: any).prototype[withName(name)] = function(newVal) {
            const newArgs = {};
            for (const copyName of fieldNames) {
                newArgs[copyName] = this[copyName];
            }
            newArgs.type = this.type;
            newArgs[name] = newVal;
            return new UnionCaseClass(newArgs);
        };
        (UnionCaseClass: any).prototype[updateName(name)] = function(updater) {
            return this[withName(name)](updater(this[name]));
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