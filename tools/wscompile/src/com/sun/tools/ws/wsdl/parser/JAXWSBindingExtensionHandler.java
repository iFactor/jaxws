/*
 * $Id: JAXWSBindingExtensionHandler.java,v 1.2 2005-08-02 23:03:18 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wsdl.parser;

import java.util.Iterator;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.tools.ws.wsdl.document.BindingOperation;
import com.sun.tools.ws.wsdl.document.Documentation;
import com.sun.tools.ws.wsdl.document.MessagePart;
import com.sun.tools.ws.wsdl.document.Operation;
import com.sun.tools.ws.wsdl.document.Service;
import com.sun.tools.ws.wsdl.document.jaxws.CustomName;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBinding;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBindingsConstants;
import com.sun.tools.ws.wsdl.document.jaxws.Parameter;
import com.sun.tools.ws.wsdl.document.schema.SchemaKinds;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.ParserContext;
import com.sun.tools.ws.wsdl.framework.WriterContext;
import com.sun.tools.ws.util.xml.XmlUtil;


/**
 * @author Vivek Pandey
 *
 * jaxws:bindings exension handler.
 *
 */
public class JAXWSBindingExtensionHandler extends ExtensionHandlerBase {

    /**
     *
     */
    public JAXWSBindingExtensionHandler() {
    }

    /* (non-Javadoc)
     * @see ExtensionHandler#getNamespaceURI()
     */
    public String getNamespaceURI() {
        return JAXWSBindingsConstants.NS_JAXWS_BINDINGS;
    }


    /**
     * @param context
     * @param parent
     * @param e
     */
    private boolean parseGlobalJAXWSBindings(ParserContext context, Extensible parent, Element e) {
        context.push();
        context.registerNamespaces(e);
        JAXWSBinding jaxwsBinding = new JAXWSBinding();

        if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.PACKAGE)){
            parsePackage(context, jaxwsBinding, e);
        }else if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.ENABLE_WRAPPER_STYLE)){
            parseWrapperStyle(context, jaxwsBinding, e);
        }else if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.ENABLE_ASYNC_MAPPING)){
            parseAsynMapping(context, jaxwsBinding, e);
        }else if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.ENABLE_ADDITIONAL_SOAPHEADER_MAPPING)){
            parseAdditionalSOAPHeaderMapping(context, jaxwsBinding, e);
        }else if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.ENABLE_MIME_CONTENT)){
            parseMimeContent(context, jaxwsBinding, e);
        }else{
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }

