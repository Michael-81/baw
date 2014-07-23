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

@Algorithm(version = "1.0.0", abstrakt="Maps the seriesConvert time series on the seriesModel timeSeries by computing a B-Spline interpolation")
public class BSpline {
	private String seriesModel, seriesConvert,outputXML;
	
	@LiteralDataInput(identifier="seriesModel", abstrakt="Time series to map the second one on to. O&M-XML format is expected")
	public void setSeriesModel(String seriesModel) {
	 this.seriesModel = seriesModel;
	}
	
	@LiteralDataInput(identifier="seriesConvert", abstrakt="Time series that will be mapped on the first one. O&M-XML format is expected")
	public void setSeriesConvert(String seriesConvert) {
	 this.seriesConvert = seriesConvert;
	}
	
	@LiteralDataOutput(identifier = "outputXML", abstrakt="Interpolated time series, O&M-XML", binding=LiteralStringBinding.class)
	public String getResult() {
	 return this.outputXML;
	}
	
	@Execute
	public void runBSpline() {
					
		OMdocBuilder omb = new OMdocBuilder();
		
		//aus dem XML-String wird ein Document-Object geparst
		OMObservationDocument omModel=omb.stringToDoc(seriesModel);
		OMObservationDocument omConvert=omb.stringToDoc(seriesConvert);
		
		//Die einzelnen XML-Elemente werden extrahiert, um in den Ergebnisdatensatz integriert zu werden
		String[] contentSeriesModelValues =  OMexplorer.getValues(omModel).split(";");
		String[] contentSeriesConvertValues = OMexplorer.getValues(omConvert).split(";");
        
		String title = OMexplorer.getTitle(omConvert)+" harmonized time steps (Spline Interpolation)";
		String begin = OMexplorer.getBegin(omModel);
	    String end = OMexplorer.getEnd(omModel);
		String procChainLink = OMexplorer.getProcedure(omConvert)+",de.baw.wps.BSpline";
		String parName=OMexplorer.getParameterName(omConvert);
		String units = OMexplorer.getUnits(omConvert);
		String observedProperty = OMexplorer.getObservedProperty(omConvert);
		String featureOfInterest = OMexplorer.getFeatureOfInterest(omConvert);
		BigInteger count = OMexplorer.getCount(omConvert);
		//String metadataUUIDs = (String)metadataUUIDsList.get(0).getNamedValue().getValue().getContent().get(0);
		List<Double> pos = OMexplorer.getPosition(omConvert);
		
		
        double[] dataModX = new double[contentSeriesModelValues.length];
        double[] dataMesX = new double[contentSeriesConvertValues.length];
        double[] dataMesY = new double[contentSeriesConvertValues.length];
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        
        int length = contentSeriesConvertValues.length;
        if(contentSeriesModelValues.length > length){
        	length = contentSeriesModelValues.length;
        }
        
        //der Daten-CSV-Block aus dem XML wird aufgebrochen und die X- und Y-Werte in die Arrays geschrieben
        for(int i = 0; i < length; i++){
        	if(i < contentSeriesModelValues.length){
        		String[] contentSeriesModelBlock= contentSeriesModelValues[i].split(",");
            	DateTime dateModX = dateTimeFormatter.parseDateTime(contentSeriesModelBlock[0]);
            	double dateModXsec = ((double)dateModX.getMillis())/1000;           	
            	dataModX[i]=dateModXsec;
        	}
        	if(i < contentSeriesConvertValues.length){
            	String[] contentSeriesConvertblock = contentSeriesConvertValues[i].split(",");            	
            	DateTime dateMesX = dateTimeFormatter.parseDateTime(contentSeriesConvertblock[0]);
            	double dateMesXsec = ((double)dateMesX.getMillis())/1000;               	
            	dataMesX[i]=dateMesXsec;
            	dataMesY[i]=Double.parseDouble(contentSeriesConvertblock[1]);
        	}
        }
                
        String values="";
        
        //Der Ergbnis CSV-Block wird geschrieben, wobei der Messwert auf die Modellzeitschritte interpoliert wird
        for(int i = 0; i < dataModX.length; i++){
        	String[] contentSeriesModelBlock= contentSeriesModelValues[i].split(",");
        	values+=contentSeriesModelBlock[0]+","+poly_interpolate(dataMesX, dataMesY,dataModX[i], 2)+";";
        }
      
		if (values.length()>0) {
			values = values.substring(0, values.length() - 1);
		}
		
		
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
	
	//Interpolation mit Hilfe des Gau?chen Eliminationsalgorithmus
	//Quelle: http://www.javaprogrammingforums.com/java-programming-tutorials/6121-java-tip-nov-20-2010-spline-interpolation.html
	private double poly_interpolate(double[] dataX, double[] dataY, double x, int power){
		int xIndex = 0;
		
		while (xIndex < dataX.length - (1 + power + (dataX.length - 1) % power) && dataX[xIndex + power] < x){
			xIndex += power;
		}
 
		double matrix[][] = new double[power + 1][power + 2];
		
		for (int i = 0; i < power + 1; ++i){
			for (int j = 0; j < power; ++j){
				matrix[i][j] = Math.pow(dataX[xIndex + i], (power - j));
			}
			matrix[i][power] = 1;
			matrix[i][power + 1] = dataY[xIndex + i];
		}
		
		double[] coefficients = lin_solve(matrix);
		double answer = 0;
		
		for (int i = 0; i < coefficients.length; ++i){
			answer += coefficients[i] * Math.pow(x, (power - i));
		}
		
		return answer;
	}
	
	private double[] lin_solve(double[][] matrix){
		double[] results = new double[matrix.length];
		int[] order = new int[matrix.length];
		
		for (int i = 0; i < order.length; ++i){
			order[i] = i;
		}
		for (int i = 0; i < matrix.length; ++i){
			// partial pivot
			int maxIndex = i;
			for (int j = i + 1; j < matrix.length; ++j){
				if (Math.abs(matrix[maxIndex][i]) < Math.abs(matrix[j][i])){
					maxIndex = j;
				}
			}
			if (maxIndex != i){
				// swap order
				{
					int temp = order[i];
					order[i] = order[maxIndex];
					order[maxIndex] = temp;
				}
				// swap matrix
				for (int j = 0; j < matrix[0].length; ++j){
					double temp = matrix[i][j];
					matrix[i][j] = matrix[maxIndex][j];
					matrix[maxIndex][j] = temp;
				}
			}
//			if (Math.abs(matrix[i][i]) < 1e-15){
//				throw new RuntimeException("Singularity detected");
//			}
			for (int j = i + 1; j < matrix.length; ++j){
				double factor = matrix[j][i] / matrix[i][i];
				for (int k = i; k < matrix[0].length; ++k){
					matrix[j][k] -= matrix[i][k] * factor;
				}
			}
		}
		for (int i = matrix.length - 1; i >= 0; --i){
			// back substitute
			results[i] = matrix[i][matrix.length];
			for (int j = i + 1; j < matrix.length; ++j){
				results[i] -= results[j] * matrix[i][j];
			}
			results[i] /= matrix[i][i];
		}
		double[] correctResults = new double[results.length];
		for (int i = 0; i < order.length; ++i){
			// switch the order around back to the original order
			correctResults[order[i]] = results[i];
		}
		return results;
	}
}
