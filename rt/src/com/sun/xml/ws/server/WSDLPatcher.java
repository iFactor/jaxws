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
package com.sun.xml.ws.server;

import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.util.xml.XMLStreamReaderToXMLStreamWriter;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import com.sun.xml.ws.addressing.W3CAddressingConstants;
import com.sun.istack.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Patches WSDL with the correct endpoint address and the relative paths
 * to other documents.
 *
 * @author Jitendra Kotamraju
 * @author Kohsuke Kawaguchi
 */
final class WSDLPatcher extends XMLStreamReaderToXMLStreamWriter {
    
    private static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    private static final QName SCHEMA_INCLUDE_QNAME = new QName(NS_XSD, "include");
    private static final QName SCHEMA_IMPORT_QNAME = new QName(NS_XSD, "import");
    private static final QName SCHEMA_REDEFINE_QNAME = new QName(NS_XSD, "redefine");

    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".wsdl.patcher");

    /**
     * {@link WSEndpoint} that owns the WSDL we are patching right now.
     */
    private final WSEndpointImpl<?> endpoint;

    /**
     * Document that is being patched.
     */
    private final SDDocumentImpl current;

    private final DocumentAddressResolver resolver;
    private final PortAddressResolver portAddressResolver;


    //
    // fields accumulated as we parse through documents
    //
    private String targetNamespace;
    private QName serviceName;
    private QName portName;

    private enum EPR_ADDRESS_STATE {IN, OUT, DONE}
    private EPR_ADDRESS_STATE eprAddressState = EPR_ADDRESS_STATE.OUT;

    /**
     * Creates a {@link WSDLPatcher} for patching WSDL.
     *
     * @param endpoint
     *      The endpoint that we are patchinig WSDL for. This object is consulted
     *      to check other {@link SDDocument}s. Must not be null.
     * @param current
     *      The document that we are patching. Must not be null.
     * @param portAddressResolver
     *      address of the endpoint is resolved using this resolver.
     * @param resolver
     *      Consulted to generate references among  {@link SDDocument}s.
     *      Must not be null.
     */
    public WSDLPatcher(WSEndpointImpl<?> endpoint, SDDocumentImpl current,
            PortAddressResolver portAddressResolver, DocumentAddressResolver resolver) {
        this.endpoint = endpoint;
        this.current = current;
        this.portAddressResolver = portAddressResolver;
        this.resolver = resolver;
    }

    @Override
    protected void handleAttribute(int i) throws XMLStreamException {
        QName name = in.getName();
        String attLocalName = in.getAttributeLocalName(i);

        if((name.equals(SCHEMA_INCLUDE_QNAME) && attLocalName.equals("schemaLocation"))
        || (name.equals(SCHEMA_IMPORT_QNAME)  && attLocalName.equals("schemaLocation"))
        || (name.equals(SCHEMA_REDEFINE_QNAME)  && attLocalName.equals("schemaLocation"))
        || (name.equals(WSDLConstants.QNAME_IMPORT)  && attLocalName.equals("location"))) {
            // patch this attribute value.

            String relPath = in.getAttributeValue(i);
            String actualPath = getPatchedImportLocation(relPath);
            if (actualPath == null) {
                return; // skip this attribute to leave it up to "implicit reference".
            }

            logger.fine("Fixing the relative location:"+relPath
                    +" with absolute location:"+actualPath);
            writeAttribute(i, actualPath);
            return;
        }

        if (name.equals(WSDLConstants.NS_SOAP_BINDING_ADDRESS) ||
            name.equals(WSDLConstants.NS_SOAP12_BINDING_ADDRESS)) {

            if(attLocalName.equals("location")) {
                String value = getAddressLocation();
                if (value != null) {
                    logger.fine("Fixing service:"+serviceName+ " port:"+portName
                            + " address with "+value);
                    writeAttribute(i, value);
                    return;
                }
            }
        }

        super.handleAttribute(i);
    }

    /**
     * Writes out an {@code i}-th attribute but with a different value.
     * @param i attribute index
     * @param value attribute value
     * @throws XMLStreamException when an error encountered while writing attribute
     */
    private void writeAttribute(int i, String value) throws XMLStreamException {
        String nsUri = in.getAttributeNamespace(i);
        if(nsUri!=null)
            out.writeAttribute( in.getAttributePrefix(i), nsUri, in.getAttributeLocalName(i), value );
        else
            out.writeAttribute( in.getAttributeLocalName(i), value );
    }

    @Override
    protected void handleStartElement() throws XMLStreamException {
        QName name = in.getName();

        if (name.equals(WSDLConstants.QNAME_DEFINITIONS)) {
            //String value = in.getAttributeValue("","targetNamespace");
            String value = in.getAttributeValue(null,"targetNamespace");
            if (value != null) {
                targetNamespace = value;
            }
        } else if (name.equals(WSDLConstants.QNAME_SERVICE)) {
            //String value = in.getAttributeValue("","name");
            String value = in.getAttributeValue(null,"name");
            if (value != null) {
                serviceName = new QName(targetNamespace, value);
            }
        } else if (name.equals(WSDLConstants.QNAME_PORT)) {
            //String value = in.getAttributeValue("","name");
            String value = in.getAttributeValue(null,"name");
            if (value != null) {
                portName = new QName(targetNamespace,value);
            }
        } else if (name.equals(W3CAddressingConstants.WSA_ADDRESS_QNAME)) {
            eprAddressState = EPR_ADDRESS_STATE.IN;
        }
        super.handleStartElement();
    }

    @Override
    protected void handleEndElement() throws XMLStreamException {
        QName name = in.getName();
        if (name.equals(W3CAddressingConstants.WSA_ADDRESS_QNAME)) {
            eprAddressState = EPR_ADDRESS_STATE.OUT;
        }
        super.handleEndElement();
    }

    @Override
    protected void handleCharacters() throws XMLStreamException {
        // handleCharacters() may be called multiple times. To take care of this,
        // EPR_ADDRESS_STATE is used.
        if (eprAddressState == EPR_ADDRESS_STATE.IN) {
            String value = getAddressLocation();
            if (value != null) {
                logger.fine("Fixing EPR Address for service:"+serviceName+ " port:"+portName
                            + " address with "+value);
                out.writeCharacters(value);
                eprAddressState = EPR_ADDRESS_STATE.DONE;
            }
        }
        if (eprAddressState != EPR_ADDRESS_STATE.DONE) {
            super.handleCharacters();
        }
    }

    /**
     * Returns the location to be placed into the generated document.
     *
     * @param relPath relative URI to be resolved
     * @return
     *      null to leave it to the "implicit reference".
     */
    private @Nullable String getPatchedImportLocation(String relPath) {
        try {
            ServiceDefinitionImpl def = endpoint.getServiceDefinition();
            assert def !=null; // this code is only used by ServieDefinitionImpl, so this must not be null.

            URL ref = new URL(current.getURL(), relPath);
            SDDocument refDoc = def.getBySystemId(ref);
            if(refDoc==null)
                return relPath;  // not something we know. just leave it as is.

            return resolver.getRelativeAddressFor(current,refDoc);
        } catch(MalformedURLException mue) {
            return null;
        }
    }

    /**
     * For the given service, port names it matches the correct endpoint and
     * reutrns its endpoint address
     *
     * @return returns the resolved endpoint address
     */
    private String getAddressLocation() {
        return (portAddressResolver == null || portName == null)
                ? null : portAddressResolver.getAddressFor(serviceName, portName.getLocalPart());
    }
}
    

