package de.baw.wps.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import de.baw.wps.binding.OMBinding;
import net.opengis.om.x20.OMObservationDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.datahandler.parser.AbstractParser;


public class OMParser extends AbstractParser{
	
	private static Logger LOGGER = LoggerFactory.getLogger(OMParser.class);
	
	public OMParser() {
		super();
		supportedIDataTypes.add(OMBinding.class);
	}

	@Override
	public OMBinding parse(InputStream input, String mimeType, String schema) {
		String xmlContent;
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(input, writer, "UTF-8");
			xmlContent = writer.toString();
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage());
			throw new RuntimeException(e1);
		}
		
		return parseOM(xmlContent);
	}

	private OMBinding parseOM(String xmlData) {
		OMObservationDocument omFile = null;
		try {
			omFile = OMObservationDocument.Factory.parse(xmlData);
		} catch (XmlException e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException(e);
		}
		return new OMBinding(omFile);
	}
	
}
