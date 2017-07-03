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
package org.restcomm.connect.sms;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.restcomm.connect.dao.DaoManager;
import org.restcomm.connect.sms.smpp.SmppMessageHandler;
import org.restcomm.connect.sms.smpp.SmppService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import java.io.IOException;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */
public final class SmsServiceProxy extends SipServlet implements SipServletListener {
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(SmsServiceProxy.class);

    private ActorSystem system;
    private ActorRef service;
    private ActorRef smppService;
    private ActorRef smppMessageHandler;
    private ServletContext context;

    public SmsServiceProxy() {
        super();
    }

    @Override
    protected void doRequest(final SipServletRequest request) throws ServletException, IOException {
        service.tell(request, null);
    }

    @Override
    protected void doResponse(final SipServletResponse response) throws ServletException, IOException {
        service.tell(response, null);
    }


    private ActorRef service(final Configuration configuration, final SipFactory factory, final DaoManager storage) {
        final Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;
            @Override
            public UntypedActor create() throws Exception {
                return new SmsService(configuration, factory, storage, context);
            }
        });
        return system.actorOf(props);
    }

    private ActorRef smppService(final Configuration configuration, final SipFactory factory, final DaoManager storage,
                                 final ServletContext context, final ActorRef smppMessageHandler) {
        final Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;
            @Override
            public UntypedActor create() throws Exception {
                return new SmppService(system, configuration, factory, storage, context, smppMessageHandler);
            }
        });
        return system.actorOf(props);
    }

    private ActorRef smppMessageHandler () {
        final Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;
            @Override
            public UntypedActor create() throws Exception {
                return new SmppMessageHandler(context);
            }
        });
        return system.actorOf(props);
    }

    @Override
    public void servletInitialized(SipServletContextEvent event) {
        if (event.getSipServlet().getClass().equals(SmsServiceProxy.class)) {
            context = event.getServletContext();
            final SipFactory factory = (SipFactory) context.getAttribute(SIP_FACTORY);
            Configuration configuration = (Configuration) context.getAttribute(Configuration.class.getName());
            final DaoManager storage = (DaoManager) context.getAttribute(DaoManager.class.getName());
            system = (ActorSystem) context.getAttribute(ActorSystem.class.getName());
            service = service(configuration, factory, storage);
            context.setAttribute(SmsService.class.getName(), service);
            if (configuration.subset("smpp").getString("[@activateSmppConnection]", "false").equalsIgnoreCase("true")) {
                if(logger.isInfoEnabled()) {
                    logger.info("Will initialize SMPP");
                }
                smppMessageHandler = smppMessageHandler();
                smppService = smppService(configuration,factory,storage,context, smppMessageHandler);
                context.setAttribute(SmppService.class.getName(), smppService);
                context.setAttribute(SmppMessageHandler.class.getName(), smppMessageHandler);
            }
        }
    }
}
