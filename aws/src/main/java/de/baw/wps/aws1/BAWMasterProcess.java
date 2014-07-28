package de.baw.wps.aws1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import net.opengis.om.x20.OMObservationDocument;
import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import de.baw.wps.binding.OMBinding;
import de.baw.xml.OMdocBuilder;
import de.baw.xml.OMexplorer;

@Algorithm(version = "1.0.0", abstrakt="This process starts a process chain for comparison of model and measurement data")
public class BAWMasterProcess extends AbstractAnnotatedAlgorithm{
	private String dataLinkMeasurement, varNameMeasurement,startTime,endTime;
	private String outputValuesModelInput="";
	private String outputValuesMeasurementInput="";
	private String outputValuesDifference="";
	private String outputValuesFFTModel="";
	private String outputValuesFFTMeasurement="";
	private OMObservationDocument inputOM;
		
    @ComplexDataInput(identifier="inputOM", abstrakt="Reference to a O&M file (SOS getObservation)", binding=OMBinding.class)
    public void setModellNetCDF(OMObservationDocument om) {
    	this.inputOM = om;
    }
    
	@LiteralDataInput(identifier="startTime", abstrakt="Beginning of the choosen time window")
	public void setStartTime(String startTime) {
	 this.startTime = startTime;
	}
	
	@LiteralDataInput(identifier="endTime", abstrakt="End of the choosen time window")
	public void setEndTime(String endTime) {
	 this.endTime = endTime;
	}
		
	@LiteralDataInput(identifier="dataLinkMeasurement", abstrakt="Link to data file in NetCDF file format")
	public void setDatalinkTwo(String dataLinkMeasurement) {
	 this.dataLinkMeasurement = dataLinkMeasurement;
	}
	
	@LiteralDataInput(identifier="varNameMeasurement", abstrakt="Name of the variable to be read from the NetCDF file")
	public void setVarNameTwo(String varNameMeasurement) {
	 this.varNameMeasurement = varNameMeasurement;
	}
	
	@LiteralDataOutput(identifier = "outputValuesModelInput", abstrakt="Input model data in the O&M XML format", binding=LiteralStringBinding.class)
	public String getOutputValuesModelInput() {
	 return this.outputValuesModelInput;
	}
	@LiteralDataOutput(identifier = "outputValuesMeasurementInput", abstrakt="Input measurement data in the O&M XML format", binding=LiteralStringBinding.class)
	public String getOutputValuesMeasurementInput() {
	 return this.outputValuesMeasurementInput;
	}
	@LiteralDataOutput(identifier = "outputValuesDifference", abstrakt="Difference time series in the O&M XML format", binding=LiteralStringBinding.class)
	public String getOutputValuesDifference() {
	 return this.outputValuesDifference;
	}
	@LiteralDataOutput(identifier = "outputValuesFFTModel", abstrakt="Single side amplitude spectrum of the model data time series. O&M-XML", binding=LiteralStringBinding.class)
	public String getOutputValuesFFTModel() {
	 return this.outputValuesFFTModel;
	}
	@LiteralDataOutput(identifier = "outputValuesFFTMeasurement", abstrakt="Single side amplitude spectrum of the measurement data time series. O&M-XML", binding=LiteralStringBinding.class)
	public String getOutputValuesFFTMeasurement() {
	 return this.outputValuesFFTMeasurement;
	}
	
