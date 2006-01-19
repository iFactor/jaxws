package com.sun.xml.ws.api.model.wsdl;


import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Map;

/**
 * Provides abstraction of a wsdl document.
 *
 * @author Vivek Pandey
 */
public interface WSDLModel {

    /**
     * Gets {@link com.sun.xml.ws.api.model.wsdl.PortType} that models <code>wsdl:portType</code>
     *
     * @param name non-null quaified name of wsdl:message, where the localName is the value of <code>wsdl:portType@name</code> and
     *             the namespaceURI is the value of wsdl:definitions@targetNamespace
     * @return A {@link com.sun.xml.ws.api.model.wsdl.PortType} or null if no wsdl:portType found.
     */
    PortType getPortType(QName name);

    /**
     * Gets {@link WSDLBinding} that models <code>wsdl:binding</code>
     *
     * @param name non-null quaified name of wsdl:binding, where the localName is the value of <code>wsdl:binding@name</code> and
     *             the namespaceURI is the value of wsdl:definitions@targetNamespace
     * @return A {@link WSDLBinding} or null if no wsdl:binding found
     */
    WSDLBinding getBinding(QName name);

    /**
     * Give a {@link WSDLBinding} for the given wsdl:service and wsdl:port names.
     *
     * @param serviceName non-null service QName
     * @param portName    non-null port QName
     * @return A {@link WSDLBinding}. null if the Binding for the given wsd:service and wsdl:port name are not
     *         found.
     */
    WSDLBinding getBinding(QName serviceName, QName portName);

    /**
     * Returns the bindings for the given bindingId
     *
     * @param service   non-null service
     * @param bindingId non-null - can be either {@link javax.xml.ws.soap.SOAPBinding#SOAP11HTTP_BINDING} or
     *                  {@link javax.xml.ws.soap.SOAPBinding#SOAP12HTTP_BINDING}
     * @return empty List if no wsdl:binding corresponding to the bindingId is found.
     */
    List<WSDLBinding> getBindings(Service service, String bindingId);

    /**
     * Gets {@link Service} that models <code>wsdl:service</code>
     *
     * @param name non-null quaified name of wsdl:service, where the localName is the value of <code>wsdl:service@name</code> and
     *             the namespaceURI is the value of wsdl:definitions@targetNamespace
     * @return A {@link Service} or null if no wsdl:service found
     */
    Service getService(QName name);

    /**
     * Gives a {@link Map} of wsdl:portType {@link QName} and {@link PortType}
     *
     * @return an empty Map if the wsdl document has no wsdl:portType
     */
    Map<QName, PortType> getPortTypes();

    /**
     * Gives a {@link Map} of wsdl:binding {@link QName} and {@link WSDLBinding}
     *
     * @return an empty Map if the wsdl document has no wsdl:binding
     */
    Map<QName, WSDLBinding> getBindings();

    /**
     * Gives a {@link Map} of wsdl:service qualified name and {@link com.sun.xml.ws.api.model.wsdl.Service}
     *
     * @return an empty Map if the wsdl document has no wsdl:service
     */
    Map<QName, Service> getServices();

    /**
     * Gives the binding Id for a given wsdl:port and wsdl:service name. The binding Id can be either
     * {@link javax.xml.ws.soap.SOAPBinding#SOAP11HTTP_BINDING} or {@link javax.xml.ws.soap.SOAPBinding#SOAP12HTTP_BINDING}
     * of the given service and port.
     *
     * @param service qualified name of wsdl:service. Must be non-null.
     * @param port    qualified name of wsdl:port. Must be non-null.
     * @return The binding ID associated with the serivce and port.
     * @throws WebServiceException If the binding correponding to the service or port is unkown (other than
     *                             {@link javax.xml.ws.soap.SOAPBinding#SOAP11HTTP_BINDING} or
     *                             {@link javax.xml.ws.soap.SOAPBinding#SOAP12HTTP_BINDING})
     */
    String getBindingId(QName service, QName port) throws WebServiceException;
}
