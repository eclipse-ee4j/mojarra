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

package com.sun.faces.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.sun.faces.RIConstants;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

/**
 * This class backs the <code>com.sun.faces.verifyObjects</code> feature which provides basic validation of Components,
 * Converters, and Validators.
 */
public class Verifier {

    /**
     * Thread local to share the <code>Verifier</code>.
     */
    private static final ThreadLocal<Verifier> VERIFIER = new ThreadLocal<>();

    /**
     * Represent the current Faces object types we validate.
     */
    public enum ObjectType {
        COMPONENT, CONVERTER, VALIDATOR, BEHAVIOR,
    }

    /**
     * Container for any messages that may be queued.
     */
    private final List<String> messages;

    // ------------------------------------------------------- Constructors

    /**
     * Construct a new <code>Verifier</code> instance.
     */
    Verifier() {
        messages = new ArrayList<>(4);
    }

    // ------------------------------------------------- Public Methods

    /**
     * @return a <code>Verifier</code> for the current web application <em>if</em> <code>com.sun.faces.verifyObjects</code>
     * is enabled
     */
    public static Verifier getCurrentInstance() {
        return VERIFIER.get();
    }

    /**
     * Set the <code>Verifier</code> for this thread (typically the same thread that is used to bootstrap the application).
     *
     * @param verifier the <code>Verifier</code> for this web application
     */
    public static void setCurrentInstance(Verifier verifier) {
        if (verifier == null) {
            VERIFIER.remove();
        } else {
            VERIFIER.set(verifier);
        }
    }

    /**
     * @return <code>true</code> if no messages were queued by the validation process
     */
    public boolean isApplicationValid() {
        return messages.isEmpty();
    }

    /**
     * @return a <code>List</code> of all failures found
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Validate the specified faces object by:
     * <ul>
     *   <li>Ensure the class can be found and loaded
     *   <li>Ensure the object has a public, no-argument constructor
     *   <li> Ensure the object is an instance of the class represented by <code>assignableTo</code>
     * </ul>
     * If any of these tests fail, queue a message to be displayed at a later point in time.
     *
     * @param type The type of Faces object we're validating
     * @param className the class name of the Faces object we're validating
     * @param assignableTo the type we expect <code>className</code> to either implement or extend
     */
    public void validateObject(ObjectType type, String className, Class<?> assignableTo) {

        // temporary hack until we can fix the stylesheets that create
        // the runtime xml
        if ("jakarta.faces.component.html.HtmlHead".equals(className) || "jakarta.faces.component.html.HtmlBody".equals(className)) {
            return;
        }

        Class<?> c = null;
        try {
            c = Util.loadClass(className, this);
        } catch (ClassNotFoundException cnfe) {
            messages.add(MessageUtils.getExceptionMessageString(MessageUtils.VERIFIER_CLASS_NOT_FOUND_ID, type, className));
        } catch (NoClassDefFoundError ncdfe) {
            messages.add(MessageUtils.getExceptionMessageString(MessageUtils.VERIFIER_CLASS_MISSING_DEP_ID, type, className, ncdfe.getMessage()));
        }

        if (c != null) {
            try {
                Constructor ctor = c.getConstructor(RIConstants.EMPTY_CLASS_ARGS);
                if (!Modifier.isPublic(ctor.getModifiers())) {
                    messages.add(MessageUtils.getExceptionMessageString(MessageUtils.VERIFIER_CTOR_NOT_PUBLIC_ID, type, className));
                }
            } catch (NoSuchMethodException nsme) {
                messages.add(MessageUtils.getExceptionMessageString(MessageUtils.VERIFIER_NO_DEF_CTOR_ID, type, className));
            }
            if (!assignableTo.isAssignableFrom(c)) {
                messages.add(MessageUtils.getExceptionMessageString(MessageUtils.VERIFIER_WRONG_TYPE_ID, type, className));
            }
        }

    }

}
