package com.opengamma.strata.pricer.fx;

import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.index.FxIndex;
import com.opengamma.strata.finance.fx.FxDigitalOption;
import com.opengamma.strata.finance.fx.FxDigitalOptionProduct;
import com.opengamma.strata.market.sensitivity.FxOptionSensitivity;
import com.opengamma.strata.market.sensitivity.PointSensitivities;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;
import com.opengamma.strata.pricer.rate.RatesProvider;

/**
 * Pricer for foreign exchange digital option transaction products.
 * <p>
 * This function provides the ability to price an {@link FxDigitalOptionProduct}.
 */
public abstract class FxDigitalOptionProductPricer {

  //-------------------------------------------------------------------------
  /**
   * Calculates the price of the foreign exchange digital option product.
   * <p>
   * The price of the product is the value on the valuation date.
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the price of the product
   */
  public double price(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double forwardPrice = undiscountedPrice(option, ratesProvider, volatilityProvider);
    double discountFactor = ratesProvider.discountFactor(option.getStrikeCounterCurrency(), option.getPaymentDate());
    return discountFactor * forwardPrice;
  }

  /**
   * Calculates the present value of the foreign exchange digital option product.
   * <p>
   * The present value of the product is the value on the valuation date.
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the present value of the product
   */
  public CurrencyAmount presentValue(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double price = price(option, ratesProvider, volatilityProvider);
    return CurrencyAmount.of(option.getStrikeCounterCurrency(), signedNotional(option) * price);
  }

   abstract double undiscountedPrice(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider);

  //-------------------------------------------------------------------------
  /**
   * Calculates the delta of the foreign exchange digital option product.
   * <p>
   * The delta is the first derivative of the option price with respect to spot. 
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the delta of the product
   */
  public double delta(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double fwdDelta = undiscountedDelta(option, ratesProvider, volatilityProvider);
    FxIndex index = option.getIndex();
    double discountFactor = ratesProvider.discountFactor(option.getStrikeCounterCurrency(), option.getPaymentDate());
    double fwdRateSpotSensitivity = ratesProvider.fxForwardRates(index.getCurrencyPair())
      .rateFxSpotSensitivity(option.getStrikeBaseCurrency(), index.calculateMaturityFromFixing(option.getExpiryDate()));
    return fwdDelta * discountFactor * fwdRateSpotSensitivity;
  }

  /**
   * Calculates the present value delta of the foreign exchange digital option product.
   * <p>
   * The present value delta is the first derivative of the present value with respect to spot. 
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the present value delta of the product
   */
  public CurrencyAmount presentValueDelta(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double delta = delta(option, ratesProvider, volatilityProvider);
    return CurrencyAmount.of(option.getStrikeCounterCurrency(), signedNotional(option) * delta);
  }

  /**
   * Calculates the present value sensitivity of the foreign exchange digital option product.
   * <p>
   * The present value sensitivity of the product is the sensitivity of the present value to
   * the underlying curves.
   * <p>
   * The volatility is fixed in this sensitivity computation.
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the present value curve sensitivity of the product
   */
  public PointSensitivities presentValueSensitivity(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    if (volatilityProvider.relativeTime(option.getExpiryDate(), option.getExpiryTime(), option.getExpiryZone()) <= 0d) {
      return PointSensitivities.empty();
    }
    double fwdDelta = undiscountedDelta(option, ratesProvider, volatilityProvider);
    double discountFactor = ratesProvider.discountFactor(option.getStrikeCounterCurrency(), option.getPaymentDate());
    double notional = signedNotional(option);
    PointSensitivityBuilder fwdSensi = ratesProvider.fxIndexRates(option.getIndex()).ratePointSensitivity(
        option.getStrikeBaseCurrency(), option.getExpiryDate()).multipliedBy(notional * discountFactor * fwdDelta);
    double fwdPrice = undiscountedPrice(option, ratesProvider, volatilityProvider);
    PointSensitivityBuilder dscSensi = ratesProvider.discountFactors(option.getStrikeCounterCurrency())
        .zeroRatePointSensitivity(option.getPaymentDate()).multipliedBy(notional * fwdPrice);
    return fwdSensi.combinedWith(dscSensi).build();
  }

  abstract double undiscountedDelta(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider);

  //-------------------------------------------------------------------------
  /**
   * Calculates the gamma of the foreign exchange digital option product.
   * <p>
   * The delta is the second derivative of the option price with respect to spot. 
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the gamma of the product
   */
  public double gamma(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double undiscountedGamma = undiscountedGamma(option, ratesProvider, volatilityProvider);
    FxIndex index = option.getIndex();
    double discountFactor = ratesProvider.discountFactor(option.getStrikeCounterCurrency(), option.getPaymentDate());
    double fwdRateSpotSensitivity = ratesProvider.fxForwardRates(index.getCurrencyPair())
      .rateFxSpotSensitivity(option.getStrikeBaseCurrency(), index.calculateMaturityFromFixing(option.getExpiryDate()));
    return discountFactor * undiscountedGamma * fwdRateSpotSensitivity * fwdRateSpotSensitivity;
  }

  /**
   * Calculates the present value gamma of the foreign exchange digital option product.
   * <p>
   * The present value gamma is the second derivative of the present value with respect to spot. 
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the present value gamma of the product
   */
  public CurrencyAmount presentValueGamma(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double gamma = gamma(option, ratesProvider, volatilityProvider);
    return CurrencyAmount.of(option.getStrikeCounterCurrency(), signedNotional(option) * gamma);
  }

