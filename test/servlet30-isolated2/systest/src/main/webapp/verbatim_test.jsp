<%--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

--%>

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>

  <html>

    <head>
      <title>Test of the Verbatim Tag</title>
    </head>

    <body>

      <h1>Test of the Verbatim Tag</h1>

      <p>
        <f:verbatim>
          [DEFAULT]
          This text <b>has angle brackets</b>.
          The angle brackets MUST NOT be escaped.
        </f:verbatim>
      </p>

      <p>
        <f:verbatim escape="false">
          [FALSE]
          This text <b>has angle brackets</b>.
          The angle brackets MUST NOT be escaped.
        </f:verbatim>
      </p>

      <p>
        <f:verbatim escape="true">
          [TRUE]
          This text <b>has angle brackets</b>.
          The angle brackets MUST be escaped.
        </f:verbatim>
      </p>

     <p><f:verbatim rendered="false">This text must not be rendered.
        </f:verbatim>  This text is rendered.
     </p>

    </body>

  </html>

</f:view>

