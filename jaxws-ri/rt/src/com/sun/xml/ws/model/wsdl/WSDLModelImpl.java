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

package com.sun.xml.ws.model.wsdl;

import com.sun.xml.ws.api.model.ParameterBinding;
import com.sun.xml.ws.api.model.Mode;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLMessage;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * Implementation of {@link WSDLModel}
 *
 * @author Vivek Pandey
 */
public final class WSDLModelImpl implements WSDLModel {
    private final Map<QName, WSDLMessageImpl> messages = new HashMap<QName, WSDLMessageImpl>();
    private final Map<QName, WSDLPortType> portTypes = new HashMap<QName, WSDLPortType>();
    private final Map<QName, WSDLBoundPortTypeImpl> bindings = new HashMap<QName, WSDLBoundPortTypeImpl>();
    private final Map<QName, WSDLServiceImpl> services = new LinkedHashMap<QName, WSDLServiceImpl>();

    private final Map<QName,WSDLBoundPortType> unmBindings
        = Collections.<QName,WSDLBoundPortType>unmodifiableMap(bindings);

    public void addMessage(WSDLMessageImpl msg){
        messages.put(msg.getName(), msg);
    }

    public WSDLMessageImpl getMessage(QName name){
        return messages.get(name);
    }

    public void addPortType(WSDLPortType pt){
        portTypes.put(pt.getName(), pt);
    }

    public WSDLPortType getPortType(QName name){
        return portTypes.get(name);
    }

    public void addBinding(WSDLBoundPortTypeImpl boundPortType){
        bindings.put(boundPortType.getName(), boundPortType);
    }

    public WSDLBoundPortTypeImpl getBinding(QName name){
        return bindings.get(name);
    }

    public void addService(WSDLServiceImpl svc){
        services.put(svc.getName(), svc);
    }

    public WSDLServiceImpl getService(QName name){
        return services.get(name);
    }

    public Map<QName, WSDLMessageImpl> getMessages() {
        return messages;
    }

    public Map<QName, WSDLPortType> getPortTypes() {
        return portTypes;
    }

    public Map<QName, WSDLBoundPortType> getBindings() {
        return unmBindings;
    }

    public Map<QName, WSDLServiceImpl> getServices(){
        return services;
    }

    //TODO Partial impl
    public WSDLBoundOperation getOperation(QName serviceName, QName portName, QName tag) {
        WSDLService service = getService(serviceName);
        if(service  == null)
            return null;
        WSDLPort port = service.get(portName);
        if(port == null)
            return null;
        WSDLBoundPortType bpt = port.getBinding();
        if(bpt == null)
            return null;
        WSDLPortTypeImpl pt = (WSDLPortTypeImpl) bpt.getPortType();
        if(pt == null)
            return null;

        for(WSDLOperation op: pt.getOperations()){
            WSDLMessage msgName = op.getInputMessage();
            WSDLMessageImpl msg = messages.get(msgName);
            //TODO
        }
        return bpt.get(tag);
    }

    /**
     * Returns the first service QName from insertion order
     */
    public QName getFirstServiceName(){
        if(services.isEmpty())
            return null;
        return services.values().iterator().next().getName();
    }

    /**
     * Returns first port QName from first service as per the insertion order
     */
    public QName getFirstPortName(){
        WSDLPort fp = getFirstPort();
        if(fp==null)
            return null;
        else
            return fp.getName();
    }

    private WSDLPort getFirstPort(){
        if(services.isEmpty())
            return null;
        WSDLService service = services.values().iterator().next();
        Iterator<? extends WSDLPort> iter = service.getPorts().iterator();
        WSDLPort port = iter.hasNext()?iter.next():null;
        return port;
    }


    /**
     * Returns biningId of the first port
     */
    @Deprecated   // is this method still in use?
    public String getBindingId(){
        WSDLPort port = getFirstPort();
        if(port == null)
            return null;
        WSDLBoundPortType boundPortType = port.getBinding();
        if(boundPortType == null)
            return null;
        return boundPortType.getBindingId();
    }

     /**
     *
     * @param serviceName non-null service QName
     * @param portName    non-null port QName
     * @return
     *          WSDLBoundOperation on success otherwise null. throws NPE if any of the parameters null
     */
    public WSDLBoundPortTypeImpl getBinding(QName serviceName, QName portName){
        WSDLServiceImpl service = services.get(serviceName);
        if(service != null){
            WSDLPortImpl port = service.get(portName);
            if(port != null)
                return port.getBinding();
        }
        return null;
    }

    /**
     * Returns the bindings for the given bindingId
     * @param service  non-null service
     * @param bindingId  non-null binding id
     */
    public List<WSDLBoundPortType> getBindings(WSDLService service, String bindingId){
        List<WSDLBoundPortType> bs = new ArrayList<WSDLBoundPortType>();
        for (WSDLPort port : service.getPorts()) {
            WSDLBoundPortTypeImpl b = bindings.get(port.getName());
            if(b == null)
                return bs;
            if(b.equals(bindingId))
                bs.add(b);
        }
        return bs;
    }

    private WSDLBoundOperation getBoundOperation(WSDLBoundPortType bpt){
        WSDLPortTypeImpl pt = (WSDLPortTypeImpl) bpt.getPortType();
        if(pt == null)
            return null;
        for(WSDLOperation op: pt.getOperations()){
            WSDLMessage msgName = op.getInputMessage();
            WSDLMessageImpl msg = messages.get(msgName);
            //TODO: implement the rest
        }
        return null;
    }

    void finalizeRpcLitBinding(WSDLBoundPortTypeImpl boundPortType){
        assert(boundPortType != null);
        QName portTypeName = boundPortType.getPortTypeName();
        if(portTypeName == null)
            return;
        WSDLPortType pt = portTypes.get(portTypeName);
        if(pt == null)
            return;
        for (WSDLBoundOperationImpl bop : boundPortType.getBindingOperations()) {
            WSDLOperation pto = pt.get(bop.getName().getLocalPart());
            WSDLMessage inMsgName = pto.getInputMessage();
            if(inMsgName == null)
                continue;
            WSDLMessageImpl inMsg = messages.get(inMsgName.getName());
            int bodyindex = 0;
            if(inMsg != null){
                for(String name:inMsg.parts()){
                    ParameterBinding pb = bop.getInputBinding(name);
                    if(pb.isBody()){
                        bop.addPart(new WSDLPartImpl(name, pb, bodyindex++), Mode.IN);
                    }
                }
            }
            bodyindex=0;
            WSDLMessage outMsgName = pto.getOutputMessage();
            if(outMsgName == null)
                continue;
            WSDLMessageImpl outMsg = messages.get(outMsgName.getName());
            if(outMsg!= null){
                for(String name:outMsg.parts()){
                    ParameterBinding pb = bop.getOutputBinding(name);
                    if(pb.isBody()){
                        bop.addPart(new WSDLPartImpl(name, pb, bodyindex++), Mode.OUT);
                    }
                }
            }
        }
    }

    /**
     * Invoked at the end of the model construction to fix up references, etc.
     */
    public void freeze() {
        for (WSDLServiceImpl service : services.values()) {
            service.freeze(this);
        }
        for (WSDLBoundPortTypeImpl bp : bindings.values()) {
            bp.freeze(this);
        }
    }
}