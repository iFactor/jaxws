package com.sun.xml.ws.api.model.wsdl;

import com.sun.xml.ws.api.model.ParameterBinding;

/**
 * Abstracts wsdl:part after applying binding information from wsdl:binding.
 *
 * @author Vivek Pandey
 */
public interface Part {
    /**
     * Gets wsdl:part@name attribute value.
     */
    String getName();

    /**
     * Gets the wsdl:part binding as seen thru wsdl:binding
     */
    ParameterBinding getBinding();

    /**
     * Index value is as the order in which the wsdl:part appears inside the input or output wsdl:message.
     * @return n where n >= 0
     */
    int getIndex();
}
