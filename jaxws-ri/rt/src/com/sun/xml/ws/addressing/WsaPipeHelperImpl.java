/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.addressing;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;

import static com.sun.xml.ws.addressing.W3CAddressingConstants.ONLY_ANONYMOUS_ADDRESS_SUPPORTED;
import static com.sun.xml.ws.addressing.W3CAddressingConstants.ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED;
import com.sun.xml.ws.addressing.model.InvalidMapException;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.model.wsdl.WSDLBoundOperationImpl;
import com.sun.xml.ws.resources.AddressingMessages;
import org.w3c.dom.Element;

/**
 * @author Arun Gupta
 */
public class WsaPipeHelperImpl extends WsaPipeHelper {
    static final JAXBContext jc;

    static {
        try {
            jc = JAXBContext.newInstance(ProblemAction.class,
                                         ProblemHeaderQName.class);
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        }
    }

    public WsaPipeHelperImpl() {
        try {
            unmarshaller = jc.createUnmarshaller();
            marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        }
    }

    public WsaPipeHelperImpl(WSDLPort wsdlPort, WSBinding binding) {
        this();
        this.wsdlPort = wsdlPort;
        this.binding = binding;
    }

    @Override
    public final void getProblemActionDetail(String action, Element element) {
        ProblemAction pa = new ProblemAction(action);
        try {
            marshaller.marshal(pa, element);
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        }
    }

    @Override
    public final void getInvalidMapDetail(QName name, Element element) {
        ProblemHeaderQName phq = new ProblemHeaderQName(name);
        try {
            marshaller.marshal(phq, element);
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        }
    }

    @Override
    public final void getMapRequiredDetail(QName name, Element element) {
        getInvalidMapDetail(name, element);
    }

    @Override
    protected final void checkAnonymousSemantics(WSDLBoundOperation wbo, WSEndpointReference replyTo, WSEndpointReference faultTo) throws XMLStreamException {
        if (wbo == null)
            return;

        WSDLBoundOperationImpl impl = (WSDLBoundOperationImpl)wbo;
        WSDLBoundOperationImpl.ANONYMOUS anon = impl.getAnonymous();

        AddressingVersion av = binding.getAddressingVersion();

        String replyToValue = null;
        String faultToValue = null;

        if (replyTo != null)
            replyToValue = replyTo.getAddress();

        if (faultTo != null)
            faultToValue = faultTo.getAddress();

        if (anon == WSDLBoundOperationImpl.ANONYMOUS.optional) {
            // no check is required
        } else if (anon == WSDLBoundOperationImpl.ANONYMOUS.required) {
            if (replyToValue != null && !replyToValue.equals(av.getAnonymousUri()))
                throw new InvalidMapException(av.replyToTag, ONLY_ANONYMOUS_ADDRESS_SUPPORTED);

            if (faultToValue != null && !faultToValue.equals(av.getAnonymousUri()))
                throw new InvalidMapException(av.faultToTag, ONLY_ANONYMOUS_ADDRESS_SUPPORTED);

        } else if (anon == WSDLBoundOperationImpl.ANONYMOUS.prohibited) {
            if (replyToValue != null && replyToValue.equals(av.getAnonymousUri()))
                throw new InvalidMapException(av.replyToTag, ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED);

            if (faultToValue != null && faultToValue.equals(av.getAnonymousUri()))
                throw new InvalidMapException(av.faultToTag, ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED);

        } else {
            // cannot reach here
            throw new WebServiceException(AddressingMessages.INVALID_WSAW_ANONYMOUS(anon.toString()));
        }
    }
}
