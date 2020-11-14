package store.product.deal;

import store.Identifiable;
import store.Removable;
import store.Savable;

public interface Deal extends Savable<Deal>, Identifiable, Removable<Deal> {
    String getName();
    String getDescription();
    DealRule getRule();
}
