package com.learnreactivespring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
public class FluxAndMonoControllerTest {

  @Autowired
  WebTestClient webTestClient;

  @Test
  public void flux_approach1() {
    Flux<Integer> integerFlux = webTestClient.get().uri("/flux")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .returnResult(Integer.class)
        .getResponseBody();

    StepVerifier.create(integerFlux)
        .expectSubscription()
        .expectNext(1, 2, 3, 4)
        .verifyComplete();
  }

  @Test
  public void flux_approach2() {
    webTestClient.get().uri("/flux")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(Integer.class)
        .hasSize(4);
  }

  @Test
  public void flux_approach3() {
    List<Integer> expectedIntegerList = Arrays.asList(1, 2, 3, 4);

    EntityExchangeResult<List<Integer>> entityExchangeResult = webTestClient.get().uri("/flux")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(Integer.class)
        .returnResult();

    assertEquals(expectedIntegerList, entityExchangeResult.getResponseBody());
  }

  @Test
  public void flux_approach4() {
    List<Integer> expectedIntegerList = Arrays.asList(1, 2, 3, 4);

    webTestClient.get().uri("/flux")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(Integer.class)
        .consumeWith((response) -> {
          assertEquals(expectedIntegerList, response.getResponseBody());
        });
  }

  @Test
  public void fluxStream() {
    Flux<Long> longStreamFlux = webTestClient.get().uri("/fluxstream")
        .accept(MediaType.APPLICATION_STREAM_JSON)
        .exchange()
        .expectStatus().isOk()
        .returnResult(Long.class)
        .getResponseBody();

    StepVerifier.create(longStreamFlux)
        .expectNext(0L)
        .expectNext(1L)
        .expectNext(2L)
        .thenCancel()
        .verify();
  }

  @Test
  public void mono() {
    Integer expectedValue = 1;
    webTestClient.get().uri("/mono")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Integer.class)
        .consumeWith((response) -> {
          assertEquals(expectedValue, response.getResponseBody());
        });
  }
}
