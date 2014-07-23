package de.baw.wps.binding;

import java.io.IOException;
import java.io.StringWriter;

import net.opengis.om.x20.OMObservationDocument;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.data.IComplexData;

import de.baw.wps.generator.OMGenerator;

public class OMBinding implements IComplexData {

    private static final long serialVersionUID = -868454895827379449L;
    protected transient OMObservationDocument omFile;

    public OMBinding(OMObservationDocument payload) {
        this.omFile = payload;
    }

    @Override
    public OMObservationDocument getPayload() {
        return omFile;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Class getSupportedClass() {
        return OMObservationDocument.class;
    }

    private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException {
        StringWriter buffer = new StringWriter();
        OMGenerator generator = new OMGenerator();
        generator.write(this, buffer);
        oos.writeObject(buffer.toString());
    }

    private synchronized void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException, XmlException {

        OMObservationDocument omFile = OMObservationDocument.Factory.parse(ois);

        OMBinding data = new OMBinding(omFile);

        this.omFile = data.getPayload();
    }

    //@Override
    public void dispose() {

    }

}
