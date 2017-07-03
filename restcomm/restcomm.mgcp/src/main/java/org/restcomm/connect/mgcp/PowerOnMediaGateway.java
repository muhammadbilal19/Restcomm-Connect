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

import jain.protocol.ip.mgcp.JainMgcpProvider;
import jain.protocol.ip.mgcp.JainMgcpStack;
import org.restcomm.connect.commons.annotations.concurrency.Immutable;

import java.net.InetAddress;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Immutable
public final class PowerOnMediaGateway {
    // MediaGateway connection information.
    private final String name;
    private final InetAddress localIp;
    private final int localPort;
    private final InetAddress remoteIp;
    private final int remotePort;
    // Used for NAT traversal.
    private final boolean useNat;
    private final InetAddress externalIp;
    // Used to detect dead media gateways.
    private final long timeout;
    private final JainMgcpStack stack;
    private final JainMgcpProvider provider;

    public PowerOnMediaGateway(final String name, final InetAddress localIp, final int localPort, final InetAddress remoteIp,
            final int remotePort, final boolean useNat, final InetAddress externalIp, final long timeout, final JainMgcpStack stack,
                               final JainMgcpProvider provider) {
        super();
        this.name = name;
        this.localIp = localIp;
        this.localPort = localPort;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.useNat = useNat;
        this.externalIp = externalIp;
        this.timeout = timeout;
        this.stack = stack;
        this.provider = provider;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public InetAddress getLocalIp() {
        return localIp;
    }

    public int getLocalPort() {
        return localPort;
    }

    public InetAddress getRemoteIp() {
        return remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public boolean useNat() {
        return useNat;
    }

    public InetAddress getExternalIp() {
        return externalIp;
    }

    public long getTimeout() {
        return timeout;
    }

    public JainMgcpStack getStack() {
        return stack;
    }

    public JainMgcpProvider getProvider() {
        return provider;
    }

    public static final class Builder {
        private String name;
        private InetAddress localIp;
        private int localPort;
        private InetAddress remoteIp;
        private int remotePort;
        private boolean useNat;
        private InetAddress externalIp;
        private long timeout;
        private JainMgcpStack stack;
        private JainMgcpProvider provider;

        private Builder() {
            super();
        }

        public PowerOnMediaGateway build() {
            return new PowerOnMediaGateway(name, localIp, localPort, remoteIp, remotePort, useNat, externalIp, timeout, stack, provider);
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setLocalIP(final InetAddress localIp) {
            this.localIp = localIp;
        }

        public void setLocalPort(final int localPort) {
            this.localPort = localPort;
        }

        public void setRemoteIP(final InetAddress remoteIp) {
            this.remoteIp = remoteIp;
        }

        public void setRemotePort(final int remotePort) {
            this.remotePort = remotePort;
        }

        public void setUseNat(final boolean useNat) {
            this.useNat = useNat;
        }

        public void setExternalIP(final InetAddress externalIp) {
            this.externalIp = externalIp;
        }

        public void setTimeout(final long timeout) {
            this.timeout = timeout;
        }

        public void setStack(JainMgcpStack stack) {
            this.stack = stack;
        }

        public void setProvider(JainMgcpProvider provider) {
            this.provider = provider;
        }
    }
}
