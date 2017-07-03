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
package org.restcomm.connect.ussd;

import static org.junit.Assert.*;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.restcomm.connect.ussd.commons.UssdMessageType;
import org.restcomm.connect.ussd.commons.UssdRestcommResponse;
import org.restcomm.connect.ussd.commons.UssdInfoRequest;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
public class TestUssdMessages {

    private String requestLanguage = "en";
    private String responseMessage = "Press 1 to know your airtime and 2 to know your SMS balance";
    private UssdMessageType ussdMessageType = UssdMessageType.unstructuredSSRequest_Request;
    UssdRestcommResponse ussdRestcommResponse;

    String ussdOriginalRestcommResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ussd-data><language value=\"en\"></language>"
            + "<ussd-string value=\"Press 1 to know your airtime and 2 to know your SMS balance\"></ussd-string>"
            + "<anyExt><message-type>unstructuredSSRequest_Request</message-type></anyExt></ussd-data>";
    
    String ussdInfoRequestFromClient = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ussd-data><language value=\"en\"/>"
            + "<ussd-string value=\"1\"/><anyExt><message-type>unstructuredSSRequest_Response</message-type></anyExt></ussd-data>";
    
    String ussdInfoRequestFromClient2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ussd-data>"
            + "<ussd-string value=\"2\"/></ussd-data>";
    
    private void createUssdMessage() {
        ussdRestcommResponse = new UssdRestcommResponse();
        ussdRestcommResponse.setLanguage("en");
        ussdRestcommResponse.setMessage(responseMessage);
        ussdRestcommResponse.setMessageType(ussdMessageType);
    }
    
    @Test
    public void testUssdRestcommResponse() throws XMLStreamException{
        createUssdMessage();
        String ussdPayload = ussdRestcommResponse.createUssdPayload();
        assertTrue(ussdOriginalRestcommResponse.equals(ussdPayload.replaceAll("\\n", "")));
    }
    
    @Test
    public void testInforRequestFromClient() throws Exception {
        UssdInfoRequest ussdInfoRequest = new UssdInfoRequest(ussdInfoRequestFromClient);
        assertTrue("1".equals(ussdInfoRequest.getMessage()));
        assertTrue("en".equals(ussdInfoRequest.getLanguage()));
        assertTrue(UssdMessageType.unstructuredSSRequest_Response.equals(ussdInfoRequest.getUssdMessageType()));
        
        ussdInfoRequest = new UssdInfoRequest(ussdInfoRequestFromClient2);
        assertTrue("2".equals(ussdInfoRequest.getMessage()));
        assertTrue("en".equals(ussdInfoRequest.getLanguage()));
        assertTrue(UssdMessageType.unstructuredSSRequest_Response.equals(ussdInfoRequest.getUssdMessageType()));
    }

}
