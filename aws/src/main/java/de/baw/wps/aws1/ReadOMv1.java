package de.baw.wps.aws1;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.HashMap;

import de.baw.wps.binding.OMv1Binding;
import de.baw.xml.OMdocBuilder;
import net.opengis.om.x10.ObservationDocument;

import org.apache.xmlbeans.XmlOptions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Algorithm(version = "1.0.0", abstrakt = "Transforming ComplexData O&M to LiteralData")
public class ReadOMv1 extends AbstractAnnotatedAlgorithm {
	
	private static Logger LOGGER = LoggerFactory.getLogger(ReadOMv1.class);

    String outputXML;
    ObservationDocument inputOM;

    @ComplexDataInput(identifier = "inputOM", abstrakt = "Reference to a O&M file (SOS getObservation)", binding = OMv1Binding.class)
    public void inputOMv1(ObservationDocument om) {
        this.inputOM = om;
    }

    @LiteralDataOutput(identifier = "outputXML", abstrakt = "O&M-XML String", binding = LiteralStringBinding.class)
    public String getResult() {
        return this.outputXML;
    }

    @Execute
    public void execute() {     
    	
    	//http://sos.hzg.de/sos.py?request=GetObservation&service=SOS&offering=Pile_Hoernum1&observedProperty=Gauge&eventTime=2006-01-01T00:00:00Z/2006-12-31T23:59:59Z
        
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			inputOM.save(baos, getXmlOptions());
		} catch (Exception e){
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
		
		String inputString = new String (baos.toByteArray());	

		String resultString = inputString.split("<om:result>")[1].split("</om:result>")[0];

		String[] blocks = resultString.split("\\|");
		String startTime = blocks[0].split(",")[0];
		String endTime = blocks[blocks.length-1].split(",")[0];
		double latDouble = Double.parseDouble(blocks[0].split(",")[1]);
		double lonDouble = Double.parseDouble(blocks[0].split(",")[2]);
		BigInteger count = BigInteger.valueOf(blocks.length);
		
		String outputValues ="";
		for(int i = 0; i < blocks.length; i++){
			String[] split = blocks[i].split(",");
			outputValues+=split[0]+","+split[4]+";";
		}
		
		if (outputValues.length()>0) {
			outputValues = outputValues.substring(0, outputValues.length() - 1);
		}
				
		DateTimeFormatter DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		DateTime dt = new DateTime();
    	OMdocBuilder omb = new OMdocBuilder();
    	omb.setTitle(inputOM.getObservation().getName());
    	omb.setBegin(startTime);
    	omb.setEnd(endTime);
    	omb.setNow(dt.toString(DateTimeFormatter));
    	omb.setProcChainLink("de.baw.wps.ReadOMv1");
    	omb.setParameterName("depth");
    	omb.setObservedProperty(inputOM.getObservation().getObservedProperty().getHref());
    	omb.setUnits("urn:ogc:unit:meter");
    	omb.setNumber(count);
    	omb.setValue(outputValues);
    	omb.setLat(latDouble);
    	omb.setLon(lonDouble);
    	omb.setFeatureOfInterest(inputOM.getObservation().getFeatureOfInterest().getHref());
    	   	
        this.outputXML = omb.encode();
    }
    
	private XmlOptions getXmlOptions(){
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSavePrettyPrintIndent(3);
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setCharacterEncoding("UTF-8");
		HashMap<String, String> nsMap = new HashMap<String, String>();

		nsMap.put("http://www.opengis.net/om/1.0.0", "om");
		nsMap.put("http://www.opengis.net/gml/3.1.1", "gml");
		nsMap.put("http://www.w3.org/1999/xlink", "xlink");
		nsMap.put("http://www.opengis.net/swe/1.0.1", "swe");

		xmlOptions.setSaveSuggestedPrefixes(nsMap);
		
		return xmlOptions;	
	}

}
