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

package com.sun.faces.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This updater supports GFv1, v2, and v3.
 * @author edburns
 */
public class GlassfishUpdater {

    private enum Version {
        GFv1orv2,
        GFv3
    }

    private static final String ASADMIN_NAME = "asadmin";
    private static final String BACKUP_SUFFIX = "jsfbak";

    /** Creates a new instance of GlassfishUpdater */
    protected GlassfishUpdater() {
    }

    protected static File libDir = null;
    protected static File modulesDir = null;
    protected static Version version = null;

    public static void main(String args[]) throws IOException {
        if (0 == args.length) {
            printUsage();
            return;

        }
        File testFile = null,
             gfInstallDir = new File(args[0]);

        // Have we been given a directory?
        if (!gfInstallDir.isDirectory()) {
            printUsage();
            return;
        }
        // Is it a glassfish install directory?
        testFile = new File(gfInstallDir,"bin");
        File [] files = testFile.listFiles();
        boolean foundAsdmin = false;
        for (File cur : files) {
            if (-1 != cur.getName().indexOf("asadmin")) {
                foundAsdmin = true;
                break;
            }
        }
        if (!foundAsdmin) {
            printUsage();
            return;
        }

        // detect the GF version.  If GF_HOME/modules exists, this
        // is a v3 installation we're updating.  We'll have to take different
        // action.
        modulesDir = new File(gfInstallDir, "modules");
        if (modulesDir.exists() && modulesDir.isDirectory()) {
            version = Version.GFv3;
        } else {
            version = Version.GFv1orv2;
        }

        // Get the glassfish lib directory
        libDir = new File(gfInstallDir, "lib");
        if (!libDir.isDirectory()) {
            printUsage();
            return;
        }

        if (licenseAccepted()) {
            System.out.println("Updating glassfish at\n" + gfInstallDir.toString());
            System.out.println("with new JSF jars.");
            if (Version.GFv1orv2 == version) {
                stripJsfFromJavaEEJar(libDir);
                unpackJsfJarsToLib(libDir);
            } else {
                try {
                    updateV3Jars();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

    }

    public static boolean licenseAccepted() throws IOException {
        boolean result = false;
        InputStream is = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("BINARY_LICENSE.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while (null != (line = br.readLine())) {
            System.out.println(line);
        }
        System.out.print("Do you accept the above license terms? (type yes or no):");
        br = new BufferedReader(new InputStreamReader(System.in));
        line = br.readLine();
        result = (null != line) && line.equals("yes");
        return result;
    }

    protected static boolean printUsageCalled = false;

    public static void printUsage() {
        printUsageCalled = true;
        System.err.println("Usage: java -jar glassfish-jsf-update.jar <glassfish install directory>");
        System.err.println("\t<glassfish install directory> is the path to the\n\tglassfish binary install.");
    }

    public static void stripJsfFromJavaEEJar(File libDir) throws IOException {
        if (!javaEEJarHasJsfClasses(libDir)) {
            return;
        }

        File javaEEJarCopy = new File(libDir, "javaee.jar.copy"),
            javaEEJar =  new File (libDir, "javaee.jar"),
            javaEEJarOrig = new File(libDir, getBackupFilename(libDir,
                "javaee.jar"));
        javaEEJarCopy.delete();
        JarInputStream origJarStream = new JarInputStream(new FileInputStream(javaEEJar));
        JarOutputStream copyJarStream = new JarOutputStream(new FileOutputStream(javaEEJarCopy));
        JarEntry newEntry = null, cur = null;
        Pattern pat = Pattern.compile(".*javax.faces.*");
        Matcher mat = null;
        byte[] buf = new byte[1024];
        int n = 0;
        while (null != (cur = origJarStream.getNextJarEntry())) {
            mat = pat.matcher(cur.getName());
            // If the current entry does not include javax.faces...
            if (!mat.matches()) {
                // copy it to the newJar.
                newEntry = new JarEntry(cur.getName());
                copyJarStream.putNextEntry(newEntry);
                while((n = origJarStream.read(buf, 0, buf.length)) != -1) {
                    copyJarStream.write(buf, 0, n);
                }
            }
        }
        origJarStream.close();
        copyJarStream.close();

        javaEEJar.renameTo(javaEEJarOrig);
        javaEEJarCopy.renameTo(javaEEJar);

    }

    public static boolean javaEEJarHasJsfClasses(File libDir) throws IOException {
        boolean result = false;
        File javaEEJar =  new File (libDir, "javaee.jar");
        JarInputStream origJarStream = new JarInputStream(new FileInputStream(javaEEJar));
        JarEntry cur = null;
        Pattern pat = Pattern.compile(".*javax.faces.*");
        Matcher mat = null;
        cur = origJarStream.getNextJarEntry();
        while (null != cur) {
            mat = pat.matcher(cur.getName());
            // If the current entry does not include javax.faces...
            if (mat.matches()) {
                result = true;
            }
            cur = origJarStream.getNextJarEntry();
        }
        origJarStream.close();
        return result;
    }

    public static void unpackJsfJarsToLib(File libDir) throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("jsf-api.jar");
        File
             jsfApiCopy = new File(libDir, getBackupFilename(libDir,
                "jsf-api.jar")),
             jsfApi = new File(libDir, "jsf-api.jar"),
             jsfImplCopy = new File(libDir, getBackupFilename(libDir,
                "jsf-impl.jar")),
             jsfImpl = new File(libDir, "jsf-impl.jar");
        jsfApiCopy.delete();
        jsfImplCopy.delete();
        jsfApi.renameTo(jsfApiCopy);
        jsfImpl.renameTo(jsfImplCopy);
        FileOutputStream fos = new FileOutputStream(jsfApi);
        int n = 0;
        while (-1 != (n = is.read())) {
            fos.write(n);
        }
        is.close();
        fos.close();
        is = Thread.currentThread().getContextClassLoader().
               getResourceAsStream("jsf-impl.jar");
        fos = new FileOutputStream(jsfImpl);
        while (-1 != (n = is.read())) {
            fos.write(n);
        }
        is.close();
        fos.close();
    }

    protected static String getBackupFilename(File libDir, String filename) {
       File [] files = libDir.listFiles();
       Pattern pat = Pattern.compile(".*" + BACKUP_SUFFIX + "[0-9]*");
       Matcher mat = null;
       ArrayList<File> backupFiles = new ArrayList<File>();
       for (File cur : files) {
           mat = pat.matcher(cur.getName());
           // If the current entry is a backup file...
           if (mat.matches()) {
               // add it to the list.
               backupFiles.add(cur);
           }
       }
       int i = 1;
       int curInt = 0;
       String curSuffix = null, curName = null;
       if (0 < backupFiles.size()) {
           // Find the highest backup number and go one more.
           for (File cur : backupFiles) {
               curName = cur.getName();
               curSuffix = curName.substring(curName.indexOf(BACKUP_SUFFIX));
               if (BACKUP_SUFFIX.length() < curSuffix.length()) {

                   curInt = Integer.valueOf(curSuffix.
                           substring(BACKUP_SUFFIX.length())).intValue();
                   if (i == curInt) {
                       i = curInt + 1;
                   }
                   else if (i < curInt) {
                       i = curInt;
                   }
               }
           }
       }

       return filename + BACKUP_SUFFIX + i;
    }


    // Methods for updating V3
    private static Map<String,String> mapping = new HashMap<String,String>(2, 1.0f);
    static {
        mapping.put("javax.javaee", "jsf-api.jar");
        mapping.put("jsf-connector", "jsf-impl.jar");
    }

    private static void updateV3Jars() throws Exception {
        for (Map.Entry<String,String> entry : mapping.entrySet()) {
            final String targetKey = entry.getKey();
            File modulesDirTmp;
            if ("jsf-connector".equals(targetKey)) {
                modulesDirTmp = new File(modulesDir, "web");
            } else {
                modulesDirTmp = modulesDir;
            }
            String[] files = modulesDirTmp.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(targetKey) && name.endsWith(".jar");
                }
            });
            if (files.length != 1) {
                throw new RuntimeException("Multiple .jar files starting with " + targetKey + ".  Update cannot proceed");
            }

            File f = new File(modulesDirTmp, files[0]);
            // we now have the jar we need to backup
            String backupName = getBackupFilename(modulesDirTmp, files[0]);
            File backup = new File(modulesDirTmp, backupName);
            FileInputStream in = new FileInputStream(f);
            FileOutputStream out = new FileOutputStream(backup);
            byte[] buf = new byte[1024];
            for (int total = in.read(buf); total != -1; total = in.read(buf)) {
                out.write(buf, 0, total);
            }
            out.flush();
            out.close();
            in.close();

            // backup created.  Now update the file by removing the
            // javax.faces or com.sun.faces entries.
            String jarSource = entry.getValue();
            String pkg = ((jarSource.equals("jsf-api.jar") ? "javax/faces" : "com/sun/faces"));
            f = new File(modulesDirTmp, files[0]);
            String copyName = files[0] + ".copy";
            File copy = new File(modulesDirTmp, copyName);
            JarInputStream jarIn = new JarInputStream(new FileInputStream(f));
            JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(copy), jarIn.getManifest());

