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

package com.sun.faces.application;

import java.beans.PropertyEditorSupport;

/**
 * Abstract base for a {@link java.beans.PropertyEditor} that delegates to a faces Converter that was registered by-type
 * in a faces-config descriptor. Concrete implementations (such as generated by {@link ConverterPropertyEditorFactory})
 * will override {@link #getTargetClass}. (This is based on the original ConverterPropertyEditor code).
 */
public abstract class ConverterPropertyEditorBase extends PropertyEditorSupport {

    /**
     * Return the target class of the objects that are being edited. This is used as a key to find the appropriate
     * {@link jakarta.faces.convert.Converter} from the Faces application.
     *
     * @return the target class.
     */
    protected abstract Class<?> getTargetClass();

    /**
     * Convert the <code>textValue</code> to an object of type {@link #getTargetClass} by delegating to a converter in the
     * faces Application.
     */
    @Override
    public void setAsText(String textValue) throws IllegalArgumentException {
        try {
            Object appAssociate = getPropertyEditorHelper();
            // Get targetClass for the current ClassLoader
            Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(getTargetClass().getName());

            Object value = appAssociate.getClass().getMethod("convertToObject", Class.class, String.class).invoke(appAssociate, targetClass, textValue);

            if (value != null) {
                setValue(value);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected Error attempting to use this ConverterPropertyEditor.  You're deployment environment may not support"
                    + "ConverterPropertyEditors.  Try restarting your server or disabling " + "com.sun.faces.registerConverterPropertyEditors", e);
        }
    }

    private Object getPropertyEditorHelper() throws Exception {
        // Load the current
        Class<?> facesContextClass = Thread.currentThread().getContextClassLoader().loadClass("com.sun.faces.application.ApplicationAssociate");

        // Get the current context version of this class in case
        Object appAssociate = facesContextClass.getMethod("getCurrentInstance").invoke(null);

        if (appAssociate == null) {
            throw new IllegalStateException("Unable to find Deployed Faces Application.  You're deployment environment may not support"
                    + "ConverterPropertyEditors.  Try restarting your server or turn off " + "com.sun.faces.registerConverterPropertyEditors");
        }

        return appAssociate.getClass().getMethod("getPropertyEditorHelper").invoke(appAssociate);
    }

    /**
     * Convert an object of type {@link #getTargetClass} to text by delegating to a converter obtained from the Faces
     * Application.
     */
    @Override
    public String getAsText() {
        try {
            Object application = getPropertyEditorHelper();

            Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(getTargetClass().getName());

            String text = (String) application.getClass().getMethod("convertToString", Class.class, Object.class).invoke(application, targetClass, getValue());

            if (text != null) {
                return text;
            }

            return super.getAsText();

        } catch (Exception e) {
            return super.getAsText();
        }
    }
}
