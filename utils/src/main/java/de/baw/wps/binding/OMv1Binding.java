package de.baw.wps.binding;

import java.io.IOException;
import java.io.StringWriter;

import net.opengis.om.x10.ObservationDocument;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.data.IComplexData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.baw.wps.generator.OMv1Generator;

public class OMv1Binding implements IComplexData {
	
	private static Logger LOGGER = LoggerFactory.getLogger(OMv1Binding.class);

    private static final long serialVersionUID = -868454895827379449L;
    protected transient ObservationDocument omFile;

    public OMv1Binding(ObservationDocument payload) {
        this.omFile = payload;
    }

    @Override
    public ObservationDocument getPayload() {
        return omFile;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Class getSupportedClass() {
        return ObservationDocument.class;
    }

    private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException {
        StringWriter buffer = new StringWriter();
        OMv1Generator generator = new OMv1Generator();
        generator.write(this, buffer);
        oos.writeObject(buffer.toString());
    }

    private synchronized void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException, XmlException {

    	String xmlString = (String) ois.readObject();
    	
    	LOGGER.info(xmlString);
    	    	
    	ObservationDocument omFile = ObservationDocument.Factory.parse(xmlString.replace("http://www.opengis.net/om ", "http://www.opengis.net/om/1.0 "));

        OMv1Binding data = new OMv1Binding(omFile);

        this.omFile = data.getPayload();
    }

    //@Override
    public void dispose() {

    }

}
