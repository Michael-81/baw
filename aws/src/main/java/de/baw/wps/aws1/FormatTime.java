package de.baw.wps.aws1;

import java.math.BigInteger;
import java.util.List;

import net.opengis.om.x20.OMObservationDocument;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n52.wps.algorithm.annotation.Algorithm;
//import org.n52.wps.algorithm.annotation.ComplexDataInput;
//import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
//import org.n52.wps.io.data.GenericFileData;
//import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

//import de.baw.wps.utilities.ComplexDataToString;
//import de.baw.wps.utilities.StringToComplexData;
import de.baw.xml.OMdocBuilder;
import de.baw.xml.OMexplorer;

@Algorithm(version = "1.0.0", abstrakt="Format a time series from epoch time in seconds since 1.1.1900 into any other format.")
public class FormatTime {
	
	private String timeFormat;
//	private GenericFileData timeSeries;
//	private GenericFileData outputXML;
	String timeSeries,outputXML;
	
//	@ComplexDataInput(identifier="timeSeries", abstrakt="input model time series", binding=GenericFileDataBinding.class)
//	public void setTimeSeries(GenericFileData timeSeries) {
//		this.timeSeries = timeSeries;
//	}
	
	@LiteralDataInput(identifier="timeSeries", abstrakt="input time series in the O&M XML format")
	public void setTimeSeries(String timeSeries) {
	 this.timeSeries = timeSeries;
	}
	
	@LiteralDataInput(identifier="timeFormat", abstrakt="date time format")
	public void setTimeFormat(String timeFormat) {
	 this.timeFormat = timeFormat;
	}
	
//	@ComplexDataOutput(identifier = "outputXML", binding=GenericFileDataBinding.class)
//	public GenericFileData getOutputValuesOne() {
//	 return this.outputXML;
//	}
	
	@LiteralDataOutput(identifier = "outputXML", abstrakt="formatted time series in the O&M XML format", binding=LiteralStringBinding.class)
	public String getResult() {
	 return this.outputXML;
	}
	
	
	@Execute
	public void runAlgorithm() {
		DateTimeFormatter DateTimeFormatter = DateTimeFormat.forPattern(this.timeFormat);
		
//		ComplexDataToString cdts = new ComplexDataToString(this.timeSeries);
//		String contentSeries =cdts.getContentString();
//		
//		OCBuilder ocb = new OCBuilder();
//		ObservationCollection oc=null;
//		try {
//			oc = ocb.getUnmarshalledOC(contentSeries);
//		} catch (JAXBException e) {
//			throw new RuntimeException("Error handling XML");
//		}
		
		OMdocBuilder omb = new OMdocBuilder();
		OMObservationDocument om=omb.stringToDoc(timeSeries);
		
		String[] contentSeriesValues = OMexplorer.getValues(om).split(";");

		String values="";
		for(int i = 0; i < contentSeriesValues.length; i++){
			String[] contentSeriesBlock= contentSeriesValues[i].split(",");
			double time = Double.parseDouble(contentSeriesBlock[0]);
			DateTime valDt = toDateTime(time);
			values += valDt.toString(DateTimeFormatter)+","+contentSeriesBlock[1]+";";
		}
		values = values.substring(0, values.length() - 1);
		
		String title = OMexplorer.getTitle(om);
		String begin = OMexplorer.getBegin(om);
	    String end = OMexplorer.getEnd(om);
	    String procChainLink = OMexplorer.getProcedure(om)+",de.baw.wps.FormatTime";
		String parName=OMexplorer.getParameterName(om);
		String units = OMexplorer.getUnits(om);
		String observedProperty = OMexplorer.getObservedProperty(om);
		String featureOfInterest = OMexplorer.getFeatureOfInterest(om);
		List<Double> pos = OMexplorer.getPosition(om);
		
		BigInteger count = OMexplorer.getCount(om);
		DateTime dt = new DateTime();
		
    	omb.setTitle(title);
    	omb.setBegin(begin);
    	omb.setEnd(end);
    	omb.setNow(dt.toString(DateTimeFormatter));
    	omb.setProcChainLink(procChainLink);
    	omb.setParameterName(parName);
    	omb.setUnits(units);
    	omb.setNumber(count);
    	omb.setValue(values);
    	omb.setObservedProperty(observedProperty);
    	omb.setFeatureOfInterest(featureOfInterest);
    	//omb.setUUIDs(metadataUUIDs);
    	omb.setLat(pos.get(0));
    	omb.setLon(pos.get(1));
    	
    	String xmlOutput=omb.encode();
    	    	
//    	StringToComplexData stcd = new StringToComplexData(xmlOutput);
//    	
//		this.outputXML = stcd.getComplexData();
    	
    	this.outputXML = xmlOutput;
		
	}
	
	private DateTime toDateTime(double seconds) {
		long secondsLong = (long) seconds * 1000;
		DateTime dtStart = new DateTime(1900, 1, 1, 0, 0);
		return dtStart.plus(secondsLong);
	}

}
