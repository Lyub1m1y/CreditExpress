package com.neoflex.conveyor.service;

import com.neoflex.dto.CreditDTO;
import com.neoflex.dto.LoanApplicationRequestDTO;
import com.neoflex.dto.LoanOfferDTO;
import com.neoflex.dto.ScoringDataDTO;

import java.util.List;
public interface ConveyorService {

  List<LoanOfferDTO> conveyorOffersPost(LoanApplicationRequestDTO loanApplicationRequestDTO);

  CreditDTO conveyorCalculationPost(ScoringDataDTO scoringDataDTO);

}
