package de.baw.wps.aws1;

import java.math.BigInteger;
import java.util.List;

import net.opengis.om.x20.OMObservationDocument;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

import de.baw.xml.OMdocBuilder;
import de.baw.xml.OMexplorer;


@Algorithm(version = "1.0.0", abstrakt="Calculating the difference between two harmonized time series")
public class CompareTimeSeries extends AbstractAnnotatedAlgorithm{
	private String seriesOne, seriesTwo,seriesDiff;
	
	@LiteralDataInput(identifier="seriesOne", abstrakt="First time series, O&M-XML format is expected")
	public void setSeriesOne(String seriesOne) {
	 this.seriesOne = seriesOne;
	}
	
	@LiteralDataInput(identifier="seriesTwo", abstrakt="Second time series, O&M-XML format is expected")
	public void setSeriesTwo(String seriesTwo) {
	 this.seriesTwo = seriesTwo;
	}
	
	@LiteralDataOutput(identifier = "seriesDiff", abstrakt="Difference time series, O&M XML", binding=LiteralStringBinding.class)
	public String getResult() {
	 return this.seriesDiff;
	}
	
	@Execute
	public void runCTS() {
		
		OMdocBuilder omb = new OMdocBuilder();
		
		//aus dem XML-String wird ein Document-Object geparst
		OMObservationDocument omOne=omb.stringToDoc(seriesOne);
		OMObservationDocument omTwo=omb.stringToDoc(seriesTwo);
		
		String[] contentSeriesOneValues = OMexplorer.getValues(omOne).split(";");
		String[] contentSeriesTwoValues = OMexplorer.getValues(omTwo).split(";");
        
		int length = contentSeriesOneValues.length;
		
		if(length > contentSeriesTwoValues.length){
			length = contentSeriesTwoValues.length;
		}
		
		String values="";		
		
		//Berechnen der Differenz der beiden Input-Zeitreihen. Der Ergbnis CSV-Block wird geschrieben.
		//Funktioniert in dieser Implementierung nur mit harmoniserten Zeitschritten!
		for(int i = 0; i < length; i++){
			
        	String[] contentSeriesOneBlock= contentSeriesOneValues[i].split(",");
        	String[] contentSeriesTwoblock = contentSeriesTwoValues[i].split(",");
			
			double diff = Double.parseDouble(contentSeriesOneBlock[1]) - Double.parseDouble(contentSeriesTwoblock[1]);
			values += contentSeriesOneBlock[0]+","+diff+";";
						
		}
		if (values.length()>0) {
			values = values.substring(0, values.length() - 1);
		}
		
		//Die einzelnen XML-Elemente werden extrahiert, um in den Ergebnisdatensatz integriert zu werden
		String title = "Differenz der beiden Eingangszeitreihen";
		String begin = OMexplorer.getBegin(omOne);
	    String end = OMexplorer.getEnd(omOne);		
	    String procChainLink = OMexplorer.getProcedure(omOne) + "," + OMexplorer.getProcedure(omTwo)+",de.baw.wps.CompareTimeSeries";
		String parName=OMexplorer.getParameterName(omOne);
		String units = OMexplorer.getUnits(omOne);
		BigInteger count = OMexplorer.getCount(omOne);
		String observedProperty = OMexplorer.getObservedProperty(omOne);
		String featureOfInterest = OMexplorer.getFeatureOfInterest(omOne);
		//String metadataUUIDs = (String)parameterList.get(0).getNamedValue().getValue().getContent().get(0);
		List<Double> pos = OMexplorer.getPosition(omOne);
		
		DateTimeFormatter DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		DateTime dt = new DateTime();
		
		//das Ergebnis-O&M-XML wird erzeugt
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
    	//omb.setUUIDs(metadataUUIDs+","+metadataUUIDs2);
    	omb.setLat(pos.get(0));
    	omb.setLon(pos.get(1));
    	
    	String xmlOutput=omb.encode();
    	    	
    	this.seriesDiff = xmlOutput;
	}
}
