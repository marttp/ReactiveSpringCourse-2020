package com.learnreactivespring.fluxandmonoplayground;

import static reactor.core.scheduler.Schedulers.parallel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxAndMonoTransformTest {

  List<String> names = Arrays.asList("adam", "anna", "jack", "jenny");

  @Test
  public void transformUsingMap() {
    Flux<String> namesFlux = Flux.fromIterable(names)
        .map(s -> s.toUpperCase())
        .log();

    StepVerifier.create(namesFlux)
        .expectNext("ADAM", "ANNA", "JACK", "JENNY")
        .verifyComplete();
  }

  @Test
  public void transformUsingMapLength() {
    Flux<Integer> namesFlux = Flux.fromIterable(names)
        .map(s -> s.length())
        .log();

    StepVerifier.create(namesFlux)
        .expectNext(4, 4, 4, 5)
        .verifyComplete();
  }

  @Test
  public void transformUsingMapLength_repeat() {
    Flux<Integer> namesFlux = Flux.fromIterable(names)
        .map(String::length)
        .repeat(1)
        .log();

    StepVerifier.create(namesFlux)
        .expectNext(4, 4, 4, 5, 4, 4, 4, 5)
        .verifyComplete();
  }

  @Test
  public void transformUsingMap_Filter() {
    Flux<String> namesFlux = Flux.fromIterable(names)
        .filter(s -> s.length() > 4)
        .map(String::toUpperCase)
        .log();

    StepVerifier.create(namesFlux)
        .expectNext("JENNY")
        .verifyComplete();
  }

  @Test
  public void transformUsingFlatMap() {
    Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F"))
        .flatMap(s -> Flux.fromIterable(convertToList(
            s))) // DB or External service call that return a flux -> s -> Flux<String>
        .log();

    StepVerifier.create(stringFlux)
        .expectNextCount(12)
        .verifyComplete();
  }

  private List<String> convertToList(String s) {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return Arrays.asList(s, "newValue");
  }

  @Test
  public void transformUsingFlatMap_usingParallel() {
    Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F"))
        .window(2)
        .flatMap(s -> s.map(this::convertToList).subscribeOn(parallel())
            .flatMap(Flux::fromIterable))
        .log();

    StepVerifier.create(stringFlux)
        .expectNextCount(12)
        .verifyComplete();
  }

  @Test
  public void transformUsingFlatMap_parallel_maintain_order() {
    Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F"))
        .window(2)
//        .concatMap(s -> s.map(this::convertToList).subscribeOn(parallel())
        .flatMapSequential(s -> s.map(this::convertToList).subscribeOn(parallel())
            .flatMap(Flux::fromIterable))
        .log();

    StepVerifier.create(stringFlux)
        .expectNextCount(12)
        .verifyComplete();
  }
}
