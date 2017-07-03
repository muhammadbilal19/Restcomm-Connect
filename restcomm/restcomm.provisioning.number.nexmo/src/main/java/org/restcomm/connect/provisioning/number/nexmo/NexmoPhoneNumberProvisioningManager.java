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
package org.restcomm.connect.provisioning.number.nexmo;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.restcomm.connect.provisioning.number.api.ContainerConfiguration;
import org.restcomm.connect.provisioning.number.api.PhoneNumber;
import org.restcomm.connect.provisioning.number.api.PhoneNumberParameters;
import org.restcomm.connect.provisioning.number.api.PhoneNumberProvisioningManager;
import org.restcomm.connect.provisioning.number.api.PhoneNumberSearchFilters;
import org.restcomm.connect.commons.util.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;

/**
 * @author jean.deruelle@telestax.com
 */
public class NexmoPhoneNumberProvisioningManager implements PhoneNumberProvisioningManager {
    private static final Logger logger = Logger.getLogger(NexmoPhoneNumberProvisioningManager.class);

    protected Boolean telestaxProxyEnabled;
    protected String uri, apiKey, apiSecret, smppSystemType;
    protected String searchURI, buyURI, updateURI, cancelURI;
    protected Configuration activeConfiguration;
    protected ContainerConfiguration containerConfiguration;

    public NexmoPhoneNumberProvisioningManager() {}

    /*
     * (non-Javadoc)
     *
     * @see
     * PhoneNumberProvisioningManager#init(org.apache.commons.configuration
     * .Configuration, boolean)
     */
    @Override
    public void init(Configuration phoneNumberProvisioningConfiguration, Configuration telestaxProxyConfiguration, ContainerConfiguration containerConfiguration) {
        this.containerConfiguration = containerConfiguration;
        telestaxProxyEnabled = telestaxProxyConfiguration.getBoolean("enabled", false);
        if (telestaxProxyEnabled) {
            uri = telestaxProxyConfiguration.getString("uri");
            apiKey = telestaxProxyConfiguration.getString("api-key");
            apiSecret = telestaxProxyConfiguration.getString("api-secret");
            smppSystemType = telestaxProxyConfiguration.getString("smpp-system-type", "inbound");
            activeConfiguration = telestaxProxyConfiguration;
        } else {
            Configuration nexmoConfiguration = phoneNumberProvisioningConfiguration.subset("nexmo");
            uri = nexmoConfiguration.getString("uri");
            apiKey = nexmoConfiguration.getString("api-key");
            apiSecret = nexmoConfiguration.getString("api-secret");
            smppSystemType = nexmoConfiguration.getString("smpp-system-type", "inbound");
            activeConfiguration = nexmoConfiguration;
        }
        searchURI = uri + "/number/search/" + apiKey + "/" + apiSecret + "/";
        buyURI = uri + "/number/buy/" + apiKey + "/" + apiSecret + "/";
        updateURI = uri + "/number/update/" + apiKey + "/" + apiSecret + "/";
        cancelURI = uri + "/number/cancel/" + apiKey + "/" + apiSecret + "/";
    }

    private String getFriendlyName(final String number, String countryCode) {
        try {
            final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            final com.google.i18n.phonenumbers.Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(number, countryCode);
            String friendlyName = phoneNumberUtil.format(phoneNumber, PhoneNumberFormat.E164);
            return friendlyName;
        } catch (final Exception ignored) {
            return number;
        }
    }

