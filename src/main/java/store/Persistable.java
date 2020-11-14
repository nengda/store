package store;

import store.persistense.PersistentService;

/*
 * Base interface for any actions on persistense
 * */
public interface Persistable<T> {

    PersistentService<T> getPersistent();

}
