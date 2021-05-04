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

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter;
import com.sun.faces.io.FastStringWriter;
import com.sun.faces.util.HtmlUtils;
import com.sun.faces.util.MessageUtils;


/**
 * <p><strong>HtmlResponseWriter</strong> is an Html specific implementation
 * of the <code>ResponseWriter</code> abstract class.
 * Kudos to Adam Winer (Oracle) for much of this code.
 */
public class HtmlResponseWriter extends ResponseWriter {


    // Content Type for this Writer.
    //
    private String contentType = "text/html";

    // Character encoding of that Writer - this may be null
    // if the encoding isn't known.
    //
    private String encoding = null;

    // Writer to use for output;
    //
    private Writer writer = null;

    // True when we need to close a start tag
    //
    private boolean closeStart;

    // Configuration flag regarding disableUnicodeEscaping
    //
    private WebConfiguration.DisableUnicodeEscaping disableUnicodeEscaping;

    //Flag to escape Unicode
    //
    private boolean escapeUnicode;

    // Flag to escape ISO-8859-1 codes
    //
    private boolean escapeIso;

    // True when we shouldn't be escaping output (basically,
    // inside of <script> and <style> elements).   Note
    // that this will *not* be set for CDATA blocks - that's
    // instead the writingCdata flag
    //
    private boolean dontEscape;

    // flag to indicate we're writing a CDATA section
    private boolean writingCdata;

    // flat to indicate the current element is CDATA
    private boolean isCdata;

    // flag to indicate that we're writing a 'script' or 'style' element
    private boolean isScript;

    // flag to indicate that we're writing a 'style' element
    private boolean isStyle;

    // flag to indicate if we are within a script element
    private boolean withinScript;

    // flag to indicate if we are within a style element
    private boolean withinStyle;

    // flag to indicate that we're writing a 'src' attribute as part of
    // 'script' or 'style' element
    private boolean scriptOrStyleSrc;

    // flag to indicate if this is a partial response
    private boolean isPartial;

    // flag to indicate if the content type is XHTML
    private boolean isXhtml;

    // HtmlResponseWriter to use when buffering is required
    private Writer origWriter;

    // Keep one instance of the script buffer per Writer
    private FastStringWriter scriptBuffer;

    // Keep one instance of attributesBuffer to buffer the writing
    // of all attributes for a particular element to reduce the number
    // of writes
    private FastStringWriter attributesBuffer;
    
    // Enables hiding of inlined script and style
    // elements from old browsers
    private Boolean isScriptHidingEnabled;

    // Enables scripts to be included in attribute values
    private Boolean isScriptInAttributeValueEnabled;

    // Internal buffer used when outputting properly escaped information
    // using HtmlUtils class.
    //
    private char[] buffer = new char[1028];

    // Internal buffer used when outputting properly escaped CData information.
    //
    private final static int cdataBufferSize = 1024;
    private char[] cdataBuffer = new char[cdataBufferSize];
    private int cdataBufferLength = 0;
    // Secondary cdata buffer, used for writeText
    private final static int cdataTextBufferSize = 128;
    private char[] cdataTextBuffer = new char[cdataTextBufferSize];
    
    private Map<String, Object> passthroughAttributes;

    // Internal buffer for to store the result of String.getChars() for
    // values passed to the writer as String to reduce the overhead
    // of String.charAt().  This buffer will be grown, if necessary, to
    // accomodate larger values.
    private char[] textBuffer = new char[128];

    private char[] charHolder = new char[1];

    private LinkedList<String> elementNames;

    private static final String BREAKCDATA = "]]><![CDATA[";
    private static final char[] ESCAPEDSINGLEBRACKET = ("]"+BREAKCDATA).toCharArray();
    private static final char[] ESCAPEDLT= ("&lt;"+BREAKCDATA).toCharArray();
    private static final char[] ESCAPEDSTART= ("&lt;"+BREAKCDATA+"![").toCharArray();
    private static final char[] ESCAPEDEND= ("]"+BREAKCDATA+"]>").toCharArray();

    private static final int CLOSEBRACKET = ']';
    private static final int LT = '<';

    static final Pattern CDATA_START_SLASH_SLASH;

    static final Pattern CDATA_END_SLASH_SLASH;

    static final Pattern CDATA_START_SLASH_STAR;

    static final Pattern CDATA_END_SLASH_STAR;

    static {
        // At the beginning of a line, match // followed by any amount of
        // whitespace, followed by <![CDATA[
        CDATA_START_SLASH_SLASH = Pattern.compile("^//\\s*\\Q<![CDATA[\\E");

        // At the end of a line, match // followed by any amout of whitespace,
        // followed by ]]>
        CDATA_END_SLASH_SLASH = Pattern.compile("//\\s*\\Q]]>\\E$");

        // At the beginning of a line, match /* followed by any amout of
        // whitespace, followed by <![CDATA[, followed by any amount of whitespace,
        // followed by */
        CDATA_START_SLASH_STAR = Pattern.compile("^/\\*\\s*\\Q<![CDATA[\\E\\s*\\*/");

        // At the end of a line, match /* followed by any amount of whitespace,
        // followed by ]]> followed by any amount of whitespace, followed by */
        CDATA_END_SLASH_STAR = Pattern.compile("/\\*\\s*\\Q]]>\\E\\s*\\*/$");

    }

