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

import com.sun.faces.facelets.util.ReflectionUtil;

import javax.el.FunctionMapper;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Default implementation of the FunctionMapper
 *
 * @author Jacob Hookom
 * @version $Id$
 * @see java.lang.reflect.Method
 * @see javax.el.FunctionMapper
 */
public final class DefaultFunctionMapper extends FunctionMapper implements
                                                                Externalizable {

    private static final long serialVersionUID = 1L;

    private Map functions = null;

    /*
      * (non-Javadoc)
      *
      * @see javax.el.FunctionMapper#resolveFunction(java.lang.String,
      *      java.lang.String)
      */
    @Override
    public Method resolveFunction(String prefix, String localName) {
        if (this.functions != null) {
            Function f = (Function) this.functions
                  .get(prefix + ":" + localName);
            return f.getMethod();
        }
        return null;
    }

    public void addFunction(String prefix, String localName, Method m) {
        if (this.functions == null) {
            this.functions = new HashMap();
        }
        Function f = new Function(prefix, localName, m);
        synchronized (this) {
            this.functions.put(prefix + ":" + localName, f);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
      */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.functions);
    }

    /*
      * (non-Javadoc)
      *
      * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
      */
    @Override
    public void readExternal(ObjectInput in) throws IOException,
                                                    ClassNotFoundException {
        this.functions = (Map) in.readObject();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(128);
        sb.append("FunctionMapper[\n");
        for (Iterator itr = this.functions.values().iterator();
             itr.hasNext();) {
            sb.append(itr.next()).append('\n');
        }
        sb.append(']');
        return sb.toString();
    }

    private static class Function implements Externalizable {

        private static final long serialVersionUID = 1L;

        protected transient Method m;

        protected String owner;

        protected String name;

        protected String[] types;

        protected String prefix;

        protected String localName;

        /**
         *
         */
        public Function(String prefix, String localName, Method m) {
            if (localName == null) {
                throw new NullPointerException("LocalName cannot be null");
            }
            if (m == null) {
                throw new NullPointerException("Method cannot be null");
            }
            this.prefix = prefix;
            this.localName = localName;
            this.m = m;
        }

        public Function() {
            // for serialization
        }

        /*
           * (non-Javadoc)
           *
           * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
           */
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF((this.prefix != null) ? this.prefix : "");
            out.writeUTF(this.localName);
            out.writeUTF(this.m.getDeclaringClass().getName());
            out.writeUTF(this.m.getName());
            out.writeObject(ReflectionUtil.toTypeNameArray(this.m
                  .getParameterTypes()));
        }

        /*
           * (non-Javadoc)
           *
           * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
           */
        @Override
        public void readExternal(ObjectInput in) throws IOException,
                                                        ClassNotFoundException {

            this.prefix = in.readUTF();
            if ("".equals(this.prefix)) {
                this.prefix = null;
            }
            this.localName = in.readUTF();
            this.owner = in.readUTF();
            this.name = in.readUTF();
            this.types = (String[]) in.readObject();
        }

        public Method getMethod() {
            if (this.m == null) {
                try {
                    Class t = ReflectionUtil.forName(this.owner);
                    Class[] p = ReflectionUtil.toTypeArray(this.types);
                    this.m = t.getMethod(this.name, p);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                }
            }
            return this.m;
        }

        public boolean matches(String prefix, String localName) {
            if (this.prefix != null) {
                if (prefix == null) {
                    return false;
                }
                if (!this.prefix.equals(prefix)) {
                    return false;
                }
            }
            return this.localName.equals(localName);
        }

        /*
           * (non-Javadoc)
           *
           * @see java.lang.Object#equals(java.lang.Object)
           */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Function) {
                return this.hashCode() == obj.hashCode();
            }
            return false;
        }

        /*
           * (non-Javadoc)
           *
           * @see java.lang.Object#hashCode()
           */
        @Override
        public int hashCode() {
            return (this.prefix + this.localName).hashCode();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(32);
            sb.append("Function[");
            if (this.prefix != null) {
                sb.append(this.prefix).append(':');
            }
            sb.append(this.name).append("] ");
            sb.append(this.m);
            return sb.toString();
        }
    }
}
