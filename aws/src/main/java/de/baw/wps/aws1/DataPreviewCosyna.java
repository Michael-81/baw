package de.baw.wps.aws1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import net.opengis.om.x10.ObservationDocument;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlOptions;
import org.geotools.feature.FeatureCollection;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.baw.wps.binding.OMBinding;
import de.baw.wps.binding.OMv1Binding;
import de.baw.xml.OMdocBuilder;
import de.baw.xml.OMexplorer;

@Algorithm(version = "1.0.0", abstrakt="Process Chain to retrieve model and measured data.")
public class DataPreviewCosyna extends AbstractAnnotatedAlgorithm{
	private String dataLinkMeasurement, varNameMeasurement,startTime,endTime;
	private String outputValuesModelInput="";
	private String outputValuesMeasurementInput="";
	private OMObservationDocument inputOMmod;
	private ObservationDocument inputOMmes;
	
	private static Logger LOGGER = LoggerFactory.getLogger(DataPreviewCosyna.class);
	
    @ComplexDataInput(identifier="inputOMmod", abstrakt="Reference to a O&M file (SOS getObservation)", binding=OMBinding.class)
    public void setModellNetCDF(OMObservationDocument om) {
    	this.inputOMmod = om;
    }
    
    @ComplexDataInput(identifier="inputOMmes", abstrakt="Reference to a O&M file (SOS getObservation)", binding=OMv1Binding.class)
    public void setModellNetCDF(ObservationDocument om) {
    	this.inputOMmes = om;
    }
	
	
	@LiteralDataOutput(identifier = "outputValuesModelInput", abstrakt="Input model data in the O&M XML format", binding=LiteralStringBinding.class)
	public String getOutputValuesModelInput() {
	 return this.outputValuesModelInput;
	}
	@LiteralDataOutput(identifier = "outputValuesMeasurementInput", abstrakt="Input measurement data in the O&M XML format", binding=LiteralStringBinding.class)
	public String getOutputValuesMeasurementInput() {
	 return this.outputValuesMeasurementInput;
	}
	
	@Execute
	public void executeChain(){
			
		this.outputValuesModelInput=new OMdocBuilder().docToString(inputOMmod);
		this.outputValuesMeasurementInput=oM1toOM2();

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
    
private String oM1toOM2() {     
    	
    	//http://sos.hzg.de/sos.py?request=GetObservation&service=SOS&offering=Pile_Hoernum1&observedProperty=Gauge&eventTime=2006-01-01T00:00:00Z/2006-12-31T23:59:59Z
        
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			this.inputOMmes.save(baos, getXmlOptions());
		} catch (Exception e){
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
			double value = Double.parseDouble(split[4]);
			outputValues+=split[0].replace("Z", "+0100")+","+(value-4)+";";
		}
		
		if (outputValues.length()>0) {
			outputValues = outputValues.substring(0, outputValues.length() - 1);
		}
				
		DateTimeFormatter DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		DateTime dt = new DateTime();
    	OMdocBuilder omb = new OMdocBuilder();
    	omb.setTitle(this.inputOMmes.getObservation().getName());
    	omb.setBegin(startTime);
    	omb.setEnd(endTime);
    	omb.setNow(dt.toString(DateTimeFormatter));
    	omb.setProcChainLink("de.baw.wps.ReadOMv1");
    	omb.setParameterName("depth");
    	omb.setObservedProperty(this.inputOMmes.getObservation().getObservedProperty().getHref());
    	omb.setUnits("urn:ogc:unit:meter");
    	omb.setNumber(count);
    	omb.setValue(outputValues);
    	omb.setLat(latDouble);
    	omb.setLon(lonDouble);
    	omb.setFeatureOfInterest(this.inputOMmes.getObservation().getFeatureOfInterest().getHref());
    	   	
        return omb.encode();
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
