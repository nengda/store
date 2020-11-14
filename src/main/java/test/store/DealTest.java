package test.store;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import store.persistense.InMemoryPersistentService;
import store.product.*;
import store.product.deal.BundleDeal;
import store.product.deal.Deal;
import store.product.deal.DealFactory;
import store.product.deal.DiscountDeal;
import store.product.field.Discount;
import store.product.field.Quantity;
import store.product.field.QuantityType;
import store.user.CheckOutItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static store.product.field.DiscountType.*;

public class DealTest {
    private DealFactory dealfactory;
    private ProductFactory productfactory;
    private ConcurrentHashMap<Long, Deal> dealStorage;
    private String discountDealName = "foo";
    private String discountDealDescription = "buy foo get 50% off second";
    private String bundleDealName = "bar";
    private String bundleDealDescription = "buy foo get bar free";
    private Double precision = 1E-5;

    @Before
    public void init() {
        dealStorage = new ConcurrentHashMap();
        dealfactory = DealFactory.newInstance(InMemoryPersistentService.newInstance(dealStorage));
        productfactory = ProductFactory.newInstance();
    }

    @After
    public void tearDown() {
        productfactory.close();
        dealfactory.close();
    }

    @Test
    public void testCanBuildDiscount() {
        Discount d1 = Discount.of("50%");
        Assert.assertEquals("Discount should have the correct type", Percentage, d1.getType());
        Assert.assertEquals("Discount should have the correct value", Double.valueOf(50.0), d1.getValue());

        Discount d2 = Discount.of("200");
        Assert.assertEquals("Discount should have the correct type", Absolute, d2.getType());
        Assert.assertEquals("Discount should have the correct value", Double.valueOf(200), d2.getValue());

        Discount d3 = Discount.of("Free");
        Assert.assertEquals("Discount should have the correct type", Free, d3.getType());
        Assert.assertEquals("Discount should have the correct value", Double.valueOf(0.0), d3.getValue());
    }

    @Test(expected =  IllegalArgumentException.class)
    public void testCanValidateInvalidDiscount() {
        Discount d2 = Discount.of("Invalid");
    }

    @Test(expected =  IllegalArgumentException.class)
    public void testCanValidateInvalidPercentageDiscount() {
        Discount d2 = Discount.of("100d%");
    }

    @Test(expected =  IllegalArgumentException.class)
    public void testCanValidateNegativePercentageDiscount() {
        Discount d2 = Discount.of("-1%");
    }

    @Test(expected =  IllegalArgumentException.class)
    public void testCanValidateLargePercentageDiscount() {
        Discount d2 = Discount.of("101%");
    }

    @Test(expected =  IllegalArgumentException.class)
    public void testCanValidateNegtiveAbsoluteDiscount() {
        Discount d2 = Discount.of("-1");
    }

