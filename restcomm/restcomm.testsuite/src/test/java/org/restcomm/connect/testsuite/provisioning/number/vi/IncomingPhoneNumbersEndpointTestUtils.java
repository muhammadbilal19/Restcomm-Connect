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

package org.restcomm.connect.testsuite.provisioning.number.vi;


/**
 * @author jean.deruelle@telestax.com
 *
 */
public class IncomingPhoneNumbersEndpointTestUtils {
    public static String purchaseNumberSuccessResponse =
            "<response id=\"cf1d26d08aa642abb5f729b5a14c26a0\"><header><sessionid>a13b72c3ca20dc2174f9fb964bbc0111</sessionid></header>"
            + "<body><did><TN>4156902867</TN><status>Assigned to endpoint '11858' rewritten as '+14156902867' Tier 0</status><statuscode>100</statuscode><refid></refid><cnam>0</cnam><tier>0</tier></did></body></response>";
    public static String queryDIDSuccessResponse =
            "<response id=\"fab0fc4b6b094e61b06be40171911c65\"><header><sessionid>1857a2fc18c50e5f423292ce493fb34c</sessionid></header>"
            + "<body><did><tn>4156902867</tn><status>Number currently assigned to you with refid '' rewritten as '+14156902867' to endpoint '11858'</status><availability>assigned</availability><endpoint>11858</endpoint><rewrite>+14156902867</rewrite><statusCode>100</statusCode><refid></refid><cnam>0</cnam><tier>0</tier><t38>1</t38><cnamStorageActive>0</cnamStorageActive><cnamStorageAvailability>1</cnamStorageAvailability><registered911>0</registered911><registered411>0</registered411></did></body></response>";
    public static String deleteNumberSuccessResponse =
            "<response id=\"9680e514f9fb4fbd852651cefe9f4e58\"><header><sessionid>fcb4bae9401df4d88bbb1f37f55042b9</sessionid></header>"
            + "<body><did><TN>4196902867</TN><status>Released</status><statuscode>100</statuscode></did></body></response>";

