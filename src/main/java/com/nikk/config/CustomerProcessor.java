package com.nikk.config;

import com.nikk.entity.Customer;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer,Customer> {

    @Override
    public  Customer process(Customer item) throws Exception {
//        logic
        return item;
    }
}
