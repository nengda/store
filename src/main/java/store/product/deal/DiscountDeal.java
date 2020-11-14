package store.product.deal;

import store.product.Product;
import store.product.field.Discount;
import store.user.CheckOutItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Data class for any discount rule applies to one product.
 * For example, buy one product and 10 dolar off of second
 * For another example, buy one product and second free
 * Or, buy one products and 10% off the second and third
 *
 * getOneDiscountAnother is a utility function for all discount
 * rules in the form of buy one and get second XXX.
 * */
public class DiscountDeal extends DealRule {
    private Product product;
    private Function<CheckOutItem, CheckOutItem> rule;

    private DiscountDeal() {};
    public static DiscountDeal of(Product product, Function<CheckOutItem, CheckOutItem> rule) {
        DiscountDeal c = new DiscountDeal();
        c.rule = rule;
        c.product = product;
        return c;
    }

    public static Function<CheckOutItem, CheckOutItem> getOneDiscountAnother(Discount discount) {
        return new Function<CheckOutItem, CheckOutItem>() {
            @Override
            public CheckOutItem apply(CheckOutItem checkOutItem) {
                Integer q = checkOutItem.getQuantity();
                Product p = checkOutItem.getProduct();
                Double totalPrice = 0.0;
                switch(discount.getType()) {
                    case Percentage:
                        totalPrice = (1.0 + discount.getValue() / 100.0) * (q / 2) * p.getPrice() + checkOutItem.getDiscountedPrice();
                        break;
                    case Absolute:
                        totalPrice = (q / 2) * p.getPrice() + (q / 2) * (p.getPrice() - discount.getValue()) + checkOutItem.getDiscountedPrice();
                        break;
                    case Free:
                        totalPrice = (q / 2) * p.getPrice() + checkOutItem.getDiscountedPrice();
                        break;
                }
                return CheckOutItem.of(q % 2, p, totalPrice);
            }
        };
    }

    @Override
    public Function<List<CheckOutItem>, List<CheckOutItem>> toFunction() {
        return new Function<List<CheckOutItem>, List<CheckOutItem>>() {
            @Override
            public List<CheckOutItem> apply(List<CheckOutItem> input) {
                Optional<CheckOutItem> candidate = findCandidate(input, product);
                if (candidate.isPresent()) {
                    return input.stream().map((c) -> {
                        if (c.equals(candidate.get())) {
                            return rule.apply(c);
                        } else return c;
                    }).collect(Collectors.toList());
                } else return input;
            }
        };
    }

}
