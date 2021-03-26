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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_0_rev_a">A Validator</span> that checks against a Regular Expression (which is the
 * pattern property). The pattern must resolve to a String that follows the java.util.regex standards.
 * </p>
 *
 * @since 2.0
 */
public class RegexValidator implements Validator, PartialStateHolder {

    private String regex;

    /**
     * <p>
     * The standard converter id for this converter.
     * </p>
     */
    public static final String VALIDATOR_ID = "jakarta.faces.RegularExpression";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the value returned from
     * {@link #getPattern} is <code>null</code> or the empty String.
     * </p>
     */
    public static final String PATTERN_NOT_SET_MESSAGE_ID = "jakarta.faces.validator.RegexValidator.PATTERN_NOT_SET";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the act of matching the
     * value against the pattern returned from {@link #getPattern} fails because the value does not match the pattern.
     * </p>
     */
    public static final String NOT_MATCHED_MESSAGE_ID = "jakarta.faces.validator.RegexValidator.NOT_MATCHED";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the act of matching the
     * value against the pattern returned from {@link #getPattern} fails because of a <code>PatternSyntaxException</code>.
     * </p>
     */
    public static final String MATCH_EXCEPTION_MESSAGE_ID = "jakarta.faces.validator.RegexValidator.MATCH_EXCEPTION";

    /**
     * <p>
     * The Regular Expression property to validate against.
     * </p>
     *
     * @param pattern a regular expression pattern
     */
    public void setPattern(String pattern) {
        clearInitialState();
        regex = pattern;
    }

    /**
     * <p>
     * Return the <code>ValueExpression</code> that yields the regular expression pattern when evaluated.
     * </p>
     *
     * @return the pattern
     */

    public String getPattern() {
        return regex;
    }

    /**
     *
     * <p>
     * Validate a String against a regular expression pattern. The full regex pattern must be matched in order to pass the
     * validation.
     * </p>
     *
     * @param context {@inheritDoc}
     * @param component {@inheritDoc}
     * @param value {@inheritDoc}
     *
     * @throws NullPointerException {@inheritDoc}
     * @throws ValidatorException {@inheritDoc}
     *
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (component == null) {
            throw new NullPointerException();
        }

        if (value == null) {
            return;
        }

        FacesMessage fmsg;

        Locale locale = context.getViewRoot().getLocale();

        if (regex == null || regex.length() == 0) {
            fmsg = MessageFactory.getMessage(locale, PATTERN_NOT_SET_MESSAGE_ID, (Object) null);
            throw new ValidatorException(fmsg);
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher((String) value);
            if (!matcher.matches()) {
                Object[] params = { regex };
                fmsg = MessageFactory.getMessage(locale, NOT_MATCHED_MESSAGE_ID, params);
                throw new ValidatorException(fmsg);
            }
        } catch (PatternSyntaxException pse) {
            fmsg = MessageFactory.getMessage(locale, MATCH_EXCEPTION_MESSAGE_ID, (Object) null);
            throw new ValidatorException(fmsg, pse);
        }
    }

    // ----------------------------------------------------- StateHolder Methods

    @Override
    public Object saveState(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (!initialStateMarked()) {
            Object values[] = new Object[1];
            values[0] = regex;

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
            regex = (String) values[0];
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
