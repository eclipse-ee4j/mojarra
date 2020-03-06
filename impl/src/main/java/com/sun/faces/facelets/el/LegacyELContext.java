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

package com.sun.faces.facelets.el;

import jakarta.el.*;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.PropertyNotFoundException;
import jakarta.faces.el.PropertyResolver;
import jakarta.faces.el.VariableResolver;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 
 * 
 * @author Jacob Hookom
 * @version $Id$
 * @deprecated
 */
public final class LegacyELContext extends ELContext {

    private static final String[] IMPLICIT_OBJECTS = new String[] {
            "application", "applicationScope", "cookie", "facesContext",
            "header", "headerValues", "initParam", "param", "paramValues",
            "request", "requestScope", "session", "sessionScope", "view" };

    private final static FunctionMapper FUNCTIONS = new EmptyFunctionMapper();

    private final FacesContext faces;

    private final ELResolver resolver;

    private final VariableMapper variables;

    public LegacyELContext(FacesContext faces) {
        this.faces = faces;
        this.resolver = new LegacyELResolver();
        this.variables = new DefaultVariableMapper();
    }

    @Override
    public ELResolver getELResolver() {
        return this.resolver;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return FUNCTIONS;
    }

    @Override
    public VariableMapper getVariableMapper() {
        return this.variables;
    }
    
    public FacesContext getFacesContext() {
        return this.faces;
    }

    private final class LegacyELResolver extends ELResolver {

        @Override
        public Class getCommonPropertyType(ELContext context, Object base) {
            return Object.class;
        }

        @Override
        public Iterator getFeatureDescriptors(ELContext context, Object base) {
            return Collections.EMPTY_LIST.iterator();
        }

        private VariableResolver getVariableResolver() {
            return faces.getApplication().getVariableResolver();
        }

        private PropertyResolver getPropertyResolver() {
            return faces.getApplication().getPropertyResolver();
        }

        @Override
        public Class getType(ELContext context, Object base, Object property) {
            if (property == null) {
                return null;
            }
            try {
                context.setPropertyResolved(true);
                if (base == null) {
                    Object obj = this.getVariableResolver().resolveVariable(
                            faces, property.toString());
                    return (obj != null) ? obj.getClass() : null;
                } else {
                    if (base instanceof List || base.getClass().isArray()) {
                        return this.getPropertyResolver().getType(base,
                                Integer.parseInt(property.toString()));
                    } else {
                        return this.getPropertyResolver().getType(base,
                                property);
                    }
                }
            } catch (PropertyNotFoundException e) {
                throw new jakarta.el.PropertyNotFoundException(e.getMessage(), e
                        .getCause());
            } catch (EvaluationException e) {
                throw new ELException(e.getMessage(), e.getCause());
            }
        }

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            if (property == null) {
                return null;
            }
            try {
                context.setPropertyResolved(true);
                if (base == null) {
                    return this.getVariableResolver().resolveVariable(faces,
                            property.toString());
                } else {
                    if (base instanceof List || base.getClass().isArray()) {
                        return this.getPropertyResolver().getValue(base,
                                Integer.parseInt(property.toString()));
                    } else {
                        return this.getPropertyResolver().getValue(base,
                                property);
                    }
                }
            } catch (PropertyNotFoundException e) {
                throw new jakarta.el.PropertyNotFoundException(e.getMessage(), e
                        .getCause());
            } catch (EvaluationException e) {
                throw new ELException(e.getMessage(), e.getCause());
            }
        }

        @Override
        public boolean isReadOnly(ELContext context, Object base,
                Object property) {
            if (property == null) {
                return true;
            }
            try {
                context.setPropertyResolved(true);
                if (base == null) {
                    return false; // what can I do?
                } else {
                    if (base instanceof List || base.getClass().isArray()) {
                        return this.getPropertyResolver().isReadOnly(base,
                                Integer.parseInt(property.toString()));
                    } else {
                        return this.getPropertyResolver().isReadOnly(base,
                                property);
                    }
                }
            } catch (PropertyNotFoundException e) {
                throw new jakarta.el.PropertyNotFoundException(e.getMessage(), e
                        .getCause());
            } catch (EvaluationException e) {
                throw new ELException(e.getMessage(), e.getCause());
            }
        }

        @Override
        public void setValue(ELContext context, Object base, Object property,
                Object value) {
            if (property == null) {
                throw new PropertyNotWritableException("Null Property");
            }
            try {
                context.setPropertyResolved(true);
                if (base == null) {
                    if (Arrays.binarySearch(IMPLICIT_OBJECTS, property
                            .toString()) >= 0) {
                        throw new PropertyNotWritableException(
                                "Implicit Variable Not Setable: " + property);
                    } else {
                        Map scope = this.resolveScope(property.toString());
                        this.getPropertyResolver().setValue(scope, property,
                                value);
                    }
                } else {
                    if (base instanceof List || base.getClass().isArray()) {
                        this.getPropertyResolver().setValue(base,
                                Integer.parseInt(property.toString()), value);
                    } else {
                        this.getPropertyResolver().setValue(base, property,
                                value);
                    }
                }
            } catch (PropertyNotFoundException e) {
                throw new jakarta.el.PropertyNotFoundException(e.getMessage(), e
                        .getCause());
            } catch (EvaluationException e) {
                throw new ELException(e.getMessage(), e.getCause());
            }

        }

        private Map resolveScope(String var) {
            ExternalContext ext = faces.getExternalContext();

            // cycle through the scopes to find a match, if no
            // match is found, then return the requestScope
            Map map = ext.getRequestMap();
            if (!map.containsKey(var)) {
                map = ext.getSessionMap();
                if (!map.containsKey(var)) {
                    map = ext.getApplicationMap();
                    if (!map.containsKey(var)) {
                        map = ext.getRequestMap();
                    }
                }
            }
            return map;
        }
    }

    private final static class EmptyFunctionMapper extends FunctionMapper {

        @Override
        public Method resolveFunction(String prefix, String localName) {
            return null;
        }

    }

}
