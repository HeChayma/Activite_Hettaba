package com.example.billing_service;

import com.example.billing_service.entities.Bill;
import com.example.billing_service.entities.ProductItem;
import com.example.billing_service.feign.CustomerRestClient;
import com.example.billing_service.feign.ProductRestClient;
import com.example.billing_service.model.Customer;
import com.example.billing_service.model.Product;
import com.example.billing_service.repository.BillRepository;
import com.example.billing_service.repository.ProductItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.Collection;
import java.util.Date;
import java.util.Random;

@SpringBootApplication
@EnableFeignClients
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            BillRepository billRepository,
            ProductItemRepository productItemRepository,
            CustomerRestClient customerRestClient,
            ProductRestClient productRestClient) {

        return args -> {
            try {
                System.out.println("⏳ Fetching customers from Customer Service...");
                var customersPage = customerRestClient.getAllCustomers();
                Collection<Customer> customers = customersPage.getContent();

                System.out.println("⏳ Fetching products from Inventory Service...");
                var productsPage = productRestClient.getAllProducts();
                Collection<Product> products = productsPage.getContent();

                customers.forEach(customer -> {
                    Bill bill = Bill.builder()
                            .billingDate(new Date())
                            .customerId(customer.getId())
                            .build();
                    billRepository.save(bill);

                    products.forEach(product -> {
                        ProductItem productItem = ProductItem.builder()
                                .bill(bill)
                                .productId(product.getId())
                                .quantity(1 + new Random().nextInt(10))
                                .unitPrice(product.getPrice())
                                .build();
                        productItemRepository.save(productItem);
                    });
                });

                System.out.println("✅ Bills generated successfully!");

            } catch (Exception e) {
                System.err.println("⚠️ Error fetching data from services: " + e.getMessage());
                System.err.println("💡 Make sure inventory-service and customer-service are running and registered in Eureka.");
            }
        };
    }
}
