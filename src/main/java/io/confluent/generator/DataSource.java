package io.confluent.generator;

import io.confluent.model.avro.Book;
import io.confluent.model.avro.Customer;
import io.confluent.model.avro.Order;

public interface DataSource {

    Order retrieveOrder();

    Book retrieveBook();

    Customer retrieveCustomer();
}