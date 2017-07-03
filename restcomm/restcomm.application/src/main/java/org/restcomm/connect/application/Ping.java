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
package org.restcomm.connect.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipURI;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.restcomm.connect.commons.configuration.RestcommConfiguration;
import org.restcomm.connect.commons.common.http.CustomHttpClientBuilder;
import org.restcomm.connect.provisioning.number.api.ProvisionProvider;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
@Deprecated
public class Ping {

    private Logger logger = Logger.getLogger(Ping.class);
    private Configuration configuration;
    private ServletContext context;
    private Timer timer;
    private Timer subsequentTimer;
    private boolean subsequentTimerIsSet = false;
    private PingTask ping;
    private final String provider;

    Ping(Configuration configuration, ServletContext context){
        this.configuration = configuration;
        this.context = context;
        this.provider = configuration.getString("phone-number-provisioning[@class]");
    }

    public void sendPing(){
        boolean daemon = true;
        timer = new Timer(daemon);
        ping = new PingTask(configuration);
        timer.schedule(ping, 0, 60000);
    }

    private class PingTask extends TimerTask {
        private Configuration configuration;

        public PingTask(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void run() {
            Configuration proxyConf = configuration.subset("runtime-settings").subset("telestax-proxy");
            Boolean proxyEnabled = proxyConf.getBoolean("enabled");
            Configuration mediaConf = configuration.subset("media-server-manager").subset("mgcp-server");
            String publicIpAddress = mediaConf.getString("external-address");
            if (proxyEnabled) {
                String proxyUri = proxyConf.getString("uri");
                String username = proxyConf.getString("login");
                String password = proxyConf.getString("password");
                String endpoint = proxyConf.getString("endpoint");

                final StringBuilder buffer = new StringBuilder();
                buffer.append("<request id=\""+generateId()+"\">");
                buffer.append(header(username, password));
                buffer.append("<body>");
                buffer.append("<requesttype>").append("ping").append("</requesttype>");
                buffer.append("<item>");
                buffer.append("<endpointgroup>").append(endpoint).append("</endpointgroup>");
//                buffer.append("<provider>").append(provider).append("</provider>");
                buffer.append("</item>");
                buffer.append("</body>");
                buffer.append("</request>");
                final String body = buffer.toString();
                final HttpPost post = new HttpPost(proxyUri);
                try {
                    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                    parameters.add(new BasicNameValuePair("apidata", body));
                    post.setEntity(new UrlEncodedFormEntity(parameters));
                    final HttpClient client = CustomHttpClientBuilder.build(RestcommConfiguration.getInstance().getMain());

                    //This will work as a flag for LB that this request will need to be modified and proxied to VI
                    post.addHeader("TelestaxProxy", String.valueOf(proxyEnabled));
                    //Adds the Provision provider class name
                    post.addHeader("Provider", provider);
                    //This will tell LB that this request is a getAvailablePhoneNumberByAreaCode request
                    post.addHeader("RequestType", ProvisionProvider.REQUEST_TYPE.PING.name());
                    //This will let LB match the DID to a node based on the node host+port
                    List<SipURI> uris = outboundInterface();
                    for (SipURI uri: uris) {
                        post.addHeader("OutboundIntf", uri.getHost()+":"+uri.getPort()+":"+uri.getTransportParam());
                    }
                    if (publicIpAddress != null || !publicIpAddress.equalsIgnoreCase("")) {
                        post.addHeader("PublicIpAddress",publicIpAddress);
                    }
                    final HttpResponse response = client.execute(post);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        logger.info("Ping to Telestax Proxy was successfully sent");
                        timer.cancel();
//                        if (!subsequentTimerIsSet) {
//                            logger.info("Will set subsequent timer");
//                            boolean daemon = true;
//                            subsequentTimer = new Timer(daemon);
//                            subsequentTimer.schedule(ping, 1800000, 1800000);
//                            subsequentTimerIsSet = true;
//                        }
                        return;
                    } else {
                        logger.error("Ping to Telestax Proxy was sent, but there was a problem. Response status line: "+response.getStatusLine());
                        return;
                    }
                } catch (final Exception e){
                    logger.error("Ping to Telestax Proxy was sent, but there was a problem. Exception: "+e);
                    return;
                }
            } else {
                timer.cancel();
                return;
            }
        }

        @SuppressWarnings("unchecked")
        private List<SipURI> outboundInterface() {
            final List<SipURI> uris = (List<SipURI>) context.getAttribute(SipServlet.OUTBOUND_INTERFACES);
            return uris;
        }

        private String generateId() {
            return UUID.randomUUID().toString().replace("-", "");
        }

        private String header(final String login, final String password) {
            final StringBuilder buffer = new StringBuilder();
            buffer.append("<header><sender>");
            buffer.append("<login>").append(login).append("</login>");
            buffer.append("<password>").append(password).append("</password>");
            buffer.append("</sender></header>");
            return buffer.toString();
        }
    }
}
