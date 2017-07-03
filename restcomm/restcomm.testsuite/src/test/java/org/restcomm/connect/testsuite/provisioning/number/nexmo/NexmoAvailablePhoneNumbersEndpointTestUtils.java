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
	
package org.restcomm.connect.testsuite.provisioning.number.nexmo;

/**
 * @author jean.deruelle@telestax.com
 *
 */
public class NexmoAvailablePhoneNumbersEndpointTestUtils {
    public static final String body501AreaCode = "{\"count\":6,\"numbers\":[{\"country\":\"US\",\"msisdn\":\"15013036357\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"US\",\"msisdn\":\"15013036361\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"US\",\"msisdn\":\"15013036365\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"US\",\"msisdn\":\"15013036367\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"US\",\"msisdn\":\"15013036371\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"US\",\"msisdn\":\"15013036372\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]}]}";
    public static final String bodyCA418AreaCode = "{\"count\":14,\"numbers\":[{\"country\":\"CA\",\"msisdn\":\"13658000418\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14387939418\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"16475594180\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"16475594181\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"16475594182\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"16475594183\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"16475594184\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"16475594185\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"16475594186\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"16475594187\",\"cost\":\"0.67\",\"type\":\"mobile-lvn\",\"features\":[\"VOICE\",\"SMS\"]}]}";
    public static final String bodyCA450AreaCode = "{\"count\":41,\"numbers\":[{\"country\":\"CA\",\"msisdn\":\"14506001110\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14506001113\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14506001114\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14506001115\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14506001117\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14506001118\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14506001119\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14506001121\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14506001122\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]},{\"country\":\"CA\",\"msisdn\":\"14506001123\",\"cost\":\"0.67\",\"type\":\"landline\",\"features\":[\"VOICE\",\"SMS\"]}]}";
    public static String jsonResponseES700 = 
            "{\"count\":1,\"numbers\":[{\"country\":\"ES\",\"msisdn\":\"34911067000\",\"type\":\"landline\",\"features\":[\"SMS\"],\"cost\":\"0.50\"}]}";
    public static String jsonResponseUSRange = 
            "{\"count\":177,\"numbers\":[{\"country\":\"US\",\"msisdn\":\"15102694548\",\"type\":\"mobile-lvn\",\"features\":[\"SMS\",\"VOICE\"],\"cost\":\"0.67\"},{\"country\":\"US\",\"msisdn\":\"17088568490\",\"type\":\"mobile-lvn\",\"features\":[\"SMS\",\"VOICE\"],\"cost\":\"0.67\"},{\"country\":\"US\",\"msisdn\":\"17088568491\",\"type\":\"mobile-lvn\",\"features\":[\"SMS\",\"VOICE\"],\"cost\":\"0.67\"},{\"country\":\"US\",\"msisdn\":\"17088568492\",\"type\":\"mobile-lvn\",\"features\":[\"SMS\",\"VOICE\"],\"cost\":\"0.67\"},{\"country\":\"US\",\"msisdn\":\"17088568973\",\"type\":\"mobile-lvn\",\"features\":[\"SMS\",\"VOICE\"],\"cost\":\"0.67\"}]}";
    public static String jsonResultES700 = "{\"friendlyName\":\"+34911067000\",\"phoneNumber\":\"34911067000\",\"isoCountry\":\"ES\",\"cost\":\"0.50\",\"voiceCapable\":false,\"smsCapable\":true}";
    public static String jsonResultUSRange = "{\"friendlyName\":\"+15102694548\",\"phoneNumber\":\"15102694548\",\"isoCountry\":\"US\",\"cost\":\"0.67\",\"voiceCapable\":true,\"smsCapable\":true}";
    public static String firstJSonResult501AreaCode = "{\"friendlyName\":\"+15013036357\",\"phoneNumber\":\"15013036357\",\"isoCountry\":\"US\",\"cost\":\"0.67\",\"voiceCapable\":true,\"smsCapable\":true}";
    public static final String firstJSonResultCA418AreaCode = "{\"friendlyName\":\"+13658000418\",\"phoneNumber\":\"13658000418\",\"isoCountry\":\"CA\",\"cost\":\"0.67\",\"voiceCapable\":true,\"smsCapable\":true}";
    public static final String firstJSonResultCA450AreaCode = "{\"friendlyName\":\"+14506001110\",\"phoneNumber\":\"14506001110\",\"isoCountry\":\"CA\",\"cost\":\"0.67\",\"voiceCapable\":true,\"smsCapable\":true}";
}