    @Test
    public void testCanCalculatePercentageDiscount() {
        Product p = getTestProduct();
        List<CheckOutItem> items = new ArrayList();
        items.add(CheckOutItem.of(1, p));
        Deal d = getTestDiscountDeal(p, () -> Discount.of("60%"));
        List<CheckOutItem> applied = d.getRule().toFunction().apply(items);

        Assert.assertEquals("Deal storage should have the right quantity", Integer.valueOf(1), applied.get(0).getQuantity());
        Assert.assertEquals(20.5, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(2, p));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(0), applied.get(0).getQuantity());
        Assert.assertEquals(20.5 * 1.6, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(3, p));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(1), applied.get(0).getQuantity());
        Assert.assertEquals(20.5 * 1.6 + 20.5, getTotalPrice(applied), precision);
    }

    @Test
    public void testCanCalculateAbsoluteDiscount() {
        Product p = getTestProduct();
        List<CheckOutItem> items = new ArrayList();
        items.add(CheckOutItem.of(1, p));
        Deal d = getTestDiscountDeal(p, () -> Discount.of("10"));
        List<CheckOutItem> applied = d.getRule().toFunction().apply(items);

        Assert.assertEquals("Deal storage should have the right quantity", Integer.valueOf(1), applied.get(0).getQuantity());
        Assert.assertEquals(20.5, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(2, p));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(0), applied.get(0).getQuantity());
        Assert.assertEquals(20.5 + 10.5, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(3, p));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(1), applied.get(0).getQuantity());
        Assert.assertEquals(20.5 * 2 + 10.5, getTotalPrice(applied), precision);
    }

    @Test
    public void testCanCalculateFreeDiscount() {
        Product p = getTestProduct();
        List<CheckOutItem> items = new ArrayList();
        items.add(CheckOutItem.of(1, p));
        Deal d = getTestDiscountDeal(p, () -> Discount.of("Free"));
        List<CheckOutItem> applied = d.getRule().toFunction().apply(items);

        Assert.assertEquals("Deal storage should have the right quantity", Integer.valueOf(1), applied.get(0).getQuantity());
        Assert.assertEquals(20.5, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(2, p));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(0), applied.get(0).getQuantity());
        Assert.assertEquals(20.5, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(3, p));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(1), applied.get(0).getQuantity());
        Assert.assertEquals(20.5 * 2, getTotalPrice(applied), precision);
    }

    @Test
    public void testCanCalculatePercentageBundle() {
        Product p1 = getTestProduct();
        Product p2 = productfactory.edit(getTestProduct()).withPrice(10.0).build();
        p2.save();
        List<CheckOutItem> items = new ArrayList();
        items.add(CheckOutItem.of(1, p1));
        Deal d = getTestBundleDeal(p1, p2, () -> Discount.of("60%"));
        List<CheckOutItem> applied = d.getRule().toFunction().apply(items);

        Assert.assertEquals("Deal storage should have the right quantity", Integer.valueOf(1), applied.get(0).getQuantity());
        Assert.assertEquals(20.5, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(1, p1));
        items.add(CheckOutItem.of(2, p2));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(1), applied.get(1).getQuantity());
        Assert.assertEquals(20.5 + 10.0 * 1.6, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(1, p1));
        items.add(CheckOutItem.of(1, p2));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(0), applied.get(1).getQuantity());
        Assert.assertEquals(20.5 + 0.6 * 10.0, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(2, p1));
        items.add(CheckOutItem.of(1, p2));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(0), applied.get(1).getQuantity());
        Assert.assertEquals(20.5 * 2 + 0.6 * 10.0, getTotalPrice(applied), precision);
    }

    @Test
    public void testCanCalculateAbsoluteBundle() {
        Product p1 = getTestProduct();
        Product p2 = productfactory.edit(getTestProduct()).withPrice(10.0).build();
        p2.save();
        List<CheckOutItem> items = new ArrayList();
        items.add(CheckOutItem.of(1, p1));
        Deal d = getTestBundleDeal(p1, p2, () -> Discount.of("9"));
        List<CheckOutItem> applied = d.getRule().toFunction().apply(items);

        Assert.assertEquals("Deal storage should have the right quantity", Integer.valueOf(1), applied.get(0).getQuantity());
        Assert.assertEquals(20.5, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(1, p1));
        items.add(CheckOutItem.of(2, p2));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(1), applied.get(1).getQuantity());
        Assert.assertEquals(20.5 + 10.0 + 1.0, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(1, p1));
        items.add(CheckOutItem.of(1, p2));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(0), applied.get(1).getQuantity());
        Assert.assertEquals(20.5 + 1.0, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(2, p1));
        items.add(CheckOutItem.of(1, p2));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(0), applied.get(1).getQuantity());
        Assert.assertEquals(20.5 * 2 + 1.0, getTotalPrice(applied), precision);
    }

    @Test
    public void testCanCalculateFreeBundle() {
        Product p1 = getTestProduct();
        Product p2 = productfactory.edit(getTestProduct()).withPrice(10.0).build();
        p2.save();
        List<CheckOutItem> items = new ArrayList();
        items.add(CheckOutItem.of(1, p1));
        Deal d = getTestBundleDeal(p1, p2, () -> Discount.of("Free"));
        List<CheckOutItem> applied = d.getRule().toFunction().apply(items);

        Assert.assertEquals("Deal storage should have the right quantity", Integer.valueOf(1), applied.get(0).getQuantity());
        Assert.assertEquals(20.5, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(1, p1));
        items.add(CheckOutItem.of(2, p2));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(1), applied.get(1).getQuantity());
        Assert.assertEquals(20.5 + 10.0, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(1, p1));
        items.add(CheckOutItem.of(1, p2));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(0), applied.get(1).getQuantity());
        Assert.assertEquals(20.5, getTotalPrice(applied), precision);

        items = new ArrayList();
        items.add(CheckOutItem.of(2, p1));
        items.add(CheckOutItem.of(1, p2));
        applied = d.getRule().toFunction().apply(items);
        Assert.assertEquals("Deal storage should have the right undiscounted quantity", Integer.valueOf(0), applied.get(1).getQuantity());
        Assert.assertEquals(20.5 * 2, getTotalPrice(applied), precision);
    }

    private double getTotalPrice(List<CheckOutItem> items) {
        return items.stream().map(i -> i.getTotalPrice()).collect(Collectors.summingDouble(Double::doubleValue));
    }


    private Product getTestProduct() {
        Product p = productfactory.builder()
                .withName("foo")
                .withPrice(20.5)
                .withQuantity(Quantity.of(1000L, QuantityType.Piece))
                .build();
        p.save();
        return p;
    }

    private Deal getTestDiscountDeal(Product p, Supplier<Discount> discountProvider) {
        return dealfactory.builder()
                .withName(discountDealName)
                .withDescription(discountDealDescription)
                .withRule(DiscountDeal.of(p, DiscountDeal.getOneDiscountAnother(discountProvider.get())))
                .build();
    }

    private Deal getTestBundleDeal(Product p1, Product p2, Supplier<Discount> discountProvider) {
        return dealfactory.builder()
                .withName(bundleDealName)
                .withDescription(bundleDealDescription)
                .withRule(BundleDeal.of(p1, p2, BundleDeal.getOneAndBundle(discountProvider.get())))
                .build();
    }
}
