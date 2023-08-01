package com.neoflex.conveyor.service;

import com.neoflex.dto.CreditDTO;
import com.neoflex.dto.LoanApplicationRequestDTO;
import com.neoflex.dto.LoanOfferDTO;
import com.neoflex.dto.ScoringDataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConveyorFacade {

  private final LoanOfferService offerService;
  private final ScoringService scoringService;

  public List<LoanOfferDTO> conveyorOffersPost(LoanApplicationRequestDTO request) {
    return offerService.conveyorOffersPost(request);
  }

  public CreditDTO conveyorCalculationPost(ScoringDataDTO scoringData) {
    return scoringService.conveyorCalculationPost(scoringData);
  }
}
