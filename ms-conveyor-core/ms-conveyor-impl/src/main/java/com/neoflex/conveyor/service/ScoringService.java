package com.neoflex.conveyor.service;

import com.neoflex.conveyor.exception.ScoringException;
import com.neoflex.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ScoringService {

  private static final String FUNDING_RATE = "15.00";
  private static BigDecimal CURRENT_RATE = new BigDecimal(FUNDING_RATE);
  private static final String INSURANCE_DISCOUNT = "4.00";
  private static final String SALARY_CLIENT_DISCOUNT = "1.00";
  private static final String INSURANCE_BASE_PRICE = "10000.00";
  private static final String BASE_LOAN_AMOUNT = "200000.00";
  private static final String INSURANCE_LOAN_AMOUNT_MULTIPLICAND = "0.05";
  private static final Integer BASE_PERIODS_AMOUNT_IN_YEAR = 12;
  private static final Integer DEFAULT_DECIMAL_SCALE = 2;
  private static final int MIN_CLIENT_AGE = 20;
  private static final int MAX_CLIENT_AGE = 60;

  public CreditDTO conveyorCalculationPost(ScoringDataDTO scoringData) {
    scoring(scoringData);

    BigDecimal totalAmount = evaluateTotalAmountByServices(scoringData.getAmount(),
        scoringData.getIsInsuranceEnabled());
    BigDecimal requestedAmount = scoringData.getAmount();
    BigDecimal rate = calculateRate(scoringData.getIsInsuranceEnabled(), scoringData.getIsSalaryClient());
    Integer term = scoringData.getTerm();
    BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, term, rate);
    List<PaymentScheduleElement> paymentSchedule = calculatePaymentSchedule(totalAmount,
        scoringData.getTerm(), rate, monthlyPayment);

    return new CreditDTO()
        .amount(totalAmount)
        .monthlyPayment(calculateMonthlyPayment(totalAmount, term, rate))
        .psk(calculatePSK(paymentSchedule, requestedAmount, term))
        .paymentSchedule(paymentSchedule)
        .term(term)
        .rate(rate)
        .isInsuranceEnabled(scoringData.getIsInsuranceEnabled())
        .isSalaryClient(scoringData.getIsSalaryClient());
  }

  public BigDecimal calculateRate(Boolean isInsuranceEnabled, Boolean isSalaryClient) {
    BigDecimal rate = new BigDecimal(CURRENT_RATE.toString());

    if (isInsuranceEnabled) {
      rate = rate.subtract(new BigDecimal(INSURANCE_DISCOUNT));
    }
    if (isSalaryClient) {
      rate = rate.subtract(new BigDecimal(SALARY_CLIENT_DISCOUNT));
    }

    return rate;
  }

  public BigDecimal evaluateTotalAmountByServices(BigDecimal amount, Boolean isInsuranceEnabled) {
    if (isInsuranceEnabled) {
      BigDecimal insurancePrice = new BigDecimal(INSURANCE_BASE_PRICE);
      if (amount.compareTo(new BigDecimal(BASE_LOAN_AMOUNT)) > 0) {
        insurancePrice = insurancePrice
            .add(amount
                .multiply(new BigDecimal(INSURANCE_LOAN_AMOUNT_MULTIPLICAND)));
      }
      return amount.add(insurancePrice);
    } else {
      return amount;
    }
  }

  public BigDecimal calculateMonthlyPayment(BigDecimal totalAmount, Integer term, BigDecimal rate) {
    log.info("Calculating monthly payment: totalAmount = {}, term = {}, rate = {}", totalAmount, term, rate);

    BigDecimal monthlyRateAbsolute = rate.divide(BigDecimal.valueOf(100), 5, RoundingMode.CEILING);
    log.info("monthlyRateAbsolute = {}", monthlyRateAbsolute);

    BigDecimal monthlyRate = monthlyRateAbsolute.divide(new BigDecimal(BASE_PERIODS_AMOUNT_IN_YEAR),
        6, RoundingMode.CEILING);
    log.info("monthlyRate = {}", monthlyRate);

    if (term <= 0) {
      throw new ScoringException("Term must be greater than zero.");
    }

    BigDecimal intermediateCoefficient = (BigDecimal.ONE.add(monthlyRate)).pow(term)
        .setScale(5, RoundingMode.CEILING);
    log.info("intermediateCoefficient = {}", intermediateCoefficient);

    BigDecimal annuityCoefficient = monthlyRate.multiply(intermediateCoefficient)
        .divide(intermediateCoefficient.subtract(BigDecimal.ONE), RoundingMode.CEILING);
    log.info("annuityCoefficient = {}", annuityCoefficient);

    BigDecimal monthlyPayment = totalAmount.multiply(annuityCoefficient).setScale(2, RoundingMode.CEILING);

    log.info("Monthly payment calculated: {}", monthlyPayment);
    return monthlyPayment;
  }

  private BigDecimal calculatePSK(List<PaymentScheduleElement> paymentSchedule,
                                  BigDecimal requestedAmount, Integer term) {
    log.info("Calculating PSK...");

    BigDecimal paymentAmount = paymentSchedule
        .stream()
        .map(PaymentScheduleElement::getTotalPayment)
        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    log.info("paymentAmount = {}", paymentAmount);


    BigDecimal termInYears = divideWithScaleAndRoundingMode(BigDecimal.valueOf(term),
        new BigDecimal(BASE_PERIODS_AMOUNT_IN_YEAR));
    log.info("termInYears = {}", termInYears);

    BigDecimal intermediateCoefficient = divideWithScaleAndRoundingMode(paymentAmount, requestedAmount)
        .subtract(BigDecimal.ONE);
    log.info("intermediateCoefficient = {}", intermediateCoefficient);

    BigDecimal psk = intermediateCoefficient.divide(termInYears, 3, RoundingMode.CEILING)
        .multiply(BigDecimal.valueOf(100));

    log.info("PSK calculated: {}", psk);
    log.info("PSK calculation completed.");

    return psk;
  }

  private List<PaymentScheduleElement> calculatePaymentSchedule(BigDecimal totalAmount, Integer term,
                                                               BigDecimal rate, BigDecimal monthlyPayment) {
    log.info("Calculating payment schedule...");

    BigDecimal remainingDebt = totalAmount.setScale(2, RoundingMode.CEILING);
    List<PaymentScheduleElement> paymentSchedule = new ArrayList<>();
    LocalDate paymentDate = LocalDate.now();

    paymentSchedule.add(new PaymentScheduleElement()
        .number(0)
        .date(paymentDate)
        .totalPayment(BigDecimal.ZERO)
        .remainingDebt(remainingDebt)
        .interestPayment(BigDecimal.ZERO)
        .debtPayment(BigDecimal.ZERO));

    for (int i = 1; i < term + 1; i++) {
      paymentDate = paymentDate.plusMonths(1);

      BigDecimal interestPayment = calculateInterest(remainingDebt, rate).setScale(2, RoundingMode.CEILING);
      BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);

      remainingDebt = remainingDebt.subtract(debtPayment);

      paymentSchedule.add(new PaymentScheduleElement()
          .number(i)
          .date(paymentDate)
          .totalPayment(monthlyPayment)
          .remainingDebt(remainingDebt)
          .interestPayment(interestPayment)
          .debtPayment(debtPayment));
    }

    log.info("Payment schedule calculated.");
    return paymentSchedule;
  }

  private BigDecimal calculateInterest(BigDecimal remainingDebt, BigDecimal rate) {
    BigDecimal monthlyRateAbsolute = rate.divide(BigDecimal.valueOf(100), RoundingMode.CEILING);

    BigDecimal monthlyRate = monthlyRateAbsolute.divide(new BigDecimal(BASE_PERIODS_AMOUNT_IN_YEAR),
        10, RoundingMode.CEILING);

    return remainingDebt.multiply(monthlyRate);
  }

  private BigDecimal divideWithScaleAndRoundingMode(BigDecimal number, BigDecimal divisor) {
    return number.divide(divisor, DEFAULT_DECIMAL_SCALE, RoundingMode.CEILING);
  }

  private void scoring(ScoringDataDTO scoringData) {

    log.info("Start scoring process for client {} {} {}",
        scoringData.getLastName(), scoringData.getFirstName(), scoringData.getMiddleName());

    List<String> scoringRefuseCauses = new ArrayList<>();

    EmploymentDTO employment = scoringData.getEmployment();

    BigDecimal currentRate = new BigDecimal(FUNDING_RATE);

    if (employment.getEmploymentStatus() == EmploymentStatusEnum.UNEMPLOYED) {
      scoringRefuseCauses.add("Refuse cause: Client unemployed.");
    } else if (employment.getEmploymentStatus() == EmploymentStatusEnum.SELF_EMPLOYED) {
      log.info("Funding rate increases by 1 percent point because employment status = SELF_EMPLOYED");
      currentRate = currentRate.add(BigDecimal.ONE);
    } else if (employment.getEmploymentStatus() == EmploymentStatusEnum.BUSINESS_OWNER) {
      log.info("Funding rate increases by 3 percent point because employment status = BUSINESS_OWNER");
      currentRate = currentRate.add(BigDecimal.valueOf(3));
    }

    if (employment.getPosition() == PositionEnum.MANAGER) {
      log.info("Funding rate decreases by 2 percent point because employment position = MANAGER");
      currentRate = currentRate.subtract(BigDecimal.valueOf(2));
    }

    if (employment.getPosition() == PositionEnum.ANALYST) {
      log.info("Funding rate decreases by 4 percent point because employment position = ANALYST");
      currentRate = currentRate.subtract(BigDecimal.valueOf(4));
    }

    if (scoringData.getAmount()
        .compareTo(employment.getSalary().multiply(BigDecimal.valueOf(20))) > 0) {
      scoringRefuseCauses.add("Refuse cause: Too much loan amount due to client's salary.");
    }

    if (scoringData.getDependentAmount() > 1) {
      log.info("Funding rate increases by 1 percent point because dependent amount > 1");
      currentRate = currentRate.add(BigDecimal.ONE);
    }

    if (scoringData.getMaritalStatus() == MaritalStatusEnum.MARRIED) {
      log.info("Funding rate decreases by 3 percent point because marital status = MARRIED");
      currentRate = currentRate.subtract(BigDecimal.valueOf(3));
    } else if (scoringData.getMaritalStatus() == MaritalStatusEnum.DIVORCED) {
      log.info("Funding rate increases by 1 percent point because marital status = DIVORCED");
      currentRate = currentRate.add(BigDecimal.ONE);
    }

    long clientAge = ChronoUnit.YEARS.between(scoringData.getBirthdate(), LocalDate.now());
    if (clientAge > MAX_CLIENT_AGE) {
      scoringRefuseCauses.add("Refuse cause: Client too old.");
    } else if (clientAge < MIN_CLIENT_AGE) {
      scoringRefuseCauses.add("Refuse cause: Client too young.");
    }

    if (scoringData.getTerm() < 12) {
      log.info("Funding rate increases by 5 percent point because loan term < 12 months");
      currentRate = currentRate.add(BigDecimal.valueOf(5));
    } else if (scoringData.getTerm() >= 120) {
      log.info("Funding rate decreases by 2 percent point because loan term >= 120 months");
      currentRate = currentRate.subtract(BigDecimal.valueOf(2));
    }

    if (scoringData.getGender() == GenderEnum.NON_BINARY) {
      log.info("Funding rate increases by 3 percent point because gender = NON_BINARY");
      currentRate = currentRate.add(BigDecimal.valueOf(3));
    } else if (scoringData.getGender() == GenderEnum.FEMALE && (clientAge > 35 && clientAge < MAX_CLIENT_AGE)) {
      log.info("Funding rate decreases by 3 percent point because gender = FEMALE and age between 35 and 60");
      currentRate = currentRate.subtract(BigDecimal.valueOf(3));
    } else if (scoringData.getGender() == GenderEnum.FEMALE && (clientAge > 30 && clientAge < 55)) {
      log.info("Funding rate decreases by 3 percent point because gender = MALE and age between 30 and 55");
      currentRate = currentRate.subtract(BigDecimal.valueOf(3));
    }

    if (employment.getWorkExperienceTotal() < 12) {
      scoringRefuseCauses.add("Refuse cause: Too small total working experience.");
    }
    if (employment.getWorkExperienceCurrent() < 3) {
      scoringRefuseCauses.add("Refuse cause: Too small current working experience.");
    }

    if (scoringRefuseCauses.size() > 0) {
      log.info("Scoring errors: {}", Arrays.deepToString(scoringRefuseCauses.toArray()));
      throw new ScoringException(Arrays.deepToString(scoringRefuseCauses.toArray()));
    }

    CURRENT_RATE = currentRate;
    log.info("Scoring process completed for client {} {} {}", scoringData.getLastName(), scoringData.getFirstName(), scoringData.getMiddleName());
  }
}
