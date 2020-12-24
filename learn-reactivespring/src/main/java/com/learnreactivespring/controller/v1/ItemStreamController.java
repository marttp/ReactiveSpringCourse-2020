package com.learnreactivespring.controller.v1;

import static com.learnreactivespring.constants.ItemConstants.ITEM_STREAM_END_POINT_V1;

import com.learnreactivespring.document.ItemCapped;
import com.learnreactivespring.repository.ItemReactiveCappedRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ItemStreamController {

  final
  ItemReactiveCappedRepository itemReactiveCappedRepository;

  public ItemStreamController(
      ItemReactiveCappedRepository itemReactiveCappedRepository) {
    this.itemReactiveCappedRepository = itemReactiveCappedRepository;
  }

  @GetMapping(value = ITEM_STREAM_END_POINT_V1, produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
  public Flux<ItemCapped> getItemsStream() {

    return itemReactiveCappedRepository.findItemCappedBy();
  }


}
