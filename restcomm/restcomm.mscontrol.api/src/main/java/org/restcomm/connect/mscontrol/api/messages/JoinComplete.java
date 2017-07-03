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
package org.restcomm.connect.mscontrol.api.messages;

import org.restcomm.connect.commons.annotations.concurrency.Immutable;

import akka.actor.ActorRef;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Immutable
public final class JoinComplete {

    private final ActorRef endpoint;
    private int sessionid;
    private ConnectionIdentifier connectionIdentifier;

    public JoinComplete() {
        this(null);
    }

    public JoinComplete(final ActorRef endpoint) {
        this.endpoint = endpoint;
    }

    public JoinComplete(ActorRef bridgeEndpoint, int sessionid) {
        this(bridgeEndpoint, sessionid, null);
    }

    public JoinComplete(ActorRef bridgeEndpoint, int sessionid, ConnectionIdentifier connectionIdentifier) {
        super();
        this.endpoint = bridgeEndpoint;
        this.sessionid = sessionid;
        this.connectionIdentifier = connectionIdentifier;
    }

    public Object endpoint() {
        return endpoint;
    }

    public int sessionid() {
        return sessionid;
    }

    public ConnectionIdentifier connectionIdentifier() {
        return connectionIdentifier;
    }
}
