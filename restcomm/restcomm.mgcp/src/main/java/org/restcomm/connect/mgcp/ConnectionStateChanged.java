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

import org.restcomm.connect.commons.annotations.concurrency.Immutable;

import jain.protocol.ip.mgcp.message.parms.ConnectionDescriptor;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Immutable
public final class ConnectionStateChanged {
    public static enum State {
        CLOSED, HALF_OPEN, OPEN
    };

    private final ConnectionDescriptor descriptor;
    private final State state;
    private ConnectionIdentifier connectionIdentifier;

    public ConnectionStateChanged(final ConnectionDescriptor descriptor, final State state) {
        this(descriptor,state,null);
    }

    public ConnectionStateChanged(final ConnectionDescriptor descriptor, final State state, final ConnectionIdentifier connectionIdentifier) {
        super();
        this.descriptor = descriptor;
        this.state = state;
        this.connectionIdentifier = connectionIdentifier;
    }

    public ConnectionStateChanged(final State state) {
        this(null, state);
    }

    public ConnectionStateChanged(final State state, final ConnectionIdentifier connectionIdentifier) {
        this(null, state, connectionIdentifier);
    }

    public ConnectionDescriptor descriptor() {
        return descriptor;
    }

    public State state() {
        return state;
    }

    public ConnectionIdentifier connectionIdentifier() {
        return connectionIdentifier;
    }
}