            JarEntry newEntry = null, cur = null;
            Matcher mat = null;
            int n;
            while (null != (cur = jarIn.getNextJarEntry())) {

                // If the current entry does not include javax.faces...
                if (!cur.getName().startsWith(pkg)) {
                    // copy it to the newJar.
                    newEntry = new JarEntry(cur.getName());
                    jarOut.putNextEntry(newEntry);
                    while ((n = jarIn.read(buf, 0, buf.length)) != -1) {
                        jarOut.write(buf, 0, n);
                    }
                }
            }
            jarIn.close();
            jarOut.flush();
            jarOut.close();

            // ok, the copy is now without the javax.faces classes.
            // read in from the packaged jsf-api.jar and add the entries
            jarIn = new JarInputStream(Thread.currentThread()
                  .getContextClassLoader().
                  getResourceAsStream(jarSource));
            JarInputStream jarInCopy = new JarInputStream(new FileInputStream(copy));
            jarOut = new JarOutputStream(new FileOutputStream(f), jarInCopy.getManifest());
            while (null != (cur = jarIn.getNextJarEntry())) {
                if (cur.getName().contains("MANIFEST.MF") || cur.getName()
                      .contains("com.sun.faces.spi.injectionprovider")) {
                    continue;
                }

                // copy it to the newJar.
                newEntry = new JarEntry(cur.getName());
                jarOut.putNextEntry(newEntry);
                while ((n = jarIn.read(buf, 0, buf.length)) != -1) {
                    jarOut.write(buf, 0, n);
                }

            }
            while (null != (cur = jarInCopy.getNextJarEntry())) {                

                // copy it to the newJar.
                newEntry = new JarEntry(cur.getName());
                try {
                jarOut.putNextEntry(newEntry);
                } catch (Exception e) {
                    continue;
                }
                while ((n = jarInCopy.read(buf, 0, buf.length)) != -1) {
                    jarOut.write(buf, 0, n);
                }

            }
            jarIn.close();
            jarInCopy.close();
            jarOut.flush();
            jarOut.close();
            copy.delete();
            
        }
    }

}
