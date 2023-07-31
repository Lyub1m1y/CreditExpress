package com.neoflex.conveyor.service.impl;

import com.neoflex.conveyor.service.ConveyorService;
import com.neoflex.dto.CreditDTO;
import com.neoflex.dto.LoanApplicationRequestDTO;
import com.neoflex.dto.LoanOfferDTO;
import com.neoflex.dto.ScoringDataDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConveyorServiceImpl implements ConveyorService {


  @Override
  public List<LoanOfferDTO> conveyorOffersPost(LoanApplicationRequestDTO loanApplicationRequestDTO) {
    return null;
  }

  @Override
  public CreditDTO conveyorCalculationPost(ScoringDataDTO scoringDataDTO) {
    return null;
  }
}
