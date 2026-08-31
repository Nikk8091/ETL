package com.nikk.config;

import com.nikk.entity.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerProcessorTests {

    private final CustomerProcessor processor = new CustomerProcessor();

    @Test
    void normalizesCustomerFields() {
        Customer customer = new Customer(1L, " Riya ", " Reddy ", " RIYA@EXAMPLE.COM ",
                " Mumbai ", " MH ", " India ", " 656538 ");

        Customer result = processor.process(customer);

        assertEquals("Riya", result.getFirstname());
        assertEquals("riya@example.com", result.getEmail());
        assertEquals("Mumbai", result.getCity());
        assertEquals("656538", result.getZipcode());
    }

    @Test
    void rejectsInvalidEmail() {
        Customer customer = new Customer(1L, "Riya", "Reddy", "not-an-email",
                "Mumbai", "MH", "India", "656538");

        assertThrows(CustomerValidationException.class, () -> processor.process(customer));
    }

    @Test
    void rejectsMissingRequiredFields() {
        Customer customer = new Customer(1L, " ", "Reddy", "riya@example.com",
                "Mumbai", "MH", "India", "656538");

        assertThrows(CustomerValidationException.class, () -> processor.process(customer));
    }
}