    public static String jSonResultPurchaseNumber = "{\"sid\":\"PN*\",\"account_sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"friendly_name\":\"My Company Line\",\"phone_number\":\"+14156902867\",\"voice_url\":\"http://demo.telestax.com/docs/voice.xml\",\"voice_method\":\"GET\",\"voice_fallback_url\":null,\"voice_fallback_method\":\"POST\",\"status_callback\":null,\"status_callback_method\":\"POST\",\"voice_caller_id_lookup\":false,\"voice_application_sid\":null,\"date_created\":\"*\",\"date_updated\":\"*\",\"sms_url\":null,\"sms_method\":\"POST\",\"sms_fallback_url\":null,\"sms_fallback_method\":\"POST\",\"sms_application_sid\":null,\"ussd_url\":null,\"ussd_method\":\"POST\",\"ussd_fallback_url\":null,\"ussd_fallback_method\":\"POST\",\"ussd_application_sid\":null,\"refer_url\":null,\"refer_method\":\"POST\",\"refer_application_sid\":null,\"capabilities\":{\"voice_capable\":false,\"sms_capable\":false,\"mms_capable\":false,\"fax_capable\":false},\"api_version\":\"2012-04-24\",\"uri\":\"/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/IncomingPhoneNumbers/PN*.json\"}";
    public static String jSonResultLocalPurchaseNumber = "{\"sid\":\"PN*\",\"account_sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"friendly_name\":\"My Company Line\",\"phone_number\":\"+14166902867\",\"voice_url\":\"http://demo.telestax.com/docs/voice.xml\",\"voice_method\":\"GET\",\"voice_fallback_url\":null,\"voice_fallback_method\":\"POST\",\"status_callback\":null,\"status_callback_method\":\"POST\",\"voice_caller_id_lookup\":false,\"voice_application_sid\":null,\"date_created\":\"*\",\"date_updated\":\"*\",\"sms_url\":null,\"sms_method\":\"POST\",\"sms_fallback_url\":null,\"sms_fallback_method\":\"POST\",\"sms_application_sid\":null,\"ussd_url\":null,\"ussd_method\":\"POST\",\"ussd_fallback_url\":null,\"ussd_fallback_method\":\"POST\",\"ussd_application_sid\":null,\"refer_url\":null,\"refer_method\":\"POST\",\"refer_application_sid\":null,\"capabilities\":{\"voice_capable\":false,\"sms_capable\":false,\"mms_capable\":false,\"fax_capable\":false},\"api_version\":\"2012-04-24\",\"uri\":\"/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/IncomingPhoneNumbers/PN*.json\"}";
    public static String jSonResultTollFreePurchaseNumber = "{\"sid\":\"PN*\",\"account_sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"friendly_name\":\"My Company Line\",\"phone_number\":\"+14176902867\",\"voice_url\":\"http://demo.telestax.com/docs/voice.xml\",\"voice_method\":\"GET\",\"voice_fallback_url\":null,\"voice_fallback_method\":\"POST\",\"status_callback\":null,\"status_callback_method\":\"POST\",\"voice_caller_id_lookup\":false,\"voice_application_sid\":null,\"date_created\":\"*\",\"date_updated\":\"*\",\"sms_url\":null,\"sms_method\":\"POST\",\"sms_fallback_url\":null,\"sms_fallback_method\":\"POST\",\"sms_application_sid\":null,\"ussd_url\":null,\"ussd_method\":\"POST\",\"ussd_fallback_url\":null,\"ussd_fallback_method\":\"POST\",\"ussd_application_sid\":null,\"refer_url\":null,\"refer_method\":\"POST\",\"refer_application_sid\":null,\"capabilities\":{\"voice_capable\":false,\"sms_capable\":false,\"mms_capable\":false,\"fax_capable\":false},\"api_version\":\"2012-04-24\",\"uri\":\"/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/IncomingPhoneNumbers/PN*.json\"}";
    public static String jSonResultMobilePurchaseNumber = "{\"sid\":\"PN*\",\"account_sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"friendly_name\":\"My Company Line\",\"phone_number\":\"+14186902867\",\"voice_url\":\"http://demo.telestax.com/docs/voice.xml\",\"voice_method\":\"GET\",\"voice_fallback_url\":null,\"voice_fallback_method\":\"POST\",\"status_callback\":null,\"status_callback_method\":\"POST\",\"voice_caller_id_lookup\":false,\"voice_application_sid\":null,\"date_created\":\"*\",\"date_updated\":\"*\",\"sms_url\":null,\"sms_method\":\"POST\",\"sms_fallback_url\":null,\"sms_fallback_method\":\"POST\",\"sms_application_sid\":null,\"ussd_url\":null,\"ussd_method\":\"POST\",\"ussd_fallback_url\":null,\"ussd_fallback_method\":\"POST\",\"ussd_application_sid\":null,\"refer_url\":null,\"refer_method\":\"POST\",\"refer_application_sid\":null,\"capabilities\":{\"voice_capable\":false,\"sms_capable\":false,\"mms_capable\":false,\"fax_capable\":false},\"api_version\":\"2012-04-24\",\"uri\":\"/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/IncomingPhoneNumbers/PN*.json\"}";
    public static String jSonResultDeletePurchaseNumber = "{\"sid\":\"PN*\",\"account_sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"friendly_name\":\"My Company Line\",\"phone_number\":\"+14196902867\",\"voice_url\":\"http://demo.telestax.com/docs/voice.xml\",\"voice_method\":\"GET\",\"voice_fallback_url\":null,\"voice_fallback_method\":\"POST\",\"status_callback\":null,\"status_callback_method\":\"POST\",\"voice_caller_id_lookup\":false,\"voice_application_sid\":null,\"date_created\":\"*\",\"date_updated\":\"*\",\"sms_url\":null,\"sms_method\":\"POST\",\"sms_fallback_url\":null,\"sms_fallback_method\":\"POST\",\"sms_application_sid\":null,\"ussd_url\":null,\"ussd_method\":\"POST\",\"ussd_fallback_url\":null,\"ussd_fallback_method\":\"POST\",\"ussd_application_sid\":null,\"refer_url\":null,\"refer_method\":\"POST\",\"refer_application_sid\":null,\"capabilities\":{\"voice_capable\":false,\"sms_capable\":false,\"mms_capable\":false,\"fax_capable\":false},\"api_version\":\"2012-04-24\",\"uri\":\"/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/IncomingPhoneNumbers/PN*.json\"}";
    public static String jSonResultUpdatePurchaseNumber = "{\"sid\":\"PN*\",\"account_sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"friendly_name\":\"My Company Line\",\"phone_number\":\"+14206902867\",\"voice_url\":\"http://demo.telestax.com/docs/voice.xml\",\"voice_method\":\"GET\",\"voice_fallback_url\":null,\"voice_fallback_method\":\"POST\",\"status_callback\":null,\"status_callback_method\":\"POST\",\"voice_caller_id_lookup\":false,\"voice_application_sid\":null,\"date_created\":\"*\",\"date_updated\":\"*\",\"sms_url\":null,\"sms_method\":\"POST\",\"sms_fallback_url\":null,\"sms_fallback_method\":\"POST\",\"sms_application_sid\":null,\"ussd_url\":null,\"ussd_method\":\"POST\",\"ussd_fallback_url\":null,\"ussd_fallback_method\":\"POST\",\"ussd_application_sid\":null,\"refer_url\":null,\"refer_method\":\"POST\",\"refer_application_sid\":null,\"capabilities\":{\"voice_capable\":false,\"sms_capable\":false,\"mms_capable\":false,\"fax_capable\":false},\"api_version\":\"2012-04-24\",\"uri\":\"/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/IncomingPhoneNumbers/PN*.json\"}";
    public static String jSonResultUpdateSuccessPurchaseNumber = "{\"sid\":\"PN*\",\"account_sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"friendly_name\":\"My Company Line\",\"phone_number\":\"+14206902867\",\"voice_url\":\"http://demo.telestax.com/docs/voice2.xml\",\"voice_method\":\"POST\",\"voice_fallback_url\":null,\"voice_fallback_method\":\"POST\",\"status_callback\":null,\"status_callback_method\":\"POST\",\"voice_caller_id_lookup\":false,\"voice_application_sid\":null,\"date_created\":\"*\",\"date_updated\":\"*\",\"sms_url\":\"http://demo.telestax.com/docs/sms2.xml\",\"sms_method\":\"POST\",\"sms_fallback_url\":null,\"sms_fallback_method\":\"POST\",\"sms_application_sid\":null,\"ussd_url\":null,\"ussd_method\":\"POST\",\"ussd_fallback_url\":null,\"ussd_fallback_method\":\"POST\",\"ussd_application_sid\":null,\"refer_url\":null,\"refer_method\":\"POST\",\"refer_application_sid\":null,\"capabilities\":{\"voice_capable\":false,\"sms_capable\":false,\"mms_capable\":false,\"fax_capable\":false},\"api_version\":\"2012-04-24\",\"uri\":\"/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/IncomingPhoneNumbers/PN*.json\"}";
    public static String jSonResultAccountAssociatedPurchaseNumber = "{\"sid\":\"PN*\",\"account_sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"friendly_name\":\"My Company Line\",\"phone_number\":\"+14216902867\",\"voice_url\":\"http://demo.telestax.com/docs/voice.xml\",\"voice_method\":\"GET\",\"voice_fallback_url\":null,\"voice_fallback_method\":\"POST\",\"status_callback\":null,\"status_callback_method\":\"POST\",\"voice_caller_id_lookup\":false,\"voice_application_sid\":null,\"date_created\":\"*\",\"date_updated\":\"*\",\"sms_url\":null,\"sms_method\":\"POST\",\"sms_fallback_url\":null,\"sms_fallback_method\":\"POST\",\"sms_application_sid\":null,\"ussd_url\":null,\"ussd_method\":\"POST\",\"ussd_fallback_url\":null,\"ussd_fallback_method\":\"POST\",\"ussd_application_sid\":null,\"refer_url\":null,\"refer_method\":\"POST\",\"refer_application_sid\":null,\"capabilities\":{\"voice_capable\":false,\"sms_capable\":false,\"mms_capable\":false,\"fax_capable\":false},\"api_version\":\"2012-04-24\",\"uri\":\"/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/IncomingPhoneNumbers/PN*.json\"}";
    public static String jSonResultAccountAssociatedPurchaseNumberResult = "{\"sid\":\"PN*\",\"account_sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"friendly_name\":\"My*Company Line\",\"phone_number\":\"+1*6902867\",\"voice_url\":\"http://demo.telestax.com/docs/voice*.xml\",\"voice_method\":\"*\",\"voice_fallback_url\":null,\"voice_fallback_method\":\"POST\",\"status_callback\":null,\"status_callback_method\":\"POST\",\"voice_caller_id_lookup\":false,\"voice_application_sid\":null,\"date_created\":\"*\",\"date_updated\":\"*\",\"sms_url\":*,\"sms_method\":\"POST\",\"sms_fallback_url\":null,\"sms_fallback_method\":\"POST\",\"sms_application_sid\":null,\"ussd_url\":null,\"ussd_method\":\"POST\",\"ussd_fallback_url\":null,\"ussd_fallback_method\":\"POST\",\"ussd_application_sid\":null,\"refer_url\":null,\"refer_method\":\"POST\",\"refer_application_sid\":null,\"capabilities\":{\"voice_capable\":false,\"sms_capable\":false,\"mms_capable\":false,\"fax_capable\":false},\"api_version\":\"2012-04-24\",\"uri\":\"/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/IncomingPhoneNumbers/PN*.json\"}";

    public static boolean match(String text, String pattern)
    {
        text = text.trim();
        pattern = pattern.trim();
        // Create the cards by splitting using a RegEx. If more speed
        // is desired, a simpler character based splitting can be done.
        String [] cards = pattern.split("\\*");

        // Iterate over the cards.
        for (String card : cards)
        {
            int idx = text.indexOf(card);

            // Card not detected in the text.
            if(idx == -1)
            {
                return false;
            }

            // Move ahead, towards the right of the text.
            text = text.substring(idx + card.length());
        }

        return true;
    }
}
