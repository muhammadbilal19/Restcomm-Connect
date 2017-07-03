package org.restcomm.connect.provisioning.number.vi.converter;

import org.restcomm.connect.provisioning.number.vi.GetDIDListResponse;
import org.restcomm.connect.provisioning.number.vi.VoipInnovationsBody;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public final class VoipInnovationsBodyConverter extends AbstractConverter {
    public VoipInnovationsBodyConverter() {
        super();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return VoipInnovationsBody.class.equals(klass);
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        Object content = null;
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            final String child = reader.getNodeName();
            if ("search".equals(child)) {
                content = (GetDIDListResponse) context.convertAnother(null, GetDIDListResponse.class);
            }
            reader.moveUp();
        }
        return new VoipInnovationsBody(content);
    }
}
