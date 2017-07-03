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
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.dao.entities.Transcription;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class TranscriptionConverter extends AbstractConverter implements JsonSerializer<Transcription> {
    public TranscriptionConverter(final Configuration configuration) {
        super(configuration);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return Transcription.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Transcription transcription = (Transcription) object;
        writer.startNode("Transcription");
        writeSid(transcription.getSid(), writer);
        writeDateCreated(transcription.getDateCreated(), writer);
        writeDateUpdated(transcription.getDateUpdated(), writer);
        writeAccountSid(transcription.getAccountSid(), writer);
        writeStatus(transcription.getStatus().toString(), writer);
        writeRecordingSid(transcription.getRecordingSid(), writer);
        writeDuration(transcription.getDuration(), writer);
        writeTranscriptionText(transcription.getTranscriptionText(), writer);
        writePrice(transcription.getPrice(), writer);
        writePriceUnit(transcription.getPriceUnit(), writer);
        writeUri(transcription.getUri(), writer);
        writer.endNode();
    }

    @Override
    public JsonElement serialize(final Transcription transcription, final Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeSid(transcription.getSid(), object);
        writeDateCreated(transcription.getDateCreated(), object);
        writeDateUpdated(transcription.getDateUpdated(), object);
        writeAccountSid(transcription.getAccountSid(), object);
        writeStatus(transcription.getStatus().toString(), object);
        writeRecordingSid(transcription.getRecordingSid(), object);
        writeDuration(transcription.getDuration(), object);
        writeTranscriptionText(transcription.getTranscriptionText(), object);
        writePrice(transcription.getPrice(), object);
        writePriceUnit(transcription.getPriceUnit(), object);
        writeUri(transcription.getUri(), object);
        return object;
    }

    private void writeRecordingSid(final Sid recordingSid, final HierarchicalStreamWriter writer) {
        writer.startNode("RecordingSid");
        writer.setValue(recordingSid.toString());
        writer.endNode();
    }

    private void writeRecordingSid(final Sid recordingSid, final JsonObject object) {
        object.addProperty("recording_sid", recordingSid.toString());
    }

    private void writeTranscriptionText(final String transcriptionText, final HierarchicalStreamWriter writer) {
        writer.startNode("TranscriptionText");
        if (transcriptionText != null) {
            writer.setValue(transcriptionText);
        }
        writer.endNode();
    }

    private void writeTranscriptionText(final String transcriptionText, final JsonObject object) {
        if (transcriptionText != null) {
            object.addProperty("transcription_text", transcriptionText);
        } else {
            object.add("transcription_text", JsonNull.INSTANCE);
        }
    }
}
