/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
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

package server.provider.wsdl_hello_lit_oneway.server;

import java.rmi.Remote;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.MimeHeader;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Node;

/**
 * Impl class for interface generated by wscompile -import.
 * This class will overwrite the impl class generated by wscompile.
 */
@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
public class HelloImpl implements Provider<SOAPMessage> {

	public SOAPMessage invoke(SOAPMessage msg) {
		SOAPBody body;
		try {
			body = msg.getSOAPBody();
		} catch(SOAPException e) {
			throw new WebServiceException(e);
		}
		Node node = body.getFirstChild();
		if (!node.getLocalName().equals("Hello")) {
			throw new WebServiceException("Expecting localName=Hello but got="+node.getLocalName());
		}
		if (!node.getNamespaceURI().equals("urn:test:types")) {
			throw new WebServiceException("Expecting NS=urn:test:types but got="+node.getNamespaceURI());
		}
		MimeHeaders headers = msg.getMimeHeaders();
/*
		Iterator i = headers.getAllHeaders();
		while(i.hasNext()) {
			MimeHeader header = (MimeHeader)i.next();
			System.out.println("name="+header.getName()+" value="+header.getValue());
		}
*/
		String[] action = headers.getHeader("SOAPAction");
		if (action == null || action.length > 1 || !action[0].equals("\"urn:test:hello\"")) {
			throw new WebServiceException("SOAPMessage doesn't contain transport header: SOAPAction");
		}
		String[] ct = headers.getHeader("Content-Type");
		if (ct == null || ct.length > 1 || !ct[0].startsWith("text/xml")) {
			throw new WebServiceException("SOAPMessage doesn't contain transport header: Content-Type");
		}
		return null;
	}
}
