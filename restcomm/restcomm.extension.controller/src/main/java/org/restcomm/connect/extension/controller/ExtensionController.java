package org.restcomm.connect.extension.controller;

import org.restcomm.connect.extension.api.ExtensionResponse;
import org.restcomm.connect.extension.api.ExtensionType;
import org.restcomm.connect.extension.api.IExtensionRequest;
import org.restcomm.connect.extension.api.RestcommExtension;
import org.restcomm.connect.extension.api.RestcommExtensionGeneric;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by gvagenas on 21/09/16.
 */
public class ExtensionController {
    private static Logger logger = Logger.getLogger(ExtensionController.class);

    private static ExtensionController instance;
    private List callManagerExtensions;
    private List smsSessionExtensions;
    private List ussdCallManagerExtensions;
    private List restApiExtensions;

    private ExtensionController(){
        this.callManagerExtensions = new CopyOnWriteArrayList();
        this.smsSessionExtensions = new CopyOnWriteArrayList();
        this.ussdCallManagerExtensions = new CopyOnWriteArrayList();
        this.restApiExtensions = new CopyOnWriteArrayList();
    }

    public static ExtensionController getInstance() {
        if (instance == null) {
            instance = new ExtensionController();
        }
        return instance;
    }

    public List<RestcommExtensionGeneric> getExtensions(final ExtensionType type) {
        //Check the sender's class and return the extensions that are supported for this class
        if (type.equals(ExtensionType.CallManager) && (callManagerExtensions != null && callManagerExtensions.size() > 0)) {
            return callManagerExtensions;
        } else if (type.equals(ExtensionType.SmsService) && (smsSessionExtensions != null && smsSessionExtensions.size() > 0)) {
            return smsSessionExtensions;
        } else if (type.equals(ExtensionType.UssdCallManager) && (ussdCallManagerExtensions != null && ussdCallManagerExtensions.size() > 0)) {
            return ussdCallManagerExtensions;
        } else if (type.equals(ExtensionType.RestApi) && (restApiExtensions != null && restApiExtensions.size() > 0)) {
            return restApiExtensions;
        } else {
            return null;
        }
    }

    public void registerExtension(final RestcommExtensionGeneric extension) {
        //scan the annotation to see what this extension supports
        ExtensionType[] types = extension.getClass().getAnnotation(RestcommExtension.class).type();
        String extensionName = extension.getClass().getName();
        for (ExtensionType type : types) {
            if (type.equals(ExtensionType.CallManager)) {
                callManagerExtensions.add(extension);
                if (logger.isDebugEnabled()) {
                    logger.debug("CallManager extension added: "+extensionName);
                }
            }
            if (type.equals(ExtensionType.SmsService)) {
                smsSessionExtensions.add(extension);
                if (logger.isDebugEnabled()) {
                    logger.debug("SmsService extension added: "+extensionName);
                }
            }
            if (type.equals(ExtensionType.UssdCallManager)) {
                ussdCallManagerExtensions.add(extension);
                if (logger.isDebugEnabled()) {
                    logger.debug("UssdCallManager extension added: "+extensionName);
                }
            }
            if (type.equals(ExtensionType.RestApi)) {
                restApiExtensions.add(extension);
                if (logger.isDebugEnabled()) {
                    logger.debug("RestApi extension added: "+extensionName);
                }
            }
        }
    }

    public ExtensionResponse executePreOutboundAction(final IExtensionRequest ier, List<RestcommExtensionGeneric> extensions) {
        //FIXME: if we have more than one extension in chain
        // and all of them are successful, we only receive the last
        // extensionResponse
        ExtensionResponse response = new ExtensionResponse();
        if (extensions != null && extensions.size() > 0) {

            for (RestcommExtensionGeneric extension : extensions) {
                if(logger.isInfoEnabled()) {
                    logger.info( extension.getName()+" is enabled="+extension.isEnabled());
                }
                if (extension.isEnabled()) {
                    response = extension.preOutboundAction(ier);
                    //fail fast
                    if (!response.isAllowed()){
                        break;
                    }
                }
            }
        }
        return response;
    }

    public ExtensionResponse executePostOutboundAction(Object er, List<RestcommExtensionGeneric> extensions) {
        ExtensionResponse response = new ExtensionResponse();
        //TODO: implement actual calls
        return response;
    }
    public ExtensionResponse executePostOutboundAction(final IExtensionRequest er, List<RestcommExtensionGeneric> extensions) {
        ExtensionResponse response = new ExtensionResponse();
        //TODO: implement actual calls
        return response;
    }
}
