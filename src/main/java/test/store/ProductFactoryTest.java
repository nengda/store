package test.store;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import store.persistense.InMemoryPersistentService;
import store.product.*;
import store.product.field.Quantity;
import store.product.field.QuantityType;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.stream.Collectors.*;

public class ProductFactoryTest {

    private ProductFactory factory;
    private ConcurrentHashMap<Long, Product> inMemoryStorage;
    private String testName = "test";
    private Quantity testQuantity = Quantity.of(100L, QuantityType.Piece);
    private Double testPrice = 100.4;
    private String testCategory = "category 1";
    private String testBrand = "brand 1";
    private String testSpec = "spec 1";

    @Before
    public void init() {
        inMemoryStorage = new ConcurrentHashMap();
        factory = ProductFactory.newInstance(InMemoryPersistentService.newInstance(inMemoryStorage));
    }

    @After
    public void tearDown() {
        factory.close();
    }

    @Test(expected =  IllegalArgumentException.class)
    public void testCanValidateInvalidQuantity() {
        Quantity d2 = Quantity.of(-100L, QuantityType.Piece);
    }

    @Test
    public void testCanBuildProduct() {
        Product p = getTestData();

        Assert.assertTrue("Product builder should be able to build", equalTestData(p));

        Product p1 = factory.builder().withName(testName).build();
        Product p2 = factory.builder().build();
        Assert.assertTrue("Product builder should be able to build with default product value",
                p1.getBrand().equals(p2.getBrand())
                        && p1.getSpec().equals(p2.getSpec())
                        && p1.getQuantity().equals(p2.getQuantity())
                        && p1.getCategory().equals(p2.getCategory())
                        && p1.getPrice().equals(p2.getPrice())
                        && p1.getName().equals(testName)
        );
    }

    @Test
    public void testCanStoreProduct() {
        Product p = getTestData();
        p.save();

        List<Product> products = factory.list().collect(toList());
        Assert.assertEquals("Stored product count should be correct", 1, products.size());
        Assert.assertTrue("Stored product content should be correct", equalTestData(products.get(0)));
    }

    @Test
    public void testCanStoreDuplicatedProduct() {
        getTestData().save();
        getTestData().save();

        List<Product> products = factory.list().collect(toList());
        Assert.assertEquals("Stored product count should be correct", 2, products.size());
        Assert.assertTrue("First item should be correct", equalTestData(products.get(0)));
        Assert.assertTrue("Second item should be correct", products.get(1).equals(products.get(0)));
    }

    @Test
    public void testCanEqualProduct() {
        Product p1 = getTestData();
        Product p2 = getTestData();
        Assert.assertEquals("Duplicated products should equal", p1, p2);
        Assert.assertEquals("Duplicated products' hashCode should equal", p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testCanEditProduct() {
        Product p1 = getTestData();
        Product p2 = factory.edit(p1).withPrice(34.5).build();
        Assert.assertNotEquals("Edited products should not equal", p1, p2);
        Assert.assertNotEquals("Edited products' hashCode should not equal", p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testCanEditAndSaveProduct() {
        Product p1 = getTestData();
        p1.save();
        Product p2 = factory.edit(p1).withPrice(34.5).build();
        p2.save();

        List<Product> products = factory.list().collect(toList());
        Assert.assertEquals("Stored product count should be correct", 1, products.size());
        Assert.assertNotEquals("Stored product should not equal to orignal product", p1, products.get(0));
        Assert.assertEquals("Stored product should equal to edited product", p2, products.get(0));

        Product p3 = factory.edit(products.get(0)).withQuantity(Quantity.of(50L, QuantityType.Piece)).build();
        p3.save();

        products = factory.list().collect(toList());
        Assert.assertEquals("Stored product count should be correct", 1, products.size());
        Assert.assertNotEquals("Stored product should not equal to origin product", p1, products.get(0));
        Assert.assertNotEquals("Stored product should not equal to old edited product", p2, products.get(0));
        Assert.assertEquals("Stored product should equal to edited product", p3, products.get(0));
    }

    @Test
    public void testCanEditAndRemoveProduct() {
        Product p1 = getTestData();
        p1.save();
        Product p2 = factory.edit(p1).withPrice(34.5).build();
        p2.save();

        List<Product> products = factory.list().collect(toList());
        Assert.assertEquals("Stored product count should be correct", 1, products.size());

        p2.remove();
        products = factory.list().collect(toList());
        Assert.assertTrue("Stored product should be empty after removal", products.isEmpty());
    }

    @Test
    public void testCanSaveAndRemoveProduct() {
        getTestData().save();
        getTestData().save();

        List<Product> products = factory.list().collect(toList());
        Assert.assertEquals("Stored product count should be correct", 2, products.size());

        factory.edit(products.get(0)).withBrand("Intel").build().save();
        products = factory.list().collect(toList());
        Assert.assertEquals("Stored product count should be correct after edit", 2, products.size());

        getTestData().remove();
        products = factory.list().collect(toList());
        Assert.assertEquals("Stored product count should be correct after removing unsaved product", 2, products.size());

        products.get(0).remove();
        products.get(1).remove();
        products = factory.list().collect(toList());
        Assert.assertTrue("Stored product should be empty after removal", products.isEmpty());
    }

    private Product getTestData() {
        return factory.builder()
                .withName(testName)
                .withBrand(testBrand)
                .withCategory(testCategory)
                .withPrice(testPrice)
                .withQuantity(testQuantity)
                .withSpec(testSpec)
                .build();
    }

    private boolean equalTestData(Product from) {
        return from.getBrand().equals(testBrand)
                && from.getCategory().equals(testCategory)
                && from.getName().equals(testName)
                && from.getPrice().equals(testPrice)
                && from.getQuantity().equals(testQuantity)
                && from.getSpec().equals(testSpec);
    }

}
