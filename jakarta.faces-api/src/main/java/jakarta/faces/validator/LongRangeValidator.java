/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package jakarta.faces.validator;

import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

/**
 * <p>
 * <strong class="changed_modified_2_0_rev_a">LongRangeValidator</strong> is a {@link Validator} that checks the value
 * of the corresponding component against specified minimum and maximum values. The following algorithm is implemented:
 * </p>
 *
 * <ul>
 * <li>If the passed value is <code>null</code>, exit immediately.</li>
 * <li>If the current component value is not a floating point type, or a String that is convertible to long, throw a
 * {@link ValidatorException} containing a TYPE_MESSAGE_ID message.</li>
 * <li>If both a <code>maximum</code> and <code>minimum</code> property has been configured on this {@link Validator},
 * check the component value against both limits. If the component value is not within this specified range, throw a
 * {@link ValidatorException} containing a {@link #NOT_IN_RANGE_MESSAGE_ID} message.</li>
 * <li>If a <code>maximum</code> property has been configured on this {@link Validator}, check the component value
 * against this limit. If the component value is greater than the specified maximum, throw a {@link ValidatorException}
 * containing a MAXIMUM_MESSAGE_ID message.</li>
 * <li>If a <code>minimum</code> property has been configured on this {@link Validator}, check the component value
 * against this limit. If the component value is less than the specified minimum, throw a {@link ValidatorException}
 * containing a MINIMUM_MESSAGE_ID message.</li>
 * </ul>
 *
 * <p>
 * For all of the above cases that cause a {@link ValidatorException} to be thrown, if there are parameters to the
 * message that match up with validator parameters, the values of these parameters must be converted using the
 * {@link Converter} registered in the application under the converter id <code>jakarta.faces.Number</code>. This allows
 * the values to be localized according to the current <code>Locale</code>.
 * </p>
 */

public class LongRangeValidator implements Validator, PartialStateHolder {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard converter id for this converter.
     * </p>
     */
    public static final String VALIDATOR_ID = "jakarta.faces.LongRange";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the maximum value check
     * fails. The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the configured maximum value.</li>
     * <li><code>{1}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     */
    public static final String MAXIMUM_MESSAGE_ID = "jakarta.faces.validator.LongRangeValidator.MAXIMUM";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the minimum value check
     * fails. The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the configured minimum value.</li>
     * <li><code>{1}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     */
    public static final String MINIMUM_MESSAGE_ID = "jakarta.faces.validator.LongRangeValidator.MINIMUM";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the maximum or minimum
     * value check fails, and both the maximum and minimum values for this validator have been set. The message format
     * string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the configured minimum value.</li>
     * <li><code>{1}</code> replaced by the configured maximum value.</li>
     * <li><code>{2}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     */
    public static final String NOT_IN_RANGE_MESSAGE_ID = "jakarta.faces.validator.LongRangeValidator.NOT_IN_RANGE";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the current value of
     * this component is not of the correct type. The message format string for this message may optionally include a
     * <code>{0}</code> placeholder that will be replaced by a <code>String</code> whose value is the label of the input
     * component that produced this message.
     * </p>
     */
    public static final String TYPE_MESSAGE_ID = "jakarta.faces.validator.LongRangeValidator.TYPE";

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Construct a {@link Validator} with no preconfigured limits.
     * </p>
     */
    public LongRangeValidator() {

        super();

    }

    /**
     * <p>
     * Construct a {@link Validator} with the specified preconfigured limit.
     * </p>
     *
     * @param maximum Maximum value to allow
     */
    public LongRangeValidator(long maximum) {

        super();
        setMaximum(maximum);

    }

    /**
     * <p>
     * Construct a {@link Validator} with the specified preconfigured limits.
     * </p>
     *
     * @param maximum Maximum value to allow
     * @param minimum Minimum value to allow
     */
    public LongRangeValidator(long maximum, long minimum) {

        super();
        setMaximum(maximum);
        setMinimum(minimum);

    }

    // -------------------------------------------------------------- Properties

    private Long maximum;

    /**
     * <p>
     * Return the maximum value to be enforced by this {@link Validator}.
     * </p>
     *
     * @return the maximum
     */
    public long getMaximum() {

        return maximum != null ? maximum : 0;

    }

    /**
     * <p>
     * Set the maximum value to be enforced by this {@link Validator}.
     * </p>
     *
     * @param maximum The new maximum value
     */
    public void setMaximum(long maximum) {

        clearInitialState();
        this.maximum = maximum;

    }

