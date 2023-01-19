package io.confluent.generator;

import io.confluent.model.avro.Book;
import io.confluent.model.avro.OnlineOrder;

public interface DataSource {

    Book retrieveBook();

    OnlineOrder retrieveOnlineOrder();
}