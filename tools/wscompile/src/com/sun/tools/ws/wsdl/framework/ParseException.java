/*
 * $Id: ParseException.java,v 1.2 2005-05-25 20:16:37 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * An exception signalling a parsing error.
 *
 * @author JAX-RPC Development Team
 */
public class ParseException extends JAXWSExceptionBase {

    public ParseException(String key) {
        super(key);
    }

    public ParseException(String key, String arg) {
        super(key, arg);
    }

    public ParseException(String key, Localizable localizable) {
        super(key, localizable);
    }

    public ParseException(String key, String arg, Localizable localizable) {
        this(key, new Object[] { arg, localizable });
    }

    public ParseException(String key, Object[] args) {
        super(key, args);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.wsdl";
    }
}
