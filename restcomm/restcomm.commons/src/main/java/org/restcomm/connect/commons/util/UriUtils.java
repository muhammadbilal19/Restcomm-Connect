package org.restcomm.connect.commons.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.ReflectionException;

import org.apache.log4j.Logger;
import org.restcomm.connect.commons.HttpConnector;
import org.restcomm.connect.commons.configuration.RestcommConfiguration;
import org.restcomm.connect.commons.HttpConnectorList;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;

/**
 * Utility class to manipulate URI.
 * @author Henrique Rosa
 */
@ThreadSafe
public final class UriUtils {

    private static Logger logger = Logger.getLogger(UriUtils.class);
    private static HttpConnector httpConnector;
    private static HttpConnectorList httpConnectorList;
    /**
     * Default constructor.
     */
    private UriUtils() {
        super();
    }

    /**
     * Resolves a relative URI.
     * @param base The base of the URI
     * @param uri The relative URI.
     * @return The absolute URI
     */
    public static URI resolve(final URI base, final URI uri) {
        if (base.equals(uri)) {
            return uri;
        } else {
            if (!uri.isAbsolute()) {
                return base.resolve(uri);
            } else {
                return uri;
            }
        }
    }

    private static HttpConnectorList getHttpConnectors() throws MalformedObjectNameException,NullPointerException, UnknownHostException, AttributeNotFoundException,
    InstanceNotFoundException, MBeanException, ReflectionException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        Set<ObjectName> jbossObjs = mbs.queryNames(new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http*"), null);
        Set<ObjectName> tomcatObjs = mbs.queryNames(new ObjectName("*:type=Connector,*"), Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));

        ArrayList<HttpConnector> endPoints = new ArrayList<HttpConnector>();
        if (jbossObjs != null && jbossObjs.size() > 0) {
            for (Iterator<ObjectName> i = jbossObjs.iterator(); i.hasNext();) {
                ObjectName obj = i.next();
                Boolean bound = (Boolean) mbs.getAttribute(obj, "bound");
//                Boolean bound = Boolean.getBoolean(mbs.getAttribute(obj, "bound").toString());
                if (bound) {
                    String scheme = mbs.getAttribute(obj, "name").toString().replaceAll("\"", "");
                    Integer port = (Integer) mbs.getAttribute(obj, "boundPort");
                    String address = ((String)mbs.getAttribute(obj, "boundAddress")).replaceAll("\"", "");
                    if(logger.isInfoEnabled()) {
                        logger.info("Jboss Http Connector: "+scheme+"://"+address+":"+port);
                    }
                    HttpConnector httpConnector = new HttpConnector(scheme, address, port, scheme.equalsIgnoreCase("https"));
                    endPoints.add(httpConnector);
                }            }
        } else if (tomcatObjs != null && tomcatObjs.size() > 0) {
            for (Iterator<ObjectName> i = tomcatObjs.iterator(); i.hasNext();) {
                ObjectName obj = i.next();
                String scheme = mbs.getAttribute(obj, "scheme").toString().replaceAll("\"", "");
                String port = obj.getKeyProperty("port").replaceAll("\"", "");
                String address = obj.getKeyProperty("address").replaceAll("\"", "");
                if(logger.isInfoEnabled()){
                    logger.info("Tomcat Http Connector: "+scheme+"://"+address+":"+port);
                }
                HttpConnector httpConnector = new HttpConnector(scheme, address, Integer.parseInt(port), scheme.equalsIgnoreCase("https"));
                endPoints.add(httpConnector);
            }
        }
        if (endPoints.isEmpty()) {
            logger.error("Coundn't discover any Http Interfaces");
        }
        httpConnectorList = new HttpConnectorList(endPoints);
        return httpConnectorList;
    }

    /**
     * Resolves a relative URI.
     * @param uri The relative URI
     * @return The absolute URI
     */
    public static URI resolve(final URI uri) {
        if (httpConnector == null) {
            if (httpConnectorList == null) {
                try {
                    httpConnectorList = getHttpConnectors();
                } catch (MalformedObjectNameException | AttributeNotFoundException | InstanceNotFoundException
                        | NullPointerException | UnknownHostException | MBeanException | ReflectionException exception) {
                    logger.error("Exception during HTTP Connectors discovery: ", exception);
                }
            }
            if (httpConnectorList != null && !httpConnectorList.getConnectors().isEmpty()) {
                List<HttpConnector> connectors = httpConnectorList.getConnectors();
                Iterator<HttpConnector> iterator = connectors.iterator();
                while (iterator.hasNext()) {
                    HttpConnector connector = iterator.next();
                    if (connector.isSecure()) {
                        httpConnector = connector;
                    }
                }
                if (httpConnector == null) {
                    httpConnector = connectors.get(0);
                }
            }
        }

// Since this is a relative URL that we are trying to resolve, we don't care about the public URL.
//        //HttpConnector address could be a local address while the request came from a public address
//        String address;
//        if (httpConnector.getAddress().equalsIgnoreCase(localAddress)) {
//            address = httpConnector.getAddress();
//        } else {
//            address = localAddress;
//        }
        String restcommAddress = null;
        if (RestcommConfiguration.getInstance().getMain().isUseHostnameToResolveRelativeUrls()) {
            restcommAddress = RestcommConfiguration.getInstance().getMain().getHostname();
            if (restcommAddress == null || restcommAddress.isEmpty()) {
                try {
                    InetAddress addr = InetAddress.getByName(httpConnector.getAddress());
                    restcommAddress = addr.getCanonicalHostName();
                } catch (UnknownHostException e) {
                    logger.error("Unable to resolve: " + httpConnector + " to hostname: " + e);
                    restcommAddress = httpConnector.getAddress();
                }
            }
        } else {
            restcommAddress = httpConnector.getAddress();
        }

        String base = httpConnector.getScheme()+"://" + restcommAddress + ":" + httpConnector.getPort();
        try {
            return resolve(new URI(base), uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Badly formed URI: " + base, e);
        }
    }

    public static HttpConnectorList getHttpConnectorList() {
        if (httpConnectorList == null) {
            try{
                getHttpConnectors();
            }catch(Exception e){
                //TODO: handle exceptions properly
                logger.error("Problem during the retrieval of HTTP Connectors "+e);
            }
        }
        return httpConnectorList;
    }
}
