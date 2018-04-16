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

package javax.faces.el;


import javax.faces.FacesException;


/**
 * <p>An exception reporting an error that occurred during the evaluation
 * of an expression in a {@link MethodBinding} or {@link ValueBinding}.</p>
 *
 * @deprecated This has been replaced by {@link javax.el.ELException}.
 */

public class EvaluationException extends FacesException {


    private static final long serialVersionUID = 6143696299509378423L;

    /**
     * <p>Construct a new exception with no detail message or root cause.</p>
     */
    public EvaluationException() {

        super();

    }


    /**
     * <p>Construct a new exception with the specified detail message and
     * no root cause.</p>
     *
     * @param message The detail message for this exception
     */
    public EvaluationException(String message) {

        super(message);

    }


    /**
     * <p>Construct a new exception with the specified root cause.  The detail
     * message will be set to <code>(cause == null ? null :
     * cause.toString()</code>
     *
     * @param cause The root cause for this exception
     */
    public EvaluationException(Throwable cause) {

        super(cause);

    }


    /**
     * <p>Construct a new exception with the specified detail message and
     * root cause.</p>
     *
     * @param message The detail message for this exception
     * @param cause The root cause for this exception
     */
    public EvaluationException(String message, Throwable cause) {

        super(message, cause);

    }



}
