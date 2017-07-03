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
package org.restcomm.connect.provisioning.number.api;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
public class ProvisionProvider {
    public static enum PROVIDER {VOIPINNOVATIONS,BANDWIDTH, UNKNOWN};
    public static enum REQUEST_TYPE {PING, GETDIDS, ASSIGNDID, QUERYDID, RELEASEDID};
    public static String voipinnovationsClass = "org.restcomm.connect.provisioning.number.vi.VoIPInnovationsNumberProvisioningManager";
    public static String bandiwidthClass = "org.restcomm.connect.provisioning.number.bandwidth.BandwidthNumberProvisioningManager";
}
