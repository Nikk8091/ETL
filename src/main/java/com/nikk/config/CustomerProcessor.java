package com.nikk.config;

import com.nikk.entity.Customer;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.util.Locale;
import java.util.regex.Pattern;

public class CustomerProcessor implements ItemProcessor<Customer,Customer> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public Customer process(Customer item) {
        if (item.getCustomerId() == null || item.getCustomerId() <= 0) {
            throw new CustomerValidationException("customerId must be a positive number");
        }
        item.setFirstname(required(item.getFirstname(), "firstname"));
        item.setLastname(required(item.getLastname(), "lastname"));

        String email = required(item.getEmail(), "email").toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new CustomerValidationException("invalid email for customer " + item.getCustomerId());
        }
        item.setEmail(email);
        item.setCity(normalize(item.getCity()));
        item.setState(normalize(item.getState()));
        item.setCountry(normalize(item.getCountry()));
        item.setZipcode(normalize(item.getZipcode()));
        return item;
    }

    private String required(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isEmpty()) {
            throw new CustomerValidationException(fieldName + " is required");
        }
        return normalized;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
