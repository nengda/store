package store.user;

import store.Identifiable;
import store.Savable;
import store.product.deal.Deal;
import store.product.Product;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * A basket used for checkout. totalPrice chains all deal rule
 * functions into a single function and applies it to checkout items.
 * */
public interface Basket extends Identifiable, Savable<Basket> {
    List<CheckOutItem> listItems();

    void addItem(CheckOutItem item);

    void removeItem(Product p);

    default double totalPrice(List<Deal> deals) {
        Function<List<CheckOutItem>, List<CheckOutItem>> rule = deals.stream()
                .map(d -> d.getRule().toFunction())
                .reduce( (r1, r2) -> r1.andThen(r2))
                .orElse( Function.identity() );
        return rule.apply(listItems()).stream()
                .map(CheckOutItem::getTotalPrice)
                .collect(Collectors.summingDouble(Double::doubleValue));
    }
}
