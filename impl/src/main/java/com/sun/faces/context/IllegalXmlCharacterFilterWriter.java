/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.faces.context;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

 public class IllegalXmlCharacterFilterWriter extends FilterWriter {

 	/**
	 * Create a new filtered writer.
	 *
	 * @param out a Writer object to provide the underlying stream.
	 * @throws NullPointerException if <code>out</code> is <code>null</code>
	 */
	public IllegalXmlCharacterFilterWriter(Writer out) {
		super(out);
	}

 	@Override
	public void write(int c) throws IOException {
		super.write(xmlEncode((char) c));
	}

 	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		super.write(xmlEncode(cbuf), off, len);
	}

 	@Override
	public void write(String str, int off, int len) throws IOException {
		super.write(xmlEncode(str.toCharArray()), off, len);
	}

 	private char[] xmlEncode(char[] ca) {
		for (int i = 0; i < ca.length; i++) {
			ca[i] = xmlEncode(ca[i]);
		}
		return ca;
	}

 	private char xmlEncode(char c) {
		if (Character.isSurrogate(c)) {
			return ' ';
		}
		if (c == '\u0009' || c == '\n' || c == '\r') {
			return c;
		}
		if (c > '\u0020' && c < '\uD7FF') {
			return c;
		}
		if (c > '\uE000' && c < '\uFFFD') {
			return c;
		}
		return ' ';
	}
}