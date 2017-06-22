package org.restcomm.connect.sms.smpp;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.actor.UntypedActorFactory;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.cloudhopper.smpp.tlv.Tlv;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.apache.commons.configuration.Configuration;
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.commons.util.UriUtils;
import org.restcomm.connect.dao.AccountsDao;
import org.restcomm.connect.dao.ApplicationsDao;
import org.restcomm.connect.dao.DaoManager;
import org.restcomm.connect.dao.IncomingPhoneNumbersDao;
import org.restcomm.connect.dao.entities.Application;
import org.restcomm.connect.dao.entities.IncomingPhoneNumber;
//import org.restcomm.connect.extension.api.ExtensionRequest;
//import org.restcomm.connect.extension.api.ExtensionResponse;
import org.restcomm.connect.extension.api.ExtensionType;
import org.restcomm.connect.extension.api.IExtensionCreateSmsSessionRequest;
import org.restcomm.connect.extension.api.RestcommExtensionException;
import org.restcomm.connect.extension.api.RestcommExtensionGeneric;
import org.restcomm.connect.extension.controller.ExtensionController;
import org.restcomm.connect.interpreter.StartInterpreter;
import org.restcomm.connect.monitoringservice.MonitoringService;
import org.restcomm.connect.sms.SmsSession;
import org.restcomm.connect.sms.api.CreateSmsSession;
import org.restcomm.connect.sms.api.DestroySmsSession;
import org.restcomm.connect.sms.api.SmsServiceResponse;
import org.restcomm.smpp.parameter.TlvSet;

import javax.servlet.ServletContext;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipURI;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Collection;

public class SmppMessageHandler extends UntypedActor  {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    private final ActorSystem system = getContext().system();
    private final ServletContext servletContext;
    private final DaoManager storage;
    private final Configuration configuration;
    private final SipFactory sipFactory;
    private final ActorRef monitoringService;
    //List of extensions for SmsService
    List<RestcommExtensionGeneric> extensions;

