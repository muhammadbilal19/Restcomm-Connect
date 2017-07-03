/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.restcomm.connect.mscontrol.api.messages;

import jain.protocol.ip.mgcp.message.parms.ConnectionMode;

import org.restcomm.connect.commons.annotations.concurrency.Immutable;

import akka.actor.ActorRef;
import org.restcomm.connect.commons.dao.Sid;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
@Immutable
public final class JoinCall {

    private final ActorRef call;
    private final ConnectionMode connectionMode;
    private final Sid sid;

    public JoinCall(final ActorRef call, final ConnectionMode connectionMode) {
        this(call, connectionMode, null);
    }

    public JoinCall(final ActorRef call, final ConnectionMode connectionMode, final Sid sid) {
        this.call = call;
        this.connectionMode = connectionMode;
        this.sid = sid;
    }

    public ActorRef getCall() {
        return call;
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public Sid getSid () {
        return sid;
    }
}
