package test.store;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import store.persistense.InMemoryPersistentService;
import store.product.*;
import store.product.deal.BundleDeal;
import store.product.deal.Deal;
import store.product.deal.DealFactory;
import store.product.deal.DiscountDeal;
import store.product.field.Discount;
import store.product.field.Quantity;
import store.product.field.QuantityType;
import store.user.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static java.util.stream.Collectors.*;

public class UserFactoryTest {
    private ProductFactory productfactory;
    private UserFactory userfactory;
    private DealFactory dealfactory;
    private ConcurrentHashMap<Long, Product> productStorage;
    private ConcurrentHashMap<Long, User> userStorage;
    private ConcurrentHashMap<Long, Basket> basketStorage;
    private ConcurrentHashMap<Long, Deal> dealStorage;
    private String testName = "foo";
    private String testEmail = "bar@foo.com";
    private Double precision = 1E-5;

    @Before
    public void init() {
        productStorage = new ConcurrentHashMap();
        userStorage = new ConcurrentHashMap();
        basketStorage = new ConcurrentHashMap();
        dealStorage = new ConcurrentHashMap();
        dealfactory = DealFactory.newInstance(InMemoryPersistentService.newInstance(dealStorage));
        productfactory = ProductFactory.newInstance(InMemoryPersistentService.newInstance(productStorage));
        userfactory = UserFactory.newInstance(
                InMemoryPersistentService.newInstance(userStorage),
                InMemoryPersistentService.newInstance(basketStorage));
    }

    @After
    public void tearDown() {
        productfactory.close();
        userfactory.close();
    }

