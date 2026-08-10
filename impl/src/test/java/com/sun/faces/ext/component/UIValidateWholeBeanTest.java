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

package com.sun.faces.ext.component;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIPanel;

import org.junit.jupiter.api.Test;

/**
 * {@code f:validateWholeBean} validates a bean whose properties the inputs before it have already applied, so a
 * rendered input placed after it would not be part of what it validates. It rejects that at render time by walking the
 * form backwards from the last child until it reaches itself.
 */
class UIValidateWholeBeanTest {

    @Test
    void anInputAfterTheValidatorIsRejected() {
        UIForm form = new UIForm();
        UIValidateWholeBean validator = new UIValidateWholeBean();
        form.getChildren().add(new UIInput());
        form.getChildren().add(validator);
        form.getChildren().add(new UIInput());

        assertThrows(IllegalArgumentException.class, () -> validator.encodeBegin(null));
    }

    @Test
    void inputsBeforeTheValidatorAreAccepted() {
        UIForm form = new UIForm();
        UIValidateWholeBean validator = new UIValidateWholeBean();
        form.getChildren().add(new UIInput());
        form.getChildren().add(new UIInput());
        form.getChildren().add(validator);

        assertDoesNotThrow(() -> validator.encodeBegin(null));
    }

    /**
     * The walk recurses into containers, so an input nested behind one still counts as placed after the validator.
     */
    @Test
    void anInputNestedInAContainerAfterTheValidatorIsRejected() {
        UIForm form = new UIForm();
        UIValidateWholeBean validator = new UIValidateWholeBean();
        UIPanel panel = new UIPanel();
        panel.getChildren().add(new UIInput());
        form.getChildren().add(new UIInput());
        form.getChildren().add(validator);
        form.getChildren().add(panel);

        assertThrows(IllegalArgumentException.class, () -> validator.encodeBegin(null));
    }

    /**
     * A container holding exactly one child is the case a reverse walk is most likely to get wrong off by one, and it
     * is a common panelGroup/panelGrid shape, so it gets its own test rather than relying on the nested case above.
     */
    @Test
    void aSingleChildContainerAfterTheValidatorIsStillWalked() {
        UIForm form = new UIForm();
        UIValidateWholeBean validator = new UIValidateWholeBean();
        UIPanel panel = new UIPanel();
        panel.getChildren().add(new UIInput());
        form.getChildren().add(validator);
        form.getChildren().add(panel);

        assertThrows(IllegalArgumentException.class, () -> validator.encodeBegin(null));
    }

    /**
     * The first child of the form is the last one the backwards walk reaches, so it is the one an off-by-one drops.
     */
    @Test
    void anInputAsTheVeryFirstChildIsNotMistakenForBeingAfterTheValidator() {
        UIForm form = new UIForm();
        UIValidateWholeBean validator = new UIValidateWholeBean();
        form.getChildren().add(new UIInput());
        form.getChildren().add(validator);

        assertDoesNotThrow(() -> validator.encodeBegin(null));
    }

    @Test
    void anUnrenderedInputAfterTheValidatorIsIgnored() {
        UIForm form = new UIForm();
        UIValidateWholeBean validator = new UIValidateWholeBean();
        UIComponent hidden = new UIInput();
        hidden.setRendered(false);
        form.getChildren().add(validator);
        form.getChildren().add(hidden);

        assertDoesNotThrow(() -> validator.encodeBegin(null));
    }

    @Test
    void aValidatorOutsideAFormIsRejected() {
        UIValidateWholeBean validator = new UIValidateWholeBean();
        new UIPanel().getChildren().add(validator);

        assertThrows(IllegalArgumentException.class, () -> validator.encodeBegin(null));
    }
}