    private List<PhoneNumber> toAvailablePhoneNumbers(final JsonArray phoneNumbers, PhoneNumberSearchFilters listFilters) {
        Pattern searchPattern = listFilters.getFilterPattern();
        final List<PhoneNumber> numbers = new ArrayList<PhoneNumber>();
        for (int i = 0; i < phoneNumbers.size(); i++) {
            JsonObject number = phoneNumbers.get(i).getAsJsonObject();
            String countryCode = number.get("country").getAsString();
            String features = null;
            if(number.get("features") != null) {
                features = number.get("features").toString();
            }
            boolean isVoiceCapable = false;
            boolean isSmsCapable = false;
            if(features.contains("SMS")) {
                isSmsCapable = true;
            }
            if(features.contains("VOICE")) {
                isVoiceCapable = true;
            }
            final PhoneNumber phoneNumber = new PhoneNumber(
                    getFriendlyName(number.get("msisdn").getAsString(), countryCode),
                    number.get("msisdn").getAsString(),
                    null, null, null, null, null, null,
                    countryCode, number.get("cost").getAsString(), isVoiceCapable, isSmsCapable, null, null, null);
            numbers.add(phoneNumber);
        }
        return numbers;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * PhoneNumberProvisioningManager#searchForNumbers(java.lang.String,
     * java.lang.String, java.util.regex.Pattern, boolean, boolean, boolean, boolean, int, int)
     */
    @Override
    public List<PhoneNumber> searchForNumbers(String country, PhoneNumberSearchFilters listFilters) {
        if(logger.isDebugEnabled()) {
            logger.debug("searchPattern " + listFilters.getFilterPattern());
        }
        Pattern filterPattern = listFilters.getFilterPattern();
        String filterPatternString = null;
        if(filterPattern != null) {
            filterPatternString = filterPattern.toString().replaceAll("[^\\d]", "");
        }
        if(logger.isDebugEnabled()) {
            logger.debug("searchPattern simplified for nexmo " + filterPatternString);
        }

        String queryUri = searchURI + country;
        boolean queryParamAdded = false;
        if(("US".equalsIgnoreCase(country) || "CA".equalsIgnoreCase(country)) && listFilters.getAreaCode() != null) {
            // https://github.com/Mobicents/RestComm/issues/551 fixing the search pattern for US when Area Code is selected
            // https://github.com/Mobicents/RestComm/issues/602 fixing the search pattern for CA when Area Code is selected
            queryUri = queryUri + "?pattern=1" + listFilters.getAreaCode() + "&search_pattern=0";
            queryParamAdded = true;
        } else if(filterPatternString != null) {
            queryUri = queryUri + "?pattern=" + filterPatternString + "&search_pattern=1";
            queryParamAdded = true;
        }
        if(listFilters.getSmsEnabled() != null || listFilters.getVoiceEnabled() != null) {
            if(!queryParamAdded) {
                queryUri = queryUri + "?";
                queryParamAdded = true;
            } else {
                queryUri = queryUri + "&";
            }
            if(listFilters.getSmsEnabled() != null && listFilters.getVoiceEnabled() != null) {
                queryUri = queryUri + "features=SMS,VOICE";
            } else if(listFilters.getSmsEnabled() != null) {
                queryUri = queryUri + "features=SMS";
            } else {
                queryUri = queryUri + "features=VOICE";
            }
        }
        if(listFilters.getRangeIndex() != -1) {
            if(!queryParamAdded) {
                queryUri = queryUri + "?";
                queryParamAdded = true;
            } else {
                queryUri = queryUri + "&";
            }
            queryUri = queryUri + "index=" + listFilters.getRangeIndex();
        }
        if(listFilters.getRangeSize() != -1) {
            if(!queryParamAdded) {
                queryUri = queryUri + "?";
                queryParamAdded = true;
            } else {
                queryUri = queryUri + "&";
            }
            queryUri = queryUri + "size=" + listFilters.getRangeSize();
        }
        final HttpGet get = new HttpGet(queryUri);
        try {

            final DefaultHttpClient client = new DefaultHttpClient();
//                if (telestaxProxyEnabled) {
//                    // This will work as a flag for LB that this request will need to be modified and proxied to VI
//                    get.addHeader("TelestaxProxy", String.valueOf(telestaxProxyEnabled));
//                    // This will tell LB that this request is a getAvailablePhoneNumberByAreaCode request
//                    get.addHeader("RequestType", "GetAvailablePhoneNumbersByAreaCode");
//                    //This will let LB match the DID to a node based on the node host+port
//                    for (SipURI uri: containerConfiguration.getOutboundInterfaces()) {
//                        get.addHeader("OutboundIntf", uri.getHost()+":"+uri.getPort()+":"+uri.getTransportParam());
//                    }
//                }
            if(logger.isDebugEnabled()) {
                logger.debug("Nexmo query " + queryUri);
            }
            final HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                final String content = StringUtils.toString(response.getEntity().getContent());
                JsonParser parser = new JsonParser();
                JsonObject jsonResponse = parser.parse(content).getAsJsonObject();
                if(logger.isDebugEnabled()) {
                    logger.debug("Nexmo response " + jsonResponse.toString());
                }
                JsonPrimitive countPrimitive = jsonResponse.getAsJsonPrimitive("count");
                if(countPrimitive == null) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("No numbers found");
                    }
                    return new ArrayList<PhoneNumber>();
                }
                long count = countPrimitive.getAsLong();
                if(logger.isDebugEnabled()) {
                    logger.debug("Number of numbers found : "+count);
                }
                JsonArray nexmoNumbers = jsonResponse.getAsJsonArray("numbers");
                final List<PhoneNumber> numbers = toAvailablePhoneNumbers(nexmoNumbers, listFilters);
                return numbers;
            } else {
                logger.warn("Couldn't reach uri for getting Phone Numbers. Response status was: "+response.getStatusLine().getStatusCode());
            }
        } catch (final Exception e) {
            logger.warn("Couldn't reach uri for getting Phone Numbers" + uri, e);
        }

        return new ArrayList<PhoneNumber>();
    }

    @Override
    public boolean buyNumber(PhoneNumber phoneNumberObject, PhoneNumberParameters phoneNumberParameters) {
        String phoneNumber = phoneNumberObject.getPhoneNumber();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String country = null;
        try {
            // phone must begin with '+'
            com.google.i18n.phonenumbers.Phonenumber.PhoneNumber numberProto = null;
            if(phoneNumber.startsWith("+")) {
                numberProto = phoneUtil.parse(phoneNumber, "US");
            } else {
                numberProto = phoneUtil.parse("+" + phoneNumber, "US");
            }
            int countryCode = numberProto.getCountryCode();
            country = phoneUtil.getRegionCodeForNumber(numberProto);
        } catch (NumberParseException e) {
            if(logger.isDebugEnabled())
                logger.debug("problem parsing phone number " + phoneNumber, e);
            return false;
        }
        String queryUri = null;
        if(phoneNumber.startsWith("+")) {
            queryUri = buyURI + country + "/" + phoneNumber.substring(1, phoneNumber.length());
        } else {
            queryUri = buyURI + country + "/" + phoneNumber;
        }

        final HttpPost post = new HttpPost(queryUri);
        try {

            final DefaultHttpClient client = new DefaultHttpClient();
//                if (telestaxProxyEnabled) {
//                    // This will work as a flag for LB that this request will need to be modified and proxied to VI
//                    get.addHeader("TelestaxProxy", String.valueOf(telestaxProxyEnabled));
//                    // This will tell LB that this request is a getAvailablePhoneNumberByAreaCode request
//                    get.addHeader("RequestType", "GetAvailablePhoneNumbersByAreaCode");
//                    //This will let LB match the DID to a node based on the node host+port
//                    for (SipURI uri: containerConfiguration.getOutboundInterfaces()) {
//                        get.addHeader("OutboundIntf", uri.getHost()+":"+uri.getPort()+":"+uri.getTransportParam());
//                    }
//                }
            final HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                updateNumber(phoneNumberObject, phoneNumberParameters);
                // we always return true as the phone number was bought
                return true;
            } else {
                if(logger.isDebugEnabled())
                    logger.debug("Couldn't buy Phone Number " + phoneNumber + ". Response status was: "+ response.getStatusLine().getStatusCode());
            }
        } catch (final Exception e) {
            logger.warn("Couldn't reach uri for buying Phone Numbers" + uri, e);
        }

        return false;
    }

    @Override
    public boolean updateNumber(PhoneNumber phoneNumberObj, PhoneNumberParameters phoneNumberParameters) {
        String phoneNumber = phoneNumberObj.getPhoneNumber();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String country = null;
        try {
            // phone must begin with '+'
            com.google.i18n.phonenumbers.Phonenumber.PhoneNumber numberProto = null;
            if(phoneNumber.startsWith("+")) {
                numberProto = phoneUtil.parse(phoneNumber, "US");
            } else {
                numberProto = phoneUtil.parse("+" + phoneNumber, "US");
            }
            int countryCode = numberProto.getCountryCode();
            country = phoneUtil.getRegionCodeForCountryCode(countryCode);
        } catch (NumberParseException e) {
            if(logger.isDebugEnabled())
                logger.debug("problem parsing phone number " + phoneNumber, e);
            return false;
        }
        String updateUri = null;
        if(phoneNumber.startsWith("+")) {
            updateUri = updateURI + country + "/" + phoneNumber.substring(1, phoneNumber.length());
        } else {
            updateUri = updateURI + country + "/" + phoneNumber;
        }
        try {
            if((phoneNumberParameters.getVoiceUrl() != null && !phoneNumberParameters.getVoiceUrl().isEmpty()) ||
                    (phoneNumberParameters.getSmsUrl() != null && phoneNumberParameters.getSmsUrl().isEmpty())) {
                updateUri = updateUri + "?";
                if(phoneNumberParameters.getSmsUrl() != null && !phoneNumberParameters.getSmsUrl().isEmpty()
                        && phoneNumberParameters.getVoiceUrl() != null && !phoneNumberParameters.getVoiceUrl().isEmpty()) {
                    updateUri = updateUri + "voiceCallbackValue=" + URLEncoder.encode(phoneNumber+"@"+phoneNumberParameters.getVoiceUrl(), "UTF-8") + "&voiceCallbackType=sip" +
                            "&moHttpUrl=" + URLEncoder.encode(phoneNumber+"@"+phoneNumberParameters.getSmsUrl(), "UTF-8") + "&moSmppSysType=" + smppSystemType;
                } else if(phoneNumberParameters.getVoiceUrl() != null && !phoneNumberParameters.getVoiceUrl().isEmpty()) {
                    updateUri = updateUri + "voiceCallbackValue=" + URLEncoder.encode(phoneNumber+"@"+phoneNumberParameters.getVoiceUrl(), "UTF-8") + "&voiceCallbackType=sip"  + "&moSmppSysType=" + smppSystemType;
                } else {
                    updateUri = updateUri + "moHttpUrl=" + URLEncoder.encode(phoneNumber+"@"+phoneNumberParameters.getSmsUrl(), "UTF-8") + "&moSmppSysType=" + smppSystemType ;
                }
            }
            final HttpPost updatePost = new HttpPost(updateUri);
            final DefaultHttpClient client = new DefaultHttpClient();

            final HttpResponse updateResponse = client.execute(updatePost);
            if (updateResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            } else {
                if(logger.isDebugEnabled())
                    logger.debug("Couldn't update Phone Number " + phoneNumber + ". Response status was: "+ updateResponse.getStatusLine().getStatusCode());
                return false;
            }
        } catch (final Exception e) {
            logger.warn("Couldn't reach uri for update Phone Numbers" + uri, e);
        }
        return false;
    }

    @Override
    public boolean cancelNumber(PhoneNumber phoneNumberObj) {
        String phoneNumber = phoneNumberObj.getPhoneNumber();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String country = null;
        try {
            // phone must begin with '+'
            com.google.i18n.phonenumbers.Phonenumber.PhoneNumber numberProto = null;
            if(phoneNumber.startsWith("+")) {
                numberProto = phoneUtil.parse(phoneNumber, "US");
            } else {
                numberProto = phoneUtil.parse("+" + phoneNumber, "US");
            }
            int countryCode = numberProto.getCountryCode();
            country = phoneUtil.getRegionCodeForCountryCode(countryCode);
        } catch (NumberParseException e) {
            if(logger.isDebugEnabled())
                logger.debug("problem parsing phone number " + phoneNumber, e);
            return false;
        }
        String queryUri = null;
        if(phoneNumber.startsWith("+")) {
            queryUri = cancelURI + country + "/" + phoneNumber.substring(1, phoneNumber.length());
        } else {
            queryUri = cancelURI + country + "/" + phoneNumber;
        }

        final HttpPost post = new HttpPost(queryUri);
        try {

            final DefaultHttpClient client = new DefaultHttpClient();
//                if (telestaxProxyEnabled) {
//                    // This will work as a flag for LB that this request will need to be modified and proxied to VI
//                    get.addHeader("TelestaxProxy", String.valueOf(telestaxProxyEnabled));
//                    // This will tell LB that this request is a getAvailablePhoneNumberByAreaCode request
//                    get.addHeader("RequestType", "GetAvailablePhoneNumbersByAreaCode");
//                    //This will let LB match the DID to a node based on the node host+port
//                    for (SipURI uri: containerConfiguration.getOutboundInterfaces()) {
//                        get.addHeader("OutboundIntf", uri.getHost()+":"+uri.getPort()+":"+uri.getTransportParam());
//                    }
//                }
            final HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            } else {
                if(logger.isDebugEnabled())
                    logger.debug("Couldn't cancel Phone Number " + phoneNumber + ". Response status was: "+response.getStatusLine().getStatusCode());
            }
        } catch (final Exception e) {
            logger.warn("Couldn't reach uri for cancelling Phone Numbers" + uri, e);
        }

        return false;
    }

    @Override
    public List<String> getAvailableCountries() {
        List<String> countries = new ArrayList<String>();
        String[] locales = Locale.getISOCountries();

        for (String countryCode : locales) {
            countries.add(countryCode);
        }

        return countries;
    }
}
