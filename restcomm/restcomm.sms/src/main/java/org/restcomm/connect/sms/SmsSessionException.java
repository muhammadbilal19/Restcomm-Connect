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
package org.restcomm.connect.sms;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
public final class SmsSessionException extends Exception {
    private static final long serialVersionUID = 1L;

    public SmsSessionException(final String message) {
        super(message);
    }

    public SmsSessionException(final Throwable cause) {
        super(cause);
    }

    public SmsSessionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
