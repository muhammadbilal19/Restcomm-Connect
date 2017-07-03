/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.restcomm.connect.mgcp;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.actor.UntypedActorFactory;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import jain.protocol.ip.mgcp.DeleteProviderException;
import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpListener;
import jain.protocol.ip.mgcp.JainMgcpProvider;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.JainMgcpStack;
import jain.protocol.ip.mgcp.message.Constants;
import jain.protocol.ip.mgcp.message.NotificationRequest;
import jain.protocol.ip.mgcp.message.Notify;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;
import jain.protocol.ip.mgcp.message.parms.NotifiedEntity;
import org.restcomm.connect.commons.util.RevolvingCounter;

import java.net.InetAddress;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
public final class MediaGateway extends UntypedActor implements JainMgcpListener {
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    // MediaGateway connection information.
    private String name;
    private InetAddress localIp;
    private int localPort;
    private InetAddress remoteIp;
    private int remotePort;
    // Used for NAT traversal.
    private boolean useNat;
    private InetAddress externalIp;
    // Used to detect dead media gateways.
    private long timeout;
    // JAIN MGCP stuff.
    private JainMgcpProvider provider;
    private JainMgcpStack stack;
    // Call agent.
    private NotifiedEntity agent;
    // Media gateway domain name.
    private String domain;
    // Message responseListeners.
    private Map<String, ActorRef> notificationListeners;
    private Map<Integer, ActorRef> responseListeners;
    // Runtime stuff.
    private RevolvingCounter requestIdPool;
    private RevolvingCounter sessionIdPool;
    private RevolvingCounter transactionIdPool;
    private ActorSystem system;

    public MediaGateway() {
        super();
        notificationListeners = new ConcurrentHashMap<String, ActorRef>();
        responseListeners = new ConcurrentHashMap<Integer, ActorRef>();
        system = context().system();
    }

