/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.binding;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.client.HandlerConfiguration;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import com.sun.xml.ws.developer.BindingTypeFeature;

import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.handler.Handler;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Instances are created by the service, which then
 * sets the handler chain on the binding impl.
 *
 * <p>
 * This class is made abstract as we don't see a situation when
 * a BindingImpl has much meaning without binding id.
 * IOW, for a specific binding there will be a class
 * extending BindingImpl, for example SOAPBindingImpl.
 *
 * <p>
 * The spi Binding interface extends Binding.
 *
 * @author WS Development Team
 */
public abstract class BindingImpl implements WSBinding {

    protected static final WebServiceFeature[] EMPTY_FEATURES = new WebServiceFeature[0];

    //This is reset when ever Binding.setHandlerChain() or SOAPBinding.setRoles() is called.
    protected HandlerConfiguration handlerConfig;
    private final BindingID bindingId;
    // Features that are set(enabled/disabled) on the binding
    protected final WebServiceFeatureList features = new WebServiceFeatureList();

    protected javax.xml.ws.Service.Mode serviceMode = javax.xml.ws.Service.Mode.PAYLOAD;

    protected BindingImpl(BindingID bindingId, WebServiceFeature ... features) {
        this.bindingId = bindingId;
        handlerConfig = new HandlerConfiguration(Collections.<String>emptySet(), Collections.<Handler>emptyList());
        setFeatures(features);
    }

    public
    @NotNull
    List<Handler> getHandlerChain() {
        return handlerConfig.getHandlerChain();
    }

    public HandlerConfiguration getHandlerConfig() {
        return handlerConfig;
    }


    public void setMode(@NotNull Service.Mode mode) {
        this.serviceMode = mode;
    }

    public Set<QName> getKnownHeaders() {
    	return handlerConfig.getHandlerKnownHeaders();
    }
    
    public
    @NotNull
    BindingID getBindingId() {
        return bindingId;
    }

    public final SOAPVersion getSOAPVersion() {
        return bindingId.getSOAPVersion();
    }

    public AddressingVersion getAddressingVersion() {
        AddressingVersion addressingVersion;
        if (features.isEnabled(AddressingFeature.class))
            addressingVersion = AddressingVersion.W3C;
        else if (features.isEnabled(MemberSubmissionAddressingFeature.class))
            addressingVersion = AddressingVersion.MEMBER;
        else
            addressingVersion = null;
        return addressingVersion;
    }

    public final
    @NotNull
    Codec createCodec() {
        return bindingId.createEncoder(this);
    }

    public static BindingImpl create(@NotNull BindingID bindingId) {
        if (bindingId.equals(BindingID.XML_HTTP))
            return new HTTPBindingImpl();
        else
            return new SOAPBindingImpl(bindingId);
    }

    public static BindingImpl create(@NotNull BindingID bindingId, WebServiceFeature[] features) {
        // Override the BindingID from the features
        for(WebServiceFeature feature : features) {
            if (feature instanceof BindingTypeFeature) {
                BindingTypeFeature f = (BindingTypeFeature)feature;
                bindingId = BindingID.parse(f.getBindingId());
            }
        }
        if (bindingId.equals(BindingID.XML_HTTP))
            return new HTTPBindingImpl(features);
        else
            return new SOAPBindingImpl(bindingId, features);
    }

    public static WSBinding getDefaultBinding() {
        return new SOAPBindingImpl(BindingID.SOAP11_HTTP);
    }

    public String getBindingID() {
        return bindingId.toString();
    }

    public @Nullable <F extends WebServiceFeature> F getFeature(@NotNull Class<F> featureType){
        return features.get(featureType);
    }

    public boolean isFeatureEnabled(@NotNull Class<? extends WebServiceFeature> feature){
        return features.isEnabled(feature);
    }

    @NotNull
    public WebServiceFeatureList getFeatures() {
        return features;
    }


    public void setFeatures(WebServiceFeature... newFeatures) {
        if (newFeatures != null) {
            for (WebServiceFeature f : newFeatures) {
                features.replace(f);
            }
        }
    }

    public void addFeature(@NotNull WebServiceFeature newFeature) {
        features.add(newFeature);
    }
}