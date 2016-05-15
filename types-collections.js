/**
 * @flow
 */
import * as Immutable from 'immutable'

import {Lens, makeLens} from './types-lib'
import type {Updater} from './types-lib'

/**
 * Immutable map class based on Immutable.Map, but with some extra features like
 * type-safe lenses.
 */
export class IMap<K, V> {
    backingMap: Immutable.Map<K, V>;

    constructor(backingMap: Immutable.Map<K, V>) {
        this.backingMap = backingMap;
    }

    static make<K, V>(): IMap<K, V> {
        return new IMap(Reflect.construct(Immutable.Map, arguments));
    }

    set(key: K, value: V): IMap<K, V> {
        return new IMap(this.backingMap.set(key, value));
    }

    update(key: K, updater: Updater<V>): IMap<K, V> {
        return new IMap(this.backingMap.update(key, updater));
    }

    get(key: K): V {
        return this.backingMap.get(key);
    }

    lens(): IMapLens<K, V, IMap<K, V>> {
        return this.makeLens((newMap) => newMap);
    }

    makeLens<Result>(replace: (map: IMap<K, V>) => Result):
            IMapLens<K, V, Result> {
        return new IMapLens(this, replace);
    }
}

export class IMapLens<K, V, Result> extends Lens<IMap<K, V>, Result> {
    atKey(key: K): Lens<V, Result> {
        const replaceChild = newChildVal =>
            this.replace(this.value.set(key, newChildVal));
        return makeLens(this.value.get(key), replaceChild);
    }
}

export class IList<T> {
    backingList: Immutable.List<T>;

    constructor(backingList: Immutable.List<T>) {
        this.backingList = backingList;
    }

    static make<K, V>(): IList<T> {
        return new IList(Reflect.construct(Immutable.List, arguments));
    }

    set(index: number, value: T): IList<T> {
        return new IList(this.backingList.set(index, value));
    }

    update(index: number, updater: Updater<T>): IList<T> {
        return new IList(this.backingList.update(index, updater));
    }

    get(index: number): T {
        return this.backingList.get(index);
    }

    lens(): IListLens<T, IList<T>> {
        return this.makeLens((newList) => newList);
    }

    makeLens<Result>(replace: (list: IList<T>) => Result):
            IListLens<T, Result> {
        return new IListLens(this, replace);
    }
}

export class IListLens<T, Result> extends Lens<IList<T>, Result> {
    atIndex(index: number): Lens<T, Result> {
        const replaceChild = newChildVal =>
            this.replace(this.value.set(index, newChildVal));
        return makeLens(this.value.get(index), replaceChild);
    }
}