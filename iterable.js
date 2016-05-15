export default class Iterable<T> {
    [Symbol.iterator](): Iterator<T> {
        return this.iterator();
    }
}