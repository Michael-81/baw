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
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.baw.fft.*;
import de.baw.xml.OMdocBuilder;
import de.baw.xml.OMexplorer;

@Algorithm(version = "1.0.0", abstrakt="Frequency analysis using FFT of the input time series")
public class ComputeFFT extends AbstractAnnotatedAlgorithm {
	
	private String seriesInput, outputXML;
	private DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
	
	private static Logger LOGGER = LoggerFactory.getLogger(ComputeFFT.class);
	
	@LiteralDataInput(identifier="seriesInput", abstrakt="Time series with equidistant time steps. O&M-XML format is expected")
	public void setSeriesInput(String seriesInput) {
	 this.seriesInput = seriesInput;
	}
	
	@LiteralDataOutput(identifier = "outputXML", abstrakt="Single side amplitude spectrum of the input time series. O&M-XML", binding=LiteralStringBinding.class)
	public String getResult() {
	 return this.outputXML;
	}
	
	@Execute
	public void runFFT() {
		OMdocBuilder omb = new OMdocBuilder();
		
		LOGGER.info("ComputeFFT: "+seriesInput);
		
		//aus dem XML-String wird ein Document-Object geparst
		OMObservationDocument omInput=omb.stringToDoc(seriesInput);

		String[] contentSeriesInputValues = OMexplorer.getValues(omInput).split(";");
		
		//Siehe: http://www.mathworks.de/de/help/matlab/ref/fft.html
		//Frequenz
    	double Fs = 1.0/getDeltaT(contentSeriesInputValues);
    	//Laenge der Zeitreihe, nächste Potenz von 2 der Signallaenge
        int L = (contentSeriesInputValues.length>1 ? Integer.highestOneBit(contentSeriesInputValues.length-1)<<1 : 1);
        
        if(L > contentSeriesInputValues.length){
        	L = L/2;
        }
        
        //Funktionswerte werden in komplexe Zahlen umgewandelt.
        Complex[] x = new Complex[L];

        for (int i = 0; i < L; i++) {
        	String[] temp = contentSeriesInputValues[i].split(",");
            x[i] = new Complex(Double.parseDouble(temp[1]), 0);        	
        }

        //FFT wird ausgefuehrt
        //Quelle: http://introcs.cs.princeton.edu/java/97data/FFT.java.html
        Complex[] y = FFT.fft(x);
        
        double[] yAbs = new double[L/2];
        double[] f = new double[L/2];
        double step=1.0/(((double)L/2.0)+1.0);
        
        //Amplitude (yAbs) und Frequenz wird berechnet
        for (int i = 0; i < L/2; i++) {
        	yAbs[i]=2.0*(y[i].abs()/(double)contentSeriesInputValues.length);     	
        	f[i]=((double)i*step)*Fs/2.0;
        }
		
		String values="";
		
		//Ergebnisse werden als CSV-Block geschrieben
		for(int i = 0; i < L/2; i++){
			values+=f[i]+","+yAbs[i]+";";
		}
		
		if (values.length()>0) {
			values = values.substring(0, values.length() - 1);
		}
				
		//Die einzelnen XML-Elemente werden extrahiert, um in den Ergebnisdatensatz integriert zu werden
		String title = OMexplorer.getTitle(omInput)+" - single sided amplitude spectrum";
		String begin = OMexplorer.getBegin(omInput);
	    String end = OMexplorer.getEnd(omInput);		
	    String procChainLink = OMexplorer.getProcedure(omInput)+",de.baw.wps.ComputeFFT";
	    String parName="|Y(t)|";		
		String units = "";
		String observedProperty = "http://mdi-dienste/parameter#|Y(t)|";
		String featureOfInterest = OMexplorer.getFeatureOfInterest(omInput);
		//String metadataUUIDs = (String)parameterList.get(0).getNamedValue().getValue().getContent().get(0);
		List<Double> pos = OMexplorer.getPosition(omInput);
    	
		BigInteger count = BigInteger.valueOf(L/2);
		
		DateTime dt = new DateTime();
		    	
		//das Ergebnis-O&M-XML wird erzeugt
    	omb.setTitle(title);
    	omb.setBegin(begin);
    	omb.setEnd(end);
    	omb.setNow(dt.toString(dateTimeFormatter));
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
    	
    	this.outputXML = xmlOutput;
	}
	
	private double getDeltaT(String[] input){
		String block1=input[0];
		String block2=input[1];
		String t1=block1.split(",")[0];
		String t2=block2.split(",")[0];
		
    	DateTime datet1 = dateTimeFormatter.parseDateTime(t1);
    	DateTime datet2 = dateTimeFormatter.parseDateTime(t2);
    	double datet1sec = ((double)datet1.getMillis())/1000;   
    	double datet2sec = ((double)datet2.getMillis())/1000; 
		
		return datet2sec-datet1sec;
	}

}
