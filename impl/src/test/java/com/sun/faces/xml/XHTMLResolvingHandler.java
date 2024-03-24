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

package com.sun.faces.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XHTMLResolvingHandler extends DefaultHandler {

    private ResourceBundle bundle;

    public XHTMLResolvingHandler() {
        bundle = ResourceBundle.getBundle(this.getClass().getPackage().getName() + ".Entities",
                Locale.US);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        InputSource is = null;
        int slashslash = systemId.indexOf("//");
        String key = systemId;
        if (-1 != slashslash) {
            key = systemId.substring(slashslash + 2);
        }
        final String value;

        try {
            value = bundle.getString(key);
            is = new InputSource(systemId) {

                @Override
                public InputStream getByteStream() {
                    InputStream inputStream = null;
                    try {
                        inputStream = new ByteArrayInputStream(value.getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                    }
                    return inputStream;
                }

                @Override
                public Reader getCharacterStream() {
                    Reader reader = null;
                    reader = new StringReader(value);
                    return reader;
                }

            };
        } catch (Exception e) {

        }

        return is;
    }
}