    public SmppMessageHandler(final ServletContext servletContext) {
        this.servletContext = servletContext;
        this.storage = (DaoManager) servletContext.getAttribute(DaoManager.class.getName());
        this.configuration = (Configuration) servletContext.getAttribute(Configuration.class.getName());
        this.sipFactory = (SipFactory) servletContext.getAttribute(SipFactory.class.getName());
        this.monitoringService = (ActorRef) servletContext.getAttribute(MonitoringService.class.getName());
        //FIXME:Should new ExtensionType.SmppMessageHandler be defined?
        extensions = ExtensionController.getInstance().getExtensions(ExtensionType.SmsService);
        if (logger.isInfoEnabled()) {
            logger.info("SmsService extensions: "+(extensions != null ? extensions.size() : "0"));
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        final UntypedActorContext context = getContext();
        final ActorRef sender = sender();
        final ActorRef self = self();
        ExtensionController ec = ExtensionController.getInstance();
        if (message instanceof SmppInboundMessageEntity){
            if(logger.isInfoEnabled()) {
                logger.info("SmppMessageHandler processing Inbound Message " + message.toString());
            }
            inbound((SmppInboundMessageEntity) message);
        }else if(message instanceof SmppOutboundMessageEntity ){
            if(logger.isInfoEnabled()) {
                logger.info("SmppMessageHandler processing Outbound Message " + message.toString());
            }
            outbound((SmppOutboundMessageEntity) message);
        } else if (message instanceof CreateSmsSession) {
            IExtensionCreateSmsSessionRequest ier = (CreateSmsSession)message;
            ier.setConfiguration(this.configuration);
            ec.executePreOutboundAction(ier, this.extensions);
            if (ier.isAllowed()) {
                final ActorRef session = session(ier.getConfiguration());
                final SmsServiceResponse<ActorRef> response = new  SmsServiceResponse<ActorRef>(session);
                sender.tell(response, self);
            } else {
                final SmsServiceResponse<ActorRef> response = new SmsServiceResponse(new RestcommExtensionException("Now allowed to create SmsSession"));
                sender.tell(response, self());
            }
            ec.executePostOutboundAction(message, this.extensions);
        }else if (message instanceof DestroySmsSession) {
            final DestroySmsSession destroySmsSession = (DestroySmsSession) message;
            final ActorRef session = destroySmsSession.session();
            context.stop(session);
        }
    }

    private void inbound(final SmppInboundMessageEntity request ) throws IOException {
        final ActorRef self = self();

        String to = request.getSmppTo();
        final IncomingPhoneNumbersDao numbers = storage.getIncomingPhoneNumbersDao();
        IncomingPhoneNumber number = numbers.getIncomingPhoneNumber(to);

        if( redirectToHostedSmsApp(self,request, storage.getAccountsDao(), storage.getApplicationsDao(),to  )){
            if(logger.isInfoEnabled()) {
                logger.info("SMPP Message Accepted - A Restcomm Hosted App is Found for Number : " + number.getPhoneNumber() );
            }
            return;
        } else {
            logger.warning("SMPP Message Rejected : No Restcomm Hosted App Found for inbound number : " + to );
        }
    }

    private boolean redirectToHostedSmsApp(final ActorRef self, final SmppInboundMessageEntity request, final AccountsDao accounts,
                                           final ApplicationsDao applications, String id) throws IOException {
        boolean isFoundHostedApp = false;

        String to = request.getSmppTo();
        String phone = to;

        final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        try {
            phone = phoneNumberUtil.format(phoneNumberUtil.parse(to, "US"), PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (Exception e) {}
        // Try to find an application defined for the phone number.
        final IncomingPhoneNumbersDao numbers = storage.getIncomingPhoneNumbersDao();
        IncomingPhoneNumber number = numbers.getIncomingPhoneNumber(phone);
        if (number == null) {
            number = numbers.getIncomingPhoneNumber(to);
        }
        if (number == null) {
            // https://github.com/Mobicents/RestComm/issues/84 using wildcard as default application
            number = numbers.getIncomingPhoneNumber("*");
        }
        try {
            if (number != null) {
                ActorRef interpreter = null;

                URI appUri = number.getSmsUrl();

                final SmppInterpreterBuilder builder = new SmppInterpreterBuilder(system);
                builder.setSmsService(self);
                builder.setConfiguration(configuration);
                builder.setStorage(storage);
                builder.setAccount(number.getAccountSid());
                builder.setVersion(number.getApiVersion());
                final Sid sid = number.getSmsApplicationSid();
                if (sid != null) {
                    final Application application = applications.getApplication(sid);
                    builder.setUrl(UriUtils.resolve(application.getRcmlUrl()));
                } else if (appUri != null) {
                    builder.setUrl(UriUtils.resolve(appUri));
                } else {
                    logger.error("the matched number doesn't have SMS application attached, number: "+number.getPhoneNumber());
                    return false;
                }
                builder.setMethod(number.getSmsMethod());
                URI appFallbackUrl = number.getSmsFallbackUrl();
                if (appFallbackUrl != null) {
                    builder.setFallbackUrl(UriUtils.resolve(number.getSmsFallbackUrl()));
                    builder.setFallbackMethod(number.getSmsFallbackMethod());
                }
                interpreter = builder.build();
                Configuration cfg = this.configuration;
                //Extension
                final ActorRef session = session(cfg);
                session.tell(request, self);
                final StartInterpreter start = new StartInterpreter(session);
                interpreter.tell(start, self);
                isFoundHostedApp = true;

            }
        } catch (Exception e) {
            logger.error("Error processing inbound SMPP Message. There is no locally hosted Restcomm app for the number :" + e);
        }
        return isFoundHostedApp;
    }


    @SuppressWarnings("unchecked")
    private SipURI outboundInterface() {
        SipURI result = null;
        final List<SipURI> uris = (List<SipURI>) servletContext.getAttribute(SipServlet.OUTBOUND_INTERFACES);
        for (final SipURI uri : uris) {
            final String transport = uri.getTransportParam();
            if ("udp".equalsIgnoreCase(transport)) {
                result = uri;
            }
        }
        return result;
    }

    private ActorRef session(final Configuration p_configuration) {
        final Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public UntypedActor create() throws Exception {
                return new SmsSession(p_configuration, sipFactory, outboundInterface(), storage, monitoringService, servletContext);
            }
        });
        return system.actorOf(props);
    }

    public void outbound(SmppOutboundMessageEntity request) throws SmppInvalidArgumentException, IOException {
//        if(logger.isInfoEnabled()) {
//            logger.info("Message is Received by the SmppSessionOutbound Class");
//        }

        byte[] textBytes;
        int smppTonNpiValue =  Integer.parseInt(SmppService.getSmppTonNpiValue()) ;
        // add delivery receipt
        //submit0.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);
        SubmitSm submit0 = new SubmitSm();
        submit0.setSourceAddress(new Address((byte)smppTonNpiValue, (byte) smppTonNpiValue, request.getSmppFrom() ));
        submit0.setDestAddress(new Address((byte)smppTonNpiValue, (byte)smppTonNpiValue, request.getSmppTo()));
        if (CharsetUtil.CHARSET_UCS_2 == request.getSmppEncoding()) {
            submit0.setDataCoding(DataCoding.DATA_CODING_UCS2);
            textBytes = CharsetUtil.encode(request.getSmppContent(), CharsetUtil.CHARSET_UCS_2);
        } else {
            submit0.setDataCoding(DataCoding.DATA_CODING_GSM7);
            textBytes = CharsetUtil.encode(request.getSmppContent(), request.getSmppEncoding());
        }

        submit0.setShortMessage(textBytes);

        TlvSet tlvSet = request.getTlvSet();

        if(tlvSet!=null) {
            for (Tlv tlv : (Collection<Tlv>)tlvSet.getOptionalParameters()) {
                submit0.setOptionalParameter(tlv);
            }
        }else{
            if(logger.isInfoEnabled()) {
                logger.info("TlvSet is null");
            }
        }
        try {
            if(logger.isInfoEnabled()) {
                logger.info("Sending SubmitSM for " + request);
            }
            SmppClientOpsThread.getSmppSession().submit(submit0, 10000); //send message through SMPP connector
        } catch (RecoverablePduException | UnrecoverablePduException
                | SmppTimeoutException | SmppChannelException
                | InterruptedException e) {
            logger.error("SMPP message cannot be sent : " + e );
        }
    }
}
