package com.ozzz.skip.demo.config;

import com.ozzz.skip.demo.model.*;
import com.ozzz.skip.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // Only seed if database is empty
        if (userRepository.count() > 0) {
            log.info("Database already has data — skipping seeder.");
            return;
        }

        log.info("========================================");
        log.info("  Starting database seeding...");
        log.info("========================================");

        List<User> users           = seedUsers();
        List<Category> categories  = seedCategories();
        seedProducts(users, categories);
        seedOrders(users);

        log.info("========================================");
        log.info("  Database seeding completed!");
        log.info("  Users    : {}", userRepository.count());
        log.info("  Categories: {}", categoryRepository.count());
        log.info("  Products : {}", productRepository.count());
        log.info("  Orders   : {}", orderRepository.count());
        log.info("========================================");
    }

    private List<User> seedUsers() {
        log.info("Seeding users...");

        String defaultPassword = passwordEncoder.encode("password123");

        User admin = User.builder()
                .username("admin")
                .email("admin@ecommerce.com")
                .password(defaultPassword)
                .fullName("System Administrator")
                .role(Role.ROLE_ADMIN)
                .address("1 Admin Plaza, San Francisco, CA")
                .phoneNumber("4155550001")
                .isActive(true)
                .build();

        User seller1 = User.builder()
                .username("techstore")
                .email("techstore@ecommerce.com")
                .password(defaultPassword)
                .fullName("Tech Store Official")
                .role(Role.ROLE_SELLER)
                .address("500 Market Street, San Francisco, CA")
                .phoneNumber("4155550002")
                .isActive(true)
                .build();

        User seller2 = User.builder()
                .username("fashionhub")
                .email("fashionhub@ecommerce.com")
                .password(defaultPassword)
                .fullName("Fashion Hub Store")
                .role(Role.ROLE_SELLER)
                .address("200 Fashion Ave, New York, NY")
                .phoneNumber("2125550003")
                .isActive(true)
                .build();

        User seller3 = User.builder()
                .username("homegarden")
                .email("homegarden@ecommerce.com")
                .password(defaultPassword)
                .fullName("Home and Garden Co")
                .role(Role.ROLE_SELLER)
                .address("300 Garden Road, Austin, TX")
                .phoneNumber("5125550004")
                .isActive(true)
                .build();

        User buyer1 = User.builder()
                .username("alice")
                .email("alice@example.com")
                .password(defaultPassword)
                .fullName("Alice Johnson")
                .role(Role.ROLE_BUYER)
                .address("123 Main Street, Boston, MA")
                .phoneNumber("6175550005")
                .isActive(true)
                .build();

        User buyer2 = User.builder()
                .username("bob")
                .email("bob@example.com")
                .password(defaultPassword)
                .fullName("Bob Smith")
                .role(Role.ROLE_BUYER)
                .address("456 Oak Avenue, Chicago, IL")
                .phoneNumber("3125550006")
                .isActive(true)
                .build();

        User buyer3 = User.builder()
                .username("carol")
                .email("carol@example.com")
                .password(defaultPassword)
                .fullName("Carol Williams")
                .role(Role.ROLE_BUYER)
                .address("789 Pine Road, Seattle, WA")
                .phoneNumber("2065550007")
                .isActive(true)
                .build();

        List<User> savedUsers = userRepository.saveAll(
                List.of(admin, seller1, seller2, seller3, buyer1, buyer2, buyer3));

        log.info("  Seeded {} users (password for all: password123)", savedUsers.size());
        return savedUsers;
    }

    private List<Category> seedCategories() {
        log.info("Seeding categories...");

        Category electronics = Category.builder()
                .name("Electronics")
                .description("Electronic devices, gadgets and accessories")
                .build();

        Category fashion = Category.builder()
                .name("Fashion")
                .description("Clothing, shoes, and accessories")
                .build();

        Category homeGarden = Category.builder()
                .name("Home & Garden")
                .description("Furniture, decor, and garden supplies")
                .build();

        Category sports = Category.builder()
                .name("Sports & Outdoors")
                .description("Sports equipment and outdoor gear")
                .build();

        electronics = categoryRepository.save(electronics);
        fashion     = categoryRepository.save(fashion);
        homeGarden  = categoryRepository.save(homeGarden);
        sports      = categoryRepository.save(sports);

        Category smartphones = Category.builder()
                .name("Smartphones")
                .description("Mobile phones and smartphones")
                .parent(electronics)
                .build();

        Category laptops = Category.builder()
                .name("Laptops")
                .description("Laptops and notebook computers")
                .parent(electronics)
                .build();

        Category audio = Category.builder()
                .name("Audio")
                .description("Headphones, speakers and audio equipment")
                .parent(electronics)
                .build();

        Category mensClothing = Category.builder()
                .name("Men's Clothing")
                .description("Clothing and apparel for men")
                .parent(fashion)
                .build();

        Category womensClothing = Category.builder()
                .name("Women's Clothing")
                .description("Clothing and apparel for women")
                .parent(fashion)
                .build();

        Category furniture = Category.builder()
                .name("Furniture")
                .description("Home and office furniture")
                .parent(homeGarden)
                .build();

        Category kitchenware = Category.builder()
                .name("Kitchenware")
                .description("Kitchen tools and appliances")
                .parent(homeGarden)
                .build();

        List<Category> subCategories = categoryRepository.saveAll(List.of(
                smartphones, laptops, audio,
                mensClothing, womensClothing,
                furniture, kitchenware));

        log.info("  Seeded 4 parent categories and {} sub-categories",
                subCategories.size());

        return categoryRepository.findAll();
    }

    private void seedProducts(List<User> users, List<Category> categories) {
        log.info("Seeding products...");

        User techSeller    = getUserByUsername(users, "techstore");
        User fashionSeller = getUserByUsername(users, "fashionhub");
        User homeSeller    = getUserByUsername(users, "homegarden");

        Category smartphones   = getCategoryByName(categories, "Smartphones");
        Category laptops       = getCategoryByName(categories, "Laptops");
        Category audio         = getCategoryByName(categories, "Audio");
        Category mensClothing  = getCategoryByName(categories, "Men's Clothing");
        Category womensClothing= getCategoryByName(categories, "Women's Clothing");
        Category furniture     = getCategoryByName(categories, "Furniture");
        Category kitchenware   = getCategoryByName(categories, "Kitchenware");

        Product iphone = Product.builder()
                .name("Apple iPhone 15 Pro")
                .description("The latest iPhone with titanium design, A17 Pro chip, " +
                        "and a 48MP main camera. Available in 256GB storage.")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .imageUrl("https://example.com/images/iphone15pro.jpg")
                .status(ProductStatus.ACTIVE)
                .category(smartphones)
                .seller(techSeller)
                .build();

        Product samsung = Product.builder()
                .name("Samsung Galaxy S24 Ultra")
                .description("Premium Android flagship with S Pen, " +
                        "200MP camera, and Snapdragon 8 Gen 3 processor.")
                .price(new BigDecimal("1199.99"))
                .stockQuantity(35)
                .imageUrl("https://example.com/images/s24ultra.jpg")
                .status(ProductStatus.ACTIVE)
                .category(smartphones)
                .seller(techSeller)
                .build();

        Product pixel = Product.builder()
                .name("Google Pixel 8 Pro")
                .description("Google's flagship phone with Tensor G3 chip, " +
                        "best-in-class computational photography.")
                .price(new BigDecimal("799.99"))
                .stockQuantity(40)
                .imageUrl("https://example.com/images/pixel8pro.jpg")
                .status(ProductStatus.ACTIVE)
                .category(smartphones)
                .seller(techSeller)
                .build();

        Product macbook = Product.builder()
                .name("Apple MacBook Pro 14-inch M3")
                .description("Supercharged by M3 Pro chip, 18GB unified memory, " +
                        "Liquid Retina XDR display, 18-hour battery life.")
                .price(new BigDecimal("1999.99"))
                .stockQuantity(20)
                .imageUrl("https://example.com/images/macbookpro14.jpg")
                .status(ProductStatus.ACTIVE)
                .category(laptops)
                .seller(techSeller)
                .build();

        Product dellXps = Product.builder()
                .name("Dell XPS 15 Laptop")
                .description("Intel Core i9, 32GB RAM, 1TB SSD, " +
                        "OLED 4K display, NVIDIA RTX 4070.")
                .price(new BigDecimal("1799.99"))
                .stockQuantity(15)
                .imageUrl("https://example.com/images/dellxps15.jpg")
                .status(ProductStatus.ACTIVE)
                .category(laptops)
                .seller(techSeller)
                .build();

        Product airpods = Product.builder()
                .name("Apple AirPods Pro 2nd Gen")
                .description("Active noise cancellation, Adaptive Transparency, " +
                        "Personalized Spatial Audio, MagSafe charging case.")
                .price(new BigDecimal("249.99"))
                .stockQuantity(100)
                .imageUrl("https://example.com/images/airpodspro2.jpg")
                .status(ProductStatus.ACTIVE)
                .category(audio)
                .seller(techSeller)
                .build();

        Product sonyHeadphones = Product.builder()
                .name("Sony WH-1000XM5 Headphones")
                .description("Industry-leading noise cancellation, " +
                        "30-hour battery, multipoint connection.")
                .price(new BigDecimal("349.99"))
                .stockQuantity(60)
                .imageUrl("https://example.com/images/sonywh1000xm5.jpg")
                .status(ProductStatus.ACTIVE)
                .category(audio)
                .seller(techSeller)
                .build();

        Product mensJacket = Product.builder()
                .name("Classic Wool Overcoat")
                .description("Premium wool blend overcoat, slim fit design, " +
                        "available in sizes S to XXL. Perfect for winter.")
                .price(new BigDecimal("189.99"))
                .stockQuantity(80)
                .imageUrl("https://example.com/images/woolovercoat.jpg")
                .status(ProductStatus.ACTIVE)
                .category(mensClothing)
                .seller(fashionSeller)
                .build();

        Product mensShirts = Product.builder()
                .name("Slim Fit Oxford Shirt Pack (3x)")
                .description("Pack of 3 premium cotton Oxford shirts, " +
                        "wrinkle-resistant, available in multiple colors.")
                .price(new BigDecimal("79.99"))
                .stockQuantity(120)
                .imageUrl("https://example.com/images/oxfordshirts.jpg")
                .status(ProductStatus.ACTIVE)
                .category(mensClothing)
                .seller(fashionSeller)
                .build();

        Product womensDress = Product.builder()
                .name("Floral Midi Wrap Dress")
                .description("Elegant wrap dress with floral print, " +
                        "V-neckline, adjustable waist tie. Perfect for all seasons.")
                .price(new BigDecimal("69.99"))
                .stockQuantity(90)
                .imageUrl("https://example.com/images/floraldress.jpg")
                .status(ProductStatus.ACTIVE)
                .category(womensClothing)
                .seller(fashionSeller)
                .build();

        Product womensJacket = Product.builder()
                .name("Leather Biker Jacket")
                .description("Genuine leather biker jacket, " +
                        "zip-up front, multiple pockets, classic design.")
                .price(new BigDecimal("249.99"))
                .stockQuantity(45)
                .imageUrl("https://example.com/images/leatherjacket.jpg")
                .status(ProductStatus.ACTIVE)
                .category(womensClothing)
                .seller(fashionSeller)
                .build();

        Product sofa = Product.builder()
                .name("3-Seater Linen Sofa")
                .description("Modern 3-seater sofa in premium linen fabric, " +
                        "solid wood legs, removable cushion covers.")
                .price(new BigDecimal("899.99"))
                .stockQuantity(10)
                .imageUrl("https://example.com/images/linensofa.jpg")
                .status(ProductStatus.ACTIVE)
                .category(furniture)
                .seller(homeSeller)
                .build();

        Product deskChair = Product.builder()
                .name("Ergonomic Office Chair")
                .description("Fully adjustable ergonomic chair with lumbar support, " +
                        "breathable mesh back, 4D armrests.")
                .price(new BigDecimal("449.99"))
                .stockQuantity(25)
                .imageUrl("https://example.com/images/ergonomicchair.jpg")
                .status(ProductStatus.ACTIVE)
                .category(furniture)
                .seller(homeSeller)
                .build();

        Product coffeeMaker = Product.builder()
                .name("Breville Barista Express Espresso Machine")
                .description("Built-in grinder, 15-bar Italian pump, " +
                        "precise espresso extraction, steam wand for milk texturing.")
                .price(new BigDecimal("699.99"))
                .stockQuantity(30)
                .imageUrl("https://example.com/images/brevilleespresso.jpg")
                .status(ProductStatus.ACTIVE)
                .category(kitchenware)
                .seller(homeSeller)
                .build();

        Product airFryer = Product.builder()
                .name("Ninja AF161 Air Fryer Max XL")
                .description("5.5-quart capacity, up to 450°F, removes 75% more fat, " +
                        "dishwasher-safe parts.")
                .price(new BigDecimal("129.99"))
                .stockQuantity(55)
                .imageUrl("https://example.com/images/ninjaairfryer.jpg")
                .status(ProductStatus.ACTIVE)
                .category(kitchenware)
                .seller(homeSeller)
                .build();

        productRepository.saveAll(List.of(
                iphone, samsung, pixel,
                macbook, dellXps,
                airpods, sonyHeadphones,
                mensJacket, mensShirts,
                womensDress, womensJacket,
                sofa, deskChair,
                coffeeMaker, airFryer));

        log.info("  Seeded 15 products across 7 categories");
    }

    private void seedOrders(List<User> users) {
        log.info("Seeding orders...");

        User alice = getUserByUsername(users, "alice");
        User bob   = getUserByUsername(users, "bob");
        User carol = getUserByUsername(users, "carol");

        List<Product> allProducts = productRepository.findAll();

        Product iphone         = getProductByName(allProducts, "Apple iPhone 15 Pro");
        Product airpods        = getProductByName(allProducts, "Apple AirPods Pro 2nd Gen");
        Product macbook        = getProductByName(allProducts, "Apple MacBook Pro 14-inch M3");
        Product womensDress    = getProductByName(allProducts, "Floral Midi Wrap Dress");
        Product coffeeMaker    = getProductByName(allProducts, "Breville Barista Express Espresso Machine");
        Product sonyHeadphones = getProductByName(allProducts, "Sony WH-1000XM5 Headphones");

        OrderItem aliceItem1 = OrderItem.builder()
                .product(iphone)
                .quantity(1)
                .unitPrice(iphone.getPrice())
                .build();

        OrderItem aliceItem2 = OrderItem.builder()
                .product(airpods)
                .quantity(2)
                .unitPrice(airpods.getPrice())
                .build();

        BigDecimal aliceTotal = iphone.getPrice()
                .add(airpods.getPrice().multiply(BigDecimal.valueOf(2)));

        Order aliceOrder = Order.builder()
                .buyer(alice)
                .status(OrderStatus.DELIVERED)
                .shippingAddress("123 Main Street, Boston, MA 02101")
                .paymentMethod("CREDIT_CARD")
                .totalAmount(aliceTotal)
                .notes("Leave at door if no answer")
                .build();

        aliceItem1.setOrder(aliceOrder);
        aliceItem2.setOrder(aliceOrder);
        aliceOrder.setOrderItems(List.of(aliceItem1, aliceItem2));

        OrderItem bobItem1 = OrderItem.builder()
                .product(macbook)
                .quantity(1)
                .unitPrice(macbook.getPrice())
                .build();

        Order bobOrder = Order.builder()
                .buyer(bob)
                .status(OrderStatus.SHIPPED)
                .shippingAddress("456 Oak Avenue, Chicago, IL 60601")
                .paymentMethod("PAYPAL")
                .totalAmount(macbook.getPrice())
                .build();

        bobItem1.setOrder(bobOrder);
        bobOrder.setOrderItems(List.of(bobItem1));

        OrderItem carolItem1 = OrderItem.builder()
                .product(womensDress)
                .quantity(2)
                .unitPrice(womensDress.getPrice())
                .build();

        OrderItem carolItem2 = OrderItem.builder()
                .product(coffeeMaker)
                .quantity(1)
                .unitPrice(coffeeMaker.getPrice())
                .build();

        OrderItem carolItem3 = OrderItem.builder()
                .product(sonyHeadphones)
                .quantity(1)
                .unitPrice(sonyHeadphones.getPrice())
                .build();

        BigDecimal carolTotal = womensDress.getPrice().multiply(BigDecimal.valueOf(2))
                .add(coffeeMaker.getPrice())
                .add(sonyHeadphones.getPrice());

        Order carolOrder = Order.builder()
                .buyer(carol)
                .status(OrderStatus.PENDING)
                .shippingAddress("789 Pine Road, Seattle, WA 98101")
                .paymentMethod("DEBIT_CARD")
                .totalAmount(carolTotal)
                .notes("Please gift wrap the dress")
                .build();

        carolItem1.setOrder(carolOrder);
        carolItem2.setOrder(carolOrder);
        carolItem3.setOrder(carolOrder);
        carolOrder.setOrderItems(List.of(carolItem1, carolItem2, carolItem3));

        OrderItem aliceItem3 = OrderItem.builder()
                .product(sonyHeadphones)
                .quantity(1)
                .unitPrice(sonyHeadphones.getPrice())
                .build();

        Order aliceOrder2 = Order.builder()
                .buyer(alice)
                .status(OrderStatus.CONFIRMED)
                .shippingAddress("123 Main Street, Boston, MA 02101")
                .paymentMethod("CREDIT_CARD")
                .totalAmount(sonyHeadphones.getPrice())
                .build();

        aliceItem3.setOrder(aliceOrder2);
        aliceOrder2.setOrderItems(List.of(aliceItem3));

        orderRepository.saveAll(List.of(aliceOrder, bobOrder, carolOrder, aliceOrder2));

        iphone.setStockQuantity(iphone.getStockQuantity() - 1);
        airpods.setStockQuantity(airpods.getStockQuantity() - 2);
        macbook.setStockQuantity(macbook.getStockQuantity() - 1);
        womensDress.setStockQuantity(womensDress.getStockQuantity() - 2);
        coffeeMaker.setStockQuantity(coffeeMaker.getStockQuantity() - 1);
        sonyHeadphones.setStockQuantity(sonyHeadphones.getStockQuantity() - 2);
        productRepository.saveAll(List.of(
                iphone, airpods, macbook, womensDress, coffeeMaker, sonyHeadphones));

        log.info("  Seeded 4 orders (DELIVERED, SHIPPED, PENDING, CONFIRMED)");
    }

    private User getUserByUsername(List<User> users, String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Seeder: User not found: " + username));
    }

    private Category getCategoryByName(List<Category> categories, String name) {
        return categories.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Seeder: Category not found: " + name));
    }

    private Product getProductByName(List<Product> products, String name) {
        return products.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Seeder: Product not found: " + name));
    }
}