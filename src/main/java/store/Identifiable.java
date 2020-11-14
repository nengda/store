package store;

/*
 * Identifiable object as the key for persistence. The key
 * type is Long but it can be further abstracted for different
 * key types.
 * */
public interface Identifiable {
    Long getId();
}
