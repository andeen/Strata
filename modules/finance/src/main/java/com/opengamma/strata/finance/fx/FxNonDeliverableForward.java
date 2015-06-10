/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.finance.fx;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableValidator;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.index.FxIndex;

/**
 * A Non-Deliverable Forward (NDF).
 * <p>
 * An NDF is a financial instrument that returns the difference between a fixed FX rate 
 * agreed at the inception of the trade and the FX rate at maturity. 
 * It is primarily used to handle FX requirements for currencies that have settlement restrictions.
 * For example, the forward may be between USD and CNY (Chinese Yuan).
 */
@BeanDefinition
public final class FxNonDeliverableForward
    implements FxNonDeliverableForwardProduct, ImmutableBean, Serializable {

  /**
   * The notional amount, positive if receiving, negative if paying.
   * <p>
   * The notional amount is expressed in settlement currency amount. 
   * The amount is signed.
   * A positive amount indicates the payment is to be received.
   * A negative amount indicates the payment is to be paid.
   * <p>
   * This must be specified in one of the two currencies of the forward.
   */
  @PropertyDefinition(validate = "notNull")
  private final CurrencyAmount settlementCurrencyNotional;
  /**
   * The FX rate agreed for the value date at the inception of the trade.
   * <p>
   * The settlement amount is based on the difference between this rate and the
   * rate observed on the fixing date using the {@code index}.
   * <p>
   * The forward is between the two currencies defined by the rate.
   */
  @PropertyDefinition(validate = "notNull")
  private final FxRate agreedFxRate;
  /**
   * The date that the forward settles.
   * <p>
   * On this date, the settlement amount will be exchanged.
   * This date should be a valid business day.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate paymentDate;
  /**
   * The index defining the FX rate to observe on the fixing date.
   * <p>
   * The index is used to settle the trade by providing the actual FX rate on the fixing date.
   * The value of the trade is based on the difference between the actual rate and the agreed rate.
   * <p>
   * The forward is between the two currencies defined by the index.
   */
  @PropertyDefinition(validate = "notNull")
  private final FxIndex index;

  //-------------------------------------------------------------------------
  @ImmutableValidator
  private void validate() {
    CurrencyPair pair = index.getCurrencyPair();
    if (!pair.contains(settlementCurrencyNotional.getCurrency())) {
      throw new IllegalArgumentException("FxIndex and settlement currency are incompatible");
    }
    if (!(pair.equals(agreedFxRate.getPair()) || pair.isInverse(agreedFxRate.getPair()))) {
      throw new IllegalArgumentException("FxIndex and agreed FX rate are incompatible");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the settlement currency.
   * 
   * @return the currency that is to be settled
   */
  public Currency getSettlementCurrency() {
    return settlementCurrencyNotional.getCurrency();
  }

  /**
   * Gets the non-deliverable currency.
   * <p>
   * Returns the currency that is not the settlement currency.
   * 
   * @return the currency that is not to be settled
   */
  public Currency getNonDeliverableCurrency() {
    Currency base = agreedFxRate.getPair().getBase();
    return base.equals(getSettlementCurrency()) ? agreedFxRate.getPair().getCounter() : base;
  }

  //-------------------------------------------------------------------------
  /**
   * Expands this FX forward into an {@code ExpandedFxNonDeliverableForward}.
   * 
   * @return the transaction
   */
  @Override
  public ExpandedFxNonDeliverableForward expand() {
    return ExpandedFxNonDeliverableForward.builder()
        .settlementCurrencyNotional(settlementCurrencyNotional)
        .agreedFxRate(agreedFxRate)
        .paymentDate(paymentDate)
        .index(index)
        .build();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FxNonDeliverableForward}.
   * @return the meta-bean, not null
   */
  public static FxNonDeliverableForward.Meta meta() {
    return FxNonDeliverableForward.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FxNonDeliverableForward.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FxNonDeliverableForward.Builder builder() {
    return new FxNonDeliverableForward.Builder();
  }

  private FxNonDeliverableForward(
      CurrencyAmount settlementCurrencyNotional,
      FxRate agreedFxRate,
      LocalDate paymentDate,
      FxIndex index) {
    JodaBeanUtils.notNull(settlementCurrencyNotional, "settlementCurrencyNotional");
    JodaBeanUtils.notNull(agreedFxRate, "agreedFxRate");
    JodaBeanUtils.notNull(paymentDate, "paymentDate");
    JodaBeanUtils.notNull(index, "index");
    this.settlementCurrencyNotional = settlementCurrencyNotional;
    this.agreedFxRate = agreedFxRate;
    this.paymentDate = paymentDate;
    this.index = index;
    validate();
  }

  @Override
  public FxNonDeliverableForward.Meta metaBean() {
    return FxNonDeliverableForward.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the notional amount, positive if receiving, negative if paying.
   * <p>
   * The notional amount is expressed in settlement currency amount.
   * The amount is signed.
   * A positive amount indicates the payment is to be received.
   * A negative amount indicates the payment is to be paid.
   * <p>
   * This must be specified in one of the two currencies of the forward.
   * @return the value of the property, not null
   */
  public CurrencyAmount getSettlementCurrencyNotional() {
    return settlementCurrencyNotional;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the FX rate agreed for the value date at the inception of the trade.
   * <p>
   * The settlement amount is based on the difference between this rate and the
   * rate observed on the fixing date using the {@code index}.
   * <p>
   * The forward is between the two currencies defined by the rate.
   * @return the value of the property, not null
   */
  public FxRate getAgreedFxRate() {
    return agreedFxRate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date that the forward settles.
   * <p>
   * On this date, the settlement amount will be exchanged.
   * This date should be a valid business day.
   * @return the value of the property, not null
   */
  public LocalDate getPaymentDate() {
    return paymentDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the index defining the FX rate to observe on the fixing date.
   * <p>
   * The index is used to settle the trade by providing the actual FX rate on the fixing date.
   * The value of the trade is based on the difference between the actual rate and the agreed rate.
   * <p>
   * The forward is between the two currencies defined by the index.
   * @return the value of the property, not null
   */
  public FxIndex getIndex() {
    return index;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FxNonDeliverableForward other = (FxNonDeliverableForward) obj;
      return JodaBeanUtils.equal(getSettlementCurrencyNotional(), other.getSettlementCurrencyNotional()) &&
          JodaBeanUtils.equal(getAgreedFxRate(), other.getAgreedFxRate()) &&
          JodaBeanUtils.equal(getPaymentDate(), other.getPaymentDate()) &&
          JodaBeanUtils.equal(getIndex(), other.getIndex());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getSettlementCurrencyNotional());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAgreedFxRate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getIndex());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("FxNonDeliverableForward{");
    buf.append("settlementCurrencyNotional").append('=').append(getSettlementCurrencyNotional()).append(',').append(' ');
    buf.append("agreedFxRate").append('=').append(getAgreedFxRate()).append(',').append(' ');
    buf.append("paymentDate").append('=').append(getPaymentDate()).append(',').append(' ');
    buf.append("index").append('=').append(JodaBeanUtils.toString(getIndex()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FxNonDeliverableForward}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code settlementCurrencyNotional} property.
     */
    private final MetaProperty<CurrencyAmount> settlementCurrencyNotional = DirectMetaProperty.ofImmutable(
        this, "settlementCurrencyNotional", FxNonDeliverableForward.class, CurrencyAmount.class);
    /**
     * The meta-property for the {@code agreedFxRate} property.
     */
    private final MetaProperty<FxRate> agreedFxRate = DirectMetaProperty.ofImmutable(
        this, "agreedFxRate", FxNonDeliverableForward.class, FxRate.class);
    /**
     * The meta-property for the {@code paymentDate} property.
     */
    private final MetaProperty<LocalDate> paymentDate = DirectMetaProperty.ofImmutable(
        this, "paymentDate", FxNonDeliverableForward.class, LocalDate.class);
    /**
     * The meta-property for the {@code index} property.
     */
    private final MetaProperty<FxIndex> index = DirectMetaProperty.ofImmutable(
        this, "index", FxNonDeliverableForward.class, FxIndex.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "settlementCurrencyNotional",
        "agreedFxRate",
        "paymentDate",
        "index");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 594670010:  // settlementCurrencyNotional
          return settlementCurrencyNotional;
        case 1040357930:  // agreedFxRate
          return agreedFxRate;
        case -1540873516:  // paymentDate
          return paymentDate;
        case 100346066:  // index
          return index;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FxNonDeliverableForward.Builder builder() {
      return new FxNonDeliverableForward.Builder();
    }

    @Override
    public Class<? extends FxNonDeliverableForward> beanType() {
      return FxNonDeliverableForward.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code settlementCurrencyNotional} property.
     * @return the meta-property, not null
     */
    public MetaProperty<CurrencyAmount> settlementCurrencyNotional() {
      return settlementCurrencyNotional;
    }

    /**
     * The meta-property for the {@code agreedFxRate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<FxRate> agreedFxRate() {
      return agreedFxRate;
    }

    /**
     * The meta-property for the {@code paymentDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> paymentDate() {
      return paymentDate;
    }

    /**
     * The meta-property for the {@code index} property.
     * @return the meta-property, not null
     */
    public MetaProperty<FxIndex> index() {
      return index;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 594670010:  // settlementCurrencyNotional
          return ((FxNonDeliverableForward) bean).getSettlementCurrencyNotional();
        case 1040357930:  // agreedFxRate
          return ((FxNonDeliverableForward) bean).getAgreedFxRate();
        case -1540873516:  // paymentDate
          return ((FxNonDeliverableForward) bean).getPaymentDate();
        case 100346066:  // index
          return ((FxNonDeliverableForward) bean).getIndex();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code FxNonDeliverableForward}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<FxNonDeliverableForward> {

    private CurrencyAmount settlementCurrencyNotional;
    private FxRate agreedFxRate;
    private LocalDate paymentDate;
    private FxIndex index;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(FxNonDeliverableForward beanToCopy) {
      this.settlementCurrencyNotional = beanToCopy.getSettlementCurrencyNotional();
      this.agreedFxRate = beanToCopy.getAgreedFxRate();
      this.paymentDate = beanToCopy.getPaymentDate();
      this.index = beanToCopy.getIndex();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 594670010:  // settlementCurrencyNotional
          return settlementCurrencyNotional;
        case 1040357930:  // agreedFxRate
          return agreedFxRate;
        case -1540873516:  // paymentDate
          return paymentDate;
        case 100346066:  // index
          return index;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 594670010:  // settlementCurrencyNotional
          this.settlementCurrencyNotional = (CurrencyAmount) newValue;
          break;
        case 1040357930:  // agreedFxRate
          this.agreedFxRate = (FxRate) newValue;
          break;
        case -1540873516:  // paymentDate
          this.paymentDate = (LocalDate) newValue;
          break;
        case 100346066:  // index
          this.index = (FxIndex) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public FxNonDeliverableForward build() {
      return new FxNonDeliverableForward(
          settlementCurrencyNotional,
          agreedFxRate,
          paymentDate,
          index);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code settlementCurrencyNotional} property in the builder.
     * @param settlementCurrencyNotional  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder settlementCurrencyNotional(CurrencyAmount settlementCurrencyNotional) {
      JodaBeanUtils.notNull(settlementCurrencyNotional, "settlementCurrencyNotional");
      this.settlementCurrencyNotional = settlementCurrencyNotional;
      return this;
    }

    /**
     * Sets the {@code agreedFxRate} property in the builder.
     * @param agreedFxRate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder agreedFxRate(FxRate agreedFxRate) {
      JodaBeanUtils.notNull(agreedFxRate, "agreedFxRate");
      this.agreedFxRate = agreedFxRate;
      return this;
    }

    /**
     * Sets the {@code paymentDate} property in the builder.
     * @param paymentDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder paymentDate(LocalDate paymentDate) {
      JodaBeanUtils.notNull(paymentDate, "paymentDate");
      this.paymentDate = paymentDate;
      return this;
    }

    /**
     * Sets the {@code index} property in the builder.
     * @param index  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder index(FxIndex index) {
      JodaBeanUtils.notNull(index, "index");
      this.index = index;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(160);
      buf.append("FxNonDeliverableForward.Builder{");
      buf.append("settlementCurrencyNotional").append('=').append(JodaBeanUtils.toString(settlementCurrencyNotional)).append(',').append(' ');
      buf.append("agreedFxRate").append('=').append(JodaBeanUtils.toString(agreedFxRate)).append(',').append(' ');
      buf.append("paymentDate").append('=').append(JodaBeanUtils.toString(paymentDate)).append(',').append(' ');
      buf.append("index").append('=').append(JodaBeanUtils.toString(index));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
