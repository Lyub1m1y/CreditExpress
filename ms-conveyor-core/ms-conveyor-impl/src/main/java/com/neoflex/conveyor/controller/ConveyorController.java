package com.neoflex.conveyor.controller;

import com.neoflex.api.ConveyorApi;
import com.neoflex.conveyor.service.ConveyorFacade;
import com.neoflex.dto.CreditDTO;
import com.neoflex.dto.LoanApplicationRequestDTO;
import com.neoflex.dto.LoanOfferDTO;
import com.neoflex.dto.ScoringDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ConveyorController implements ConveyorApi {

  private final ConveyorFacade conveyorFacade;

  @Override
  public ResponseEntity<List<LoanOfferDTO>> conveyorOffersPost(LoanApplicationRequestDTO loanApplicationRequest) {
    try {
      final List<LoanOfferDTO> loanOffers = conveyorFacade.conveyorOffersPost(loanApplicationRequest);
      return new ResponseEntity<>(loanOffers, HttpStatus.OK);
    } catch (Exception ex) {
      log.error("An exception occurred while executing conveyorOffersPost.", ex);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

  }

  @Override
  public ResponseEntity<CreditDTO> conveyorCalculationPost(ScoringDataDTO scoringData) {
    try {
    final CreditDTO credit = conveyorFacade.conveyorCalculationPost(scoringData);
    return new ResponseEntity<>(credit, HttpStatus.OK);
    } catch (Exception ex) {
      log.error("An exception occurred while executing conveyorCalculationPost.", ex);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

}
