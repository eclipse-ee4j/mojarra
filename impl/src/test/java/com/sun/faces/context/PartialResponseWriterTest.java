package com.sun.faces.context;

 import com.sun.faces.renderkit.html_basic.HtmlResponseWriter;
import org.junit.Before;
import org.junit.Test;

import javax.faces.context.PartialResponseWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

 public class PartialResponseWriterTest {

 	private PartialResponseWriter partialResponseWriter;
	private StringWriter collector;

 	/**
	 * initialize like in {@link PartialViewContextImpl#createPartialResponseWriter()}
	 */
	@Before
	public void setUp() {
		collector = new StringWriter(100);
		HtmlResponseWriter htmlResponseWriter = new HtmlResponseWriter(collector, "text/xml", StandardCharsets.UTF_8.name());
		partialResponseWriter = new PartialResponseWriter(htmlResponseWriter.cloneWithWriter(new IllegalXmlCharacterFilterWriter(htmlResponseWriter)));
	}


 	@Test
	public void testWriteIllegalXmlUnicodeCharacters() {
		try {
			String illegalChars = " \u0001\u0002\u0003\u0004\u0005\u0006\u000B\f\u000E\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F \uD7FF\uDBFF\uDC00\uE000��";
			String legalChars = "foo";
			partialResponseWriter.write(illegalChars + legalChars);
			assertEquals("All illegal XML unicode characters should have been replaced by spaces", legalChars, collector.toString().trim());

 		} catch (IOException e) {
			fail(e.toString());
		}
	}

 	@Test
	public void testWriteTextIllegalXmlUnicodeCharacters() {
		try {
			String illegalChars = " \u0001\u0002\u0003\u0004\u0005\u0006\u000B\f\u000E\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F \uD7FF\uDBFF\uDC00\uE000��";
			String legalChars = "foobar";
			partialResponseWriter.writeText(illegalChars + legalChars, null);
			assertEquals("All illegal XML unicode characters should have been replaced by spaces", legalChars, collector.toString().trim());
		} catch (IOException e) {
			fail(e.toString());
		}
	}
}