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

package com.sun.faces.application.annotation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.NamedEventManager;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.NamedEvent;
import jakarta.faces.event.SystemEvent;

/**
 * This class handles the processing the NamedEvent annotation. For each class with this annotation, the following logic
 * is applied:
 * <ol>
 * <li>Get the unqualified class name (e.g., UserLoginEvent)</li>
 * <li>Strip off the trailing "Event", if present (e.g., UserLogin)</li>
 * <li>Convert the first character to lower-case (e.g., userLogin)</li>
 * <li>Prepend the package name to the lower-cased name</li>
 * <li>If the <code>shortName</code> attribute is specified, register the event by that name as well.</li>
 * </ol>
 */
public class NamedEventConfigHandler implements ConfigAnnotationHandler {

    private static final Collection<Class<? extends Annotation>> HANDLES = List.of(NamedEvent.class);

    private Map<Class<?>, Annotation> namedEvents;

    @Override
    public Collection<Class<? extends Annotation>> getHandledAnnotations() {
        return HANDLES;
    }

    @Override
    public void collect(Class<?> target, Annotation annotation) {
        if (namedEvents == null) {
            namedEvents = new HashMap<>();
        }
        namedEvents.put(target, annotation);
    }

    @Override
    public void push(FacesContext ctx) {
        if (namedEvents != null) {
            ApplicationAssociate associate = ApplicationAssociate.getInstance(ctx.getExternalContext());
            if (associate != null) {
                NamedEventManager nem = associate.getNamedEventManager();
                for (Map.Entry<Class<?>, Annotation> entry : namedEvents.entrySet()) {
                    process(nem, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    // --------------------------------------------------------- Private Methods
    /*
     */
    private void process(NamedEventManager nem, Class<?> annotatedClass, Annotation annotation) {
        String name = annotatedClass.getSimpleName();
        int index = name.lastIndexOf("Event");
        if (index > -1) {
            name = name.substring(0, index);
        }

        name = annotatedClass.getPackage().getName() + '.' + Character.toLowerCase(name.charAt(0)) + name.substring(1);

        nem.addNamedEvent(name, (Class<? extends SystemEvent>) annotatedClass);

        String shortName = ((NamedEvent) annotation).shortName();

        if ( !shortName.isEmpty() ) {
            if (nem.isDuplicateNamedEvent(shortName)) {
                nem.addDuplicateName(shortName, (Class<? extends SystemEvent>) annotatedClass);
            } else {
                nem.addNamedEvent(shortName, (Class<? extends SystemEvent>) annotatedClass);
            }
        }
    }
}
