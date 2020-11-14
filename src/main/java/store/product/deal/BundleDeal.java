package store.product.deal;

import store.product.Product;
import store.product.field.Discount;
import store.user.CheckOutItem;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Data class for any discount rule applies to a pair of different products.
 * For example, buy one product and 10 dolar off of another different product
 * For another example, buy one product and second different product free
 *
 * getOneAndBundle is a utility function for all discount
 * rules in the form of buy one product and get another product XXX.
 * */
public class BundleDeal extends DealRule {
    private Product from;
    private Product to;
    private BiFunction<CheckOutItem, CheckOutItem, CheckOutItem> rule;

    private BundleDeal() {};
    public static BundleDeal of(Product from, Product to, BiFunction<CheckOutItem, CheckOutItem, CheckOutItem> rule) {
        BundleDeal b = new BundleDeal();
        b.rule = rule;
        b.from = from;
        b.to = to;
        return b;
    }

    public static BiFunction<CheckOutItem, CheckOutItem, CheckOutItem> getOneAndBundle(Discount discount) {
        return new BiFunction<CheckOutItem, CheckOutItem, CheckOutItem>() {
            @Override
            public CheckOutItem apply(CheckOutItem from, CheckOutItem to) {
                Integer fromQuantity = from.getQuantity();
                Integer toQuantity = to.getQuantity();
                Product toProduct = to.getProduct();
                Double totalPrice = 0.0;
                Integer leftQuantity = (toQuantity - (fromQuantity > 1 ? fromQuantity / 2 : fromQuantity));
                leftQuantity = leftQuantity > 0 ? leftQuantity : 0;
                switch(discount.getType()) {
                    case Percentage:
                        totalPrice = (toQuantity - leftQuantity) * toProduct.getPrice() * discount.getValue() / 100.0
                                + to.getDiscountedPrice();
                        break;
                    case Absolute:
                        totalPrice = (toQuantity - leftQuantity) * (toProduct.getPrice() - discount.getValue())
                                + to.getDiscountedPrice();
                        break;
                    case Free:
                        totalPrice = to.getDiscountedPrice();
                        break;
                }
                return CheckOutItem.of(leftQuantity, toProduct, totalPrice);
            }
        };
    }

    @Override
    public Function<List<CheckOutItem>, List<CheckOutItem>> toFunction() {
        return new Function<List<CheckOutItem>, List<CheckOutItem>>() {
            @Override
            public List<CheckOutItem> apply(List<CheckOutItem> input) {
                Optional<CheckOutItem> cFrom = findCandidate(input, from);
                Optional<CheckOutItem> cTo = findCandidate(input, to);
                if (cFrom.isPresent() && cTo.isPresent()) {
                    return input.stream().map((c) -> {
                        if (c.equals(cTo.get())) {
                            return rule.apply(cFrom.get(), c);
                        } else return c;
                    }).collect(Collectors.toList());
                } else return input;
            }
        };
    }

}
