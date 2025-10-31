package com.github.jonasmelchior.service;

import io.quarkiverse.diameter.DiameterConfig;
import io.quarkiverse.diameter.DiameterService;
import io.quarkiverse.diameter.DiameterServiceOptions;
import io.quarkus.logging.Log;
import org.apache.commons.codec.binary.Hex;
import org.jdiameter.api.*;
import org.jdiameter.api.app.*;
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.ClientS6aSessionListener;
import org.jdiameter.api.s6a.events.*;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.client.impl.app.s6a.ClientS6aSessionDataLocalImpl;
import org.jdiameter.client.impl.parser.MessageImpl;
import org.jdiameter.client.impl.parser.MessageParser;
import org.jdiameter.common.impl.DiameterUtilities;
import org.jdiameter.common.impl.app.cca.JCreditControlRequestImpl;
import org.jdiameter.common.impl.app.s6a.JAuthenticationInformationRequestImpl;
import org.jdiameter.common.impl.validation.MessageRepresentationImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@DiameterServiceOptions(timeOut = 3000)
@DiameterService
public class S6aClient implements ClientS6aSessionListener {

    @DiameterConfig
    Stack stack;

    public Stack getStack() {
        return stack;
    }

    @Override
    public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doCancelLocationRequestEvent(ClientS6aSession session, JCancelLocationRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        Log.info("Received CLR");
        DiameterUtilities.printMessage(request.getMessage());
    }

    @Override
    public void doInsertSubscriberDataRequestEvent(ClientS6aSession session, JInsertSubscriberDataRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doDeleteSubscriberDataRequestEvent(ClientS6aSession session, JDeleteSubscriberDataRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doResetRequestEvent(ClientS6aSession session, JResetRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    }

    @Override
    public void doAuthenticationInformationAnswerEvent(ClientS6aSession session, JAuthenticationInformationRequest request, JAuthenticationInformationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        Log.info("AIA Received");
        DiameterUtilities.printMessage(answer.getMessage());
    }

    public void sendTestAuthenticationInformationRequest() throws IllegalDiameterStateException, InternalException, RouteException, OverloadException, AvpDataException {
        ISessionFactory sessionFactory = (ISessionFactory) stack.getSessionFactory();
        long vendorId = stack.getMetaData().getLocalPeer().getVendorId();
        ApplicationId application = ApplicationId.createByAuthAppId(vendorId, 16777251);
        ClientS6aSession clientS6aSession = stack.getSessionFactory().getNewAppSession(null, application, ClientS6aSession.class);

        MessageParser messageParser = new MessageParser();
        JAuthenticationInformationRequest jAuthenticationInformationRequest = new JAuthenticationInformationRequest() {
            @Override
            public String getDestinationHost() throws AvpDataException {
                return "localhost";
            }

            @Override
            public String getDestinationRealm() throws AvpDataException {
                return "server.jdiameter.com";
            }

            @Override
            public int getCommandCode() {
                return 318;
            }

            @Override
            public Message getMessage() throws InternalException {
                Message message = messageParser.createEmptyMessage(318, 16777251);
                message.getApplicationIdAvps().add(ApplicationId.createByAuthAppId(vendorId, 16777251));
                message.setRequest(true);
                message.getAvps().addAvp(1, "310150123456789", false);
                message.getAvps().addAvp(1407, "32F810", true);
                message.getAvps().addAvp(Avp.DESTINATION_REALM, "server.test.com", false);
                message.getAvps().addAvp(Avp.DESTINATION_HOST, "ocs.demo.org", false);
                message.getAvps().addAvp(Avp.ORIGIN_REALM, "client.test.com", false);
                message.getAvps().addAvp(Avp.ORIGIN_HOST, "ocsclient", false);

                Log.info("Vendor ID: " + vendorId);
                Log.info("AIR Message:");
                DiameterUtilities.printMessage(message);

                return message;
            }

            @Override
            public String getOriginHost() throws AvpDataException {
                return "localhost";
            }

            @Override
            public String getOriginRealm() throws AvpDataException {
                return "client.jdiameter.com";
            }


        };

        for (ApplicationId applicationId : jAuthenticationInformationRequest.getMessage().getApplicationIdAvps()) {
            Log.info(applicationId.getAuthAppId());
            Log.info(applicationId.getVendorId());

        }

        System.out.println(jAuthenticationInformationRequest.getDestinationRealm());
        clientS6aSession.sendAuthenticationInformationRequest(jAuthenticationInformationRequest);
    }

    public void sendTestUpdateLocationRequest(String originHost) throws Exception {
        long vendorId = stack.getMetaData().getLocalPeer().getVendorId();
        ApplicationId application = ApplicationId.createByAuthAppId(vendorId, 16777251); // S6a application ID

        ClientS6aSession clientS6aSession = stack.getSessionFactory().getNewAppSession(null, application, ClientS6aSession.class);

        MessageParser messageParser = new MessageParser();

        JUpdateLocationRequest ulr = new JUpdateLocationRequest() {
            @Override
            public String getDestinationHost() {
                return "localhost";
            }

            @Override
            public String getDestinationRealm() {
                return "server.jdiameter.com";
            }

            @Override
            public int getCommandCode() {
                return 316; // ULR command code
            }

            @Override
            public Message getMessage() {
                Message message = messageParser.createEmptyMessage(316, 16777251); // ULR
                message.getApplicationIdAvps().add(ApplicationId.createByAuthAppId(vendorId, 16777251));
                message.setRequest(true);


                // --- Required AVPs ---
                message.getAvps().addAvp(Avp.USER_NAME, "310150123456789", false);  // IMSI
                message.getAvps().addAvp(Avp.DESTINATION_REALM, "server.test.com", false);
                message.getAvps().addAvp(Avp.DESTINATION_HOST, "ocs.demo.org", false);
                message.getAvps().addAvp(Avp.ORIGIN_REALM, "client.test.com", false);
                message.getAvps().addAvp(Avp.ORIGIN_HOST, originHost, false);
                message.getAvps().addAvp(Avp.VENDOR_ID, vendorId, false);

                Log.info("ULR Message:");
                DiameterUtilities.printMessage(message);

                return message;
            }

            @Override
            public String getOriginHost() {
                return originHost;
            }

            @Override
            public String getOriginRealm() {
                return "client.jdiameter.com";
            }
        };

        // --- Optional: log application IDs ---
        for (ApplicationId applicationId : ulr.getMessage().getApplicationIdAvps()) {
            Log.info("Auth App ID: " + applicationId.getAuthAppId());
            Log.info("Vendor ID: " + applicationId.getVendorId());
        }

        System.out.println("Destination Realm: " + ulr.getDestinationRealm());

        // --- Send the request ---
        clientS6aSession.sendUpdateLocationRequest(ulr);
    }



    @Override
    public void doPurgeUEAnswerEvent(ClientS6aSession session, JPurgeUERequest request, JPurgeUEAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doUpdateLocationAnswerEvent(ClientS6aSession session, JUpdateLocationRequest request, JUpdateLocationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        Log.info("ULA Received");
        DiameterUtilities.printMessage(answer.getMessage());
    }

    @Override
    public void doNotifyAnswerEvent(ClientS6aSession session, JNotifyRequest request, JNotifyAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }
}
