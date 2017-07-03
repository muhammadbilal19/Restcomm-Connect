package org.restcomm.connect.http;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.util.Timeout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.restcomm.connect.commons.cache.DiskCacheFactory;
import org.restcomm.connect.commons.cache.DiskCacheRequest;
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.dao.entities.Announcement;
import org.restcomm.connect.dao.entities.RestCommResponse;
import org.restcomm.connect.http.converter.AnnouncementConverter;
import org.restcomm.connect.http.converter.AnnouncementListConverter;
import org.restcomm.connect.http.converter.RestCommResponseConverter;
import org.restcomm.connect.tts.api.SpeechSynthesizerRequest;
import org.restcomm.connect.tts.api.SpeechSynthesizerResponse;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.ok;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */
public abstract class AnnouncementsEndpoint extends SecuredEndpoint {
    private static Logger logger = Logger.getLogger(AnnouncementsEndpoint.class);

    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected Configuration runtime;
    protected ActorRef synthesizer;
    protected ActorRef cache;
    protected Gson gson;
    protected XStream xstream;
    private URI uri;
    private ActorSystem system;

    public AnnouncementsEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        system = (ActorSystem) context.getAttribute(ActorSystem.class.getName());
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        Configuration ttsConfiguration = configuration.subset("speech-synthesizer");
        runtime = configuration.subset("runtime-settings");
        synthesizer = tts(ttsConfiguration);
        super.init(runtime);
        final AnnouncementConverter converter = new AnnouncementConverter(configuration);
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Announcement.class, converter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new AnnouncementListConverter(configuration));
        xstream.registerConverter(new RestCommResponseConverter(configuration));
    }

    public Response putAnnouncement(final String accountSid, final MultivaluedMap<String, String> data,
            final MediaType responseType) throws Exception {
        secure(accountsDao.getAccount(accountSid), "RestComm:Create:Announcements");
        if(cache == null)
            createCacheActor(accountSid);

        Announcement announcement = createFrom(accountSid, data);
        if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(announcement), APPLICATION_JSON).build();
        } else if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(announcement);
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else {
            return null;
        }
    }

    private void createCacheActor(final String accountId) {
        String path = runtime.getString("cache-path");
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        path = path + accountId.toString();
        String uri = runtime.getString("cache-uri");
        if (!uri.endsWith("/")) {
            uri = uri + "/";
        }
        uri = uri + accountId.toString();
        this.cache = cache(path, uri);
    }

    private void precache(final String text, final String gender, final String language) throws Exception {
        if(logger.isInfoEnabled()){
             logger.info("Synthesizing announcement");
        }
        final SpeechSynthesizerRequest synthesize = new SpeechSynthesizerRequest(gender, language, text);
        Timeout expires = new Timeout(Duration.create(6000, TimeUnit.SECONDS));
        Future<Object> future = (Future<Object>) ask(synthesizer, synthesize, expires);
        Object object = Await.result(future, Duration.create(6000, TimeUnit.SECONDS));
        if(object != null) {
            SpeechSynthesizerResponse<URI> response = (SpeechSynthesizerResponse<URI>)object;
            uri = response.get();
        }
        final DiskCacheRequest request = new DiskCacheRequest(uri);
        if(logger.isInfoEnabled()){
            logger.info("Caching announcement");
        }
        cache.tell(request, null);
    }

    private Announcement createFrom(String accountSid, MultivaluedMap<String, String> data) throws Exception {
        Sid sid = Sid.generate(Sid.Type.ANNOUNCEMENT);
        String gender = data.getFirst("Gender");
        if (gender == null) {
            gender = "man";
        }
        String language = data.getFirst("Language");
        if (language == null) {
            language = "en";
        }
        String text = data.getFirst("Text");
        if (text != null) {
            precache(text, gender, language);
        }
        if(logger.isInfoEnabled()){
            logger.info("Creating annnouncement");
        }
        Announcement announcement = new Announcement(sid, new Sid(accountSid), gender, language, text, uri);
        return announcement;
    }

    private ActorRef tts(final Configuration configuration) {
        final String classpath = configuration.getString("[@class]");

        final Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public Actor create() throws Exception {
                return (UntypedActor) Class.forName(classpath).getConstructor(Configuration.class).newInstance(configuration);
            }
        });
        return system.actorOf(props);
    }

    private ActorRef cache(final String path, final String uri) {
        final Props props = new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public Actor create() throws Exception {
                return new DiskCacheFactory(configuration).getDiskCache(path, uri);
            }
        });
        return system.actorOf(props);
    }

    @PreDestroy
    private void cleanup() {
        if(logger.isInfoEnabled()){
            logger.info("Stopping actors before endpoint destroy");
        }
        system.stop(cache);
        system.stop(synthesizer);
    }
}
