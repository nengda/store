package store.user;

import store.*;
import store.persistense.InMemoryPersistentService;
import store.persistense.PersistentService;
import store.product.Product;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/*
 * Factory to create and edit user. The combination of
 * Builder pattern and internal class of UserImpl ensures
 * mutation of user can only done via factory interface.
 * */
final public class UserFactory implements Listable<User>, Closable {
    private final PersistentService<User> persistentService;
    private final PersistentService<Basket> basketPersistentService;
    private UserFactory() {
        persistentService = InMemoryPersistentService.newInstance();
        basketPersistentService = InMemoryPersistentService.newInstance();
    }
    private UserFactory(PersistentService<User> service, PersistentService<Basket> basketService) {
        persistentService = service;
        basketPersistentService = basketService;
    }

    public static UserFactory newInstance() {
        return new UserFactory();
    }

    public static UserFactory newInstance(PersistentService<User> service, PersistentService<Basket> basketService) {
        return new UserFactory(service, basketService);
    }

    public UserBuilder builder() {
        return new UserBuilderImpl(persistentService, basketPersistentService);
    }

    public UserBuilder edit(User u) {
        return new UserBuilderImpl(u);
    }

    @Override
    public void close() {
        persistentService.close();
        basketPersistentService.close();
    }

    public PersistentService getPersistent() { return persistentService; }

    final class UserBuilderImpl implements UserBuilder {
        private PersistentService<User> userService;
        private PersistentService<Basket> basketService;
        private Optional<Long> id;
        private Optional<Basket> basket;
        private Optional<String> name ;
        private Optional<String> email;

        UserBuilderImpl(PersistentService<User> userService, PersistentService<Basket> basketService) {
            this.id = Optional.empty();
            this.basket = Optional.empty();
            this.name = Optional.empty();
            this.email = Optional.empty();
            this.userService = userService;
            this.basketService = basketService;
        }

        UserBuilderImpl(User user) {
            this.userService = user.getPersistent();
            this.id = Optional.of(user.getId());
            this.name = Optional.of(user.getName());
            this.email = Optional.of(user.getEmail());
            this.basket = Optional.of(user.getBasket());
        }

        @Override
        public User build() {
            UserImpl impl = new UserImpl(userService);
            impl.name = this.name.orElseThrow( () -> new IllegalArgumentException("User name is a compulsory field"));
            impl.email = this.email.orElseThrow( () -> new IllegalArgumentException("User email is a compulsory field"));
            impl.id = this.id.orElse(userService.nextId());
            if (this.basket.isPresent()) {
                impl.basketId = this.basket.get().getId();
                impl.basket = this.basket.get();
            } else {
                impl.basket = BasketImpl.newInstance(basketService);
                impl.basketId = impl.basket.getId();
            }
            return impl;
        }

        @Override
        public UserBuilder withEmail(String email) {
            this.email = Optional.of(email);
            return this;
        }

        @Override
        public UserBuilder withName(String name) {
            this.name = Optional.of(name);
            return this;
        }
    }

    final class UserImpl implements User {
        private PersistentService<User> service;
        private Long id;
        private Long basketId;
        private Basket basket;
        private String name;
        private String email;

        private UserImpl(PersistentService<User> service) {
            this.service = service;
        }

        private UserImpl() {}

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public User getInstanceToPersist() { return this; }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public Basket getBasket() { return basket; }

        @Override
        public PersistentService<User> getPersistent() { return service; }

        @Override
        public String toString() {
            return "User [" +
                    "Name: '" + this.getName() + "'; " +
                    "Email: '" + this.getEmail() + "'; " +
                    "Basket: '" + this.getBasket() + "']";
        }

    }

    final static class BasketImpl implements Basket, Loadable<Basket> {
        private PersistentService<Basket> service;
        private Long id;
        private ConcurrentHashMap<Product, Integer> items;

        private BasketImpl() {}

        public static Basket newInstance(PersistentService<Basket> service) {
            BasketImpl impl = new BasketImpl();
            impl.service = service;
            impl.id = service.nextId();
            impl.items = new ConcurrentHashMap();
            return impl;
        }

        @Override
        public List<CheckOutItem> listItems() {
            return items.entrySet().stream().map(
                    (e) -> (CheckOutItem.of(e.getValue(), e.getKey()))
            ).collect(Collectors.toList());
        }

        @Override
        public void addItem(CheckOutItem item) {
            items.put(item.getProduct(), item.getQuantity());
        }

        @Override
        public void removeItem(Product p) {
            items.remove(p);
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public Basket getInstanceToPersist() {
            return this;
        }

        @Override
        public PersistentService<Basket> getPersistent() {
            return service;
        }

        @Override
        public String toString() {
            return listItems().toString();
        }
    }
}