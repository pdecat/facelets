/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * Licensed under the Common Development and Distribution License,
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.sun.com/cddl/
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.sun.facelets.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Jacob Hookom
 * @version $Id: Classpath.java,v 1.1 2005-05-21 17:54:45 jhook Exp $
 */
public final class Classpath {

    /**
     * 
     */
    public Classpath() {
        super();
    }

    public static URL[] search(String prefix, String suffix) throws IOException {
        return search(Thread.currentThread().getContextClassLoader(), prefix,
                suffix);
    }

    public static URL[] search(ClassLoader cl, String prefix, String suffix)
            throws IOException {
        Enumeration e = cl.getResources(prefix);
        Set all = new HashSet();
        URL url;
        URLConnection conn;
        while (e.hasMoreElements()) {
            url = (URL) e.nextElement();
            conn = url.openConnection();
            if (conn instanceof JarURLConnection) {
                searchJar(cl, all, ((JarURLConnection) conn).getJarFile(),
                        prefix, suffix);
            } else {
                searchDir(all, new File(url.getFile()), suffix);
            }
        }
        URL[] urlArray = (URL[]) all.toArray(new URL[all.size()]);
        return urlArray;
    }

    private static void searchDir(Set result, File file, String suffix)
            throws IOException {
        if (file.exists() && file.isDirectory()) {
            File[] fc = file.listFiles();
            String path;
            URL src;
            for (int i = 0; i < fc.length; i++) {
                path = fc[i].getAbsolutePath();
                if (fc[i].isDirectory()) {
                    searchDir(result, fc[i], suffix);
                } else if (path.endsWith(suffix)) {
                    result.add(new URL("file:/" + path));
                }
            }
        }
    }

    private static void searchJar(ClassLoader cl, Set result, JarFile file,
            String prefix, String suffix) throws IOException {
        Enumeration e = file.entries();
        JarEntry entry;
        String name;
        while (e.hasMoreElements()) {
            try {
                entry = (JarEntry) e.nextElement();
            } catch (Throwable t) {
                continue;
            }
            name = entry.getName();
            if (name.startsWith(prefix) && name.endsWith(suffix)) {
                Enumeration e2 = cl.getResources(name);
                while (e2.hasMoreElements()) {
                    result.add(e2.nextElement());
                }
            }
        }
    }

}
