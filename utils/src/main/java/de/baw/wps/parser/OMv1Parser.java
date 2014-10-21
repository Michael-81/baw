package de.baw.wps.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import de.baw.wps.binding.OMv1Binding;
import net.opengis.om.x10.ObservationDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.datahandler.parser.AbstractParser;


public class OMv1Parser extends AbstractParser{
	
	private static Logger LOGGER = LoggerFactory.getLogger(OMv1Parser.class);
	
	public OMv1Parser() {
		super();
		supportedIDataTypes.add(OMv1Binding.class);
	}

	@Override
	public OMv1Binding parse(InputStream input, String mimeType, String schema) {
		String xmlContent;
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(input, writer, "UTF-8");
			xmlContent = writer.toString();
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage());
			throw new RuntimeException(e1);
		}
		xmlContent = xmlContent.replaceAll("http://www.opengis.net/om", "http://www.opengis.net/om/1.0");
		xmlContent = xmlContent.replaceAll("http://www.opengis.net/swe/0", "http://www.opengis.net/swe/1.0.1");
		
		return parseOM(xmlContent);
	}

	private OMv1Binding parseOM(String xmlData) {
		ObservationDocument omFile = null;
		try {
			omFile = ObservationDocument.Factory.parse(xmlData);
		} catch (XmlException e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException(e);
		}
		return new OMv1Binding(omFile);
	}
	
}
