/**
 * @flow
 */
import * as Immutable from 'immutable'

import Iterable from './iterable'
import {Lens, makeLens} from './types-lib'
import type {Updater} from './types-lib'

/**
 * Immutable map class based on Immutable.Map, but with some extra features like
 * type-safe lenses.
 */
export class IMap<K, V> extends Iterable<[K, V]> {
    backingMap: Immutable.Map<K, V>;
    size: number;

    constructor(backingMap: Immutable.Map<K, V>) {
        super();
        this.backingMap = backingMap;
        this.size = backingMap.size;
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

    delete(key: K): IMap<K, V> {
        return new IMap(this.backingMap.delete(key));
    }

    lens(): IMapLens<K, V, IMap<K, V>> {
        return this.makeLens((newMap) => newMap);
    }

    makeLens<Result>(replace: (map: IMap<K, V>) => Result):
            IMapLens<K, V, Result> {
        return new IMapLens(this, replace);
    }
    
    iterator(): Iterator<[K, V]> {
        return (this.backingMap: any)[Symbol.iterator]();
    }
}

export class IMapLens<K, V, Result> extends Lens<IMap<K, V>, Result> {
    atKey(key: K): Lens<V, Result> {
        const replaceChild = newChildVal =>
            this.replace(this.value.set(key, newChildVal));
        return makeLens(this.value.get(key), replaceChild);
    }
}

export class IList<T> extends Iterable<T> {
    backingList: Immutable.List<T>;
    size: number;

    constructor(backingList: Immutable.List<T>) {
        super();
        this.backingList = backingList;
        this.size = backingList.size;
    }

    static make<K, V>(): IList<T> {
        return new IList(Reflect.construct(Immutable.List, arguments));
    }

    set(index: number, value: T): IList<T> {
        return new IList(this.backingList.set(index, value));
    }

    push(value: T): IList<T> {
        return new IList(this.backingList.push(value));
    }

    pop(): IList<T> {
        return new IList(this.backingList.pop());
    }

    update(index: number, updater: Updater<T>): IList<T> {
        return new IList(this.backingList.update(index, updater));
    }

    map<U>(mapper: (t: T, i: number) => U): IList<U> {
        return new IList(this.backingList.map(mapper));
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

    iterator(): Iterator<T> {
        return (this.backingList: any)[Symbol.iterator]();
    }
}

export class IListLens<T, Result> extends Lens<IList<T>, Result> {
    atIndex(index: number): Lens<T, Result> {
        const replaceChild = newChildVal =>
            this.replace(this.value.set(index, newChildVal));
        return makeLens(this.value.get(index), replaceChild);
    }
}

export class ISet<T> extends Iterable<T> {
    backingSet: Immutable.Set<T>;
    size: number;

    constructor(backingSet: Immutable.Set<T>) {
        super();
        this.backingSet = backingSet;
        this.size = backingSet.size;
    }

    static make<K, V>(): ISet<T> {
        return new ISet(Reflect.construct(Immutable.Set, arguments));
    }

    add(value: T): ISet<T> {
        return new ISet(this.backingSet.add(value));
    }

    has(value: T): boolean {
        return this.backingSet.has(value);
    }

    iterator(): Iterator<T> {
        return (this.backingSet: any)[Symbol.iterator]();
    }
}