    // ------------------------------------------------------------ Constructors


    /**
     * Constructor sets the <code>ResponseWriter</code> and
     * encoding, and enables script hiding by default.
     *
     * @param writer      the <code>ResponseWriter</code>
     * @param contentType the content type.
     * @param encoding    the character encoding.
     *
     * @throws javax.faces.FacesException the encoding is not recognized.
     */
    public HtmlResponseWriter(Writer writer,
                              String contentType,
                              String encoding)
    throws FacesException {
        this(writer, contentType, encoding, null, null, null, false);
    }

    /**
     * <p>Constructor sets the <code>ResponseWriter</code> and
     * encoding.</p>
     *
     * <p>The argument configPrefs is a map of configurable prefs that affect
     * this instance's behavior.  Supported keys are:</p>
     *
     * <p>BooleanWebContextInitParameter.EnableJSStyleHiding: <code>true</code>
     * if the writer should attempt to hide JS from older browsers</p>
     *
     * @param writer      the <code>ResponseWriter</code>
     * @param contentType the content type.
     * @param encoding    the character encoding.
     *
     * @throws javax.faces.FacesException the encoding is not recognized.
     */
    public HtmlResponseWriter(Writer writer,
                              String contentType,
                              String encoding,
                              Boolean isScriptHidingEnabled,
                              Boolean isScriptInAttributeValueEnabled,
                              WebConfiguration.DisableUnicodeEscaping disableUnicodeEscaping,
                              boolean isPartial)
    throws FacesException {

        this.writer = writer;

        if (null != contentType) {
            this.contentType = contentType;
        }

        this.encoding = encoding;

        // init those configuration parameters not yet initialized
        WebConfiguration webConfig = null;
        if (isScriptHidingEnabled == null) {
            webConfig = getWebConfiguration(webConfig);
            isScriptHidingEnabled = (null == webConfig) ? BooleanWebContextInitParameter.EnableJSStyleHiding.getDefaultValue() :
                                webConfig.isOptionEnabled(
                                BooleanWebContextInitParameter.EnableJSStyleHiding);
        }

        if (isScriptInAttributeValueEnabled == null) {
            webConfig = getWebConfiguration(webConfig);
            isScriptInAttributeValueEnabled = (null == webConfig) ? BooleanWebContextInitParameter.EnableScriptInAttributeValue.getDefaultValue() :
                             webConfig.isOptionEnabled(
                             BooleanWebContextInitParameter.EnableScriptInAttributeValue);
        }

        if (disableUnicodeEscaping == null) {
            webConfig = getWebConfiguration(webConfig);
            disableUnicodeEscaping =
                    WebConfiguration.DisableUnicodeEscaping.getByValue(
                        (null == webConfig) ? WebConfiguration.WebContextInitParameter.DisableUnicodeEscaping.getDefaultValue() :
                                webConfig.getOptionValue(
                                 WebConfiguration.WebContextInitParameter.DisableUnicodeEscaping));
            if (disableUnicodeEscaping == null) {
                disableUnicodeEscaping = WebConfiguration.DisableUnicodeEscaping.False;
            }
        }

        // and store them for later use
        this.isPartial = isPartial;
        this.isScriptHidingEnabled = isScriptHidingEnabled;
        this.isScriptInAttributeValueEnabled = isScriptInAttributeValueEnabled;
        this.disableUnicodeEscaping = disableUnicodeEscaping;

        this.attributesBuffer = new FastStringWriter(128);

        // Check the character encoding
        if (!HtmlUtils.validateEncoding(encoding)) {
            throw new IllegalArgumentException(MessageUtils.getExceptionMessageString(
                  MessageUtils.ENCODING_ERROR_MESSAGE_ID));
        }

        String charsetName = encoding.toUpperCase();

        switch (disableUnicodeEscaping)
        {
            case True:
                // html escape noting (except the dangerous characters like "<>'" etc
                escapeUnicode = false;
                escapeIso = false;
                break;
            case False:
                // html escape any non-ascii character
                escapeUnicode = true;
                escapeIso = true;
                break;
            case Auto:
                // is stream capable of rendering unicode, do not escape
                escapeUnicode = !HtmlUtils.isUTFencoding(charsetName);
                // is stream capable of rendering unicode or iso-8859-1, do not escape
                escapeIso = !HtmlUtils.isISO8859_1encoding(charsetName) && !HtmlUtils.isUTFencoding(charsetName);
                break;
        }
    }

