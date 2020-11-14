package store.product;

import store.Identifiable;
import store.Removable;
import store.Savable;
import store.product.field.Quantity;

public interface Product extends Identifiable, Savable<Product>, Removable<Product> {
    String getName();
    Quantity getQuantity();
    Double getPrice();
    String getCategory();
    String getBrand();
    String getSpec();
}


