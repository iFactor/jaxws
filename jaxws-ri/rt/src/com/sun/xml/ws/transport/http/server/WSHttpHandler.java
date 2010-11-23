/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.transport.http.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.xml.ws.resources.HttpserverMessages;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * {@link HttpHandler} implementation that serves the actual request.
 *
 * @author Jitendra Kotamraju
 * @author Kohsuke Kawaguhi
 */
final class WSHttpHandler implements HttpHandler {

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String HEAD_METHOD = "HEAD";
    private static final String PUT_METHOD = "PUT";
    private static final String DELETE_METHOD = "DELETE";

    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");

    private final HttpAdapter adapter;
    private final Executor executor;

    public WSHttpHandler(@NotNull HttpAdapter adapter, @Nullable Executor executor) {
        assert adapter!=null;
        this.adapter = adapter;
        this.executor = executor;
    }
    
    /**
     * Called by HttpServer when there is a matching request for the context
     */
    public void handle(HttpExchange msg) {
        try {
            logger.fine("Received HTTP request:"+msg.getRequestURI());
            if (executor != null) {
                // Use application's Executor to handle request. Application may
                // have set an executor using Endpoint.setExecutor().
                executor.execute(new HttpHandlerRunnable(msg));
            } else {
                handleExchange(msg);
            }
        } catch(Throwable e) {
            // Dont't propagate the exception otherwise it kills the httpserver
            e.printStackTrace();
        }
    }

    public void handleExchange(HttpExchange msg) throws IOException {
        WSHTTPConnection con = new ServerConnectionImpl(adapter,msg);
        try {
            logger.fine("Received HTTP request:"+msg.getRequestURI());
            String method = msg.getRequestMethod();
            if(method.equals(GET_METHOD) || method.equals(POST_METHOD) || method.equals(HEAD_METHOD)
            || method.equals(PUT_METHOD) || method.equals(DELETE_METHOD)) {
                adapter.handle(con);
            } else {
                logger.warning(HttpserverMessages.UNEXPECTED_HTTP_METHOD(method));
            }
        } finally {
            msg.close();
        }
    }

    /**
     * Wrapping the processing of request in a Runnable so that it can be
     * executed in Executor.
     */
    class HttpHandlerRunnable implements Runnable {
        final HttpExchange msg;

        HttpHandlerRunnable(HttpExchange msg) {
            this.msg = msg;
        }

        public void run() {
            try {
                handleExchange(msg);
            } catch (Throwable e) {
                // Does application's executor handle this exception ?
                e.printStackTrace();
            }
        }
    }


    /**
     * Computes the Endpoint's address from the request. Use "Host" header
     * so that it has correct address(IP address or someother hostname) through
     * which the application reached the endpoint.
     *
     * @return
     *      a string like "http://foo.bar:1234/abc/def"
     */
    static @NotNull String getRequestAddress(HttpExchange msg) {
        StringBuilder strBuf = new StringBuilder();
        strBuf.append((msg instanceof HttpsExchange) ? "https" : "http");
        strBuf.append("://");

        String hostHeader = msg.getRequestHeaders().getFirst("Host");
        if (hostHeader != null) {
            strBuf.append(hostHeader);   // Uses Host header
        } else {
            strBuf.append(msg.getLocalAddress().getHostName());
            strBuf.append(":");
            strBuf.append(msg.getLocalAddress().getPort());
        }
        strBuf.append(msg.getRequestURI().getPath());

        return strBuf.toString();
    }
}