    private WebConfiguration getWebConfiguration(WebConfiguration webConfig) {
        if (webConfig != null)
        {
            return webConfig;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        if (null != context) {
            ExternalContext extContext = context.getExternalContext();
            if (null != extContext) {
                webConfig = WebConfiguration.getInstance(extContext);
            }
        }
        return webConfig;
    }

    // -------------------------------------------------- Methods From Closeable


    /** Methods From <code>java.io.Writer</code> */

    @Override
    public void close() throws IOException {

        closeStartIfNecessary();
        writer.close();

    }

    // -------------------------------------------------- Methods From Flushable


    /**
     * Flush any buffered output to the contained writer.
     *
     * @throws IOException if an input/output error occurs.
     */
    @Override
    public void flush() throws IOException {

        // NOTE: Internal buffer's contents (the ivar "buffer") is
        // written to the contained writer in the HtmlUtils class - see
        // HtmlUtils.flushBuffer method; Buffering is done during
        // writeAttribute/writeText - otherwise, output is written
        // directly to the writer (ex: writer.write(....)..
        //
        // close any previously started element, if necessary
        closeStartIfNecessary();

    }

    // ---------------------------------------------------------- Public Methods


    /** @return the content type such as "text/html" for this ResponseWriter. */
    @Override
    public String getContentType() {

        return contentType;

    }


    /**
     * <p>Create a new instance of this <code>ResponseWriter</code> using
     * a different <code>Writer</code>.
     *
     * @param writer The <code>Writer</code> that will be used to create
     *               another <code>ResponseWriter</code>.
     */
    @Override
    public ResponseWriter cloneWithWriter(Writer writer) {

        try {
            HtmlResponseWriter responseWriter =  new HtmlResponseWriter(writer,
                                          getContentType(),
                                          getCharacterEncoding(),
                                          isScriptHidingEnabled,
                                          isScriptInAttributeValueEnabled,
                                          disableUnicodeEscaping,
                                          isPartial);
            responseWriter.dontEscape = this.dontEscape;
            responseWriter.writingCdata = this.writingCdata;
            return responseWriter;
            
        } catch (FacesException e) {
            // This should never happen
            throw new IllegalStateException();
        }

    }


    /** Output the text for the end of a document. */
    @Override
    public void endDocument() throws IOException {

        /*
         * If the FastStringWriter is kept because of an error in <script>
         * writing we get it here and write out the result. See issue #3473
         */
        if (writer instanceof FastStringWriter) {
            FastStringWriter fastStringWriter = (FastStringWriter) writer;
            String result = fastStringWriter.getBuffer().toString();
            fastStringWriter.reset();
            writer = origWriter;
            writer.write(result);
        }
        
        writer.flush();

    }


    /**
     * <p>Write the end of an element. This method will first
     * close any open element created by a call to
     * <code>startElement()</code>.
     *
     * @param name Name of the element to be ended
     *
     * @throws IOException          if an input/output error occurs
     * @throws NullPointerException if <code>name</code>
     *                              is <code>null</code>
     */
    @Override
    public void endElement(String name) throws IOException {

        if (name == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(
                  MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "name"));
        }

        // Keep track when we are exiting a script or style element
        // for escaping purposes.

        if ("script".equalsIgnoreCase(name)) {
            withinScript = false;
        }

        if ("style".equalsIgnoreCase(name)) {
            withinStyle = false;
        }

        // always turn escaping back on once an element ends
        if (!withinScript && !withinStyle) {
            dontEscape = false;
        }

        isXhtml = getContentType().equals(
            RIConstants.XHTML_CONTENT_TYPE);

        if (isScriptOrStyle(name)
             && !scriptOrStyleSrc
             && writer instanceof FastStringWriter) {
            String result = ((FastStringWriter) writer).getBuffer().toString();
            writer = origWriter;

            if (result != null) {
                String trim = result.trim();
                if (isXhtml) {
                    if (isScript) {
                        Matcher
                            cdataStartSlashSlash =
                              CDATA_START_SLASH_SLASH.matcher(trim),
                            cdataEndSlashSlash =
                              CDATA_END_SLASH_SLASH.matcher(trim),
                            cdataStartSlashStar =
                              CDATA_START_SLASH_STAR.matcher(trim),
                            cdataEndSlashStar =
                              CDATA_END_SLASH_STAR.matcher(trim);
                        int trimLen = trim.length(), start, end;
                        // case 1 start is // end is //
                        if (cdataStartSlashSlash.find() &&
                            cdataEndSlashSlash.find()) {
                            start = cdataStartSlashSlash.end() - cdataStartSlashSlash.start();
                            end = trimLen - (cdataEndSlashSlash.end() - cdataEndSlashSlash.start());
                            writer.write(trim.substring(start, end));
                        }
                        // case 2 start is // end is /* */
                        else if ((null != cdataStartSlashSlash.reset() && cdataStartSlashSlash.find()) &&
                                 cdataEndSlashStar.find()) {
                            start = cdataStartSlashSlash.end() - cdataStartSlashSlash.start();
                            end = trimLen - (cdataEndSlashStar.end() - cdataEndSlashStar.start());
                            writer.write(trim.substring(start, end));
                        }
                        // case 3 start is /* */ end is /* */
                        else if (cdataStartSlashStar.find() &&
                                 (null != cdataEndSlashStar.reset() && cdataEndSlashStar.find())) {
                            start = cdataStartSlashStar.end() - cdataStartSlashStar.start();
                            end = trimLen - (cdataEndSlashStar.end() - cdataEndSlashStar.start());
                            writer.write(trim.substring(start, end));
                        }
                        // case 4 start is /* */ end is //
                        else if ((null != cdataStartSlashStar.reset() && cdataStartSlashStar.find()) &&
                                 (null != cdataEndSlashStar.reset() && cdataEndSlashSlash.find())) {
                            start = cdataStartSlashStar.end() - cdataStartSlashStar.start();
                            end = trimLen - (cdataEndSlashSlash.end() - cdataEndSlashSlash.start());
                            writer.write(trim.substring(start, end));
                        }
                        // case 5 no commented out cdata present.
                        else {
                            writer.write(result);
                        }
                    } else {
                        if (trim.startsWith("<![CDATA[") && trim.endsWith("]]>")) {
                            writer.write(trim.substring(9, trim.length() - 3));
                        } else {
                            writer.write(result);
                        }
                    }
                } else {
                    if (trim.startsWith("<!--") && trim.endsWith("//-->")) {
                        writer.write(trim.substring(4, trim.length() - 5));
                    } else {
                        writer.write(result);
                    }
                }
            }
            if (isXhtml) {
                if (!writingCdata) {
                    if (isScript) {
                        writer.write("\n//]]>\n");
                    } else {
                        writer.write("\n]]>\n");
                    }
                }
            } else {
                if (isScriptHidingEnabled) {
                    writer.write("\n//-->\n");
                }
            }
        }

        if (!withinScript || isScript) {
            isScript = false;
        } else if (!withinStyle || isStyle) {
            isStyle = false;
        }
        if (!withinScript && !withinScript) {
            dontEscape = false;
        }
        
        if ("cdata".equalsIgnoreCase(name)) {
            endCDATA();
            return;
        }
        // See if we need to close the start of the last element
        if (closeStart) {
            boolean isEmptyElement = HtmlUtils.isEmptyElement(name);

            // Tricky: we need to use the writer ivar here, rather than the
            // one from the FacesContext because we don't want
            // spurious /> characters to appear in the output.
            if (isEmptyElement) {
                flushAttributes();
                writer.write(" />");
                closeStart = false;
                popElementName(name);
                return;
            }
            flushAttributes();
            writer.write('>');
            closeStart = false;
        }

        writer.write("</");
        writer.write(popElementName(name));
        writer.write('>');

    }


