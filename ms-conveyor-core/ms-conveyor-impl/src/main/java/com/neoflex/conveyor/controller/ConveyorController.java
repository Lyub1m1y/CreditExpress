package com.neoflex.conveyor.controller;

import com.neoflex.api.ConveyorApi;
import com.neoflex.dto.CreditDTO;
import com.neoflex.dto.LoanApplicationRequestDTO;
import com.neoflex.dto.LoanOfferDTO;
import com.neoflex.dto.ScoringDataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConveyorController implements ConveyorApi {
  @Override
  public ResponseEntity<List<LoanOfferDTO>> conveyorOffersPost(LoanApplicationRequestDTO loanApplicationRequestDTO) {
    return null;
  }

  @Override
  public ResponseEntity<CreditDTO> conveyorCalculationPost(ScoringDataDTO scoringDataDTO) {
    return null;
  }

}
