package store.product;

import store.product.field.Quantity;

/*
 * Builder to create or modify product.
 * */
public interface ProductBuilder {
    Product build();
    ProductBuilder withQuantity(Quantity quantity);
    ProductBuilder withPrice(Double price);
    ProductBuilder withName(String name);
    ProductBuilder withCategory(String category);
    ProductBuilder withBrand(String brand);
    ProductBuilder withSpec(String spec);
}
