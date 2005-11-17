/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.util;

import com.sun.xml.ws.encoding.soap.message.SOAPMsgCreateException;
import com.sun.xml.ws.encoding.soap.message.SOAPMsgFactoryCreateException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.soap.*;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.namespace.QName;

import org.w3c.dom.*;
import org.w3c.dom.Node;

/**
 * $author: JAXWS Development Team
 */

/**
 * Has utility methods to create SOAPMessage
 */
public class SOAPUtil {
    
    private static final MessageFactory soap11messageFactory =
            createMessageFactory(SOAPConstants.SOAP_1_1_PROTOCOL);
    private static final MessageFactory soap12messageFactory =
            createMessageFactory(SOAPConstants.SOAP_1_2_PROTOCOL);
    private static final SOAPFactory soap11Factory = createSOAPFactory(SOAPConstants.SOAP_1_1_PROTOCOL);
    private static final SOAPFactory soap12Factory = createSOAPFactory(SOAPConstants.SOAP_1_2_PROTOCOL);

    private static SOAPFactory createSOAPFactory(String soapProtocol) {
        try {
            return SOAPFactory.newInstance(soapProtocol);
        } catch(SOAPException e) {
            throw new SOAPMsgFactoryCreateException(
                "soap.factory.create.err",
                new Object[] { e });
        }
    }

    /**
     *
     * @param bindingId
     * @return
     *          returns SOAPFactor for SOAP 1.2 if bindingID equals SOAP1.2 HTTP binding else
     *          SOAPFactory for SOAP 1.1
     */
    public static SOAPFactory getSOAPFactory(String bindingId){
        if(bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)){
            return soap12Factory;
        }
        return soap11Factory;
    }

    public static SOAPFault createSOAPFault(String bindingId){
        if(bindingId == null)
            bindingId = SOAPBinding.SOAP11HTTP_BINDING;
         try {
            if(bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING))
                return soap12Factory.createFault();                
            return soap11Factory.createFault();
        } catch (SOAPException e) {
            throw new SOAPMsgFactoryCreateException(
                "soap.fault.create.err",
                new Object[] { e });
        }
    }

    /**
     * Creates SOAP 1.1 or SOAP 1.2 SOAPFault based on the bindingId
     * @param msg
     * @param code
     * @param actor
     * @param detail
     * @return the created SOAPFault
     */
    public static SOAPFault createSOAPFault(String msg, QName code, String actor, Detail detail, String bindingId){
        if(bindingId == null)
            bindingId = SOAPBinding.SOAP11HTTP_BINDING;
        try {
            SOAPFault fault = null;
            if(bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING))
                fault = soap12Factory.createFault(msg, code);
            else if(bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING))
                fault = soap11Factory.createFault(msg, code);
            if(actor != null)
                fault.setFaultActor(actor);
            if(detail != null){
                Node n = fault.getOwnerDocument().importNode(detail, true);
                fault.appendChild(n);
            }
            return fault;
        } catch (SOAPException e) {
            throw new SOAPMsgFactoryCreateException(
                "soap.fault.create.err",
                new Object[] { e });
        }
    }

    public static SOAPMessage createMessage() {
        return createMessage(SOAPBinding.SOAP11HTTP_BINDING);
    }

    /**
     *
     * @param binding
     * @return a <code>SOAPMessage</code> associated with <code>binding</code>
     */
    public static SOAPMessage createMessage(String binding) {
        try {
            return getMessageFactory(binding).createMessage();
        } catch (SOAPException e) {
            throw new SOAPMsgCreateException(
                    "soap.msg.create.err",
                    new Object[] { e });
        }
    }

    /**
     *
     * @param binding
     * @param headers
     * @param in
     * @return <code>SOAPMessage</code> with <code>MimeHeaders</code> from an
     *         <code>InputStream</code> and binding.
     * @throws IOException
     */
    public static SOAPMessage createMessage(MimeHeaders headers, InputStream in,
            String binding) throws IOException {
        try {
            return getMessageFactory(binding).createMessage(headers, in);
        } catch (SOAPException e) {
            throw new SOAPMsgCreateException(
                    "soap.msg.create.err",
                    new Object[] { e });
        }
    }

    public static SOAPMessage createMessage(MimeHeaders headers, InputStream in)
    throws IOException {
        return createMessage(headers, in, SOAPBinding.SOAP11HTTP_BINDING);
    }

    public static MessageFactory getMessageFactory(String binding) {
        if (binding.equals(SOAPBinding.SOAP11HTTP_BINDING)) {
            return soap11messageFactory;
        } else if (binding.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            return soap12messageFactory;
        }
        return soap11messageFactory;
    }

    private static MessageFactory createMessageFactory(String bindingId) {
        try {
            return MessageFactory.newInstance(bindingId);
        } catch(SOAPException e) {
            throw new SOAPMsgFactoryCreateException(
                "soap.msg.factory.create.err",
                new Object[] { e });
        }
    }

}
