/*
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.measure.security;

import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.collect.TestHelper.coverPrivateConstructor;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.calc.Measure;
import com.opengamma.strata.calc.runner.CalculationParameters;
import com.opengamma.strata.calc.runner.FunctionRequirements;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.data.scenario.CurrencyScenarioArray;
import com.opengamma.strata.data.scenario.ScenarioMarketData;
import com.opengamma.strata.market.observable.QuoteId;
import com.opengamma.strata.measure.Measures;
import com.opengamma.strata.measure.curve.TestMarketDataMap;
import com.opengamma.strata.product.GenericSecurity;
import com.opengamma.strata.product.GenericSecurityTrade;
import com.opengamma.strata.product.SecurityId;
import com.opengamma.strata.product.SecurityInfo;
import com.opengamma.strata.product.TradeInfo;

/**
 * Test {@link GenericSecurityTradeCalculationFunction}.
 */
@Test
public class GenericSecurityTradeCalculationFunctionTest {

  private static final ReferenceData REF_DATA = ReferenceData.standard();
  private static final CalculationParameters PARAMS = CalculationParameters.empty();
  private static final double MARKET_PRICE = 99.42;
  private static final double TICK_SIZE = 0.01;
  private static final int TICK_VALUE = 10;
  private static final int QUANTITY = 20;
  private static final SecurityId SEC_ID = SecurityId.of("OG-Future", "Foo-Womble-Mar14");
  private static final GenericSecurity FUTURE = GenericSecurity.of(
      SecurityInfo.of(SEC_ID, TICK_SIZE, CurrencyAmount.of(EUR, TICK_VALUE)));
  public static final GenericSecurityTrade TRADE = GenericSecurityTrade.builder()
      .info(TradeInfo.builder()
          .settlementDate(LocalDate.of(2013, 12, 15))
          .build())
      .security(FUTURE)
      .quantity(QUANTITY)
      .price(99.550)
      .build();
  private static final Currency CURRENCY = TRADE.getCurrency();
  private static final LocalDate VAL_DATE = LocalDate.of(2013, 12, 8);

  //-------------------------------------------------------------------------
  public void test_requirementsAndCurrency() {
    GenericSecurityTradeCalculationFunction function = new GenericSecurityTradeCalculationFunction();
    Set<Measure> measures = function.supportedMeasures();
    FunctionRequirements reqs = function.requirements(TRADE, measures, PARAMS, REF_DATA);
    assertThat(reqs.getOutputCurrencies()).containsOnly(CURRENCY);
    assertThat(reqs.getValueRequirements()).isEqualTo(ImmutableSet.of(QuoteId.of(SEC_ID.getStandardId())));
    assertThat(reqs.getTimeSeriesRequirements()).isEmpty();
    assertThat(function.naturalCurrency(TRADE, REF_DATA)).isEqualTo(CURRENCY);
  }

  public void test_presentValue() {
    GenericSecurityTradeCalculationFunction function = new GenericSecurityTradeCalculationFunction();
    ScenarioMarketData md = marketData();

    double unitPv = (MARKET_PRICE / TICK_SIZE) * TICK_VALUE;
    CurrencyAmount expectedPv = CurrencyAmount.of(CURRENCY, unitPv * QUANTITY);

    Set<Measure> measures = ImmutableSet.of(Measures.PRESENT_VALUE);
    assertThat(function.calculate(TRADE, measures, PARAMS, md, REF_DATA))
        .containsEntry(
            Measures.PRESENT_VALUE, Result.success(CurrencyScenarioArray.of(ImmutableList.of(expectedPv))));
  }

  //-------------------------------------------------------------------------
  private ScenarioMarketData marketData() {
    TestMarketDataMap md = new TestMarketDataMap(
        VAL_DATE,
        ImmutableMap.of(QuoteId.of(SEC_ID.getStandardId()), MARKET_PRICE),
        ImmutableMap.of());
    return md;
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    coverPrivateConstructor(SecurityMeasureCalculations.class);
  }

}
