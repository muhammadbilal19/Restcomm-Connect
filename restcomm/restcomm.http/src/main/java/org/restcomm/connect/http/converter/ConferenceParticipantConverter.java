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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.configuration.Configuration;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.dao.entities.CallDetailRecord;

import java.lang.reflect.Type;

/**
 * @author maria-farooq@live.com.com (Maria Farooq)
 */
@ThreadSafe
public final class ConferenceParticipantConverter extends AbstractConverter implements JsonSerializer<CallDetailRecord> {
    private final String apiVersion;

    public ConferenceParticipantConverter(final Configuration configuration) {
        super(configuration);
        apiVersion = configuration.getString("api-version");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return CallDetailRecord.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final CallDetailRecord cdr = (CallDetailRecord) object;
        writer.startNode("Call");
        writeSid(cdr.getSid(), writer);
        writeConferenceSid(cdr.getConferenceSid(), writer);
        writeDateCreated(cdr.getDateCreated(), writer);
        writeDateUpdated(cdr.getDateUpdated(), writer);
        writeAccountSid(cdr.getAccountSid(), writer);
        writeMuted(cdr.isMuted(), writer);
        writeHold(cdr.isOnHold(), writer);
        writeStartConferenceOnEnter(cdr.isStartConferenceOnEnter(), writer);
        writeEndConferenceOnEnter(cdr.isEndConferenceOnExit(), writer);
        writeUri(cdr.getUri(), writer);
        writer.endNode();
    }

    private String prefix(final CallDetailRecord cdr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("/").append(apiVersion).append("/Accounts/");
        buffer.append(cdr.getAccountSid().toString()).append("/Calls/");
        buffer.append(cdr.getSid());
        return buffer.toString();
    }

    @Override
    public JsonElement serialize(final CallDetailRecord cdr, Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeSid(cdr.getSid(), object);
        writeConferenceSid(cdr.getParentCallSid(), object);
        writeDateCreated(cdr.getDateCreated(), object);
        writeDateUpdated(cdr.getDateUpdated(), object);
        writeAccountSid(cdr.getAccountSid(), object);
        writeMuted(cdr.isMuted(), object);
        writeHold(cdr.isOnHold(), object);
        writeStartConferenceOnEnter(cdr.isStartConferenceOnEnter(), object);
        writeEndConferenceOnEnter(cdr.isEndConferenceOnExit(), object);
        writeUri(cdr.getUri(), object);
        return object;
    }

    private void writeMuted(final Boolean muted, final HierarchicalStreamWriter writer) {
        writer.startNode("Muted");
        if (muted != null) {
            writer.setValue(muted.toString());
        }
        writer.endNode();
    }

    private void writeMuted(final Boolean muted, final JsonObject object) {
        object.addProperty("muted", muted);
    }

    private void writeHold(final Boolean hold, final HierarchicalStreamWriter writer) {
        writer.startNode("Hold");
        if (hold != null) {
            writer.setValue(hold.toString());
        }
        writer.endNode();
    }

    private void writeHold(final Boolean hold, final JsonObject object) {
        object.addProperty("hold", hold);
    }

    private void writeStartConferenceOnEnter(final Boolean startConferenceOnEnter, final HierarchicalStreamWriter writer) {
        writer.startNode("StartConferenceOnEnter");
        if (startConferenceOnEnter != null) {
            writer.setValue(startConferenceOnEnter.toString());
        }
        writer.endNode();
    }

    private void writeStartConferenceOnEnter(final Boolean startConferenceOnEnter, final JsonObject object) {
        object.addProperty("start_conference_on_enter", startConferenceOnEnter);
    }

    private void writeEndConferenceOnEnter(final Boolean endConferenceOnEnter, final HierarchicalStreamWriter writer) {
        writer.startNode("EndConferenceOnEnter");
        if (endConferenceOnEnter != null) {
            writer.setValue(endConferenceOnEnter.toString());
        }
        writer.endNode();
    }

    private void writeEndConferenceOnEnter(final Boolean endConferenceOnEnter, final JsonObject object) {
        object.addProperty("end_conference_on_enter", endConferenceOnEnter);
    }

    private void writeConferenceSid(final Sid sid, final HierarchicalStreamWriter writer) {
        writer.startNode("ConferenceSid");
        if (sid != null) {
            writer.setValue(sid.toString());
        }
        writer.endNode();
    }

    private void writeConferenceSid(final Sid sid, final JsonObject object) {
        if (sid != null) {
            object.addProperty("conference_sid", sid.toString());
        }
    }
}
