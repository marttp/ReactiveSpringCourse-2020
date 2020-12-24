package com.learnreactivespring.controller.v1;

import static com.learnreactivespring.constants.ItemConstants.ITEM_END_POINT_V1;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.learnreactivespring.document.Item;
import com.learnreactivespring.repository.ItemReactiveRepository;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ItemControllerTest {

  @Autowired
  WebTestClient webTestClient;

  @Autowired
  ItemReactiveRepository itemReactiveRepository;

  public List<Item> data() {

    return Arrays.asList(new Item(null, "Samsung TV", 399.99),
        new Item(null, "LG TV", 329.99),
        new Item(null, "Apple Watch", 349.99),
        new Item("ABC", "Beats HeadPhones", 149.99));
  }


  @BeforeEach
  public void setUp() {
    itemReactiveRepository.deleteAll()
        .thenMany(Flux.fromIterable(data()))
        .flatMap(itemReactiveRepository::save)
        .doOnNext((item -> {
          System.out.println("Inserted item is : " + item);
        }))
        .blockLast();
  }

  @Test
  public void getAllItems() {
    webTestClient.get().uri(ITEM_END_POINT_V1)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Item.class)
        .hasSize(4);
  }

  @Test
  public void getAllItems_approach2() {
    webTestClient.get().uri(ITEM_END_POINT_V1)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Item.class)
        .hasSize(4)
        .consumeWith((response) -> {
          List<Item> items = response.getResponseBody();
          assert items != null;
          items.forEach((item) -> {
            assertNotNull(item.getId());
          });
        });
  }

  @Test
  public void getAllItems_approach3() {
    Flux<Item> itemsFlux = webTestClient.get().uri(ITEM_END_POINT_V1)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .returnResult(Item.class)
        .getResponseBody();

    StepVerifier.create(itemsFlux.log("value from network : "))
        .expectNextCount(4)
        .verifyComplete();
  }

  @Test
  public void getOneItem() {
    webTestClient.get().uri(ITEM_END_POINT_V1.concat("/{id}"), "ABC")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.price", 149.99);
  }

  @Test
  public void getOneItem_notFound() {
    webTestClient.get().uri(ITEM_END_POINT_V1.concat("/{id}"), "DEF")
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  public void createItem() {

    Item item = new Item(null, "Iphone X", 999.99);

    webTestClient.post().uri(ITEM_END_POINT_V1)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(item), Item.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.description").isEqualTo("Iphone X")
        .jsonPath("$.price").isEqualTo(999.99);
  }

  @Test
  public void deleteItem() {
    webTestClient.delete().uri(ITEM_END_POINT_V1.concat("/{id}"), "ABC")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Void.class);
  }

  @Test
  public void updateItem() {
    double newPrice = 129.99;
    Item item = new Item(null, "Beats HeadPhones", newPrice);

    webTestClient.put().uri(ITEM_END_POINT_V1.concat("/{id}"), "ABC")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(Mono.just(item), Item.class)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.price", newPrice);
  }

  @Test
  public void updateItem_notFound() {
    double newPrice = 129.99;
    Item item = new Item(null, "Beats HeadPhones", newPrice);
    webTestClient.put().uri(ITEM_END_POINT_V1.concat("/{id}"), "DEF")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(Mono.just(item), Item.class)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  public void runTimeException() {
    webTestClient.get().uri(ITEM_END_POINT_V1 + "/runtimeException")
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody(String.class)
        .isEqualTo("RuntimeException Occurred.");
  }

}
