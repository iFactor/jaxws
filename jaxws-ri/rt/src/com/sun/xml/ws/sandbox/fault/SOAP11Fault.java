package com.sun.xml.ws.sandbox.fault;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents SOAP1.1 Fault. This class will be used to marshall/unmarshall a soap fault using JAXB.
 * <p/>
 * <pre>
 * Example:
 *
 *     &lt;soap:Fault xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' >
 *         &lt;faultcode>soap:Client&lt;/faultcode>
 *         &lt;faultstring>Invalid message format&lt;/faultstring>
 *         &lt;faultactor>http://example.org/someactor&lt;/faultactor>
 *         &lt;detail>
 *             &lt;m:msg xmlns:m='http://example.org/faults/exceptions'>
 *                 Test message
 *             &lt;/m:msg>
 *         &lt;/detail>
 *     &lt;/soap:Fault>
 * Above, m:msg, if a known fault (described in the WSDL), IOW, if m:msg is known by JAXBContext it should be unmarshalled into a
 * Java object otherwise it should be deserialized as {@link javax.xml.soap.Detail}
 * </pre>
 * <p/>
 * TODO: Add any missing annotation
 *
 * @author Vivek Pandey
 */
@XmlRootElement(name = "Fault", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
public class SOAP11Fault<T> {
    @XmlElement(name = "faultcode", namespace = "")
    private String code;

    @XmlElement(name = "faultstring", namespace = "")
    private String reason;

    @XmlElement(name = "faultactor", namespace = "")
    private String actor;

    /**
     * detail is a choice between {@link javax.xml.soap.Detail} and a JAXB object. Lets keep it as T or can be {@link Object} as well
     */
    @XmlElement(name = "detail", namespace = "")
    private T detail;

    /**
     * This constructor takes soap fault detail among other things. The detail could represent {@link javax.xml.soap.Detail}
     * or a java object that can be marshalled/unmarshalled by JAXB.
     *
     * @param code
     * @param reason
     * @param actor
     * @param detail
     */
    public SOAP11Fault(String code, String reason, String actor, T detail) {
        this.code = code;
        this.reason = reason;
        this.actor = actor;
        this.detail = detail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * returns a java type T - this could be a {@link javax.xml.soap.Detail} or a JAXB object
     */
    public T getDetail() {
        return detail;
    }

    /**
     * @param detail could be {@link javax.xml.soap.Detail} or a JAXB object
     */
    public void setDetail(T detail) {
        this.detail = detail;
    }
}
