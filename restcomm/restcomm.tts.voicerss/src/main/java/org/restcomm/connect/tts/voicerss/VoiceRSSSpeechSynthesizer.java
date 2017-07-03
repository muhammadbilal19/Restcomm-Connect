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
package org.restcomm.connect.tts.voicerss;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.restcomm.connect.commons.cache.HashGenerator;
import org.restcomm.connect.tts.api.GetSpeechSynthesizerInfo;
import org.restcomm.connect.tts.api.SpeechSynthesizerException;
import org.restcomm.connect.tts.api.SpeechSynthesizerInfo;
import org.restcomm.connect.tts.api.SpeechSynthesizerRequest;
import org.restcomm.connect.tts.api.SpeechSynthesizerResponse;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * @author gvagenas@gmail.com (George Vagenas)
 */
public final class VoiceRSSSpeechSynthesizer extends UntypedActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private static final List<NameValuePair> parameters;
    static {
        parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("c", "WAV"));
        parameters.add(new BasicNameValuePair("f", "8khz_16bit_mono"));
    }

    private final URI service;
    private final Map<String, String> men;

    public VoiceRSSSpeechSynthesizer(final Configuration configuration) {
        super();
        // Add the credentials.
        final String apiKey = configuration.getString("apikey");
        BasicNameValuePair apiNameValuePair = new BasicNameValuePair("key", apiKey);
        if (!parameters.contains(apiNameValuePair))
            parameters.add(apiNameValuePair);

        // Initialize the speech synthesizer state.
        service = URI.create(configuration.getString("service-root"));

        men = new HashMap<String, String>();
        load(configuration);
    }

    private SpeechSynthesizerInfo info() {
        return new SpeechSynthesizerInfo(men.keySet());
    }

    private void load(final Configuration configuration) throws RuntimeException {
        // Initialize male voices.
        men.put("ca", configuration.getString("languages.catalan"));
        men.put("zh-cn", configuration.getString("languages.chinese-china"));
        men.put("zh-hk", configuration.getString("languages.chinese-hongkong"));
        men.put("zh-tw", configuration.getString("languages.chinese-taiwan"));
        men.put("da", configuration.getString("languages.danish"));
        men.put("nl", configuration.getString("languages.dutch"));
        men.put("en-au", configuration.getString("languages.english-australia"));
        men.put("en-ca", configuration.getString("languages.english-canada"));
        men.put("en-gb", configuration.getString("languages.english-greatbritain"));
        men.put("en-in", configuration.getString("languages.english-india"));
        men.put("en", configuration.getString("languages.english-us"));
        men.put("fi", configuration.getString("languages.finish"));
        men.put("fr-ca", configuration.getString("languages.french-canada"));
        men.put("fr", configuration.getString("languages.french-france"));
        men.put("de", configuration.getString("languages.german"));
        men.put("it", configuration.getString("languages.italina"));
        men.put("ja", configuration.getString("languages.japanese"));
        men.put("ko", configuration.getString("languages.korean"));
        men.put("nb", configuration.getString("languages.norwegian"));
        men.put("pl", configuration.getString("languages.polish"));
        men.put("pt-br", configuration.getString("languages.portuguese-brasil"));
        men.put("pt", configuration.getString("languages.portuguese-portugal"));
        men.put("ru", configuration.getString("languages.russian"));
        men.put("es-mx", configuration.getString("languages.spanish-mexico"));
        men.put("es", configuration.getString("languages.spanish-spain"));
        men.put("sv", configuration.getString("languages.swedish"));
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        final Class<?> klass = message.getClass();
        final ActorRef self = self();
        final ActorRef sender = sender();

        if (SpeechSynthesizerRequest.class.equals(klass)) {
            try {
                final URI uri = synthesize(message);
                if (sender != null) {
                    sender.tell(new SpeechSynthesizerResponse<URI>(uri), self);
                }
            } catch (final Exception exception) {
                logger.error("There was an exception while trying to synthesize message: "+exception);
                if (sender != null) {
                    sender.tell(new SpeechSynthesizerResponse<URI>(exception), self);
                }
            }
        } else if (GetSpeechSynthesizerInfo.class.equals(klass)) {
            sender.tell(new SpeechSynthesizerResponse<SpeechSynthesizerInfo>(info()), self);
        }
    }

    private String getLanguage(final String language) {
        String languageCode = men.get(language);
        return languageCode;
    }

    private URI synthesize(final Object message) throws IOException, SpeechSynthesizerException {
        final SpeechSynthesizerRequest request = (SpeechSynthesizerRequest) message;

        final String gender = request.gender();
        final String language = request.language();
        final String text = request.text();

        if (language == null) {
            if(logger.isInfoEnabled()){
                logger.info("There is no suitable speaker to synthesize " + request.language());
            }
            throw new IllegalArgumentException("There is no suitable language to synthesize " + request.language());
        }

        final String hash = HashGenerator.hashMessage(gender, language, text);

        final List<NameValuePair> query = new ArrayList<NameValuePair>();
        query.addAll(parameters);
        query.add(new BasicNameValuePair("hl", getLanguage(language)));
        query.add(new BasicNameValuePair("src", text));

        final HttpPost post = new HttpPost(service);
        final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(query, "UTF-8");
        post.setEntity(entity);
        final HttpClient client = new DefaultHttpClient();
        final HttpResponse response = client.execute(post);
        final StatusLine line = response.getStatusLine();
        final int status = line.getStatusCode();

        if (status == HttpStatus.SC_OK) {

            Header[] contentType = response.getHeaders("Content-Type");

            if (contentType[0].getValue().startsWith("text")) {
                final StringBuilder buffer = new StringBuilder();
                String error = EntityUtils.toString(response.getEntity());
                logger.error("VoiceRSSSpeechSynthesizer error: " + error);
                buffer.append(error);
                throw new SpeechSynthesizerException(buffer.toString());
            }

            if(logger.isInfoEnabled()){
                logger.info("VoiceRSSSpeechSynthesizer success!");
            }
            InputStream is = response.getEntity().getContent();
            File file = new File(System.getProperty("java.io.tmpdir") + File.separator + hash + ".wav");
            final OutputStream ostream = new FileOutputStream(file);

            final byte[] buffer = new byte[1024 * 8];
            while (true) {
                final int len = is.read(buffer);
                if (len <= 0) {
                    break;
                }
                ostream.write(buffer, 0, len);
            }
            ostream.close();
            is.close();
            return file.toURI();
        } else {
            if(logger.isInfoEnabled()){
                logger.info("VoiceRSSSpeechSynthesizer error, status code: " + line.getStatusCode() + (" reason phrase: ")
                    + line.getReasonPhrase());
            }
            final StringBuilder buffer = new StringBuilder();
            buffer.append(line.getStatusCode()).append(" ").append(line.getReasonPhrase());
            throw new SpeechSynthesizerException(buffer.toString());
        }
    }
}
