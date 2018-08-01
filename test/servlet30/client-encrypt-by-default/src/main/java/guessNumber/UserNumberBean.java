/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package guessNumber;

import static javax.faces.validator.LongRangeValidator.MAXIMUM_MESSAGE_ID;
import static javax.faces.validator.LongRangeValidator.MINIMUM_MESSAGE_ID;
import static javax.faces.validator.LongRangeValidator.NOT_IN_RANGE_MESSAGE_ID;
import static javax.faces.validator.LongRangeValidator.TYPE_MESSAGE_ID;

import java.io.Serializable;
import java.util.Random;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;

@Named
@SessionScoped
public class UserNumberBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userNumber;
    private Integer randomInt;
    private int maximum = 10;
    private boolean maximumSet = true;

    private int minimum = 1;
    private boolean minimumSet = true;

    public UserNumberBean() {
        Random randomGR = new Random();
        do {
            randomInt = new Integer(randomGR.nextInt(10));
        } while (randomInt.intValue() == 0);

        System.out.println("Duke's number: " + randomInt);
    }

    public void setUserNumber(Integer user_number) {
        userNumber = user_number;
        System.out.println("Set userNumber " + userNumber);
    }

    public Integer getUserNumber() {
        System.out.println("get userNumber " + userNumber);
        return userNumber;
    }

    public String getResponse() {
        if (userNumber != null && userNumber.compareTo(randomInt) == 0) {
            return "Yay! You got it!";
        }

        if (userNumber == null) {
            return "Sorry, " + userNumber + " is incorrect. Try a larger number.";
        }

        int num = userNumber.intValue();
        if (num > randomInt.intValue()) {
            return "Sorry, " + userNumber + " is incorrect. Try a smaller number.";
        }

        return "Sorry, " + userNumber + " is incorrect. Try a larger number.";
    }

    protected String[] status = null;

    public String[] getStatus() {
        return status;
    }

    public void setStatus(String[] newStatus) {
        status = newStatus;
    }

    public int getMaximum() {
        return maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        if (value != null) {
            try {
                int converted = intValue(value);
                if (maximumSet && (converted > maximum)) {
                    if (minimumSet) {
                        throw new ValidatorException(MessageFactory.getMessage(context, NOT_IN_RANGE_MESSAGE_ID,
                                new Object[] { new Integer(minimum), new Integer(maximum), MessageFactory.getLabel(context, component) }));

                    } else {
                        throw new ValidatorException(MessageFactory.getMessage(context, MAXIMUM_MESSAGE_ID,
                                new Object[] { new Integer(maximum), MessageFactory.getLabel(context, component) }));
                    }
                }
                if (minimumSet && (converted < minimum)) {
                    if (maximumSet) {
                        throw new ValidatorException(MessageFactory.getMessage(context, NOT_IN_RANGE_MESSAGE_ID,
                                new Object[] { new Double(minimum), new Double(maximum), MessageFactory.getLabel(context, component) }));

                    } else {
                        throw new ValidatorException(MessageFactory.getMessage(context, MINIMUM_MESSAGE_ID,
                                new Object[] { new Integer(minimum), MessageFactory.getLabel(context, component) }));
                    }
                }
            } catch (NumberFormatException e) {
                throw new ValidatorException(
                        MessageFactory.getMessage(context, TYPE_MESSAGE_ID,
                        new Object[] { MessageFactory.getLabel(context, component) }));
            }
        }
    }

    private int intValue(Object attributeValue) throws NumberFormatException {
        if (attributeValue instanceof Number) {
            return (((Number) attributeValue).intValue());
        }

        return Integer.parseInt(attributeValue.toString());
    }

}
