/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.restcomm.connect.mrb.api;

import org.restcomm.connect.commons.annotations.concurrency.Immutable;

/**
 * @author Maria
 *
 */
@Immutable
public class ConferenceMediaResourceControllerStateChanged {

    public enum MediaServerControllerState {
        INITIALIZED, ACTIVE, FAILED, INACTIVE;
    }

    private final MediaServerControllerState state;
    private final String conferenceState;
    private final boolean destroyEndpoint;
    private final boolean moderatorPresent;

    public ConferenceMediaResourceControllerStateChanged(MediaServerControllerState state, final String conferenceState, final boolean destroyEndpoint, final boolean moderatorPresent) {
        this.state = state;
        this.conferenceState = conferenceState;
        this.destroyEndpoint = destroyEndpoint;
        this.moderatorPresent = moderatorPresent;
    }

    public ConferenceMediaResourceControllerStateChanged(MediaServerControllerState state, final String conferenceState) {
        this(state, conferenceState, false, false);
    }

    public ConferenceMediaResourceControllerStateChanged(MediaServerControllerState state, final String conferenceState, final boolean moderatorPresent) {
        this(state, conferenceState, false, moderatorPresent);
    }

    public ConferenceMediaResourceControllerStateChanged(MediaServerControllerState state, final boolean destroyEndpoint) {
        this(state, null, destroyEndpoint, false);
    }

    public ConferenceMediaResourceControllerStateChanged(MediaServerControllerState state) {
        this(state, null, false, false);
    }

    public MediaServerControllerState state() {
        return state;
    }

    public String conferenceState() {
        return conferenceState;
    }

    public boolean destroyEndpoint (){
        return destroyEndpoint;
    }

    public boolean moderatorPresent (){
        return moderatorPresent;
    }
}
