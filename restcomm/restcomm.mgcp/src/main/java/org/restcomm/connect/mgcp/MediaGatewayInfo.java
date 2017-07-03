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

import java.net.InetAddress;

import org.restcomm.connect.commons.annotations.concurrency.Immutable;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Immutable
public final class MediaGatewayInfo {
    private final String name;
    // Server Info.
    final InetAddress ip;
    final int port;
    // Used for NAT traversal.
    private final boolean useNat;
    private final InetAddress externalIp;

    public MediaGatewayInfo(final String name, final InetAddress ip, final int port, final boolean useNat,
            final InetAddress externalIp) {
        super();
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.useNat = useNat;
        this.externalIp = externalIp;
    }

    public String name() {
        return name;
    }

    public InetAddress ip() {
        return ip;
    }

    public int port() {
        return port;
    }

    public boolean useNat() {
        return useNat;
    }

    public InetAddress externalIP() {
        return externalIp;
    }
}
