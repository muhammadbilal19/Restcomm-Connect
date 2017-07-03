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

import jain.protocol.ip.mgcp.message.parms.ConnectionDescriptor;
import jain.protocol.ip.mgcp.message.parms.ConnectionMode;

import org.restcomm.connect.commons.annotations.concurrency.Immutable;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Immutable
public final class OpenConnection {
    private final ConnectionDescriptor descriptor;
    private final ConnectionMode mode;
    private final boolean webrtc;

    public OpenConnection(final ConnectionDescriptor descriptor, final ConnectionMode mode, final boolean webrtc) {
        super();
        this.descriptor = descriptor;
        this.mode = mode;
        this.webrtc = webrtc;
    }

    public OpenConnection(final ConnectionDescriptor descriptor, final ConnectionMode mode) {
        this(descriptor, mode, false);
    }

    public OpenConnection(final ConnectionMode mode, final boolean webrtc) {
        this(null, mode, webrtc);
    }

    public ConnectionDescriptor descriptor() {
        return descriptor;
    }

    public ConnectionMode mode() {
        return mode;
    }

    public boolean isWebrtc() {
        return webrtc;
    }

}
