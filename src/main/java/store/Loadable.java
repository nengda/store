package store;

/*
 * Interface for loading one item from persistense
 * */
public interface Loadable<T> extends Persistable<T> {

    default T load(Long id) {
        return getPersistent().load(id);
    }
}
