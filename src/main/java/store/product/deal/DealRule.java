package store.product.deal;

import store.product.Product;
import store.user.CheckOutItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/*
 * Base class for all rules of deal. The abstract toFunction method
 * returns a Function that converts list of checkout items into another
 * list of checkout items according to predefined rules
 * */
public abstract class DealRule {

    protected Optional<CheckOutItem> findCandidate(List<CheckOutItem> input, Product product) {
        return input.stream()
                .filter((c) -> c.getProduct().equals(product))
                .findFirst();
    }

    public abstract Function<List<CheckOutItem>, List<CheckOutItem>> toFunction();

}