    private ActorRef getConnection(final Object message) {
        final CreateConnection request = (CreateConnection) message;
        final MediaSession session = request.session();
        final ActorRef gateway = self();

        final Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public UntypedActor create() throws Exception {
                return new Connection(gateway, session, agent, timeout);
            }
        });
        return system.actorOf(props);
    }

    private ActorRef getBridgeEndpoint(final Object message) {
        final CreateBridgeEndpoint request = (CreateBridgeEndpoint) message;
        final ActorRef gateway = self();
        final MediaSession session = request.session();
        final String endpointName = request.endpointName();
        Props props = null;
        if(endpointName == null){
            props = new Props(new UntypedActorFactory() {
                private static final long serialVersionUID = 1L;

                @Override
                public Actor create() throws Exception {
                    return new BridgeEndpoint(gateway, session, agent, domain, timeout);
                }
            });
        }else{
            props = new Props(new UntypedActorFactory() {
                private static final long serialVersionUID = 1L;

                @Override
                public Actor create() throws Exception {
                    return new BridgeEndpoint(gateway, session, agent, domain, timeout, endpointName);
                }
            });
        }
        return system.actorOf(props);
    }

    private ActorRef getConferenceEndpoint(final Object message) {
        final ActorRef gateway = self();
        final CreateConferenceEndpoint request = (CreateConferenceEndpoint) message;
        final MediaSession session = request.session();
        final String endpointName = request.endpointName();
        Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public UntypedActor create() throws Exception {
                return new ConferenceEndpoint(gateway, session, agent, domain, timeout, endpointName);
            }
        });
        return system.actorOf(props);
    }

    private MediaGatewayInfo getInfo(final Object message) {
        return new MediaGatewayInfo(name, remoteIp, remotePort, useNat, externalIp);
    }

    private ActorRef getIvrEndpoint(final Object message) {
        final ActorRef gateway = self();
        final CreateIvrEndpoint request = (CreateIvrEndpoint) message;
        final MediaSession session = request.session();
        final String endpointName = request.endpointName();
        Props props = new Props(new UntypedActorFactory() {
                private static final long serialVersionUID = 1L;

                @Override
                public UntypedActor create() throws Exception {
                    return new IvrEndpoint(gateway, session, agent, domain, timeout, endpointName);
                }
            });
        return system.actorOf(props);
    }

    private ActorRef getLink(final Object message) {
        final CreateLink request = (CreateLink) message;
        final ActorRef gateway = self();
        final MediaSession session = request.session();
        final ConnectionIdentifier connectionIdentifier = request.connectionIdentifier();
        final Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public UntypedActor create() throws Exception {
                return new Link(gateway, session, agent, timeout, connectionIdentifier);
            }
        });
        return system.actorOf(props);
    }

    private ActorRef getPacketRelayEndpoint(final Object message) {
        final ActorRef gateway = self();
        final CreatePacketRelayEndpoint request = (CreatePacketRelayEndpoint) message;
        final MediaSession session = request.session();
        final Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public UntypedActor create() throws Exception {
                return new PacketRelayEndpoint(gateway, session, agent, domain, timeout);
            }
        });
        return system.actorOf(props);
    }

    private MediaSession getSession() {
        MediaSession s=null;
        try{
            s = new MediaSession((int) sessionIdPool.get());
        }catch(Exception e){
            logger.error("Exception in getSession: "+e.getMessage() +" is MediaGateway actor terminated? "+self().isTerminated() +" current object snapshot: \n"+this.toString(), e);
        }
        return s;
    }

    private void powerOff(final Object message) {
        // Clean up the JAIN MGCP provider.
        try {
            provider.removeJainMgcpListener(this);
            stack.deleteProvider(provider);
        } catch (final DeleteProviderException exception) {
            logger.error(exception, "Could not clean up the JAIN MGCP provider.");
        }
        // Make sure we don't leave anything behind.
        name = null;
        localIp = null;
        localPort = 0;
        remoteIp = null;
        remotePort = 0;
        useNat = false;
        externalIp = null;
        timeout = 0;
        provider = null;
        stack = null;
        agent = null;
        domain = null;
        responseListeners.clear();
        responseListeners = null;
        requestIdPool = null;
        sessionIdPool = null;
        transactionIdPool = null;
    }

    private void powerOn(final Object message) {
        final PowerOnMediaGateway request = (PowerOnMediaGateway) message;
        name = request.getName();
        localIp = request.getLocalIp();
        localPort = request.getLocalPort();
        remoteIp = request.getRemoteIp();
        remotePort = request.getRemotePort();
        useNat = request.useNat();
        externalIp = request.getExternalIp();
        timeout = request.getTimeout();
        stack = request.getStack();
        provider = request.getProvider();
        //stack = new JainMgcpStackImpl(localIp, localPort);
        try {
            //provider = stack.createProvider();
            provider.addJainMgcpListener(this);
        } catch (final TooManyListenersException exception) {
        //} catch (final CreateProviderException exception) {
            logger.error(exception, "Could not create a JAIN MGCP provider.");
        }
        agent = new NotifiedEntity("restcomm", localIp.getHostAddress(), localPort);
        domain = new StringBuilder().append(remoteIp.getHostAddress()).append(":").append(remotePort).toString();
        notificationListeners.clear();
        responseListeners.clear();
        requestIdPool = new RevolvingCounter(1, Long.MAX_VALUE);
        sessionIdPool = new RevolvingCounter(1, Long.MAX_VALUE);
        transactionIdPool = new RevolvingCounter(1, Long.MAX_VALUE);
    }

    @Override
    public void processMgcpCommandEvent(final JainMgcpCommandEvent event) {
        final int value = event.getObjectIdentifier();
        switch (value) {
            case Constants.CMD_NOTIFY: {
                final Notify notify = (Notify) event;
                final String id = notify.getRequestIdentifier().toString();
                final ActorRef listener = notificationListeners.remove(id);
                if (listener != null) {
                    listener.tell(notify, self());
                }
            }
        }
    }

    @Override
    public void processMgcpResponseEvent(final JainMgcpResponseEvent event) {
        final int id = event.getTransactionHandle();
        final ActorRef listener = responseListeners.remove(id);
        if (listener != null) {
            listener.tell(event, self());
        }
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        final UntypedActorContext context = getContext();
        final Class<?> klass = message.getClass();
        final ActorRef self = self();
        final ActorRef sender = sender();

        if (logger.isDebugEnabled()){
            logger.debug("MediaGateway onReceive. self.isTerminated: "+self.isTerminated()+" | Processing "+klass.getName()+" | object snapshot: \n"+this.toString());
        }
        if(self.isTerminated())
            logger.error("MediaGateway is Terminated.");
        if (PowerOnMediaGateway.class.equals(klass)) {
            powerOn(message);
        } else if (PowerOffMediaGateway.class.equals(klass)) {
            powerOff(message);
        } else if (GetMediaGatewayInfo.class.equals(klass)) {
            sender.tell(new MediaGatewayResponse<MediaGatewayInfo>(getInfo(message)), sender);
        } else if (CreateConnection.class.equals(klass)) {
            sender.tell(new MediaGatewayResponse<ActorRef>(getConnection(message)), self);
        } else if (CreateLink.class.equals(klass)) {
            sender.tell(new MediaGatewayResponse<ActorRef>(getLink(message)), self);
        } else if (CreateMediaSession.class.equals(klass)) {
            sender.tell(new MediaGatewayResponse<MediaSession>(getSession()), self);
        } else if (CreateBridgeEndpoint.class.equals(klass)) {
            final ActorRef endpoint = getBridgeEndpoint(message);
            sender.tell(new MediaGatewayResponse<ActorRef>(endpoint), self);
        } else if (CreatePacketRelayEndpoint.class.equals(klass)) {
            final ActorRef endpoint = getPacketRelayEndpoint(message);
            sender.tell(new MediaGatewayResponse<ActorRef>(endpoint), self);
        } else if (CreateIvrEndpoint.class.equals(klass)) {
            final ActorRef endpoint = getIvrEndpoint(message);
            sender.tell(new MediaGatewayResponse<ActorRef>(endpoint), self);
        } else if (CreateConferenceEndpoint.class.equals(klass)) {
            final ActorRef endpoint = getConferenceEndpoint(message);
            sender.tell(new MediaGatewayResponse<ActorRef>(endpoint), self);
        } else if (DestroyConnection.class.equals(klass)) {
            final DestroyConnection request = (DestroyConnection) message;
            if (request.connection() != null)
                context.stop(request.connection());
        } else if (DestroyLink.class.equals(klass)) {
            final DestroyLink request = (DestroyLink) message;
            context.stop(request.link());
        } else if (DestroyEndpoint.class.equals(klass)) {
            final DestroyEndpoint request = (DestroyEndpoint) message;
            if (logger.isInfoEnabled())
                logger.info("Gateway: "+self().path()+" about to stop endpoint path: "+request.endpoint().path()+" isTerminated: "+request.endpoint().isTerminated()+" sender: "+sender().path());
            while (notificationListeners.containsValue(request.endpoint())) {
                notificationListeners.values().remove(request.endpoint());
            }
            context.stop(request.endpoint());
        } else if (message instanceof JainMgcpCommandEvent) {
            send(message, sender);
        } else if (message instanceof JainMgcpResponseEvent) {
            send(message);
        }
    }

    private void send(final Object message, final ActorRef sender) {
        final JainMgcpCommandEvent command = (JainMgcpCommandEvent) message;
        final int transactionId = (int) transactionIdPool.get();
        command.setTransactionHandle(transactionId);
        responseListeners.put(transactionId, sender);
        if (NotificationRequest.class.equals(command.getClass())) {
            final NotificationRequest request = (NotificationRequest) command;
            final String id = Long.toString(requestIdPool.get());
            request.getRequestIdentifier().setRequestIdentifier(id);
            notificationListeners.put(id, sender);
        }
        provider.sendMgcpEvents(new JainMgcpEvent[] { command });
    }

    private void send(final Object message) {
        final JainMgcpResponseEvent response = (JainMgcpResponseEvent) message;
        provider.sendMgcpEvents(new JainMgcpEvent[] { response });
    }

    @Override
    public String toString() {
        return "MediaGateway [logger=" + logger + ", name=" + name + ", localIp=" + localIp + ", localPort=" + localPort
                + ", remoteIp=" + remoteIp + ", remotePort=" + remotePort + ", useNat=" + useNat + ", externalIp="
                + externalIp + ", timeout=" + timeout + ", provider=" + provider + ", stack=" + stack + ", agent="
                + agent + ", domain=" + domain + ", notificationListeners=" + notificationListeners
                + ", responseListeners=" + responseListeners + ", requestIdPool=" + requestIdPool + ", sessionIdPool="
                + sessionIdPool + ", transactionIdPool=" + transactionIdPool + "]";
    }

    @Override
    public void postStop() {
        if (logger.isDebugEnabled()){
            logger.debug("MediaGateway at postStop, here is object snapshot: \n"+this.toString());
        }
    }
}
