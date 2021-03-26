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

    <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
    <html>
        <head>
            <title>Converters</title>
            <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
            <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
        </head>

        <body>
            <%
                java.util.Locale localeObject = new java.util.Locale("en", "US");
                java.util.TimeZone tzObject =
                    java.util.TimeZone.getTimeZone("America/New_York");
                String localeString = "en";
                String timeZoneString = "America/New_York";

                request.setAttribute("localeObject", localeObject);
                request.setAttribute("timeZoneObject", tzObject);
                request.setAttribute("localeString", localeString);
                request.setAttribute("timeZoneString", timeZoneString);
                request.setAttribute("localeObjectAU", new java.util.Locale("en", "AU"));
                request.setAttribute("timeZoneStringAU", "Australia/Melbourne");

            %>

            <f:view>
                <%--
                    Ensure timeZone and locale attributes can accept:
                       - literal string
                       - VE expression resolving to a String
                       - VE expression resolving to Locale or TimeZone instance
                         in the case of the locate and timeZone attributes (respectively)
                --%>
                <h:outputText id="outputDatetime1"
                              value="7/10/96 12:31:31 PM PDT">
                    <f:convertDateTime type="both" timeStyle="full"
                                       dateStyle="short"
                                       locale="en"
                                       timeZone="America/New_York"/>
                </h:outputText>

                <h:outputText id="outputDatetime2"
                              value="7/10/96 12:31:31 PM PDT">
                    <f:convertDateTime type="both" timeStyle="full"
                                       dateStyle="short"
                                       locale="#{requestScope.localeString}"
                                       timeZone="#{requestScope.timeZoneString}"/>
                </h:outputText>
                <h:outputText id="outputDatetime3"
                              value="7/10/96 12:31:31 PM PDT">
                    <f:convertDateTime type="both" timeStyle="full"
                                       dateStyle="short"
                                       locale="#{requestScope.localeObject}"
                                       timeZone="#{requestScope.timeZoneObject}"/>
                </h:outputText>
                <%--
                     // commented out due to output differences between
                     // JDK6u10 and releases prior to that version.  In the test below,
                     // versions prior to JDK6u10 would always output a two digit
                     // hour (i.e. 05), however, in JDK6u10, it will trim leading
                     // zeros.  This part of the test could be considered redundant
                     // anyway.
                <h:outputText id="outputDatetime4"
                              value="7/10/96 12:31:31 PM PDT">
                    <f:convertDateTime type="both" timeStyle="full"
                                       dateStyle="short"
                                       locale="#{requestScope.localeObjectAU}"
                                       timeZone="#{requestScope.timeZoneStringAU}"/>
                </h:outputText>
                --%>
                <h:outputText id="outputNumber1" value="10000">
                    <f:convertNumber locale="de"/>
                </h:outputText>
                <h:outputText id="outputNumber2" value="10000">
                    <f:convertNumber locale="#{requestScope.localeString}" />
                </h:outputText>
                <h:outputText id="outputNumber3" value="10000">
                    <f:convertNumber locale="#{requestScope.localeObject}" />
                </h:outputText>
            </f:view>
        </body>
    </html>
