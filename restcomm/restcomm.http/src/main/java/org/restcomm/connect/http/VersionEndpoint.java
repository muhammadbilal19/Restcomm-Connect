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
package org.restcomm.connect.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.restcomm.connect.commons.Version;
import org.restcomm.connect.commons.VersionEntity;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;
import org.restcomm.connect.http.converter.RestCommResponseConverter;
import org.restcomm.connect.http.converter.VersionConverter;
import org.restcomm.connect.dao.DaoManager;
import org.restcomm.connect.dao.UsageDao;
import org.restcomm.connect.dao.entities.RestCommResponse;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.ok;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
@ThreadSafe
public class VersionEndpoint extends SecuredEndpoint {
    private static Logger logger = Logger.getLogger(VersionEndpoint.class);

    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected UsageDao dao;
    protected Gson gson;
    protected XStream xstream;

    @PostConstruct
    public void init() {
        final DaoManager storage = (DaoManager) context.getAttribute(DaoManager.class.getName());
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        super.init(configuration);
        dao = storage.getUsageDao();
        accountsDao = storage.getAccountsDao();
        final VersionConverter converter = new VersionConverter(configuration);
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(VersionEntity.class, converter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
    }

    protected Response getVersion(final String accountSid, final MediaType mediaType) {
        secure(accountsDao.getAccount(accountSid), "RestComm:Read:Usage");

        VersionEntity versionEntity = Version.getVersionEntity();

        if (versionEntity != null) {
            if (APPLICATION_XML_TYPE == mediaType) {
                final RestCommResponse response = new RestCommResponse(versionEntity);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else if (APPLICATION_JSON_TYPE == mediaType) {
                Response response = ok(gson.toJson(versionEntity), APPLICATION_JSON).build();
                if(logger.isDebugEnabled()){
                    logger.debug("Supervisor endpoint response: "+gson.toJson(versionEntity));
                }
                return response;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