    /**
     * @return the character encoding, such as "ISO-8859-1" for this
     *         ResponseWriter.  Refer to:
     *         <a href="http://www.iana.org/assignments/character-sets">theIANA</a>
     *         for a list of character encodings.
     */
    @Override
    public String getCharacterEncoding() {

        return encoding;

    }


    /**
     * <p>Write the text that should begin a response.</p>
     *
     * @throws IOException if an input/output error occurs
     */
    @Override
    public void startDocument() throws IOException {

        // do nothing;

    }


    /**
     * <p>Write the start of an element, up to and including the
     * element name.  Clients call <code>writeAttribute()</code> or
     * <code>writeURIAttribute()</code> methods to add attributes after
     * calling this method.
     *
     * @param name                Name of the starting element
     * @param componentForElement The UIComponent instance that applies to this
     *                            element.  This argument may be <code>null</code>.
     *
     * @throws IOException          if an input/output error occurs
     * @throws NullPointerException if <code>name</code>
     *                              is <code>null</code>
     */
    @Override
    public void startElement(String name, UIComponent componentForElement)
          throws IOException {

        if (name == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(
                  MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "name"));
        }

        // Keep track if we are in either a script or style element so we
        // know we do not want to escape.

        if ("script".equalsIgnoreCase(name)) {
            withinScript = true;
        }

        if ("style".equalsIgnoreCase(name)) {
            withinStyle = true;
        }

        closeStartIfNecessary();
        isScriptOrStyle(name);
        scriptOrStyleSrc = false;
        if ("cdata".equalsIgnoreCase(name)) {
            isCdata = true;
            startCDATA();
            return;
        } else if (writingCdata) {
            // starting an element within a cdata section,
            // keep escaping disabled
            isCdata = false;
            writingCdata = true;
        }

        if (null != componentForElement) {
            Map<String, Object> passThroughAttrs = componentForElement.getPassThroughAttributes(false);
            if (null != passThroughAttrs && !passThroughAttrs.isEmpty()) {
                considerPassThroughAttributes(passThroughAttrs);
            }
        }

        writer.write('<');
        String elementName = pushElementName(name);
        writer.write(elementName);
        
