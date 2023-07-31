package com.neoflex.conveyor.controller;

import com.neoflex.api.ConveyorApi;
import com.neoflex.conveyor.service.ConveyorService;
import com.neoflex.dto.CreditDTO;
import com.neoflex.dto.LoanApplicationRequestDTO;
import com.neoflex.dto.LoanOfferDTO;
import com.neoflex.dto.ScoringDataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConveyorController implements ConveyorApi {

  private final ConveyorService conveyorService;

  @Override
  public ResponseEntity<List<LoanOfferDTO>> conveyorOffersPost(LoanApplicationRequestDTO loanApplicationRequestDTO) {
    final List<LoanOfferDTO> loanOffers = conveyorService.conveyorOffersPost(loanApplicationRequestDTO);
    return new ResponseEntity<>(loanOffers, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<CreditDTO> conveyorCalculationPost(ScoringDataDTO scoringDataDTO) {
    final CreditDTO credit = conveyorService.conveyorCalculationPost(scoringDataDTO);
    return new ResponseEntity<>(credit, HttpStatus.OK);
  }

}