//        String attr = XmlUtil.getAttributeOrNull(e, JAXWSBindingsConstants.WSDL_LOCATION_ATTR);
//        if (attr != null) {
//            jaxwsBinding.setWsdlLocation(attr);
//        }
//
//        attr = XmlUtil.getAttributeOrNull(e, JAXWSBindingsConstants.NODE_ATTR);
//        if (attr != null) {
//            jaxwsBinding.setNode(attr);
//        }
//
//        attr = XmlUtil.getAttributeOrNull(e, JAXWSBindingsConstants.VERSION_ATTR);
//        if (attr != null) {
//            jaxwsBinding.setVersion(attr);
//        }
//
//        for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
//            Element e2 = Util.nextElement(iter);
//            if (e2 == null)
//                break;
//
//            if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.PACKAGE)){
//                parsePackage(context, jaxwsBinding, e2);
//            }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_WRAPPER_STYLE)){
//                parseWrapperStyle(context, jaxwsBinding, e2);
//            }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_ASYNC_MAPPING)){
//                parseAsynMapping(context, jaxwsBinding, e2);
//            }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_ADDITIONAL_SOAPHEADER_MAPPING)){
//                parseAdditionalSOAPHeaderMapping(context, jaxwsBinding, e2);
//            }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_MIME_CONTENT)){
//                parseMimeContent(context, jaxwsBinding, e2);
//            }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.PROVIDER)){
//                parseProvider(context, jaxwsBinding, e2);
//            }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.JAXB_BINDINGS)){
//                parseJAXBBindings(context, jaxwsBinding, e2);
//            }else{
//                Util.fail(
//                    "parsing.invalidExtensionElement",
//                    e2.getTagName(),
//                    e2.getNamespaceURI());
//                return false;
//            }
//        }
        parent.addExtension(jaxwsBinding);
        context.pop();
        context.fireDoneParsingEntity(
                JAXWSBindingsConstants.JAXWS_BINDINGS,
                jaxwsBinding);
        return true;
    }

    /**
     * @param context
     * @param jaxwsBinding
     * @param e2
     */
    private void parseProvider(ParserContext context, Extensible parent, Element e) {
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXWSBinding)parent).setProvider(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXWSBinding)parent).setProvider(Boolean.TRUE);
        }

    }

    /**
     * @param context
     * @param jaxwsBinding
     * @param e2
     */
    private void parseJAXBBindings(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleJAXBBindingsExtension: " + e.getNodeName());
        JAXWSBinding binding = (JAXWSBinding)parent;
        binding.addJaxbBindings(e);
    }

    /**
     * @param context
     * @param parent
     * @param e
     */
    private void parsePackage(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handlePackageExtension: " + e.getNodeName());
        String packageName = XmlUtil.getAttributeOrNull(e, JAXWSBindingsConstants.NAME_ATTR);
        JAXWSBinding binding = (JAXWSBinding)parent;
        binding.setJaxwsPackage(new CustomName(packageName, getJavaDoc(e)));
    }

    /**
     * @param context
     * @param parent
     * @param e
     */
    private void parseWrapperStyle(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleWrapperStyleExtension: " + e.getNodeName());
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXWSBinding)parent).setEnableWrapperStyle(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXWSBinding)parent).setEnableWrapperStyle(Boolean.TRUE);
        }
    }

    /**
     * @param context
     * @param parent
     * @param e
     */
    private void parseAdditionalSOAPHeaderMapping(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleAdditionalSOAPHeaderExtension: " + e.getNodeName());
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXWSBinding)parent).setEnableAdditionalHeaderMapping(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXWSBinding)parent).setEnableAdditionalHeaderMapping(Boolean.TRUE);
        }
    }

    /**
     * @param context
     * @param parent
     * @param e
     */
    private void parseAsynMapping(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleAsynMappingExtension: " + e.getNodeName());
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXWSBinding)parent).setEnableAsyncMapping(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXWSBinding)parent).setEnableAsyncMapping(Boolean.TRUE);
        }
    }

    /**
     * @param context
     * @param parent
     * @param e
     * @return TODO
     */
    private void parseMimeContent(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleMimeContentExtension: " + e.getNodeName());
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXWSBinding)parent).setEnableMimeContentMapping(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXWSBinding)parent).setEnableMimeContentMapping(Boolean.TRUE);
        }
    }

    /**
     * @param context
     * @param jaxwsBinding
     * @param e2
     */
    private void parseMethod(ParserContext context, JAXWSBinding jaxwsBinding, Element e) {
        String methodName = XmlUtil.getAttributeOrNull(e, JAXWSBindingsConstants.NAME_ATTR);
        String javaDoc = getJavaDoc(e);
        CustomName name = new CustomName(methodName, javaDoc);
        jaxwsBinding.setMethodName(name);
    }

    /**
     * @param context
     * @param jaxwsBinding
     * @param e2
     */
    private void parseParameter(ParserContext context, JAXWSBinding jaxwsBinding, Element e) {
        String part = XmlUtil.getAttributeOrNull(e, JAXWSBindingsConstants.PART_ATTR);

        //evaluate this XPath
        NodeList nlst;
        try {
            nlst = XPathAPI.selectNodeList(e.getOwnerDocument(), part, e.getOwnerDocument().getFirstChild());
        } catch( TransformerException ex ) {
            ex.printStackTrace();
            return; // abort processing this <jaxws:bindings>
        }

        if( nlst.getLength()==0 ) {
            //System.out.println("ERROR: XPATH evaluated node lenght is 0!");
            return; // abort
        }

        if( nlst.getLength()!=1 ) {
            //System.out.println("ERROR: XPATH has more than one evaluated target!");
            return; // abort
        }

        Node rnode = nlst.item(0);
        if(!(rnode instanceof Element )) {
            //System.out.println("ERROR:XPATH evaluated node is not instanceof Element!");
            return; // abort
        }

        Element msgPartElm = (Element)rnode;

        MessagePart msgPart = new MessagePart();

        String partName = XmlUtil.getAttributeOrNull(msgPartElm, "name");
        if(partName == null)
            return;
        msgPart.setName(partName);

        String val = XmlUtil.getAttributeOrNull(msgPartElm, "element");
        if(val != null){
            msgPart.setDescriptor(context.translateQualifiedName(val));
            msgPart.setDescriptorKind(SchemaKinds.XSD_ELEMENT);
        }else{
            val = XmlUtil.getAttributeOrNull(msgPartElm, "type");
            if(val == null)
                    return;
            msgPart.setDescriptor(context.translateQualifiedName(val));
            msgPart.setDescriptorKind(SchemaKinds.XSD_TYPE);
        }

        String element = XmlUtil.getAttributeOrNull(e, JAXWSBindingsConstants.ELEMENT_ATTR);
        String name = XmlUtil.getAttributeOrNull(e, JAXWSBindingsConstants.NAME_ATTR);
        QName elementName = context.translateQualifiedName(element);
        jaxwsBinding.addParameter(new Parameter(msgPart.getName(), elementName, name));
    }

    /**
     * @param context
     * @param jaxwsBinding
     * @param e2
     */
    private void parseClass(ParserContext context, JAXWSBinding jaxwsBinding, Element e) {
        String className = XmlUtil.getAttributeOrNull(e, JAXWSBindingsConstants.NAME_ATTR);
        String javaDoc = getJavaDoc(e);
        jaxwsBinding.setClassName(new CustomName(className, javaDoc));
    }


    /**
     * @param context
     * @param jaxwsBinding
     * @param e2
     */
    private void parseException(ParserContext context, JAXWSBinding jaxwsBinding, Element e) {
        for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;
            if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.CLASS)){
                String className = XmlUtil.getAttributeOrNull(e2, JAXWSBindingsConstants.NAME_ATTR);
                String javaDoc = getJavaDoc(e2);
                jaxwsBinding.setException(new com.sun.tools.ws.wsdl.document.jaxws.Exception(new CustomName(className, javaDoc)));
            }
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleDefinitionsExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleDefinitionsExtension(ParserContext context, Extensible parent, Element e) {
        return parseGlobalJAXWSBindings(context, parent, e);
//        if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.JAXWS_BINDINGS)){
//            parseGlobalJAXWSBindings(context, parent, e);
//        }else {
//            Util.fail(
//                "parsing.invalidExtensionElement",
//                e.getTagName(),
//                e.getNamespaceURI());
//            return false;
//        }
//        return false;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleTypesExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleTypesExtension(ParserContext context, Extensible parent, Element e) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handlePortTypeExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handlePortTypeExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.JAXWS_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXWSBinding jaxwsBinding = new JAXWSBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_WRAPPER_STYLE)){
                    parseWrapperStyle(context, jaxwsBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_ASYNC_MAPPING)){
                    parseAsynMapping(context, jaxwsBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.CLASS)){
                    parseClass(context, jaxwsBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxwsBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXWSBindingsConstants.JAXWS_BINDINGS,
                    jaxwsBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }



    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleOperationExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleOperationExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.JAXWS_BINDINGS)){
            if(parent instanceof Operation){
                return handlePortTypeOperation(context, (Operation)parent, e);
            }else if(parent instanceof BindingOperation){
                return handleBindingOperation(context, (BindingOperation)parent, e);
            }
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
        return false;
    }

    /**
     * @param context
     * @param operation
     * @param e
     * @return
     */
    private boolean handleBindingOperation(ParserContext context, BindingOperation operation, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.JAXWS_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXWSBinding jaxwsBinding = new JAXWSBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_ADDITIONAL_SOAPHEADER_MAPPING)){
                    parseAdditionalSOAPHeaderMapping(context, jaxwsBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_MIME_CONTENT)){
                    parseMimeContent(context, jaxwsBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.PARAMETER)){
                    parseParameter(context, jaxwsBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.EXCEPTION)){
                    parseException(context, jaxwsBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            operation.addExtension(jaxwsBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXWSBindingsConstants.JAXWS_BINDINGS,
                    jaxwsBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /**
     * @param context
     * @param operation
     * @param e
     * @return
     */
    private boolean handlePortTypeOperation(ParserContext context, Operation parent, Element e) {
        context.push();
        context.registerNamespaces(e);
        JAXWSBinding jaxwsBinding = new JAXWSBinding();

        for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_WRAPPER_STYLE)){
                parseWrapperStyle(context, jaxwsBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_ASYNC_MAPPING)){
                parseAsynMapping(context, jaxwsBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.METHOD)){
                parseMethod(context, jaxwsBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.PARAMETER)){
                parseParameter(context, jaxwsBinding, e2);
            }else{
                Util.fail(
                    "parsing.invalidExtensionElement",
                    e2.getTagName(),
                    e2.getNamespaceURI());
                return false;
            }
        }
        parent.addExtension(jaxwsBinding);
        context.pop();
        context.fireDoneParsingEntity(
                JAXWSBindingsConstants.JAXWS_BINDINGS,
                jaxwsBinding);
        return true;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleBindingExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleBindingExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.JAXWS_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXWSBinding jaxwsBinding = new JAXWSBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_ADDITIONAL_SOAPHEADER_MAPPING)){
                    parseAdditionalSOAPHeaderMapping(context, jaxwsBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.ENABLE_MIME_CONTENT)){
                    parseMimeContent(context, jaxwsBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxwsBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXWSBindingsConstants.JAXWS_BINDINGS,
                    jaxwsBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleInputExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleInputExtension(ParserContext context, Extensible parent, Element e) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleOutputExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleOutputExtension(ParserContext context, Extensible parent, Element e) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleFaultExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleFaultExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.JAXWS_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXWSBinding jaxwsBinding = new JAXWSBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;
                if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.CLASS)){
                    parseClass(context, jaxwsBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxwsBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXWSBindingsConstants.JAXWS_BINDINGS,
                    jaxwsBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleServiceExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleServiceExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.JAXWS_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXWSBinding jaxwsBinding = new JAXWSBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;
                if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.CLASS)){
                    parseClass(context, jaxwsBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxwsBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXWSBindingsConstants.JAXWS_BINDINGS,
                    jaxwsBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handlePortExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handlePortExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXWSBindingsConstants.JAXWS_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXWSBinding jaxwsBinding = new JAXWSBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.PROVIDER)){
                    parseProvider(context, jaxwsBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.METHOD)){
                    parseMethod(context, jaxwsBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxwsBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXWSBindingsConstants.JAXWS_BINDINGS,
                    jaxwsBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleMIMEPartExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleMIMEPartExtension(ParserContext context, Extensible parent, Element e) {
        // TODO Auto-generated method stub
        return false;
    }

    private String getJavaDoc(Element e){
        for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;
            if(XmlUtil.matchesTagNS(e2, JAXWSBindingsConstants.JAVADOC)){
                return e2.getNodeValue();
            }
        }
        return null;
    }

    public void doHandleExtension(WriterContext context, Extension extension)
        throws IOException {
//System.out.println("JAXWSBindingExtensionHandler doHandleExtension: "+extension);
        // NOTE - this ugliness can be avoided by moving all the XML parsing/writing code
        // into the document classes themselves
        if (extension instanceof JAXWSBinding) {
            JAXWSBinding binding = (JAXWSBinding) extension;
            System.out.println("binding.getElementName: "+binding.getElementName());
            context.writeStartTag(binding.getElementName());
            context.writeStartTag(JAXWSBindingsConstants.ENABLE_WRAPPER_STYLE);
            context.writeChars(binding.isEnableWrapperStyle().toString());
            context.writeEndTag(JAXWSBindingsConstants.ENABLE_WRAPPER_STYLE);
            context.writeEndTag(binding.getElementName());
        } else {
            throw new IllegalArgumentException();
        }
    }

}
