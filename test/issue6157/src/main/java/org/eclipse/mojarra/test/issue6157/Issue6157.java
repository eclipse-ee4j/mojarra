/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.issue6157;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue6157 {

    private final List<String> invoked = new ArrayList<>();
    private String value;

    public void eventNoArg() {
        invoked.add("eventNoArg()");
    }

    public void eventOneArg(ComponentSystemEvent event) {
        invoked.add("eventOneArg(" + describe(event) + ")");
    }

    public void eventBoth() {
        invoked.add("eventBoth()");
    }

    public void eventBoth(ComponentSystemEvent event) {
        invoked.add("eventBoth(" + describe(event) + ")");
    }

    public void eventComposite(ComponentSystemEvent event) {
        invoked.add("eventComposite(" + describe(event) + ")");
    }

    public void eventCompositeNoArg() {
        invoked.add("eventCompositeNoArg()");
    }

    public void ajaxNoArg() {
        invoked.add("ajaxNoArg()");
    }

    public void ajaxOneArg(AjaxBehaviorEvent event) {
        invoked.add("ajaxOneArg(" + describe(event) + ")");
    }

    public void ajaxBoth() {
        invoked.add("ajaxBoth()");
    }

    public void ajaxBoth(AjaxBehaviorEvent event) {
        invoked.add("ajaxBoth(" + describe(event) + ")");
    }

    public void ajaxComposite(AjaxBehaviorEvent event) {
        invoked.add("ajaxComposite(" + describe(event) + ")");
    }

    public void ajaxCompositeNoArg() {
        invoked.add("ajaxCompositeNoArg()");
    }

    public void actionNoArg() {
        invoked.add("actionNoArg()");
    }

    public void actionOneArg(ActionEvent event) {
        invoked.add("actionOneArg(" + describe(event) + ")");
    }

    public void actionBoth() {
        invoked.add("actionBoth()");
    }

    public void actionBoth(ActionEvent event) {
        invoked.add("actionBoth(" + describe(event) + ")");
    }

    public void valueNoArg() {
        invoked.add("valueNoArg()");
    }

    public void valueOneArg(ValueChangeEvent event) {
        invoked.add("valueOneArg(" + describe(event) + ")");
    }

    public void valueBoth() {
        invoked.add("valueBoth()");
    }

    public void valueBoth(ValueChangeEvent event) {
        invoked.add("valueBoth(" + describe(event) + ")");
    }

    public void noArg() {
        invoked.add("noArg()");
    }

    public void withComponent(UIComponent component) {
        invoked.add("withComponent(" + component.getId() + ")");
    }

    public void withString(String string) {
        invoked.add("withString(" + string + ")");
    }

    public void withItem(Object item) {
        invoked.add("withItem(" + item + ")");
    }

    public void withSystemEvent(SystemEvent event) {
        invoked.add("withSystemEvent(" + describe(event) + ")");
    }

    public void withFacesEvent(FacesEvent event) {
        invoked.add("withFacesEvent(" + describe(event) + ")");
    }

    public void withObject(Object event) {
        invoked.add("withObject(" + describe(event) + ")");
    }

    public void varargs(Object... arguments) {
        invoked.add("varargs(" + arguments.length + ")");
    }

    public String returnsString() {
        invoked.add("returnsString()");
        return "returned";
    }

    public void binding() {
        invoked.add("binding()");
    }

    public List<String> getItems() {
        return List.of("r0", "r1");
    }

    public List<Object> getMixedItems() {
        return List.of(new NoArgItem("A"), new BothItem("B"));
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getInvoked() {
        return String.join(" ", invoked);
    }

    private static String describe(Object event) {
        return event == null ? "null" : "event";
    }

    /**
     * An item whose listener methods take no arguments.
     */
    public class NoArgItem {

        protected final String name;

        NoArgItem(String name) {
            this.name = name;
        }

        public void onEvent() {
            invoked.add(name + ".onEvent()");
        }

        public void onAjax() {
            invoked.add(name + ".onAjax()");
        }

        public void onAction() {
            invoked.add(name + ".onAction()");
        }

        public void onValueChange() {
            invoked.add(name + ".onValueChange()");
        }
    }

    /**
     * An item which has each listener method both taking the event and taking no arguments.
     */
    public class BothItem extends NoArgItem {

        BothItem(String name) {
            super(name);
        }

        public void onEvent(ComponentSystemEvent event) {
            invoked.add(name + ".onEvent(" + describe(event) + ")");
        }

        public void onAjax(AjaxBehaviorEvent event) {
            invoked.add(name + ".onAjax(" + describe(event) + ")");
        }

        public void onAction(ActionEvent event) {
            invoked.add(name + ".onAction(" + describe(event) + ")");
        }

        public void onValueChange(ValueChangeEvent event) {
            invoked.add(name + ".onValueChange(" + describe(event) + ")");
        }
    }
}
