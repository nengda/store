package store.persistense;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class InMemoryPersistentService<T> implements PersistentService<T> {

    private ConcurrentHashMap<Long, T> cache;

    private InMemoryPersistentService() {}

    public static <E> InMemoryPersistentService newInstance(ConcurrentHashMap<Long, E> cache) {
        InMemoryPersistentService s = new InMemoryPersistentService();
        s.cache = cache;
        return s;
    }

    public static <E> InMemoryPersistentService newInstance() {
        return newInstance(new ConcurrentHashMap());
    }

    @Override
    public void upsert(Long key, T value) {
        cache.put(key, value);
    }

    @Override
    public void remove(Long key) {
        cache.remove(key);
    }

    @Override
    public T load(Long key) {
        return cache.get(key);
    }

    @Override
    public void close() {
        cache = null;
    }

    @Override
    public Long nextId() {
        return System.nanoTime();
    }

    @Override
    public Stream<T> list() {
        return cache.keySet().stream().sorted().map(k -> cache.get(k));
    }
}
