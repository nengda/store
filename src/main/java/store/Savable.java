package store;

import java.util.Collections;
import java.util.List;

/*
 * Interface for save one item to persistense.
 * */
public interface Savable<T> extends Persistable<T> {
    Long getId();
    T getInstanceToPersist();

    default List<Savable<?>> getChildren() {
        return Collections.emptyList();
    }

    default void save() {
        getChildren().forEach((c) -> c.save());
        T obj = getInstanceToPersist();
        getPersistent().upsert(getId(), obj);
    }
}
