package com.learnreactivespring.controller.v1;

import com.learnreactivespring.constants.ItemConstants;
import com.learnreactivespring.document.ItemCapped;
import com.learnreactivespring.repository.ItemReactiveCappedRepository;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ItemStreamControllerTest {

  @Autowired
  ItemReactiveCappedRepository itemReactiveCappedRepository;

  @Autowired
  MongoOperations mongoOperations;

  @Autowired
  WebTestClient webTestClient;

  @BeforeEach
  public void setUp() {

    mongoOperations.dropCollection(ItemCapped.class);
    mongoOperations.createCollection(ItemCapped.class, CollectionOptions.empty().maxDocuments(20).size(50000).capped());

    Flux<ItemCapped> itemCappedFlux = Flux.interval(Duration.ofMillis(100))
        .map(i -> new ItemCapped(null, "Random Item " + i, (100.00 + i)))
        .take(5);

    itemReactiveCappedRepository
        .insert(itemCappedFlux)
        .doOnNext((itemCapped -> {
          System.out.println("Inserted Item in setUp " + itemCapped);
        }))
        .blockLast();
  }

  @Test
  public void testStreamAllItems(){

    Flux<ItemCapped> itemCappedFlux = webTestClient.get().uri(ItemConstants.ITEM_STREAM_END_POINT_V1)
        .exchange()
        .expectStatus().isOk()
        .returnResult((ItemCapped.class))
        .getResponseBody()
        .take(5);

    StepVerifier.create(itemCappedFlux)
        .expectNextCount(5)
        .thenCancel()
        .verify();

  }
}
