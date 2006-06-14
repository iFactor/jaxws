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

package com.sun.xml.ws.api.model;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.util.Pool;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Provider;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Represents abstraction of SEI.
 *
 * <p>
 * This interface would be used to access which Java concepts correspond to
 * which WSDL concepts, such as which <code>wsdl:port</code> corresponds to
 * a SEI, or which <code>wsdl:operation</code> corresponds to {@link JavaMethod}.
 *
 * <P>
 * It also retains information about the databinding done for a SEI;
 * such as {@link JAXBRIContext} and {@link Bridge}.
 *
 * <p>
 * This model is constructed only when there is a Java SEI. Therefore it's
 * not available with {@link Dispatch} or {@link Provider}. Technologies that
 * need to work regardless of such surface API difference shall not be using
 * this model.
 *
 * @author Vivek Pandey
 */
public interface SEIModel {
    Pool.Marshaller getMarshallerPool();

    /**
     * JAXBContext that will be used to marshall/unmarshall the java classes found in the SEI.
     *
     * @return the <code>{@link JAXBRIContext}</code>
     */
    JAXBRIContext getJAXBContext();

    /**
     * Get the Bridge associated with the {@link TypeReference}
     *
     * @param type
     * @return the <code>{@link Bridge}</code> for the <code>type</code>
     */
//    Bridge getBridge(TypeReference type);

    /**
     * Its a known fault if the exception thrown by {@link Method} is annotated with the
     * {@link javax.xml.ws.WebFault#name()} thas equal to the name passed as parameter.
     *
     * @param name   is the qualified name of fault detail element as specified by wsdl:fault@element value.
     * @param method is the Java {@link Method}
     * @return true if <code>name</code> is the name
     *         of a known fault name for the <code>method</code>
     */
//    boolean isKnownFault(QName name, Method method);

    /**
     * Checks if the {@link JavaMethod} for the {@link Method} knowns the exception class.
     *
     * @param m  {@link Method} to pickup the right {@link JavaMethod} model
     * @param ex exception class
     * @return true if <code>ex</code> is a Checked Exception
     *         for <code>m</code>
     */
//    boolean isCheckedException(Method m, Class ex);

    /**
     * This method will be useful to get the {@link JavaMethod} corrrespondiong to
     * a {@link Method} - such as on the client side.
     *
     * @param method for which {@link JavaMethod} is asked for
     * @return the {@link JavaMethod} representing the <code>method</code>
     */
    JavaMethod getJavaMethod(Method method);

    /**
     * Gives a {@link JavaMethod} for a given {@link QName}. The {@link QName} will
     * be equivalent to the SOAP Body or Header block or can simply be the name of an
     * infoset that corresponds to the payload.
     *
     * @param name
     * @return the <code>JavaMethod</code> associated with the
     *         operation named name
     */
    public JavaMethod getJavaMethod(QName name);

    /**
     * Gives all the {@link JavaMethod} for a wsdl:port for which this {@link SEIModel} is
     * created.
     *
     * @return a {@link Collection} of {@link JavaMethod}
     *         associated with the {@link SEIModel}
     */
    Collection<? extends JavaMethod> getJavaMethods();

    /**
     * Location of the WSDL that defines the port associated with the {@link SEIModel}
     *
     * @return wsdl location uri - always non-null
     */
    String getWSDLLocation();

    /**
     * wsdl:service qualified name for the port associated with the {@link SEIModel)
     *
     * @return wsdl:service@name value - always non-null
     */
    QName getServiceQName();

    /**
     * Gets the {@link WSDLPort} that represents the port that this SEI binds to.
     */
    WSDLPort getPort();

    /**
     * Value of the wsdl:port name associated with the {@link SEIModel)
     *
     * @return wsdl:service/wsdl:port@name value, always non-null
     */
    QName getPortName();

    /**
     * Value of wsdl:portType bound to the port associated with the {@link SEIModel)
     *
     * @return
     */
    QName getPortTypeName();

    /**
     * Namespace of the wsd;:port associated with the {@link SEIModel)
     */
    String getTargetNamespace();
}