        closeStart = true;

    }


    /**
     * Starts a CDATA block.  Nested blocks are not allowed.
     *
     * @since 2.0
     * @throws IOException on a read/write error
     * @throws IllegalStateException If startCDATA is called a second time before endCDATA.
     */
    // RELEASE_PENDING_2_1 edburns, rogerk - need to expand on this description.
    @Override
    public void startCDATA() throws IOException {
        if (writingCdata) {
            throw new IllegalStateException("CDATA tags may not nest");
        }
        closeStartIfNecessary();        
        writingCdata = true;
        writer.write("<![CDATA[");
        closeStart = false;
    }

    /**
     * Closes the CDATA block.
     *
     * @since 2.0
     * @throws IOException
     */
    // RELEASE_PENDING_2_1 edburns, rogerk - need to expand on this description.
    @Override
    public void endCDATA() throws IOException {
        closeStartIfNecessary();
        writer.write("]]>");
        writingCdata = false;
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        closeStartIfNecessary();
        writer.write(cbuf);
    }

    @Override
    public void write(int c) throws IOException {
        closeStartIfNecessary();
        writer.write(c);
    }

    @Override
    public void write(String str) throws IOException {
        closeStartIfNecessary();
        writer.write(str);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        closeStartIfNecessary();
        writer.write(cbuf, off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        closeStartIfNecessary();
        writer.write(str, off, len);
    }


    /**
     * <p>Write a properly escaped attribute name and the corresponding
     * value.  The value text will be converted to a String if
     * necessary.  This method may only be called after a call to
     * <code>startElement()</code>, and before the opened element has been
     * closed.</p>
     *
     * @param name                  Attribute name to be added
     * @param value                 Attribute value to be added
     * @param componentPropertyName The name of the component property to
     *                              which this attribute argument applies.  This argument may be
     *                              <code>null</code>.
     *
     * @throws IllegalStateException if this method is called when there
     *                               is no currently open element
     * @throws IOException           if an input/output error occurs
     * @throws NullPointerException  if <code>name</code> is <code>null</code>
     */
    @Override
    public void writeAttribute(String name, Object value,
                               String componentPropertyName)
          throws IOException {

        if (name == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(
                  MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "name"));
        }
        if (value == null) {
            return;
        }

        if (isCdata) {
            return;
        }
        
        if (containsPassThroughAttribute(name)) {
            return;
        }

        if (name.equalsIgnoreCase("src") && isScriptOrStyle()) {
            scriptOrStyleSrc = true;
        }

        Class valueClass = value.getClass();

        // Output Boolean values specially
        if (valueClass == Boolean.class) {
            if (Boolean.TRUE.equals(value)) {
                // NOTE:  HTML 4.01 states that boolean attributes
                //        may legally take a single value which is the
                //        name of the attribute itself or appear using
                //        minimization.
                //  http://www.w3.org/TR/html401/intro/sgmltut.html#h-3.3.4.2
                attributesBuffer.write(' ');
                attributesBuffer.write(name);
                attributesBuffer.write("=\"");
                attributesBuffer.write(name);
                attributesBuffer.write('"');
            }
        } else {
            attributesBuffer.write(' ');
            attributesBuffer.write(name);
            attributesBuffer.write("=\"");
            // write the attribute value
            String val = value.toString();
            ensureTextBufferCapacity(val);
            HtmlUtils.writeAttribute(attributesBuffer,
                                     escapeUnicode,
                                     escapeIso,
                                     buffer,
                                     val,
                                     textBuffer,
                                     isScriptInAttributeValueEnabled);
            attributesBuffer.write('"');
        }

    }


    /**
     * <p>Write a comment string containing the specified text.
     * The text will be converted to a String if necessary.
     * If there is an open element that has been created by a call
     * to <code>startElement()</code>, that element will be closed
     * first.</p>
     *
     * @param comment Text content of the comment
     *
     * @throws IOException          if an input/output error occurs
     * @throws NullPointerException if <code>comment</code>
     *                              is <code>null</code>
     */
    @Override
    public void writeComment(Object comment) throws IOException {

        if (comment == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(
                  MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID));
        }

        if (writingCdata) {
            return;
        }

        closeStartIfNecessary();
        // Don't include a trailing space after the '<!--'
        // or a leading space before the '-->' to support
        // IE conditional commentsoth
        writer.write("<!--");
        String str = comment.toString();
        ensureTextBufferCapacity(str);
        HtmlUtils.writeText(writer, true, true, buffer, str, textBuffer);
        writer.write("-->");

    }


    /**
     * <p>Write a properly escaped single character, If there
     * is an open element that has been created by a call to
     * <code>startElement()</code>, that element will be closed first.</p>
     * <p/>
     * <p>All angle bracket occurrences in the argument must be escaped
     * using the &amp;gt; &amp;lt; syntax.</p>
     *
     * @param text Text to be written
     *
     * @throws IOException if an input/output error occurs
     */
    public void writeText(char text) throws IOException {

        closeStartIfNecessary();
        if (dontEscape) {
            if (writingCdata) {
                charHolder[0] = text;
                writeUnescapedCData(charHolder, 0, 1);
            } else {
                writer.write(text);
            }
        } else if (isPartial || !writingCdata) {
            charHolder[0] = text;
            HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, charHolder);
        } else {  // if writingCdata
            assert writingCdata;
            charHolder[0] = text;
            writeEscaped(charHolder, 0, 1);
        }

    }


    /**
     * <p>Write properly escaped text from a character array.
     * The output from this command is identical to the invocation:
     * <code>writeText(c, 0, c.length)</code>.
     * If there is an open element that has been created by a call to
     * <code>startElement()</code>, that element will be closed first.</p>
     * </p>
     * <p/>
     * <p>All angle bracket occurrences in the argument must be escaped
     * using the &amp;gt; &amp;lt; syntax.</p>
     *
     * @param text Text to be written
     *
     * @throws IOException          if an input/output error occurs
     * @throws NullPointerException if <code>text</code>
     *                              is <code>null</code>
     */
    public void writeText(char text[]) throws IOException {

        if (text == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(
                  MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "text"));
        }
        closeStartIfNecessary();

        if (dontEscape) {
            if (writingCdata) {
                writeUnescapedCData(text, 0, text.length);
            } else {
                writer.write(text);
            }
        } else if (isPartial || !writingCdata) {
            HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, text);
        } else { // if writingCdata
            assert writingCdata;
            writeEscaped(text, 0, text.length);
        }

    }


    /**
     * <p>Write a properly escaped object. The object will be converted
     * to a String if necessary.  If there is an open element
     * that has been created by a call to <code>startElement()</code>,
     * that element will be closed first.</p>
     *
     * @param text                  Text to be written
     * @param componentPropertyName The name of the component property to
     *                              which this text argument applies.  This argument may be <code>null</code>.
     *
     * @throws IOException          if an input/output error occurs
     * @throws NullPointerException if <code>text</code>
     *                              is <code>null</code>
     */
    @Override
    public void writeText(Object text, String componentPropertyName)
          throws IOException {

        if (text == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(
                  MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "text"));
        }
        closeStartIfNecessary();
        String textStr = text.toString();

        if (dontEscape) {
            if (writingCdata && !textStr.isEmpty()) {
                writeUnescapedCData(textStr.toCharArray(), 0, textStr.length());
            } else {
                writer.write(textStr);
            }
        } else if (isPartial || !writingCdata) {
            ensureTextBufferCapacity(textStr);
            HtmlUtils.writeText(writer,
                                escapeUnicode,
                                escapeIso,
                                buffer,
                                textStr,
                                textBuffer);
        } else { // if writingCdata
            assert writingCdata;
            int textLen = textStr.length();
            if (textLen > cdataTextBufferSize) {
                writeEscaped(textStr.toCharArray(), 0, textLen);
            } else if (textLen >= 16) { // >16, < cdataTextBufferSize
                textStr.getChars(0, textLen, cdataTextBuffer, 0);
                writeEscaped(cdataTextBuffer, 0, textLen);
            } else { // <16
                for (int i=0; i < textLen;  i++)  {
                    cdataTextBuffer[i] = textStr.charAt(i);
                }
                writeEscaped(cdataTextBuffer, 0, textLen);
            }
        }
    }


    /**
     * <p>Write properly escaped text from a character array.
     * If there is an open element that has been created by a call
     * to <code>startElement()</code>, that element will be closed
     * first.</p>
     * <p/>
     * <p>All angle bracket occurrences in the argument must be escaped
     * using the &amp;gt; &amp;lt; syntax.</p>
     *
     * @param text Text to be written
     * @param off  Starting offset (zero-relative)
     * @param len  Number of characters to be written
     *
     * @throws IndexOutOfBoundsException if the calculated starting or
     *                                   ending position is outside the bounds of the character array
     * @throws IOException               if an input/output error occurs
     * @throws NullPointerException      if <code>text</code>
     *                                   is <code>null</code>
     */
    @Override
    public void writeText(char text[], int off, int len)
          throws IOException {

        if (text == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(
                  MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "text"));
        }
        if (off < 0 || off > text.length || len < 0 || len > text.length) {
            throw new IndexOutOfBoundsException();
        }
        closeStartIfNecessary();

        // optimize away zero length write, called by Facelets to close tags
        if (len == 0) return;

        if (dontEscape) {
            if (writingCdata) {
                writeUnescapedCData(text, off, len);
            } else {
                writer.write(text, off, len);
            }
        } else if (isPartial || !writingCdata) {
            HtmlUtils.writeText(writer, escapeUnicode, escapeIso, buffer, text, off, len);
        } else { // if (writingCdata)
            assert writingCdata;
            writeEscaped(text, off, len);
        }

    }


    /**
     * <p>Write a properly encoded URI attribute name and the corresponding
     * value. The value text will be converted to a String if necessary).
     * This method may only be called after a call to
     * <code>startElement()</code>, and before the opened element has been
     * closed.</p>
     *
     * @param name                  Attribute name to be added
     * @param value                 Attribute value to be added
     * @param componentPropertyName The name of the component property to
     *                              which this attribute argument applies.  This argument may be
     *                              <code>null</code>.
     *
     * @throws IllegalStateException if this method is called when there
     *                               is no currently open element
     * @throws IOException           if an input/output error occurs
     * @throws NullPointerException  if <code>name</code> or
     *                               <code>value</code> is <code>null</code>
     */
    @Override
    public void writeURIAttribute(String name, Object value,
                                  String componentPropertyName)
          throws IOException {
        if (null != name && containsPassThroughAttribute(name)) {
            return;
        }
        writeURIAttributeIgnoringPassThroughAttributes(name, value, 
                componentPropertyName, false);

    }
    
    private void writeURIAttributeIgnoringPassThroughAttributes(String name, Object value,
            String componentPropertyName, boolean isPassthrough) throws IOException {
        
        if (name == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(
                  MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "name"));
        }
        if (value == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(
                  MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "value"));
        }

        if (isCdata) {
            return;
        }
        
        if (name.equals(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY)) {
            return;
        }

        if (name.equalsIgnoreCase("src") && isScriptOrStyle()) {
            scriptOrStyleSrc = true;
        }

        attributesBuffer.write(' ');
        attributesBuffer.write(name);
        attributesBuffer.write("=\"");

        String stringValue = value.toString();
        ensureTextBufferCapacity(stringValue);
        // Javascript URLs should not be URL-encoded
        if (stringValue.startsWith("javascript:") || isPassthrough) {
            HtmlUtils.writeAttribute(attributesBuffer,
                                     escapeUnicode,
                                     escapeIso,
                                     buffer,
                                     stringValue,
                                     textBuffer,
                                     isScriptInAttributeValueEnabled);
        } else {
            HtmlUtils.writeURL(attributesBuffer,
                               stringValue,
                               textBuffer,
                               encoding);
        }

        attributesBuffer.write('"');

    }

    // --------------------------------------------------------- Private Methods

    private void ensureTextBufferCapacity(String source) {
        int len = source.length();
        if (textBuffer.length < len) {
            textBuffer = new char[len * 2];
        }
        if (buffer.length < len) {
            buffer = new char[len * 2];
        }
    }

    /**
     * This method automatically closes a previous element (if not
     * already closed).
     * @throws IOException if an error occurs writing
     */
    private void closeStartIfNecessary() throws IOException {

        if (closeStart) {
            flushAttributes();
            writer.write('>');
            closeStart = false;
            if (isScriptOrStyle() && !scriptOrStyleSrc) {
                isXhtml = getContentType().equals(
                     RIConstants.XHTML_CONTENT_TYPE);
                if (isXhtml) {
                    if (!writingCdata) {
                        if (isScript) {
                            writer.write("\n//<![CDATA[\n");
                        } else {
                            writer.write("\n<![CDATA[\n");
                        }
                    }
                } else {
                    if (isScriptHidingEnabled) {
                        writer.write("\n<!--\n");
                    }
                }
                origWriter = writer;
                if (scriptBuffer == null) {
                    scriptBuffer = new FastStringWriter(1024);
                }
                scriptBuffer.reset();
                writer = scriptBuffer;
                isScript = false;
                isStyle = false;
            }
        }

    }
    
    private void considerPassThroughAttributes(Map<String, Object> toCopy) {
        assert(null != toCopy && !toCopy.isEmpty());
        
        if (null != passthroughAttributes) {
            throw new IllegalStateException("Error, this method should only be called once per instance.");
        }
        passthroughAttributes = new ConcurrentHashMap<>(toCopy);
    }
    
    private boolean containsPassThroughAttribute(String attrName) {
        boolean result = false;
        if (null != passthroughAttributes) {
            result = passthroughAttributes.containsKey(attrName);
        }
        return result;
    }


    private void flushAttributes() throws IOException {
        boolean hasPassthroughAttributes = 
                null != passthroughAttributes && !passthroughAttributes.isEmpty();
        
        if (hasPassthroughAttributes) {
            FacesContext context = FacesContext.getCurrentInstance();
            for (Map.Entry<String, Object> entry : passthroughAttributes.entrySet()) {
                Object valObj = entry.getValue();
                String val = getAttributeValue(context, valObj);
                String key = entry.getKey();
                if (val != null) {
                    writeURIAttributeIgnoringPassThroughAttributes(key, val, key, true);
                }
            }
        }


        // a little complex, but the end result is, potentially, two
        // fewer temp objects created per call.
        StringBuilder b = attributesBuffer.getBuffer();
        int totalLength = b.length();
        if (totalLength != 0) {
            int curIdx = 0;
            while (curIdx < totalLength) {
                if ((totalLength - curIdx) > buffer.length) {
                    int end = curIdx + buffer.length;
                    b.getChars(curIdx, end, buffer, 0);
                    writer.write(buffer);
                    curIdx += buffer.length;
                } else {
                    int len = totalLength - curIdx;
                    b.getChars(curIdx, curIdx + len, buffer, 0);
                    writer.write(buffer, 0, len);
                    curIdx += len;
                }
            }
            attributesBuffer.reset();
        }
        
        if (hasPassthroughAttributes) {
            passthroughAttributes.clear();
            passthroughAttributes = null;
        }
        
    }

    private String getAttributeValue(FacesContext context, Object valObj) {
        String val;
        if (valObj instanceof ValueExpression) {
            Object result = ((ValueExpression) valObj).getValue(context.getELContext());
            val = result != null ? result.toString() : null;
        } else {
            val = valObj.toString();
        }
        return val;
    }

    private String pushElementName(String original) {
        
        if (original.equals("option")) {
            if(elementNames == null) {
                elementNames = new LinkedList<>();
            }
            elementNames.push(original);
            return original;
        }
        
        String name = getElementName(original);

        if(passthroughAttributes != null) {
            passthroughAttributes.remove(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY);
            if(passthroughAttributes.isEmpty()) {
                passthroughAttributes = null;
            }
        }

        if(!original.equals(name) || elementNames != null) {
            if(elementNames == null) {
                elementNames = new LinkedList<>();
            }
            elementNames.push(name);
        }
        return name;
    }

    private String popElementName(String original) {
        if(elementNames == null || elementNames.isEmpty()) {
            return original;
        }
        return elementNames.pop();
    }

    private String getElementName(String name) {
        if(containsPassThroughAttribute(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY)) {
            FacesContext context = FacesContext.getCurrentInstance();

            String elementName = getAttributeValue(context, passthroughAttributes.get(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY));
            if(elementName != null && elementName.trim().length() > 0) {
                return elementName;
            }
        }
        return name;
    }

    private boolean isScriptOrStyle(String name) {
        if ("script".equalsIgnoreCase(name)) {
            isScript = true;
            dontEscape = true;
        } else if ("style".equalsIgnoreCase(name)) {
            isStyle = true;
            dontEscape = true;
        } else {
            isScript = false;
            isStyle = false;
            if (!withinScript && !withinStyle) {
                dontEscape = false;
            }
        }

        return (isScript || isStyle);
    }

    private boolean isScriptOrStyle() {
        return (isScript || isStyle);
    }

