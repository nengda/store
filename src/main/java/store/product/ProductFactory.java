package store.product;

import store.Closable;
import store.persistense.InMemoryPersistentService;
import store.Listable;
import store.persistense.PersistentService;
import store.product.field.Quantity;
import store.product.field.QuantityType;

import java.util.Objects;
import java.util.Optional;

/*
 * Factory to create and edit product. The combination of
 * Builder pattern and internal class of ProductImpl ensures
 * mutation of user can only done via factory interface.
 * */
final public class ProductFactory implements Listable<Product>, Closable {
    private final PersistentService<Product> persistentService;
    private ProductFactory() {
        persistentService = InMemoryPersistentService.newInstance();
    }
    private ProductFactory(PersistentService<Product> service) {
        persistentService = service;
    }

    public static ProductFactory newInstance() {
        return new ProductFactory();
    }

    public static ProductFactory newInstance(PersistentService<Product> service) {
        return new ProductFactory(service);
    }

    public ProductBuilder builder() {
        return new ProductBuilderImpl(persistentService);
    }

    public ProductBuilder edit(Product p) {
        return new ProductBuilderImpl(p);
    }

    public PersistentService getPersistent() { return persistentService; }

    @Override
    public void close() {
        persistentService.close();
    }

    final class ProductBuilderImpl implements ProductBuilder {
        private PersistentService<Product> service;
        private Optional<Long> id;
        private String name = "";
        private Quantity quantity = Quantity.of(0L, QuantityType.Piece);
        private Double price = 0.0;
        private String category = "N/A";
        private String brand = "N/A";
        private String spec = "";

        ProductBuilderImpl(PersistentService<Product> service) {
            this.id = Optional.empty();
            this.service = service;
        }

        ProductBuilderImpl(Product product) {
            this.service = product.getPersistent();
            this.id = Optional.of(product.getId());
            this.name = product.getName();
            this.quantity = product.getQuantity();
            this.price = product.getPrice();
            this.category = product.getCategory();
            this.brand = product.getBrand();
            this.spec = product.getSpec();
        }

        public Product build() {
            ProductImpl impl = new ProductImpl(this.service);
            impl.name = this.name;
            impl.quantity = this.quantity;
            impl.price = this.price;
            impl.category = this.category;
            impl.brand = this.brand;
            impl.spec = this.spec;
            impl.id = this.id.orElse(service.nextId());
            return impl;
        }

        public ProductBuilder withQuantity(Quantity quantity) {
            this.quantity = quantity;
            return this;
        }

        public ProductBuilder withPrice(Double price) {
            this.price = price;
            return this;
        }

        public ProductBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ProductBuilder withCategory(String category) {
            this.category = category;
            return this;
        }

        public ProductBuilder withBrand(String brand) {
            this.brand = brand;
            return this;
        }

        public ProductBuilder withSpec(String spec) {
            this.spec = spec;
            return this;
        }

    }

    final class ProductImpl implements Product {
        private PersistentService<Product> service;
        private Long id;
        private String name;
        private Quantity quantity;
        private Double price;
        private String category;
        private String brand;
        private String spec;

        ProductImpl(PersistentService<Product> service) {
            this.service = service;
        }

        private ProductImpl() {}

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Quantity getQuantity() {
            return quantity;
        }

        @Override
        public Double getPrice() {
            return price;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getBrand() {
            return brand;
        }

        @Override
        public String getSpec() {
            return spec;
        }

        @Override
        public Product getInstanceToPersist() {
            return this;
        }

        @Override
        public PersistentService<Product> getPersistent() {
            return service;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || this.getClass() != obj.getClass()) return false;
            ProductImpl that = (ProductImpl) obj;
            return this.getName().equals(that.getName())
                    && this.getQuantity().equals(that.getQuantity())
                    && this.getPrice().equals(that.getPrice())
                    && this.getCategory().equals(that.getCategory())
                    && this.getBrand().equals(that.getBrand())
                    && this.getSpec().equals(that.getSpec());
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    this.getName(),
                    this.getBrand(),
                    this.getCategory(),
                    this.getSpec(),
                    this.getPrice(),
                    this.getQuantity());
        }

        @Override
        public String toString() {
            return "Product [" +
                    "Name: '" + this.getName() + "'; " +
                    "Brand: '" + this.getBrand() + "'; " +
                    "Category: '" + this.getCategory() + "'; " +
                    "Price: '" + this.getPrice() + "'; " +
                    "Quantity: '" + this.getQuantity() + "'; " +
                    "Spec: '" + this.getSpec() + "']";
        }

    }
}
