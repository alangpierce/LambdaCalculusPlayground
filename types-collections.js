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

    static make<K, V>(...args): IMap<K, V> {
        return new IMap(new Immutable.Map(...args));
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

    keys(): Iterator<K> {
        return this.backingMap.keys();
    }

    hasKey(key: K): boolean {
        return this.backingMap.has(key);
    }

    isEmpty(): boolean {
        return this.backingMap.isEmpty();
    }

    delete(key: K): IMap<K, V> {
        return new IMap(this.backingMap.delete(key));
    }

    lens(): MapLens<K, V, IMap<K, V>> {
        return this.makeLens((newMap) => newMap);
    }

    makeLens<Result>(replace: (map: IMap<K, V>) => Result):
            MapLens<K, V, Result> {
        return new MapLens(this, replace);
    }
    
    iterator(): Iterator<[K, V]> {
        return (this.backingMap: any)[Symbol.iterator]();
    }

    equals(other: IMap<K, V>): boolean {
        return this.backingMap.equals((other.backingMap: any));
    }

    hashCode(): number {
        return this.backingMap.hashCode();
    }

    toString(): string {
        return this.backingMap.toString();
    }
}

export class MapLens<K, V, Result> extends Lens<IMap<K, V>, Result> {
    atKey(key: K): Lens<V, Result> {
        const replaceChild = newChildVal =>
            this.replace(this.value.set(key, newChildVal));
        return makeLens(this.value.get(key), replaceChild);
    }
    deleteKey(key: K): Result {
        return this.replace(this.value.delete(key));
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

    static make<T>(...args): IList<T> {
        return new IList(new Immutable.List(...args));
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

    slice(begin?: number, end?: number): IList<T> {
        return new IList(this.backingList.slice(begin, end));
    }

    get(index: number): T {
        return this.backingList.get(index);
    }

    toArray(): Array<T> {
        return this.backingList.toArray();
    }

    sort(): IList<T> {
        return new IList(this.backingList.sort());
    }

    lens(): ListLens<T, IList<T>> {
        return this.makeLens((newList) => newList);
    }

    makeLens<Result>(replace: (list: IList<T>) => Result):
            ListLens<T, Result> {
        return new ListLens(this, replace);
    }

    iterator(): Iterator<T> {
        return (this.backingList: any)[Symbol.iterator]();
    }

    equals(other: IList<T>): boolean {
        return this.backingList.equals((other.backingList: any));
    }

    hashCode(): number {
        return this.backingList.hashCode();
    }

    toString(): string {
        return this.backingList.toString();
    }
}

export class ListLens<T, Result> extends Lens<IList<T>, Result> {
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

    static make<K, V>(...args): ISet<T> {
        return new ISet(new Immutable.Set(...args));
    }

    add(value: T): ISet<T> {
        return new ISet(this.backingSet.add(value));
    }

    has(value: T): boolean {
        return this.backingSet.has(value);
    }

    union(other: Iterator<T>): ISet<T> {
        return new ISet(this.backingSet.union(other));
    }

    iterator(): Iterator<T> {
        return (this.backingSet: any)[Symbol.iterator]();
    }

    equals(other: ISet<T>): boolean {
        return this.backingSet.equals((other.backingSet: any));
    }

    hashCode(): number {
        return this.backingSet.hashCode();
    }

    toString(): string {
        return this.backingSet.toString();
    }
}