  abstract double undiscountedGamma(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider);

  //-------------------------------------------------------------------------
  /**
   * Calculates the vega of the foreign exchange digital option product.
   * <p>
   * The vega is the first derivative of the option price with respect to volatility. 
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the vega of the product
   */
  public double vega(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double forwardVega = undiscountedVega(option, ratesProvider, volatilityProvider);
    double discountFactor = ratesProvider.discountFactor(option.getStrikeCounterCurrency(), option.getPaymentDate());
    return discountFactor * forwardVega;
  }

  /**
   * Calculates the present value vega of the foreign exchange digital option product.
   * <p>
   * The present value vega is the first derivative of the present value with respect to volatility. 
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the present value vega of the product
   */
  public CurrencyAmount presentValueVega(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double vega = vega(option, ratesProvider, volatilityProvider);
    return CurrencyAmount.of(option.getStrikeCounterCurrency(), signedNotional(option) * vega);
  }

  /**
   * Computes the present value sensitivity to the black volatility used in the pricing.
   * <p>
   * The result is a single sensitivity to the volatility used.
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the present value sensitivity
   */
  public PointSensitivityBuilder presentValueSensitivityBlackVolatility(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double timeToExpiry =
        volatilityProvider.relativeTime(option.getExpiryDate(), option.getExpiryTime(), option.getExpiryZone());
    if (timeToExpiry <= 0d) {
      return PointSensitivityBuilder.none();
    }
    FxRate strike = option.getStrike();
    CurrencyPair strikePair = strike.getPair();
    double forwardRate = ratesProvider.fxIndexRates(
        option.getIndex()).rate(strikePair.getBase(), option.getExpiryDate());
    CurrencyAmount valueVega = presentValueVega(option, ratesProvider, volatilityProvider);
    return FxOptionSensitivity.of(strikePair, option.getExpiryDate(), strike.fxRate(strikePair), forwardRate,
        valueVega.getCurrency(), valueVega.getAmount());
  }

  abstract double undiscountedVega(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider);

  //-------------------------------------------------------------------------
  /**
   * Calculates theta of the foreign exchange digital option product.
   * <p>
   * The theta is minus of the first derivative of the option price with respect to time parameter in the price formula, 
   * i.e., the discounted driftless theta. 
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the theta of the product
   */
  public double theta(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double forwardTheta = undiscountedTheta(option, ratesProvider, volatilityProvider);
    double discountFactor = ratesProvider.discountFactor(option.getStrikeCounterCurrency(), option.getPaymentDate());
    return discountFactor * forwardTheta;
  }

  /**
   * Calculates the present value theta of the foreign exchange digital option product.
   * <p>
   * The present value theta is minus of the first derivative of the present value with time parameter in the price 
   * formula, i.e., the driftless theta of the present value.
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the present value vega of the product
   */
  public CurrencyAmount presentValueTheta(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double theta = theta(option, ratesProvider, volatilityProvider);
    return CurrencyAmount.of(option.getStrikeCounterCurrency(), signedNotional(option) * theta);
  }

  abstract double undiscountedTheta(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider);

  //-------------------------------------------------------------------------
  /**
   * Calculates the implied Black volatility of the foreign exchange digital option product.
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the implied volatility of the product
   */
  public double impliedVolatility(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double timeToExpiry =
        volatilityProvider.relativeTime(option.getExpiryDate(), option.getExpiryTime(), option.getExpiryZone());
    if (timeToExpiry <= 0d) {
      throw new IllegalArgumentException("valuation is after option's expiry.");
    }
    FxRate strike = option.getStrike();
    CurrencyPair strikePair = strike.getPair();
    double strikeRate = strike.fxRate(strikePair);
    double forwardRate = ratesProvider.fxIndexRates(
        option.getIndex()).rate(strikePair.getBase(), option.getExpiryDate());
    return volatilityProvider.getVolatility(strikePair, option.getExpiryDate(), strikeRate, forwardRate);
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the currency exposure of the foreign exchange digital option product.
   * 
   * @param option  the option product to price
   * @param ratesProvider  the rates provider
   * @param volatilityProvider  the Black volatility provider
   * @return the currency exposure
   */
  public MultiCurrencyAmount currencyExposure(
      FxDigitalOption option,
      RatesProvider ratesProvider,
      BlackVolatilityFxProvider volatilityProvider) {
    double timeToExpiry =
        volatilityProvider.relativeTime(option.getExpiryDate(), option.getExpiryTime(), option.getExpiryZone());
    if (timeToExpiry <= 0d) {
      return MultiCurrencyAmount.empty();
    }
    CurrencyPair strikePair = option.getStrike().getPair();
    double price = price(option, ratesProvider, volatilityProvider);
    double delta = delta(option, ratesProvider, volatilityProvider);
    double spot = ratesProvider.fxRate(strikePair);
    double signedNotional = signedNotional(option);
    CurrencyAmount domestic = CurrencyAmount.of(strikePair.getCounter(), (price - delta * spot) * signedNotional);
    CurrencyAmount foreign = CurrencyAmount.of(strikePair.getBase(), delta * signedNotional);
    return MultiCurrencyAmount.of(domestic, foreign);
  }

  //-------------------------------------------------------------------------
  // signed notional amount to computed present value and value Greeks
  private double signedNotional(FxDigitalOption option) {
    return (option.getLongShort().isLong() ? 1d : -1d) * option.getNotional();
  }
  
}
