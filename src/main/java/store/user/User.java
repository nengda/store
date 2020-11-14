package store.user;

import store.Identifiable;
import store.Savable;

import java.util.ArrayList;
import java.util.List;

/*
 * Interface for user, assuming each user only has one basket
 * for checkout.
 * */
public interface User extends Identifiable, Savable<User> {
    String getName();
    String getEmail();
    Basket getBasket();

    @Override
    default List<Savable<?>> getChildren() {
        List<Savable<?>> children = new ArrayList();
        children.add(getBasket());
        return children;
    }
}
