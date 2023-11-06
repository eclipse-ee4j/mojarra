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
 * <strong class="changed_modified_2_0_rev_a">LengthValidator</strong> is a {@link Validator} that checks the number of
 * characters in the String representation of the value of the associated component. The following algorithm is
 * implemented:
 * </p>
 *
 * <ul>
 * <li>Convert the passed value to a String, if necessary, by calling its <code>toString()</code> method.</li>
 * <li>If a <code>maximum</code> property has been configured on this {@link Validator}, check the length of the
 * converted String against this limit. If the String length is larger than the specified maximum, throw a
 * {@link ValidatorException} containing a a MAXIMUM_MESSAGE_ID message.</li>
 * <li>If a <code>minimum</code> property has been configured on this {@link Validator}, check the length of the
 * converted String against this limit. If the String length is less than the specified minimum, throw a
 * {@link ValidatorException} containing a a MINIMUM_MESSAGE_ID message.</li>
 * </ul>
 *
 * <p>
 * For all of the above cases that cause a {@link ValidatorException} to be thrown, if there are parameters to the
 * message that match up with validator parameters, the values of these parameters must be converted using the
 * {@link Converter} registered in the application under the converter id <code>jakarta.faces.Number</code>. This allows
 * the values to be localized according to the current <code>Locale</code>.
 * </p>
 */

public class LengthValidator implements Validator, PartialStateHolder {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard validator id for this validator.
     * </p>
     */
    public static final String VALIDATOR_ID = "jakarta.faces.Length";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the maximum length
     * check fails. The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the configured maximum length.</li>
     * <li><code>{1}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     */
    public static final String MAXIMUM_MESSAGE_ID = "jakarta.faces.validator.LengthValidator.MAXIMUM";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the minimum length
     * check fails. The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the configured minimum length.</li>
     * <li><code>{1}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     */
    public static final String MINIMUM_MESSAGE_ID = "jakarta.faces.validator.LengthValidator.MINIMUM";

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Construct a {@link Validator} with no preconfigured limits.
     * </p>
     */
    public LengthValidator() {

        super();

    }

    /**
     * <p>
     * Construct a {@link Validator} with the specified preconfigured limit.
     * </p>
     *
     * @param maximum Maximum value to allow
     */
    public LengthValidator(int maximum) {

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
    public LengthValidator(int maximum, int minimum) {

        super();
        setMaximum(maximum);
        setMinimum(minimum);

    }

    // -------------------------------------------------------------- Properties

    private Integer maximum;

    /**
     * <p>
     * Return the maximum length to be enforced by this {@link Validator}, or <code>0</code> if the maximum has not been
     * set.
     * </p>
     *
     * @return the maximum
     */
    public int getMaximum() {

        return maximum != null ? maximum : 0;

    }

    /**
     * <p>
     * Set the maximum length to be enforced by this {@link Validator}.
     * </p>
     *
     * @param maximum The new maximum value
     */
    public void setMaximum(int maximum) {

        clearInitialState();
        this.maximum = maximum;

    }

    private Integer minimum;

    /**
     * <p>
     * Return the minimum length to be enforced by this {@link Validator}, or <code>0</code> if the minimum has not been
     * set.
     * </p>
     *
     * @return the minimum
     */
    public int getMinimum() {

        return minimum != null ? minimum : 0;

    }

    /**
     * <p>
     * Set the minimum length to be enforced by this {@link Validator}.
     * </p>
     *
     * @param minimum The new minimum value
     */
    public void setMinimum(int minimum) {

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
            String converted = stringValue(value);
            if (isMaximumSet() && converted.length() > maximum) {
                throw new ValidatorException(MessageFactory.getMessage(context, MAXIMUM_MESSAGE_ID, integerToString(component, maximum, context),
                        MessageFactory.getLabel(context, component)));
            }
            if (isMinimumSet() && converted.length() < minimum) {
                throw new ValidatorException(MessageFactory.getMessage(context, MINIMUM_MESSAGE_ID, integerToString(component, minimum, context),
                        MessageFactory.getLabel(context, component)));
            }
        }

    }

    @Override
    public boolean equals(Object otherObj) {

        if (!(otherObj instanceof LengthValidator)) {
            return false;
        }
        LengthValidator other = (LengthValidator) otherObj;
        return getMaximum() == other.getMaximum() && getMinimum() == other.getMinimum() && isMinimumSet() == other.isMinimumSet()
                && isMaximumSet() == other.isMaximumSet();

    }

    @Override
    public int hashCode() {

        int hashCode = Integer.valueOf(getMinimum()).hashCode() + Integer.valueOf(getMaximum()).hashCode() + Boolean.valueOf(isMaximumSet()).hashCode()
                + Boolean.valueOf(isMinimumSet()).hashCode();
        return hashCode;

    }

    // -------------------------------------------------------- Private Methods

    /**
     * <p>
     * Return the specified attribute value, converted to a <code>String</code>.
     * </p>
     *
     * @param attributeValue The attribute value to be converted
     */
    private static String stringValue(Object attributeValue) {

        if (attributeValue == null) {
            return null;
        } else if (attributeValue instanceof String) {
            return (String) attributeValue;
        } else {
            return attributeValue.toString();
        }

    }

    private static String integerToString(UIComponent component, Integer toConvert, FacesContext context) {

        Converter converter = context.getApplication().createConverter("jakarta.faces.Number");
        return converter.getAsString(context, component, toConvert);

    }

    private boolean isMaximumSet() {

        return maximum != null;

    }

    private boolean isMinimumSet() {

        return minimum != null;

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
            maximum = (Integer) values[0];
            minimum = (Integer) values[1];
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
