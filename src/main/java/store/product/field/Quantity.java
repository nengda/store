package store.product.field;

import java.util.Objects;

/*
 * Data class for quantity. Validation of quantity value
 * should be done within the static constructor.
 * */
public class Quantity {
    private QuantityType type;
    private Long value;

    private Quantity() {};

    public static Quantity of(Long v, QuantityType type) {
        Quantity q = new Quantity();
        if (v < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        q.type = type;
        q.value = v;
        return q;
    }

    public QuantityType getType() {
        return type;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Quantity that = (Quantity)obj;
        return this.value.equals(that.value)
                && this.type.equals(that.type);
    }

    @Override
    public String toString() {
        return "Quantity [" +
                "Type: '" + this.getType() + "'; " +
                "Value: '" + this.getValue() + "']";
    }

}
