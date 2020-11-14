package store.product.deal;

import store.Closable;
import store.persistense.InMemoryPersistentService;
import store.Listable;
import store.persistense.PersistentService;

import java.util.Optional;

/*
 * Factory to create and edit deal. The combination of
 * Builder pattern and internal class of DealImpl ensures
 * mutation of deal can only done via factory interface.
 * */
final public class DealFactory implements Listable<Deal>, Closable {
    private final PersistentService<Deal> persistentService;
    private DealFactory() {
        persistentService = InMemoryPersistentService.newInstance();
    }
    private DealFactory(PersistentService<Deal> service) {
        persistentService = service;
    }

    public static DealFactory newInstance() {
        return new DealFactory();
    }

    public static DealFactory newInstance(PersistentService<Deal> service) {
        return new DealFactory(service);
    }

    public DealBuilder builder() {
        return new DealBuilderImpl(persistentService);
    }

    public DealBuilder edit(Deal d) {
        return new DealBuilderImpl(d);
    }

    public PersistentService getPersistent() { return persistentService; }

    @Override
    public void close() {
        persistentService.close();
    }

    final class DealBuilderImpl implements DealBuilder {
        private PersistentService<Deal> service;
        private Optional<Long> id;
        private Optional<String> name;
        private Optional<String> description;
        private Optional<DealRule> rule;

        DealBuilderImpl(PersistentService<Deal> service) {
            this.id = Optional.empty();
            this.name = Optional.empty();
            this.description = Optional.empty();
            this.rule = Optional.empty();
            this.service = service;
        }

        DealBuilderImpl(Deal deal) {
            this.service = deal.getPersistent();
            this.id = Optional.of(deal.getId());
            this.name = Optional.of(deal.getName());
            this.description = Optional.of(deal.getDescription());
            this.rule = Optional.of(deal.getRule());
        }

        public Deal build() {
            DealImpl impl = new DealImpl(this.service);
            impl.name = this.name.orElseThrow( () -> new IllegalArgumentException("Deal name is a compulsory field"));
            impl.description = this.description.orElseThrow( () -> new IllegalArgumentException("Deal description is a compulsory field"));
            impl.rule = this.rule.orElseThrow( () -> new IllegalArgumentException("Deal rule is a compulsory field"));
            impl.id = this.id.orElse(service.nextId());
            return impl;
        }

        @Override
        public DealBuilder withName(String name) {
            this.name = Optional.of(name);
            return this;
        }

        @Override
        public DealBuilder withDescription(String des) {
            this.description = Optional.of(des);
            return this;
        }

        @Override
        public DealBuilder withRule(DealRule rule) {
            this.rule = Optional.of(rule);
            return this;
        }
    }

    final class DealImpl implements Deal {
        private PersistentService<Deal> service;
        private Long id;
        private String name;
        private String description;
        private DealRule rule;

        DealImpl(PersistentService<Deal> service) {
            this.service = service;
        }

        private DealImpl() {}

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public DealRule getRule() { return rule; }

        @Override
        public Deal getInstanceToPersist() {
            return this;
        }

        @Override
        public PersistentService<Deal> getPersistent() {
            return service;
        }

        @Override
        public String toString() {
            return "Deal [" +
                    "Name: '" + this.getName() + "'; " +
                    "Description: '" + this.getDescription() + "']";
        }

    }
}
