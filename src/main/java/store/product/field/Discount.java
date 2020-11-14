package store.product.field;

/*
 * Data class for discount. Validation of discount input
 * should be done within the static constructor.
 * */
public class Discount {
    private DiscountType type;
    private Double value;

    private Discount() {};

    public static Discount of(String v) {
        Discount discount = new Discount();
        if (v.endsWith("%")) {
            String s = v.substring(0, v.length() - 1);
            try {
                Double d = Double.valueOf(s);
                if (d >= 100.0 || d <= 0.0) {
                    throwInvalidDiscount("Percentage must between 0.0 and 100.0: " + d, v);
                }
                discount.type = DiscountType.Percentage;
                discount.value = d;
            } catch (NumberFormatException e) {
                throwInvalidDiscount("Unrecognized value: " + s, v);
            }
        } else if (v.equalsIgnoreCase("free")) {
            discount.type = DiscountType.Free;
            discount.value = 0.0;
        }else {
            try {
                Double d = Double.valueOf(v);
                if (d <= 0.0) {
                    throwInvalidDiscount("Absolute discount must greater than 0.0: " + d, v);
                }
                discount.type = DiscountType.Absolute;
                discount.value = d;
            } catch (NumberFormatException e) {
                throwInvalidDiscount("Unrecognized value: " + v, v);
            }
        }
        return discount;
    }

    private static void throwInvalidDiscount(String msg, String v) {
        throw new IllegalArgumentException("Invalid discount: '" + v + "'. It should be in the form like 20.3% for percenage discount or 50 for absolute discount. " + msg);
    }

    public DiscountType getType() {
        return type;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Discount [" +
                "Type: '" + this.getType() + "'; " +
                "Value: '" + this.getValue() + "']";
    }

}