	@Execute
	public void executeChain(){
		
		//O&M XML wird in String gewandelt
		String seriesModel = new OMdocBuilder().docToString(this.inputOM);

		//Einlesen der NetCDF-Datei
		HashMap<String, Object> inputsMeasurement = new HashMap<String, Object>();
		inputsMeasurement.put("inputNC",this.dataLinkMeasurement);
		inputsMeasurement.put("varName", this.varNameMeasurement);
		inputsMeasurement.put("startTime", this.startTime);
		inputsMeasurement.put("endTime", this.endTime);
		inputsMeasurement.put("metadataUUID", "");
		String measurementData[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.aws1.ReadNetCDF",inputsMeasurement, "NC");
				
		//Harmonsieren der Zeitschritte mittels Spline Interpolation
		HashMap<String, Object> inputsSpline = new HashMap<String, Object>();
		inputsSpline.put("seriesModel",seriesModel);
		inputsSpline.put("seriesConvert", measurementData[0]);
		String splineData[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.aws1.BSpline",inputsSpline, "OC");
					
		//Berechnung der Differenz der Eingangszeitreihen
		HashMap<String, Object> inputsDiff = new HashMap<String, Object>();
		inputsDiff.put("seriesOne", seriesModel);
		inputsDiff.put("seriesTwo", splineData[0]);
		String diffData[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.aws1.CompareTimeSeries",inputsDiff, "OC");
		
		//Frequenzanalyse der Modelldaten
		HashMap<String, Object> inputsFFT1 = new HashMap<String, Object>();
		inputsFFT1.put("seriesInput", seriesModel);
		String fftData1[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.aws1.ComputeFFT",inputsFFT1, "OC");
		
		//Frequenzanalyse der Messdaten
//		HashMap<String, Object> inputsFFT2 = new HashMap<String, Object>();
//		inputsFFT2.put("seriesInput", splineData[0]);
//		String fftData2[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.ComputeFFT",inputsFFT2, "OC");
		
		//TEST: ComplexData (OMObservationDocument) statt LiteralData input
		HashMap<String, Object> inputsFFT3 = new HashMap<String, Object>();
		inputsFFT3.put("seriesInput", this.inputOM);
		String fftData3[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.aws1.ComputeFFTOM",inputsFFT3, "OM");
		
		//Formatieren der Zeitangaben
//		HashMap<String, Object> inputsFormat2 = new HashMap<String, Object>();
//		inputsFormat2.put("timeSeries", measurementData[0]);
//		inputsFormat2.put("timeFormat", "yyyy-MM-dd'T'HH:mm:ssZ");
//		String formatData2[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.FormatTime",inputsFormat2, "OC");
//
//		HashMap<String, Object> inputsFormat3 = new HashMap<String, Object>();
//		inputsFormat3.put("timeSeries", diffData[0]);
//		inputsFormat3.put("timeFormat", "yyyy-MM-dd'T'HH:mm:ssZ");
//		String formatData3[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.FormatTime",inputsFormat3, "OC");
//
//				
		//Bevor die Ergebnisse zurueck gegeben werden, wird noch der Link auf die Prozesskettenbeschreibung generiert
		this.outputValuesModelInput=seriesModel;
		this.outputValuesMeasurementInput=writeProcedureLink(splineData[0]);
		this.outputValuesDifference=writeProcedureLink(diffData[0]);
		this.outputValuesFFTModel=writeProcedureLink(fftData1[0]);
		this.outputValuesFFTMeasurement=writeProcedureLink(fftData3[0]);
	}
	
	public String[] prepareExecute(String wpsURL, String processID, HashMap<String, Object> inputs, String type) {

        try {
            ProcessDescriptionType describeProcessDocument = requestDescribeProcess(wpsURL, processID);

            return executeProcess(wpsURL, processID,describeProcessDocument, inputs, type);
        
        } catch (WPSClientException e) {
            e.printStackTrace();
            String[]error={"error"};
            return error;
        } catch (IOException e) {
            e.printStackTrace();
            String[]error={"error"};
            return error;
        } catch (Exception e) {
            e.printStackTrace();
            String[]error={"error"};
            return error;
        }
    }

    public CapabilitiesDocument requestGetCapabilities(String url) throws WPSClientException {

        WPSClientSession wpsClient = WPSClientSession.getInstance();

        wpsClient.connect(url);

        CapabilitiesDocument capabilities = wpsClient.getWPSCaps(url);

        return capabilities;
    }

    public ProcessDescriptionType requestDescribeProcess(String url,String processID) throws IOException {

        WPSClientSession wpsClient = WPSClientSession.getInstance();

        ProcessDescriptionType processDescription = wpsClient.getProcessDescription(url, processID);

        return processDescription;
    }

    public String[] executeProcess(String url, String processID,ProcessDescriptionType processDescription,HashMap<String, Object> inputs, String inputType) throws Exception {
    	org.n52.wps.client.ExecuteRequestBuilder executeBuilder = new org.n52.wps.client.ExecuteRequestBuilder(processDescription);

        for (InputDescriptionType input : processDescription.getDataInputs().getInputArray()) {
            String inputName = input.getIdentifier().getStringValue();
            Object inputValue = inputs.get(inputName);
            if (input.getLiteralData() != null) {
                if (inputValue instanceof String) {
                    executeBuilder.addLiteralData(inputName,(String) inputValue);
                }
            } else if (input.getComplexData() != null) {
                if (inputValue instanceof FeatureCollection) {
                    @SuppressWarnings("rawtypes")
					IData data = new GTVectorDataBinding((FeatureCollection) inputValue);
                    executeBuilder.addComplexData(inputName,data,"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",null,"text/xml");
                }if (inputValue instanceof OMObservationDocument){
                	IData data = new OMBinding((OMObservationDocument) inputValue);
            		executeBuilder.addComplexData(inputName, data, "http://schemas.opengis.net/om/2.0/observation.xsd",null,"text/xml");
                }
                if (inputValue instanceof String) {
                	if(inputType.equals("NC")){
                		executeBuilder.addComplexDataReference(inputName,(String) inputValue,null,null, "application/x-netcdf");                 	
                	}
                	if(inputType.equals("dat")){
                		executeBuilder.addComplexDataReference(inputName,(String) inputValue,null,null, "text/plain");                 	
                	}
                	if(inputType.equals("OC")){
                    	InputStream stream = null;
                		try {
                			stream = new ByteArrayInputStream(inputValue.toString().getBytes("UTF-8"));
                		} catch (UnsupportedEncodingException e) {
                			e.printStackTrace();
                		}
                		
                		IData data = new GenericFileDataBinding(new GenericFileData(stream,"text/xml"));
                       
                		executeBuilder.addComplexData(inputName,data,"http://schemas.opengis.net/om/2.0/observation.xsd","UTF-8","text/xml");
                	}

                }

                if (inputValue == null && input.getMinOccurs().intValue() > 0) {
                    throw new IOException("Property not set, but mandatory: " + inputName);
                }
            }
        }

        for (OutputDescriptionType output : processDescription.getProcessOutputs().getOutputArray()) {
        	if(output.getComplexOutput() != null){    		
        		executeBuilder.setMimeTypeForOutput("text/xml", output.getIdentifier().getStringValue());
                executeBuilder.setSchemaForOutput("http://schemas.opengis.net/om/1.0.0/om.xsd",output.getIdentifier().getStringValue());  
        	}
        }
        
        ExecuteDocument execute = executeBuilder.getExecute();
        execute.getExecute().setService("WPS");
        WPSClientSession wpsClient = WPSClientSession.getInstance();
        Object responseObject = wpsClient.execute(url, execute);
        
        if (responseObject instanceof ExecuteResponseDocument) {
            ExecuteResponseDocument response = (ExecuteResponseDocument) responseObject;
            
            OutputDataType[] processOutputs = response.getExecuteResponse().getProcessOutputs().getOutputArray();
            String[] results = new String[processOutputs.length];
            for(int  i =0;i<processOutputs.length;i++){
            	if(processOutputs[i].getData().getComplexData() != null){
            		ByteArrayOutputStream baos = new ByteArrayOutputStream();
            		processOutputs[0].getData().getComplexData().save(baos);
            		results[i]=new String(baos.toByteArray(), "UTF-8");
            	}else if(processOutputs[i].getData().getLiteralData() != null){
            		results[i]=processOutputs[i].getData().getLiteralData().getStringValue();
            	}         	
            }
            return results;
        }
        
        throw new Exception("Exception: " + responseObject.toString());
    }
    
    private String writeProcedureLink(String input){
		OMdocBuilder omb = new OMdocBuilder();
		OMObservationDocument om=omb.stringToDoc(input);
		
		String title = OMexplorer.getTitle(om);
		String begin = OMexplorer.getBegin(om);
	    String end = OMexplorer.getEnd(om);
	    String now = OMexplorer.getResultTime(om);
	    String processes = OMexplorer.getProcedure(om);
		String parName=OMexplorer.getParameterName(om);
		String units = OMexplorer.getUnits(om);
		String observedProperty = OMexplorer.getObservedProperty(om);
		String featureOfInterest = OMexplorer.getFeatureOfInterest(om);
		List<Double> pos = OMexplorer.getPosition(om);
		
		BigInteger count = OMexplorer.getCount(om);
		String values = OMexplorer.getValues(om);

		//List<String> metadataUUIDsList = OMexplorer.getMetadataHref(om);
			
    	omb.setTitle(title);
    	omb.setBegin(begin);
    	omb.setEnd(end);
    	omb.setNow(now);
    	omb.setProcChainLink("http://kfkiserver:8080/RichWPS/getProcessChainDescription?parent=de.baw.wps.BAWMasterProcess&processes="+processes);
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

		
		return xmlOutput;
	
    }

}
