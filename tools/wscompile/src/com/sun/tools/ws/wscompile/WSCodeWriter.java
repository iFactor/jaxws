/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.ws.wscompile;

import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FileCodeWriter;

import com.sun.mirror.apt.Filer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * {@link FileCodeWriter} implementation that notifies
 * JAX-WS about newly created files.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class WSCodeWriter extends FileCodeWriter {
    private final Options options;
    
    protected static final ThreadLocal<FileSystem> threadLocalFileSystem =
        new ThreadLocal<FileSystem>();

    public WSCodeWriter( File outDir, Options options) throws IOException {
        super(outDir);
        this.options = options;
    }

    protected File getFile(JPackage pkg, String fileName ) throws IOException {
        File f = super.getFile(pkg, fileName);

        options.addGeneratedFile(f);
        // we can't really tell the file type, for we don't know
        // what this file is used for. Fortunately,
        // FILE_TYPE doesn't seem to be used, so it doesn't really
        // matter what we set.

        return f;
    }

    @Override
    public Writer openSource(JPackage jPackage, String name)
            throws IOException {
        FileSystem fileSystem = threadLocalFileSystem.get();
        return (fileSystem == null)
            ? super.openSource(jPackage, name)
            : fileSystem.openSource(getFile(jPackage, name));
    }
    
    /**
     * Sets the <tt>FileSystem</tt> to be used by the current <tt>Thread</tt>.
     * @param fileSystem FileSystem used to access file resources or
     *        <tt>null</tt> if the normal disk based file sytem should be use.
     */
    public static void setFileSystem(FileSystem fileSystem) {
        if (fileSystem == null) {
            threadLocalFileSystem.remove();
        } else {
            threadLocalFileSystem.set(fileSystem);
        }
    }
    
    public static interface FileSystem
    {
        Writer openSource(File file);
        Writer createSourceFile(Filer filer, String name) throws IOException;
    }
}