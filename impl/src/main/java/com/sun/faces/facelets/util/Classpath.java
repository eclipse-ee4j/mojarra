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

package com.sun.faces.facelets.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * @author Jacob Hookom
 * @author Roland Huss
 * @author Ales Justin (ales.justin@jboss.org)
 */
public final class Classpath {

    // discard any urls that begin with rar: and sar:
    // or end with their counterparts
    // as these should not be looked at for Faces related content.
    private static final String[] PREFIXES_TO_EXCLUDE = { "rar:", "sar:" };
    private static final String[] EXTENSIONS_TO_EXCLUDE = { ".rar", ".sar" };

    /**
     *
     */
    public Classpath() {
        super();
    }

    public enum SearchAdvice {
        FirstMatchOnly, AllMatches
    }

    public static URL[] search(String prefix, String suffix) throws IOException {
        return search(Thread.currentThread().getContextClassLoader(), prefix, suffix, SearchAdvice.AllMatches);
    }

    public static URL[] search(ClassLoader cl, String prefix, String suffix) throws IOException {
        return search(cl, prefix, suffix, SearchAdvice.AllMatches);
    }

    public static URL[] search(ClassLoader cl, String prefix, String suffix, SearchAdvice advice) throws IOException {
        Enumeration<URL>[] e = new Enumeration[] { cl.getResources(prefix), cl.getResources(prefix + "MANIFEST.MF") };
        Set<URL> all = new LinkedHashSet<>();
        URL url;
        URLConnection conn;
        JarFile jarFile;
        for (Enumeration<URL> enumeration : e) {
            while (enumeration.hasMoreElements()) {
                url = enumeration.nextElement();
                // Defensive programming. Due to issue 13045 this collection
                // can contain URLs that have their spaces incorrectly escaped
                // by having %20 replaced with %2520. This quick conditional
                // check catches this particular case and averts it.
                String str = url.getPath();
                if (str.contains("%2520")) {
                    str = url.toExternalForm();
                    str = str.replace("%2520", "%20");
                    url = new URL(str);
                }
                conn = url.openConnection();
                conn.setUseCaches(false);
                if (conn instanceof JarURLConnection) {
                    jarFile = ((JarURLConnection) conn).getJarFile();
                } else {
                    jarFile = getAlternativeJarFile(url);
                }
                if (jarFile != null) {
                    searchJar(cl, all, jarFile, prefix, suffix, advice);
                } else {
                    boolean searchDone = searchDir(all, new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8)), suffix);
                    if (!searchDone) {
                        searchFromURL(all, prefix, suffix, url);
                    }
                }
            }
        }
        URL[] urlArray = all.toArray(new URL[all.size()]);
        return urlArray;
    }

    private static boolean searchDir(Set<URL> result, File file, String suffix) throws IOException {
        if (file.exists() && file.isDirectory()) {
            File[] fc = file.listFiles();
            String path;
            URL src;
            // protect against Windows JDK bugs for listFiles -
            // if it's null (even though it shouldn't be) return false
            if (fc == null) {
                return false;
            }

            for (File value : fc) {
                path = value.getAbsolutePath();
                if (value.isDirectory()) {
                    searchDir(result, value, suffix);
                } else if (path.endsWith(suffix)) {
                    // result.add(new URL("file:/" + path));
                    result.add(value.toURL());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Search from URL. Fall back on prefix tokens if not able to read from original url param.
     *
     * @param result the result urls
     * @param prefix the current prefix
     * @param suffix the suffix to match
     * @param url the current url to start search
     *
     * @throws IOException for any error
     */
    private static void searchFromURL(Set<URL> result, String prefix, String suffix, URL url) throws IOException {
        boolean done = false;
        InputStream is = getInputStream(url);
        if (is != null) {
            try (ZipInputStream zis = is instanceof ZipInputStream ? (ZipInputStream) is : new ZipInputStream(is)) {
                ZipEntry entry = zis.getNextEntry();
                // initial entry should not be null
                // if we assume this is some inner jar
                done = entry != null;
                while (entry != null) {
                    String entryName = entry.getName();
                    if (entryName.endsWith(suffix)) {
                        String urlString = url.toExternalForm();
                        result.add(new URL(urlString + entryName));
                    }
                    entry = zis.getNextEntry();
                }
            }
        }
        if (!done && prefix.length() > 0) {
            // we add '/' at the end since join adds it as well
            String urlString = url.toExternalForm() + "/";
            String[] split = prefix.split("/");
            prefix = join(split, true);
            String end = join(split, false);
            int p = urlString.lastIndexOf(end);

            if (p < 0) {
                return;
            }

            urlString = urlString.substring(0, p);
            for (String cur : PREFIXES_TO_EXCLUDE) {
                if (urlString.startsWith(cur)) {
                    return;
                }
            }
            url = new URL(urlString);
            searchFromURL(result, prefix, suffix, url);
        }
    }

    /**
     * Join tokens, exlude last if param equals true.
     *
     * @param tokens the tokens
     * @param excludeLast do we exclude last token
     *
     * @return joined tokens
     */
    private static String join(String[] tokens, boolean excludeLast) {
        StringBuilder join = new StringBuilder();
        for (int i = 0; i < tokens.length - (excludeLast ? 1 : 0); i++) {
            join.append(tokens[i]).append('/');
        }
        return join.toString();
    }

    /**
     * Open input stream from url. Ignore any errors.
     *
     * @param url the url to open
     *
     * @return input stream or null if not possible
     */
    private static InputStream getInputStream(URL url) {
        try {
            return url.openStream();
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * For URLs to JARs that do not use JarURLConnection - allowed by the servlet spec - attempt to produce a JarFile object
     * all the same. Known servlet engines that function like this include Weblogic and OC4J. This is not a full solution,
     * since an unpacked WAR or EAR will not have JAR "files" as such.
     */
    private static JarFile getAlternativeJarFile(URL url) throws IOException {
        String urlFile = url.getFile();
        return getAlternativeJarFile(urlFile);
    }

    static JarFile getAlternativeJarFile(String urlFile) throws IOException {
        JarFile result = null;
        // Trim off any suffix - which is prefixed by "!/" on Weblogic
        int bangSlash = urlFile.indexOf("!/");
        // Try the less safe "!", used on OC4J
        int bang = urlFile.indexOf('!');
        int separatorIndex = -1;

        // if either are found, take the first one.
        if (-1 != bangSlash || -1 != bang) {
            if (bangSlash < bang) {
                separatorIndex = bangSlash;
            } else {
                separatorIndex = bang;
            }
        }

        if (separatorIndex != -1) {
            String jarFileUrl = urlFile.substring(0, separatorIndex);
            // And trim off any "file:" prefix.
            if (jarFileUrl.startsWith("file:")) {
                jarFileUrl = jarFileUrl.substring("file:".length());
                jarFileUrl = URLDecoder.decode(jarFileUrl, StandardCharsets.UTF_8);
            }
            boolean foundExclusion = false;
            for (int i = 0; i < PREFIXES_TO_EXCLUDE.length; i++) {
                if (jarFileUrl.startsWith(PREFIXES_TO_EXCLUDE[i]) || jarFileUrl.endsWith(EXTENSIONS_TO_EXCLUDE[i])) {
                    foundExclusion = true;
                    break;
                }
            }
            if (!foundExclusion) {
                try {
                    result = new JarFile(jarFileUrl);
                } catch (ZipException ze) {
                    result = null;
                }
            }

            return result;
        }
        return null;
    }

    private static void searchJar(ClassLoader cl, Set<URL> result, JarFile file, String prefix, String suffix, SearchAdvice advice) throws IOException {
        Enumeration<JarEntry> e = file.entries();
        JarEntry entry;
        String name;
        while (e.hasMoreElements()) {
            try {
                entry = e.nextElement();
            } catch (Throwable t) {
                continue;
            }
            name = entry.getName();
            if (name.startsWith(prefix) && name.endsWith(suffix)) {
                Enumeration<URL> e2 = cl.getResources(name);
                while (e2.hasMoreElements()) {
                    result.add(e2.nextElement());
                    if (advice == SearchAdvice.FirstMatchOnly) {
                        return;
                    }
                }
            }
        }
    }

}
