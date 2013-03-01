/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

import com.sun.istack.NotNull;
import com.sun.tools.ws.processor.modeler.wsdl.ConsoleErrorReporter;
import junit.framework.TestCase;
import java.io.File;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Rama Pulavarthi
 */
public class WsimportDefaultAuthTest extends TestCase {

    private static class MyAuthenticator extends DefaultAuthenticator {

        private String requestingURL;

        public MyAuthenticator(@NotNull ErrorReceiver receiver, @NotNull File authfile) throws BadCommandLineException {
            this(receiver, authfile, "http://foo.com/myservice?wsdl");
        }

        MyAuthenticator(@NotNull ErrorReceiver receiver, @NotNull File authfile, String reqUrl) throws BadCommandLineException {
            super(receiver, authfile);
            requestingURL = reqUrl;
        }

        @Override
        protected URL getRequestingURL() {
            try {
                return new URL(requestingURL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        void setRequestingURL(String url) {
            requestingURL = url;
        }
    }

    public void testDefaultAuth() throws Exception{
        URL url = getResourceAsUrl("com/sun/tools/ws/wscompile/.auth");
        DefaultAuthenticator da = new MyAuthenticator(new ConsoleErrorReporter(System.out), new File(url.toURI()));
        PasswordAuthentication pa = da.getPasswordAuthentication();
        assertTrue(pa != null && pa.getUserName().equals("duke") && Arrays.equals(pa.getPassword(), "test".toCharArray()));

    }

    public void testGetDefaultAuth() throws Exception {
        URL url = getResourceAsUrl("com/sun/tools/ws/wscompile/.auth");
        DefaultAuthenticator da = new MyAuthenticator(new ConsoleErrorReporter(System.out), new File(url.toURI()));
        Authenticator orig = DefaultAuthenticator.getCurrentAuthenticator();
        Authenticator.setDefault(da);
        assertFalse(da.equals(orig));
        Authenticator auth = DefaultAuthenticator.getCurrentAuthenticator();
        assertNotNull(auth);
        assertEquals(da, auth);
        Authenticator.setDefault(orig);
        assertEquals(orig, DefaultAuthenticator.getCurrentAuthenticator());
    }

    public void testJaxWs_1101() throws Exception{
        URL url = getResourceAsUrl("com/sun/tools/ws/wscompile/jaxws-1101.txt");
        MyAuthenticator da = new MyAuthenticator(new ConsoleErrorReporter(System.out), new File(url.toURI()));
        PasswordAuthentication pa = da.getPasswordAuthentication();
        assertNull(pa);

        da.setRequestingURL("http://server1.myserver.com/MyService/Service.svc?wsdl");
        pa = da.getPasswordAuthentication();
        assertEquals("user", pa.getUserName());
        assertEquals(")/_@B8M)gDw", new String(pa.getPassword()));

        da.setRequestingURL("http://server1.myserver.com/MyService/Service.svc?xsd=xsd0");
        pa = da.getPasswordAuthentication();
        assertEquals("user", pa.getUserName());
        assertEquals(")/_@B8M)gDw", new String(pa.getPassword()));

        da.setRequestingURL("http://server1.myserver.com/MyService/Service.svc");
        pa = da.getPasswordAuthentication();
        assertEquals("user", pa.getUserName());
        assertEquals(")/_@B8M)gDw", new String(pa.getPassword()));

        da.setRequestingURL("http://server1.myserver.com/encoded/MyService/Service.svc?wsdl");
        pa = da.getPasswordAuthentication();
        assertEquals("user2", pa.getUserName());
        assertEquals(")/_@B8M)gDw", new String(pa.getPassword()));
    }

    private static URL getResourceAsUrl(String resourceName) throws RuntimeException {
        URL input = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (input == null) {
            throw new RuntimeException("Failed to find resource \"" + resourceName + "\"");
        }
        return input;
    }

}