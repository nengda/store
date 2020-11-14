package store;

import java.util.stream.Stream;

/*
 * Interface for listing all items stored in persistense.
 * */
public interface Listable<T> extends Persistable<T> {

    default Stream<T> list() {
        return getPersistent().list();
    }
}
