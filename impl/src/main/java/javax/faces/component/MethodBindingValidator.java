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

package javax.faces.component;

import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import javax.faces.el.MethodBinding;
import javax.faces.el.EvaluationException;
import javax.faces.context.FacesContext;

/**
 * <p><strong>MethodBindingValidator</strong> is an {@link
 * ValidatorListener} that wraps a {@link MethodBinding}. When it
 * receives a {@link ActionEvent}, it executes a method on an
 * object identified by the {@link MethodBinding}.</p>
 */

class MethodBindingValidator extends MethodBindingAdapterBase implements Validator, StateHolder {


    // ------------------------------------------------------ Instance Variables
    
    private MethodBinding methodBinding = null;

    public MethodBindingValidator() {}

    
    /**
     * <p>Construct a {@link Validator} that contains a {@link
     * MethodBinding}.</p>
     */
    public MethodBindingValidator(MethodBinding methodBinding) {

        super();
        this.methodBinding = methodBinding;

    }

    public MethodBinding getWrapped() {
	return methodBinding;
    }


    // ------------------------------------------------------- Validator

    @Override
    public void validate(FacesContext context,
                         UIComponent  component,
                         Object       value) throws ValidatorException {
	if (null == context || null == component) {
	    throw new NullPointerException();
	}
        try {
            methodBinding.invoke(context, new Object[] {context, component, 
							value});
        } 
	catch (EvaluationException ee) {
	    Throwable cause = this.getExpectedCause(ValidatorException.class,
						    ee);
	    if (cause instanceof ValidatorException) {
		throw ((ValidatorException) cause);
	    }
	    if (cause instanceof RuntimeException) {
		throw ((RuntimeException) cause);
	    }
	    throw new IllegalStateException(ee);
        }
	
	
    }



    // 
    // Methods from StateHolder
    //

    

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
	Object result = null;
	if (!tranzient) {
	    if (methodBinding instanceof StateHolder) {
		Object [] stateStruct = new Object[2];
		
		// save the actual state of our wrapped methodBinding
		stateStruct[0] = ((StateHolder)methodBinding).saveState(context);
		// save the class name of the methodBinding impl
		stateStruct[1] = methodBinding.getClass().getName();

		result = stateStruct;
	    }
	    else {
		result = methodBinding;
	    }
	}

	return result;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
	// if we have state
	if (null == state) {
	    return;
	}
	
	if (!(state instanceof MethodBinding)) {
	    Object [] stateStruct = (Object []) state;
	    Object savedState = stateStruct[0];
	    String className = stateStruct[1].toString();
	    MethodBinding result = null;
	    
	    Class toRestoreClass;
	    if (null != className) {
		try {
		    toRestoreClass = loadClass(className, this);
		}
		catch (ClassNotFoundException e) {
		    throw new IllegalStateException(e);
		}
		
		if (null != toRestoreClass) {
		    try {
			result = 
			    (MethodBinding) toRestoreClass.newInstance();
		    }
		    catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		    }
		}
		
		if (null != result && null != savedState) {
		    // don't need to check transient, since that was
		    // done on the saving side.
		    ((StateHolder)result).restoreState(context, savedState);
		}
		methodBinding = result;
	    }
	}
	else {
	    methodBinding = (MethodBinding) state;
	}
    }

    private boolean tranzient = false;

    @Override
    public boolean isTransient() {
	return tranzient;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
	tranzient = newTransientValue;
    }

    //
    // Helper methods for StateHolder
    //

    private static Class loadClass(String name, 
            Object fallbackClass) throws ClassNotFoundException {
        ClassLoader loader =
            Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return Class.forName(name, false, loader);
    }
}
