package store.persistense;

import java.util.stream.Stream;

public interface PersistentService<T> {

    void upsert(Long key, T value);
    T load(Long key);
    void remove(Long key);
    void close();

    Long nextId();
    Stream<T> list();
}
