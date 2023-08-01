package com.neoflex.conveyor.service;

import com.neoflex.dto.LoanApplicationRequestDTO;
import com.neoflex.dto.LoanOfferDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanOfferService {

  private final ScoringService scoringService;

  public List<LoanOfferDTO> conveyorOffersPost(LoanApplicationRequestDTO loanApplicationRequest) {
    log.info("Generating loan offers for the request: {}", loanApplicationRequest);

    List<LoanOfferDTO> loanOffers = List.of(
        createOffer(false, false, loanApplicationRequest),
        createOffer(true, false, loanApplicationRequest),
        createOffer(false, true, loanApplicationRequest),
        createOffer(true, true, loanApplicationRequest)
    );

    log.info("Generated {} loan offers", loanOffers.size());
    return loanOffers;
  }

  private LoanOfferDTO createOffer(Boolean isInsuranceEnabled, Boolean isSalaryClient,
                                   LoanApplicationRequestDTO loanApplicationRequest) {
    log.info("Creating loan offer with parameters: isInsuranceEnabled={}, isSalaryClient={}, " +
            "loanApplicationRequest={}", isInsuranceEnabled, isSalaryClient, loanApplicationRequest);

    BigDecimal totalAmount = scoringService.evaluateTotalAmountByServices(loanApplicationRequest.getAmount(),
        isInsuranceEnabled);
    BigDecimal rate = scoringService.calculateRate(isInsuranceEnabled, isSalaryClient);


    LoanOfferDTO loanOffer = new LoanOfferDTO()
        .requestedAmount(loanApplicationRequest.getAmount())
        .totalAmount(totalAmount)
        .term(loanApplicationRequest.getTerm())
        .isInsuranceEnabled(isInsuranceEnabled)
        .isSalaryClient(isSalaryClient)
        .rate(rate)
        .monthlyPayment(scoringService.calculateMonthlyPayment(totalAmount, loanApplicationRequest.getTerm(), rate));

    log.info("Created loan offer: {}", loanOffer);
    return loanOffer;
  }
}
