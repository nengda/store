package store.product.deal;

public interface DealBuilder {
    Deal build();
    DealBuilder withName(String name);
    DealBuilder withDescription(String des);
    DealBuilder withRule(DealRule rule);
}
