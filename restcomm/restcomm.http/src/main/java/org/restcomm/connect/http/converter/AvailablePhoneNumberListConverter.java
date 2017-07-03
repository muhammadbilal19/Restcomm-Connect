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

import org.apache.commons.configuration.Configuration;
import org.restcomm.connect.dao.entities.AvailablePhoneNumber;
import org.restcomm.connect.dao.entities.AvailablePhoneNumberList;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
public final class AvailablePhoneNumberListConverter extends AbstractConverter {
    public AvailablePhoneNumberListConverter(final Configuration configuration) {
        super(configuration);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return AvailablePhoneNumberList.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final AvailablePhoneNumberList list = (AvailablePhoneNumberList) object;
        writer.startNode("AvailablePhoneNumbers");
        for (final AvailablePhoneNumber number : list.getAvailablePhoneNumbers()) {
            context.convertAnother(number);
        }
        writer.endNode();
    }
}