/*
 *  Method to escape all CDATA instances in a character array, then write to writer.
 *
 * This method looks for occurrences of "<![" and "]]>"
 */
private void writeEscaped(char cbuf[], int offset, int length) throws IOException {
    if (cbuf == null || cbuf.length == 0 || length == 0) {
        return;
    }

   if (offset < 0 || length < 0 || offset + length > cbuf.length ) {
        throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > cbuf.length");
   }

    // Single char case
    if (length == 1) {
        if (cbuf[offset] == '<') {
            appendBuffer(ESCAPEDLT);
        } else if (cbuf[offset] == ']') {
            appendBuffer(ESCAPEDSINGLEBRACKET);
        } else {
            appendBuffer(cbuf[offset]);
        }
        flushBuffer();
        return;
    }
    // two char case
    if (length == 2) {
        if (cbuf[offset] == '<' && cbuf[offset + 1] == '!') {
            appendBuffer(ESCAPEDLT);
            appendBuffer(cbuf[offset + 1]);
        } else if (cbuf[offset] == ']' && cbuf[offset + 1] == ']') {
            appendBuffer(ESCAPEDSINGLEBRACKET);
            appendBuffer(ESCAPEDSINGLEBRACKET);
        } else {
            appendBuffer(cbuf[offset]);
            appendBuffer(cbuf[offset + 1]);
        }
        flushBuffer();
        return;
    }
    // > 2 char case
    boolean last = false;
    for (int i = offset; i < length - 2; i++) {
        if (cbuf[i] == '<' && cbuf[i + 1] == '!' && cbuf[i + 2] == '[') {
            appendBuffer(ESCAPEDSTART);
            i += 2;
        } else if (cbuf[i] == ']' && cbuf[i + 1] == ']' && cbuf[i + 2] == '>') {
            appendBuffer(ESCAPEDEND);
            i += 2;
        } else {
            appendBuffer(cbuf[i]);
        }
        if (i == (offset + length - 1)) {
            last = true;
        }
    }
    // if we didn't look at the last characters, look at them now
    if (!last) {
        if (cbuf[offset + length - 2] == '<') {
            appendBuffer(ESCAPEDLT);
        } else if (cbuf[offset + length - 2] == ']') {
            appendBuffer(ESCAPEDSINGLEBRACKET);
        } else {
            appendBuffer(cbuf[offset + length - 2]);
        }
        if (cbuf[offset + length - 1] == '<') {
            appendBuffer(ESCAPEDLT);
        } else if (cbuf[offset + length - 1] == ']') {
            appendBuffer(ESCAPEDSINGLEBRACKET);
        } else {
            appendBuffer(cbuf[offset + length - 1]);
        }
    }
    flushBuffer();
}

