package com.neoflex.conveyor.controller;

import com.neoflex.api.ExampleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExampleController implements ExampleApi {

  @Override
  public ResponseEntity<Void> exampleGet() {
    return ResponseEntity.ok(null);
  }
}
