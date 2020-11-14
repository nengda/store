package test.store;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import store.persistense.InMemoryPersistentService;
import store.product.*;
import store.product.deal.Deal;
import store.product.deal.DealFactory;
import store.product.deal.DealRule;
import store.product.deal.DiscountDeal;
import store.product.field.Discount;
import store.product.field.Quantity;
import store.product.field.QuantityType;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

public class DealFactoryTest {
    private DealFactory dealfactory;
    private ProductFactory productfactory;
    private ConcurrentHashMap<Long, Deal> dealStorage;
    private String discountDealName = "foo";
    private String discountDealDescription = "buy foo get 50% off second";

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
    public void testCanBuildDeal() {
        Product p = getTestProduct();
        Deal d = getTestDeal(p);
        Assert.assertTrue("Deal builder should be able to build", equalTestDeal(d));
    }

    @Test(expected =  IllegalArgumentException.class)
    public void testCanValidateEmtpyName() {
        dealfactory.builder()
                .withDescription(discountDealDescription)
                .withRule(DiscountDeal.of(
                        getTestProduct(), DiscountDeal.getOneDiscountAnother(getTestDiscount())))
                .build();
    }

    @Test(expected =  IllegalArgumentException.class)
    public void testCanValidateEmtpyDescription() {
        dealfactory.builder()
                .withName(discountDealName)
                .withRule(DiscountDeal.of(
                        getTestProduct(), DiscountDeal.getOneDiscountAnother(getTestDiscount())))
                .build();
    }

    @Test(expected =  IllegalArgumentException.class)
    public void testCanValidateEmtpyRule() {
        dealfactory.builder()
                .withName(discountDealName)
                .withDescription(discountDealDescription)
                .build();
    }

    @Test
    public void testCanSaveDeal() {
        Product p = getTestProduct();
        Deal d = getTestDeal(p);
        d.save();

        List<Deal> deals = dealfactory.list().collect(toList());
        Assert.assertEquals("Deal storage should have the right size", 1, deals.size());
        Assert.assertTrue("Deal should be saved", equalTestDeal(deals.get(0)));
    }

    @Test
    public void testCanSaveAndEditDeal() {
        Product p = getTestProduct();
        Deal d1 = getTestDeal(p);
        d1.save();

        List<Deal> deals = dealfactory.list().collect(toList());
        Product p2 = productfactory.edit(getTestProduct()).withName("product2").build();
        p2.save();
        Deal d2 = dealfactory.edit(deals.get(0))
                .withName("bar")
                .withRule(DiscountDeal.of(p2, DiscountDeal.getOneDiscountAnother(getTestDiscount())))
                .build();
        d2.save();

        deals = dealfactory.list().collect(toList());
        Assert.assertEquals("Deal storage should have the right size", 1, deals.size());
        Assert.assertNotEquals("Edited deal should have different name", d1.getName(), d2.getName());
        Assert.assertEquals("Edited deal should have the same description", d1.getDescription(), d2.getDescription());
    }

    @Test
    public void testCanSaveRemoveDeal() {
        Deal d1 = getTestDeal(getTestProduct());
        d1.save();
        Deal d2 = getTestDeal(getTestProduct());
        d2.save();

        List<Deal> deals = dealfactory.list().collect(toList());
        Assert.assertEquals("Deal storage should have the right size", 2, deals.size());

        d2.remove();
        deals = dealfactory.list().collect(toList());
        Assert.assertEquals("Deal storage should have the right size after remove", 1, deals.size());

        Deal d3 = getTestDeal(getTestProduct());
        d3.remove();
        deals = dealfactory.list().collect(toList());
        Assert.assertEquals("Deal storage should have the right size after noop remove", 1, deals.size());
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

    private Deal getTestDeal(Product p) {
        return dealfactory.builder()
                .withName(discountDealName)
                .withDescription(discountDealDescription)
                .withRule(getTestRule(p))
                .build();
    }

    private DealRule getTestRule(Product p) {
        return DiscountDeal.of(p, DiscountDeal.getOneDiscountAnother(getTestDiscount()));
    }

    private Discount getTestDiscount() {
        return Discount.of("50%");
    }

    private boolean equalTestDeal(Deal from) {
        return from.getName().equals(discountDealName)
                && from.getDescription().equals(discountDealDescription)
                && (from.getRule() instanceof DiscountDeal);
    }

}
