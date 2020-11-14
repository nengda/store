package store.user;

import store.product.Product;

import java.util.Objects;
import java.util.Optional;

/*
 * Data class of Checkout item. All validations on checkout
 * should be done within the static constructor.
 * */
final public class CheckOutItem {
    private Integer quantity;
    private Product product;
    private Optional<Double> maybeDiscountedPrice;

    private CheckOutItem() {};
    public static CheckOutItem of(Integer quantity, Product product) {
        checkQuantityOrThrow(product, quantity);
        CheckOutItem c = new CheckOutItem();
        c.quantity = quantity;
        c.product = product;
        c.maybeDiscountedPrice = Optional.empty();
        return c;
    }

    public static CheckOutItem of(Integer quantity, Product product, Double discountedPrice) {
        checkQuantityOrThrow(product, quantity);
        CheckOutItem c = new CheckOutItem();
        c.quantity = quantity;
        c.product = product;
        c.maybeDiscountedPrice = Optional.of(discountedPrice);
        return c;
    }

    private static void checkQuantityOrThrow(Product p, Integer q) {
        if (q > p.getQuantity().getValue())
            throw new IllegalArgumentException("Invalid checkout: not enough inventory of " + p.getName()
                    + ". Inventory is: " + p.getQuantity().getValue()
                    + ". Quantity to be checked out is: " + q);
        if (q < 0) {
            throw new IllegalArgumentException("Invalid checkout: negative quantity, " + q);
        }
    }

    public Double getTotalPrice() {
        return maybeDiscountedPrice.orElse(0.0) + product.getPrice() * quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    public Double getDiscountedPrice() {
        return maybeDiscountedPrice.orElse(0.0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, product, maybeDiscountedPrice);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        CheckOutItem that = (CheckOutItem)obj;
        return this.product.equals(that.product)
                && this.quantity.equals(that.quantity)
                && this.maybeDiscountedPrice.equals(that.maybeDiscountedPrice);
    }

    @Override
    public String toString() {
        return "CheckOutItem [" +
                "Product: '" + this.getProduct().getName() + "'; " +
                "Quantity: '" + this.getQuantity() + "']";
    }
}
