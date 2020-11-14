package store;

import store.product.Product;
import store.product.ProductFactory;
import store.product.deal.BundleDeal;
import store.product.deal.DealFactory;
import store.product.deal.DiscountDeal;
import store.product.field.Discount;
import store.product.field.Quantity;
import store.product.field.QuantityType;
import store.user.CheckOutItem;
import store.user.User;
import store.user.UserFactory;

import java.util.stream.Collectors;

public class StoreDemo {

    public static void main(String[] args) {
        message("Start Store Admin");
        ProductFactory productFactory = ProductFactory.newInstance();
        message("Create product: IBM Laptop T2000 at 6000 dollars, 1000 pieces");
        Product laptop = productFactory.builder()
                .withName("T2000")
                .withBrand("IBM")
                .withCategory("Laptop")
                .withQuantity(Quantity.of(1000L, QuantityType.Piece))
                .withSpec("Dual core CPU, 4GB RAM")
                .withPrice(6000.0).build();
        laptop.save();
        message(productFactory, "product");

        message("Edit product: quantity 1000 -> 1500 pieces, price 6000 -> 5500");
        laptop = productFactory.edit(laptop)
                .withPrice(5500.0)
                .withQuantity(Quantity.of(1500L, QuantityType.Piece))
                .build();
        laptop.save();
        message(productFactory, "product");

        message("Create product: Logitech Mouse xP500 at 150 dollars, 15000 pieces");
        Product mouse = productFactory.builder()
                .withName("xP500")
                .withBrand("Logitech")
                .withCategory("Mouse")
                .withQuantity(Quantity.of(15000L, QuantityType.Piece))
                .withSpec("Bluetooth, Solar Power, Clean Energy")
                .withPrice(150.0).build();
        mouse.save();
        message(productFactory, "product");

        message("Create product: Logitech Keyboard xK400 at 250 dollars, 20000 pieces");
        Product keyboard = productFactory.builder()
                .withName("xK400")
                .withBrand("Logitech")
                .withCategory("Keyboard")
                .withQuantity(Quantity.of(20000L, QuantityType.Piece))
                .withSpec("Bluetooth, Mechanical")
                .withPrice(250.0).build();
        keyboard.save();
        message(productFactory, "product");

        message("Remove product: Logitech Keyboard xK400");
        keyboard.remove();
        message(productFactory, "product");

        DealFactory dealFactory = DealFactory.newInstance();
        message("Create deal: Buy 1 IBM Laptop T2000 get 50% off the second");
        dealFactory.builder()
                .withName("Thanks Giving Discount")
                .withDescription("Buy 1 IBM Laptop T2000 get 50% off the second")
                .withRule(DiscountDeal.of(
                        laptop,
                        DiscountDeal.getOneDiscountAnother(Discount.of("50%"))))
                .build().save();
        message(dealFactory, "deal");

        message("Create deal: Buy 1 IBM Laptop T2000 get a Mouse free");
        dealFactory.builder()
                .withName("Chrismas Bundle Deal")
                .withDescription("Buy 1 IBM Laptop T2000 get a Mouse free")
                .withRule(BundleDeal.of(
                        laptop,
                        mouse,
                        BundleDeal.getOneAndBundle(Discount.of("free"))))
                .build().save();
        message(dealFactory, "deal");

        message("Start Customer Checkout");
        message("Create new user: Nengda Jin");
        UserFactory userFactory = UserFactory.newInstance();
        User user = userFactory.builder()
                .withName("Nengda Jin")
                .withEmail("nengda.jin@gmail.com")
                .build();
        user.save();
        message(userFactory, "user");

        message("Add 10 IBM laptop to basket");
        user.getBasket().addItem(CheckOutItem.of(10, laptop));
        message(userFactory, "user");
        message(dealFactory, user);

        message("Change IBM laptop quanity to 5");
        user.getBasket().addItem(CheckOutItem.of(5, laptop));
        message(userFactory, "user");
        message(dealFactory, user);

        message("Add 5 Logitech mouse to basket");
        user.getBasket().addItem(CheckOutItem.of(5, mouse));
        message(userFactory, "user");
        message(dealFactory, user);

        message("Remove Logitech mouse from basket");
        user.getBasket().removeItem(mouse);
        message(userFactory, "user");
        message(dealFactory, user);

    }

    private static void message(String msg) {
        System.out.println("[Store Demo]: " + msg);
    }

    private static void message(Listable listable, String type) {
        message("Store now has " + type + ": " + System.lineSeparator()
                + listable.list()
                .map(l -> l.toString())
                .collect(Collectors.joining(System.lineSeparator())));
    }

    private static void message(DealFactory factory, User user) {
        message("The total price is : " + user.getBasket()
                .totalPrice(factory.list().collect(Collectors.toList())));
    }
}
