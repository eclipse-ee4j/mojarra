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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.LongRangeValidator;
import javax.faces.validator.ValidatorException;

import java.util.Random;


public class UserNumberBean {

    Integer userNumber = null;
    Integer randomInt = null;
    String response = null;


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
        } else if (userNumber == null) {
            return "Sorry, " + userNumber +
                   " is incorrect. Try a larger number.";
        } else {
            int num = userNumber.intValue();
            if (num > randomInt.intValue()) {
                return "Sorry, " + userNumber +
                       " is incorrect. Try a smaller number.";
            } else {
                return "Sorry, " + userNumber +
                       " is incorrect. Try a larger number.";
            }
        }
    }


    protected String[] status = null;


    public String[] getStatus() {
        return status;
    }


    public void setStatus(String[] newStatus) {
        status = newStatus;
    }


    private int maximum = 0;
    private boolean maximumSet = false;


    public int getMaximum() {
        return (this.maximum);
    }


    public void setMaximum(int maximum) {
        this.maximum = maximum;
        this.maximumSet = true;
    }


    private int minimum = 0;
    private boolean minimumSet = false;


    public int getMinimum() {
        return (this.minimum);
    }


    public void setMinimum(int minimum) {
        this.minimum = minimum;
        this.minimumSet = true;
    }


    public void validate(FacesContext context,
                         UIComponent component,
                         Object value) throws ValidatorException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        if (value != null) {
            try {
                int converted = intValue(value);
                if (maximumSet &&
                    (converted > maximum)) {
                    if (minimumSet) {
                        throw new ValidatorException(
                              MessageFactory.getMessage
                                    (context,
                                     LongRangeValidator.NOT_IN_RANGE_MESSAGE_ID,
                                     new Object[]{
                                           new Integer(minimum),
                                           new Integer(maximum),
                                           MessageFactory.getLabel(context,
                                                                   component)
                                     }));

                    } else {
                        throw new ValidatorException(
                              MessageFactory.getMessage
                                    (context,
                                     LongRangeValidator.MAXIMUM_MESSAGE_ID,
                                     new Object[]{
                                           new Integer(maximum),
                                           MessageFactory.getLabel(context,
                                                                   component)
                                     }));
                    }
                }
                if (minimumSet &&
                    (converted < minimum)) {
                    if (maximumSet) {
                        throw new ValidatorException(MessageFactory.getMessage
                              (context,
                               LongRangeValidator.NOT_IN_RANGE_MESSAGE_ID,
                               new Object[]{
                                     new Double(minimum),
                                     new Double(maximum),
                                     MessageFactory.getLabel(context, component)
                               }));

                    } else {
                        throw new ValidatorException(
                              MessageFactory.getMessage
                                    (context,
                                     LongRangeValidator.MINIMUM_MESSAGE_ID,
                                     new Object[]{
                                           new Integer(minimum),
                                           MessageFactory.getLabel(context,
                                                                   component)
                                     }));
                    }
                }
            } catch (NumberFormatException e) {
                throw new ValidatorException(
                      MessageFactory.getMessage
                            (context, LongRangeValidator.TYPE_MESSAGE_ID,
                             new Object[]{MessageFactory.getLabel(context,
                                                                  component)}));
            }
        }

    }


    private int intValue(Object attributeValue)
          throws NumberFormatException {

        if (attributeValue instanceof Number) {
            return (((Number) attributeValue).intValue());
        } else {
            return (Integer.parseInt(attributeValue.toString()));
        }

    }

}
