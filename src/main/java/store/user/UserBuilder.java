package store.user;

/*
 * Builder to create or modify user.
 * */
public interface UserBuilder {
    User build();
    UserBuilder withName(String name);
    UserBuilder withEmail(String email);
}