    private Long minimum;

    /**
     * <p>
     * Return the minimum value to be enforced by this {@link Validator}.
     * </p>
     *
     * @return the minimum
     */
    public long getMinimum() {

        return minimum != null ? minimum : 0;

    }

    /**
     * <p>
     * Set the minimum value to be enforced by this {@link Validator}.
     * </p>
     *
     * @param minimum The new minimum value
     */
    public void setMinimum(long minimum) {

        clearInitialState();
        this.minimum = minimum;

    }

    // ------------------------------------------------------- Validator Methods

    /**
     * @throws NullPointerException {@inheritDoc}
     * @throws ValidatorException {@inheritDoc}
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
        if (value != null) {
            try {
                long converted = longValue(value);
                if (isMaximumSet() && converted > maximum) {
                    if (isMinimumSet()) {
                        throw new ValidatorException(MessageFactory.getMessage(context, NOT_IN_RANGE_MESSAGE_ID, stringValue(component, minimum, context),
                                stringValue(component, maximum, context), MessageFactory.getLabel(context, component)));

                    } else {
                        throw new ValidatorException(MessageFactory.getMessage(context, MAXIMUM_MESSAGE_ID, stringValue(component, maximum, context),
                                MessageFactory.getLabel(context, component)));
                    }
                }
                if (isMinimumSet() && converted < minimum) {
                    if (isMaximumSet()) {
                        throw new ValidatorException(MessageFactory.getMessage(context, NOT_IN_RANGE_MESSAGE_ID, stringValue(component, minimum, context),
                                stringValue(component, maximum, context), MessageFactory.getLabel(context, component)));

                    } else {
                        throw new ValidatorException(MessageFactory.getMessage(context, MINIMUM_MESSAGE_ID, stringValue(component, minimum, context),
                                MessageFactory.getLabel(context, component)));
                    }
                }
            } catch (NumberFormatException e) {
                throw new ValidatorException(MessageFactory.getMessage(context, TYPE_MESSAGE_ID, MessageFactory.getLabel(context, component)), e);
            }
        }

    }

    @Override
    public boolean equals(Object otherObj) {

        if (!(otherObj instanceof LongRangeValidator)) {
            return false;
        }
        LongRangeValidator other = (LongRangeValidator) otherObj;
        return getMaximum() == other.getMaximum() && getMinimum() == other.getMinimum() && isMaximumSet() == other.isMaximumSet()
                && isMinimumSet() == other.isMinimumSet();

    }

    @Override
    public int hashCode() {

        int hashCode = Long.valueOf(getMinimum()).hashCode() + Long.valueOf(getMaximum()).hashCode() + Boolean.valueOf(isMinimumSet()).hashCode()
                + Boolean.valueOf(isMaximumSet()).hashCode();
        return hashCode;

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Return the specified attribute value, converted to a <code>long</code>.
     * </p>
     *
     * @param attributeValue The attribute value to be converted
     * @throws NumberFormatException if conversion is not possible
     */
    private static long longValue(Object attributeValue) throws NumberFormatException {

        if (attributeValue instanceof Number) {
            return ((Number) attributeValue).longValue();
        } else {
            return Long.parseLong(attributeValue.toString());
        }

    }

    private static String stringValue(UIComponent component, Long toConvert, FacesContext context) {

        Converter converter = context.getApplication().createConverter("jakarta.faces.Number");
        return converter.getAsString(context, component, toConvert);

    }

    private boolean isMinimumSet() {

        return minimum != null;

    }

    private boolean isMaximumSet() {

        return maximum != null;

    }

    // ----------------------------------------------------- StateHolder Methods

    @Override
    public Object saveState(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (!initialStateMarked()) {
            Object values[] = new Object[2];
            values[0] = maximum;
            values[1] = minimum;
            return values;
        }
        return null;

    }

    @Override
    public void restoreState(FacesContext context, Object state) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (state != null) {
            Object values[] = (Object[]) state;
            maximum = (Long) values[0];
            minimum = (Long) values[1];
        }

    }

    private boolean transientValue;

    @Override
    public boolean isTransient() {

        return transientValue;

    }

    @Override
    public void setTransient(boolean transientValue) {

        this.transientValue = transientValue;

    }

    private boolean initialState;

    @Override
    public void markInitialState() {
        initialState = true;
    }

    @Override
    public boolean initialStateMarked() {
        return initialState;
    }

    @Override
    public void clearInitialState() {
        initialState = false;
    }
}
