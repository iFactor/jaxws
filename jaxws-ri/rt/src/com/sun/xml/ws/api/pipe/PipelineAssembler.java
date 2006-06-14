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

package com.sun.xml.ws.api.pipe;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.Dispatch;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

/**
 * Creates a pipeline.
 *
 * <p>
 * This pluggability layer enables the upper layer to
 * control exactly how the pipeline is composed.
 *
 * <p>
 * JAX-WS is going to have its own default implementation
 * when used all by itself, but it can be substituted by
 * other implementations.
 *
 * <p>
 * See {@link PipelineAssemblerFactory} for how {@link PipelineAssembler}s
 * are located.
 *
 * <p>
 * TODO: the JAX-WS team felt that no {@link Pipe} should be relying
 * on the {@link SEIModel}, so it is no longer given to the assembler.
 * Talk to us if you need it.
 *
 *
 * @author Kohsuke Kawaguchi
 */
public interface PipelineAssembler {
    /**
     * Creates a new pipeline for clients.
     *
     * <p>
     * When a JAX-WS client creates a proxy or a {@link Dispatch} from
     * a {@link Service}, JAX-WS runtime internally uses this method
     * to create a new pipeline as a part of the initilization.
     *
     * @param address
     *      The endpoint address. Always non-null. This parameter is taken separately
     *      from {@link WSDLPort} (even though there's {@link WSDLPort#getAddress()})
     *      because sometimes WSDL is not available.
     *
     * @param wsdlModel
     *      The created pipeline will be used to serve this port.
     *      Null if the service isn't associated with any port definition in WSDL,
     *      and otherwise non-null.
     *
     * @param rootOwner
     *      The pipeline is created for this {@link WSService}.
     *      Always non-null. (To be precise, the newly created pipeline
     *      is owned by a proxy or a dispatch created from thsi {@link WSService}.)
     *
     * @param binding
     *      The binding of the new pipeline to be created.
     *
     * @return
     *      non-null freshly created pipeline.
     *
     * @throws WebServiceException
     *      if there's any configuration error that prevents the
     *      pipeline from being constructed. This exception will be
     *      propagated into the application, so it must have
     *      a descriptive error.
     */
    Pipe createClient(@NotNull EndpointAddress address, @Nullable WSDLPort wsdlModel, @NotNull WSService rootOwner, @NotNull WSBinding binding);

    /**
     * Creates a new pipeline for servers.
     *
     * <p>
     * When a JAX-WS server deploys a new endpoint, it internally
     * uses this method to create a new pipeline as a part of the
     * initialization.
     *
     * <p>
     * Note that this method is called only once to set up a
     * 'master pipeline', and it gets {@link Pipe#copy(PipeCloner) copied}
     * from it.
     *
     * @param seiModel
     *      The created pipeline will use seiModel to get java concepts for
     *      the endpoint
     *      Null if the service doesn't have SEI model e.g. Provider endpoints,
     *      and otherwise non-null.
     *
     *
     * @param wsdlModel
     *      The created pipeline will be used to serve this port.
     *      Null if the service isn't associated with any port,
     *      and otherwise non-null.
     *
     * @param owner
     *      The created pipeline is used to serve this {@link WSEndpoint}.
     *      Specifically, its {@link WSBinding} should be of interest to
     *      many {@link Pipe}s. Always non-null.
     *
     * @param terminal
     *      The last {@link Pipe} in the pipeline.
     *      Always non-null.
     *      The assembler
     *      is expected to put additional {@link Pipe}s in front
     *      of it.
     *
     *      <p>
     *      (Just to give you the idea how this is used, normally
     *      the terminal pipe is the one that invokes the user application
     *      or {@link Provider}.)
     */
    Pipe createServer(@Nullable SEIModel seiModel, @Nullable WSDLPort wsdlModel, @NotNull WSEndpoint owner, @NotNull Pipe terminal);
}
