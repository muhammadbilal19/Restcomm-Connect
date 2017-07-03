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
package org.restcomm.connect.http.converter;

import java.lang.reflect.Type;

import org.apache.commons.configuration.Configuration;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;
import org.restcomm.connect.dao.entities.Gateway;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class GatewayConverter extends AbstractConverter implements JsonSerializer<Gateway> {
    public GatewayConverter(final Configuration configuration) {
        super(configuration);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return Gateway.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Gateway gateway = (Gateway) object;
        writer.startNode("Gateway");
        writeSid(gateway.getSid(), writer);
        writeDateCreated(gateway.getDateCreated(), writer);
        writeDateUpdated(gateway.getDateUpdated(), writer);
        writeFriendlyName(gateway.getFriendlyName(), writer);
        writePassword(gateway.getPassword(), writer);
        writeProxy(gateway.getProxy(), writer);
        writeRegister(gateway.register(), writer);
        writeUserName(gateway.getUserName(), writer);
        writeTimeToLive(gateway.getTimeToLive(), writer);
        writeUri(gateway.getUri(), writer);
        writer.endNode();
    }

    @Override
    public JsonElement serialize(final Gateway gateway, final Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeSid(gateway.getSid(), object);
        writeDateCreated(gateway.getDateCreated(), object);
        writeDateUpdated(gateway.getDateUpdated(), object);
        writeFriendlyName(gateway.getFriendlyName(), object);
        writePassword(gateway.getPassword(), object);
        writeProxy(gateway.getProxy(), object);
        writeRegister(gateway.register(), object);
        writeUserName(gateway.getUserName(), object);
        writeTimeToLive(gateway.getTimeToLive(), object);
        writeUri(gateway.getUri(), object);
        return object;
    }

    private void writePassword(final String password, final HierarchicalStreamWriter writer) {
        writer.startNode("Password");
        writer.setValue(password);
        writer.endNode();
    }

    private void writePassword(final String password, final JsonObject object) {
        object.addProperty("password", password);
    }

    private void writeProxy(final String proxy, final HierarchicalStreamWriter writer) {
        writer.startNode("Proxy");
        writer.setValue(proxy);
        writer.endNode();
    }

    private void writeProxy(final String proxy, final JsonObject object) {
        object.addProperty("proxy", proxy);
    }

    private void writeRegister(final boolean register, final HierarchicalStreamWriter writer) {
        writer.startNode("Register");
        writer.setValue(Boolean.toString(register));
        writer.endNode();
    }

    private void writeRegister(final boolean register, final JsonObject object) {
        object.addProperty("register", register);
    }
}
