package de.baw.wps.aws1;

import java.io.IOException;
import java.math.BigInteger;


import de.baw.wps.binding.NetCDFBinding;
import de.baw.xml.OMdocBuilder;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;


@Algorithm(version = "1.0.0", abstrakt="Liest Daten aus der übergebenen NetCDF Datei ein und gibt sie als O&M XML aus")
public class ReadNetCDF extends AbstractAnnotatedAlgorithm{

	NetcdfFile inputNC;
	String varName,startTime,endTime,metadataUUID;
	String outputXML;
	
	protected static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReadNetCDF.class);
	
    @ComplexDataInput(identifier="inputNC", abstrakt="Link to NetCDF file", binding=NetCDFBinding.class)
    public void setModellNetCDF(NetcdfFile nc) {
    	this.inputNC = nc;
    }
    
	@LiteralDataInput(identifier="varName", abstrakt="variable name")
	public void setVarName(String varName) {
	 this.varName = varName;
	}
	
	@LiteralDataInput(identifier="startTime", abstrakt="Beginning of the choosen time window")
	public void setStartTime(String startTime) {
	 this.startTime = startTime;
	}
	
	@LiteralDataInput(identifier="endTime", abstrakt="End of the choosen time window")
	public void setEndTime(String endTime) {
	 this.endTime = endTime;
	}
	
	@LiteralDataInput(identifier="metadataUUID", abstrakt="UUID of the data set belonging metadataset")
	public void setMetadataUUID(String metadataUUID) {
	 this.metadataUUID = metadataUUID;
	}
			
	@LiteralDataOutput(identifier = "outputXML", abstrakt="Extracted time series. O&M-XML", binding=LiteralStringBinding.class)
	public String getResult() {
	 return this.outputXML;
	}
	
	@Execute
	public void runAlgorithm() {
			
		Variable dataVar = this.inputNC.findVariable(varName);
		String outputValues="";
		
		//Metadaten werden aus den Attributen der NetCDF-Datei ausgelesen
		String title =  this.inputNC.findGlobalAttribute("title").getStringValue();
		String parName = dataVar.findAttribute("long_name").getStringValue();
		String units = dataVar.findAttribute("units").getStringValue();
		Number geospatial_lat_min= this.inputNC.findGlobalAttribute("geospatial_lat_min").getNumericValue(); 
		Number geospatial_lon_min= this.inputNC.findGlobalAttribute("geospatial_lon_min").getNumericValue(); 
		double lonDouble = geospatial_lon_min.doubleValue();
		double latDouble=geospatial_lat_min.doubleValue();

		DateTimeFormatter DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		
		DateTime startDate = DateTimeFormatter.parseDateTime(startTime);
		DateTime endDate = DateTimeFormatter.parseDateTime(endTime);
				
		long startMili = startDate.getMillis();
		long endMili = endDate.getMillis();
		
		int length=0;
		
    	try {
   		
    		int[] size = dataVar.getShape();
    		int[] origin = new int[size.length];
    		Array dataArray;
    		
    		//Daten aus NetCDF-Datei werden eingelesen.
    		dataArray = (Array) dataVar.read(origin, size);
    					
			double time;
			double value;
			int[] shape = dataArray.getShape();
			Index index = dataArray.getIndex();
			length=shape[0];	
			
			//Es wird der Teil der Zeitserie, der zwischen gewaehltem Anfangs- und Endpunkt liegt, als CSV-Block geschrieben
			for (int i=0;i<shape[0];i++) {
					
				time = dataArray.getDouble(index.set(i,0));
				DateTime valDt = toDateTime(time);
				if (((long)valDt.getMillis() - startMili) >= 0 && (endMili - (long)valDt.getMillis()) >=0) {
					value = dataArray.getDouble(index.set(i,1));
					outputValues += valDt.toString(DateTimeFormatter) + ","+ String.valueOf(value) + ";";	
				}
			}

			if (outputValues.length()>0) {
				outputValues = outputValues.substring(0, outputValues.length() - 1);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
    	
    	DateTime dt = new DateTime();
    	BigInteger count = BigInteger.valueOf(length);
    	
    	//das Ergebnis-O&M-XML wird erzeugt
    	OMdocBuilder omb = new OMdocBuilder();
    	omb.setTitle(title);
    	omb.setBegin(startTime);
    	omb.setEnd(endTime);
    	omb.setNow(dt.toString(DateTimeFormatter));
    	omb.setProcChainLink("de.baw.wps.ReadNetCDF");
    	omb.setParameterName(parName);
    	omb.setObservedProperty("http://cf-pcmdi.llnl.gov/documents/cf-standard-names/standard-name-table/current/cf-standard-name-table.xml#"+parName);
    	omb.setUnits(units);
    	omb.setNumber(count);
    	omb.setValue(outputValues);
    	omb.setLat(latDouble);
    	omb.setLon(lonDouble);
    	omb.setFeatureOfInterest(varName);
    	   	
    	String xmlOutput=omb.encode();
    	
    	this.outputXML = xmlOutput;
    	
	}
	
	private DateTime toDateTime(double seconds) {
		long secondsLong = (long) seconds * 1000;
		//TODO Referenzwert des Zeitvektors aus NetCDF auslesen
		DateTime dtStart = new DateTime(1900, 1, 1, 0, 0);
		return dtStart.plus(secondsLong);
	}
	    
}