/*
 *  append a character array to the cdatabuffer
 */
private void appendBuffer(char[] cbuf) throws IOException {
    if (cbuf.length + cdataBufferLength >= cdataBufferSize) {
        flushBuffer();
    }
    if (cbuf.length >= cdataBufferSize) {  // bigger than the buffer, direct write
        writer.write(cbuf);
    }
    System.arraycopy(cbuf, 0, cdataBuffer, cdataBufferLength, cbuf.length);
    cdataBufferLength = cdataBufferLength + cbuf.length;
}
/*
 * append a character to the cdatabuffer
 */
private void appendBuffer(char c) throws IOException {
    if (cdataBufferLength + 1 >= cdataBufferSize) {
        flushBuffer();
    }
    cdataBuffer[cdataBufferLength] = c;
    cdataBufferLength++;
}

/*
 * flush the cdatabuffer to the writer
 */
private void flushBuffer() throws IOException {
    if (cdataBufferLength == 0) {
        return;
    }
    writer.write(cdataBuffer, 0, cdataBufferLength);
    cdataBufferLength = 0;
}

    /**
     * When writing un-escaped CDATA, "]]>" sequence still has to be escaped by breaking CDATA block.
     */
    private void writeUnescapedCData(char[] cbuf, int offset, int length) throws IOException {
        // zero char case
        if (length == 0) {
            return;
        }
        
        // single char case
        if (length == 1) {
            if (cbuf[offset] == ']') {
                appendBuffer(ESCAPEDSINGLEBRACKET);
            } else {
                appendBuffer(cbuf[offset]);
            }
            flushBuffer();
            return;
        }

        // two char case
        if (length == 2) {
            if (cbuf[offset] == ']' && cbuf[offset + 1] == ']') {
                appendBuffer(ESCAPEDSINGLEBRACKET);
                appendBuffer(ESCAPEDSINGLEBRACKET);
            } else {
                appendBuffer(cbuf[offset]);
                appendBuffer(cbuf[offset + 1]);
            }
            flushBuffer();
            return;
        }

        // > 2 char case
        boolean last = false;
        for (int i = offset; i < length - 2; i++) {
            if (cbuf[i] == ']' && cbuf[i + 1] == ']' && cbuf[i + 2] == '>') {
                appendBuffer(ESCAPEDEND);
                i += 2;
            } else {
                appendBuffer(cbuf[i]);
            }
            if (i == (offset + length - 1)) {
                last = true;
            }
        }
        // if we didn't look at the last characters, look at them now
        if (!last) {
            if (cbuf[offset + length - 2] == ']') {
                appendBuffer(ESCAPEDSINGLEBRACKET);
            } else {
                appendBuffer(cbuf[offset + length - 2]);
            }
            if (cbuf[offset + length - 1] == ']') {
                appendBuffer(ESCAPEDSINGLEBRACKET);
            } else {
                appendBuffer(cbuf[offset + length - 1]);
            }
        }
        flushBuffer();
    }
}