    @Test
    public void testCanBuildUser() {
        User u = getTestUser();
        Assert.assertTrue("Product builder should be able to build", equalTestUser(u));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCanValidateEmtpyName() {
        userfactory.builder().withEmail(testEmail).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCanValidateEmtpyEmail() {
        userfactory.builder().withEmail(testName).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCanNotCheckoutWithNegativeQuantity() {
        Product p = getTestProduct();
        CheckOutItem.of(-1, p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCanNotCheckoutWithLargeQuantity() {
        Product p = getTestProduct();
        CheckOutItem.of(10000, p);
    }

    @Test
    public void testCanSaveUser() {
        User u = getTestUser();
        u.save();

        List<User> users = userfactory.list().collect(toList());
        Assert.assertEquals("User storage should have the right size", 1, users.size());
        Assert.assertEquals("Basket storage should have the right size", 1, basketStorage.values().size());
        Assert.assertTrue("User should be saved", equalTestUser(users.get(0)));
        Assert.assertTrue("Basket should be empty for new user",
                basketStorage.values().stream().findFirst().get().listItems().isEmpty());
    }

    @Test
    public void testCanEditAndSaveUser() {
        String email = "bar2@for.com";
        User u = getTestUser();
        u.save();
        userfactory.edit(u).withEmail(email).build().save();

        List<User> users = userfactory.list().collect(toList());
        Assert.assertEquals("User storage should have the right size", 1, users.size());
        Assert.assertTrue("Edit should be saved",
                users.get(0).getName().equals(testName)
                && users.get(0).getEmail().equals(email));
    }

    @Test
    public void testCanEditBasketAndSaveUser() {
        User u = getTestUser();
        u.save();
        Basket b = u.getBasket();
        Product p1 = getTestProduct();
        Product p2 = productfactory.edit(getTestProduct())
                .withName("foo2")
                .withPrice(25.3)
                .withQuantity(Quantity.of(20L, QuantityType.Piece))
                .build();
        p2.save();
        b.addItem(CheckOutItem.of(10, p1));
        b.addItem(CheckOutItem.of(5, p2));
        b.save();

        List<CheckOutItem> co = basketStorage.values().stream().findFirst().get().listItems();
        Assert.assertFalse("Basket should be not empty for after editing",
                co.isEmpty());
        Assert.assertTrue("Basket should contain first product",
                co.contains(CheckOutItem.of(10, p1)));
        Assert.assertTrue("Basket should contain second product",
                co.contains(CheckOutItem.of(5, p2)));

        b.addItem(CheckOutItem.of(19, p2));
        co = basketStorage.values().stream().findFirst().get().listItems();
        Assert.assertTrue("Basket should have no change of first product",
                co.contains(CheckOutItem.of(10, p1)));
        Assert.assertTrue("Basket should have second product updated",
                co.contains(CheckOutItem.of(19, p2)));

        b.removeItem(p1);
        co = basketStorage.values().stream().findFirst().get().listItems();
        Assert.assertEquals("Basket should only have one product left",
                1, co.size());
        Assert.assertTrue("Basket should contain second product",
                co.contains(CheckOutItem.of(19, p2)));
    }

    @Test
    public void testCanSaveBasketAndCalculateTotal() {
        User u = getTestUser();
        u.save();
        Basket b = u.getBasket();
        Product p1 = getTestProduct();
        Product p2 = productfactory.edit(getTestProduct())
                .withName("foo2")
                .withPrice(25.0)
                .withQuantity(Quantity.of(20L, QuantityType.Piece))
                .build();
        p2.save();
        b.addItem(CheckOutItem.of(4, p1));
        b.addItem(CheckOutItem.of(5, p2));
        b.save();

        Deal d1 = getTestDiscountDeal(p1, () -> Discount.of("50%"));
        d1.save();
        Deal d2 = getTestDiscountDeal(p2, () -> Discount.of("10"));
        d2.save();

        double totalPrice = b.totalPrice(dealfactory.list().collect(toList()));
        Assert.assertEquals(1.5 * 2 * p1.getPrice() + 5 * p2.getPrice() - 20, totalPrice, precision);

        d2.remove();
        totalPrice = b.totalPrice(dealfactory.list().collect(toList()));
        Assert.assertEquals(1.5 * 2 * p1.getPrice() + 5 * p2.getPrice(), totalPrice, precision);

        getTestDiscountDeal(p1, () -> Discount.of("10")).save();
        totalPrice = b.totalPrice(dealfactory.list().collect(toList()));
        Assert.assertEquals(1.5 * 2 * p1.getPrice() + 5 * p2.getPrice(), totalPrice, precision);

        b.addItem(CheckOutItem.of(5, p1));
        getTestBundleDeal(p1, p2, () -> Discount.of("Free")).save();
        totalPrice = b.totalPrice(dealfactory.list().collect(toList()));
        Assert.assertEquals(1.5 * 2 * p1.getPrice() + p1.getPrice() + 4 * p2.getPrice(), totalPrice, precision);
    }

    private User getTestUser() {
        return userfactory.builder()
                .withName(testName)
                .withEmail(testEmail)
                .build();
    }

    private Product getTestProduct() {
        Product p = productfactory.builder()
                .withName(testName)
                .withPrice(20.5)
                .withQuantity(Quantity.of(10L, QuantityType.Piece))
                .build();
        p.save();
        return p;
    }

    private boolean equalTestUser(User from) {
        return from.getName().equals(testName)
                && from.getEmail().equals(testEmail);
    }

    private Deal getTestDiscountDeal(Product p, Supplier<Discount> discountProvider) {
        return dealfactory.builder()
                .withName("foo")
                .withDescription("bar")
                .withRule(DiscountDeal.of(p, DiscountDeal.getOneDiscountAnother(discountProvider.get())))
                .build();
    }

    private Deal getTestBundleDeal(Product p1, Product p2, Supplier<Discount> discountProvider) {
        return dealfactory.builder()
                .withName("foo")
                .withDescription("bar")
                .withRule(BundleDeal.of(p1, p2, BundleDeal.getOneAndBundle(discountProvider.get())))
                .build();
    }
}
