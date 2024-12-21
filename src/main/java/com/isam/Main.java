package com.isam;

import com.isam.dao.*;
import com.isam.domain.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Main {

    private final SessionFactory sessionFactory;

    private final ActorDAO actorDAO;
    private final AddressDAO addressDAO;
    private final CategoryDAO categoryDAO;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;
    private final CustomerDAO customerDAO;
    private final FilmDAO filmDAO;
    private final FilmTextDAO filmTextDAO;
    private final InventoryDAO inventoryDAO;
    private final LanguageDAO languageDAO;
    private final PaymentDAO paymentDAO;
    private final RentalDAO rentalDAO;
    private final StaffDAO staffDAO;
    private final StoreDAO storeDAO;

    public Main() {
        Properties properties = getProperties();
        sessionFactory = new Configuration()
                .addAnnotatedClass(Actor.class)
                .addAnnotatedClass(Address.class)
                .addAnnotatedClass(Category.class)
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(Customer.class)
                .addAnnotatedClass(Film.class)
                .addAnnotatedClass(FilmText.class)
                .addAnnotatedClass(Inventory.class)
                .addAnnotatedClass(Language.class)
                .addAnnotatedClass(Payment.class)
                .addAnnotatedClass(Rental.class)
                .addAnnotatedClass(Staff.class)
                .addAnnotatedClass(Store.class)
                .addProperties(properties)
                .buildSessionFactory();


        actorDAO = new ActorDAO(sessionFactory);
        addressDAO = new AddressDAO(sessionFactory);
        categoryDAO = new CategoryDAO(sessionFactory);
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        customerDAO = new CustomerDAO(sessionFactory);
        filmDAO = new FilmDAO(sessionFactory);
        filmTextDAO = new FilmTextDAO(sessionFactory);
        inventoryDAO = new InventoryDAO(sessionFactory);
        languageDAO = new LanguageDAO(sessionFactory);
        paymentDAO = new PaymentDAO(sessionFactory);
        rentalDAO = new RentalDAO(sessionFactory);
        staffDAO = new StaffDAO(sessionFactory);
        storeDAO = new StoreDAO(sessionFactory);
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/movie");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "&Y3l9RRQ01d");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        properties.put(Environment.HBM2DDL_AUTO, "validate");
        return properties;
    }

    public static void main(String[] args) {
        Main main = new Main();

        Customer customer= main.createCustomer();
        main.customerReturnsInventoryToStore();
        main.customerRentsInventory(customer);
        main.newFilmMade();
    }

    private void newFilmMade() {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Language language = languageDAO.getItems(0,20).stream().unordered().findAny().get();
            List<Category> categories= categoryDAO.getItems(0,5);
            List<Actor> actors= actorDAO.getItems(0,15);

            Film film= new Film();
            film.setActors(new HashSet<>(actors));
            film.setLanguage(language);
            film.setOriginalLanguage(language);
            film.setDescription("new film");
            film.setTitle("Newest Film 1");
            film.setRating(Rating.R);
            film.setSpecialFeatures(Set.of(Feature.TRAILER, Feature.COMMENTARIES));
            film.setLength((short) 130);
            film.setReplacementCost(BigDecimal.TEN);
            film.setRentalRate(BigDecimal.ZERO);
            film.setRentalDuration((byte)54);
            film.setYear(Year.now());
            filmDAO.save(film);

            FilmText filmText= new FilmText();
            filmText.setFilm(film);
            filmText.setDescription("new film");
            filmText.setTitle("Newest Film 1");
            filmTextDAO.save(filmText);

            session.getTransaction().commit();
        }

    }

    private void customerRentsInventory(Customer customer) {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Film film= filmDAO.getFirstAvailableFilmForRent();

            Store store = storeDAO.getItems(0,1).get(0);

            Inventory inventory=  new Inventory();
            inventory.setFilm(film);
            inventory.setStore(store);
            inventoryDAO.save(inventory);

            Staff staff= store.getStaff();

            Rental rental= new Rental();
            rental.setRentalDate(LocalDateTime.now());
            rental.setInventory(inventory);
            rental.setCustomer(customer);
            rental.setStaff(staff);
            rentalDAO.save(rental);

            Payment payment= new Payment();
            payment.setAmount(BigDecimal.valueOf(9.99));
            payment.setPaymentDate(LocalDateTime.now());
            payment.setCustomer(customer);
            payment.setRental(rental);
            payment.setStaff(staff);
            paymentDAO.save(payment);

            session.getTransaction().commit();
        }
    }

    private void customerReturnsInventoryToStore() {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Rental rental= rentalDAO.getAnyUnreturnedRental();
            rental.setReturnDate(LocalDateTime.now());
            rentalDAO.save(rental);


            session.getTransaction().commit();
        }
    }

    private Customer createCustomer() {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Store store = storeDAO.getItems(0,1).get(0);

            City city = cityDAO.getByName("Aden");

            Address address = new Address();
            address.setAddress("Test street");
            address.setDistrict("Tester mile");
            address.setCity(city);
            address.setPhone("123-456-789");
            addressDAO.save(address);

            Customer customer= new Customer();
            customer.setFirstName("Test");
            customer.setLastName("Tester");
            customer.setEmail("TestTer@test.com");
            customer.setAddress(address);
            customer.setActive(true);
            customer.setStore(store);
            customerDAO.save(customer);

            session.getTransaction().commit();
            return customer;
        }
    }
}
