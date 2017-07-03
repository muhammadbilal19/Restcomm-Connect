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
package org.restcomm.connect.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.regex.Pattern;

import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class StringUtils {
    private static final Pattern numberPattern = Pattern.compile("\\d+");

    private StringUtils() {
        super();
    }

    public static String addSuffixIfNotPresent(final String text, final String suffix) {
        if (text.endsWith(suffix)) {
            return text;
        } else {
            return text + suffix;
        }
    }

    public static boolean isPositiveInteger(final String text) {
        return numberPattern.matcher(text).matches();
    }

    public static String toString(final InputStream input) throws IOException {
        final InputStreamReader reader = new InputStreamReader(input);
        final StringWriter writer = new StringWriter();
        final char[] data = new char[512];
        int bytesRead = -1;
        do {
            bytesRead = reader.read(data);
            if (bytesRead > 0) {
                writer.write(data, 0, bytesRead);
            }
        } while (bytesRead != -1);
        return writer.getBuffer().toString();
    }
}
