package org.restcomm.connect.provisioning.number.vi.converter;

import java.util.ArrayList;
import java.util.List;

import org.restcomm.connect.provisioning.number.vi.LATA;
import org.restcomm.connect.provisioning.number.vi.RateCenter;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public final class LATAConverter extends AbstractConverter {
    public LATAConverter() {
        super();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return LATA.class.equals(klass);
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        String name = null;
        final List<RateCenter> centers = new ArrayList<RateCenter>();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            final String child = reader.getNodeName();
            if ("name".equals(child)) {
                name = reader.getValue();
            } else if ("rate_center".equals(child)) {
                final RateCenter center = (RateCenter) context.convertAnother(null, RateCenter.class);
                centers.add(center);
            }
            reader.moveUp();
        }
        return new LATA(name, centers);
    }
}
