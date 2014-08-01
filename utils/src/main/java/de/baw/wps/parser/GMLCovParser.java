package de.baw.wps.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import de.baw.wps.binding.GMLCovBinding;
import net.opengis.gmlcov.x10.GridCoverageDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.datahandler.parser.AbstractParser;


public class GMLCovParser extends AbstractParser{
	
	private static Logger LOGGER = LoggerFactory.getLogger(GMLCovParser.class);
	
	public GMLCovParser() {
		super();
		supportedIDataTypes.add(GMLCovBinding.class);
	}

	@Override
	public GMLCovBinding parse(InputStream input, String mimeType, String schema) {
		String xmlContent;
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(input, writer, "UTF-8");
			xmlContent = writer.toString();
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage());
			throw new RuntimeException(e1);
		}
		
		return parseGMLCov(xmlContent);
	}

	private GMLCovBinding parseGMLCov(String xmlData) {
		GridCoverageDocument gmlCovFile = null;
		try {
			gmlCovFile = GridCoverageDocument.Factory.parse(xmlData);
		} catch (XmlException e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException(e);
		}
		return new GMLCovBinding(gmlCovFile);
	}
	
}
