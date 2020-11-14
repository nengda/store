package store;

/*
 * Interface for removing one item from persistense
 * */
public interface Removable<T> extends Persistable<T> {
    Long getId();

    default void remove() {
        getPersistent().remove(getId());
    }
}
