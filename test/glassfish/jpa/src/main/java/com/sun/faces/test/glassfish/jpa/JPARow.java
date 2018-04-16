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

package com.sun.faces.test.glassfish.jpa;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class JPARow implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JPARow)) {
            return false;
        }
        JPARow other = (JPARow) object;
        return !((this.id == null && other.id != null) || 
                (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "com.sun.faces.test.javaee8.jpa.JpaRow[ id=" + id + " ]";
    }    

    @Column(name = "timeInMillis")
    private long timeInMillis;
    
    public long getTimeInMillis() {
        return timeInMillis;
    }
    
    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }
}
