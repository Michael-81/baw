package de.baw.wps.aws2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import net.opengis.gmlcov.x10.GridCoverageDocument;
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
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import de.baw.wps.binding.*;
import de.baw.xml.*;

@Algorithm(version = "1.0.0", abstrakt="This process starts a process chain for comparison of model and measurement profile data")
public class BAWMasterProcess extends AbstractAnnotatedAlgorithm{
	private GridCoverageDocument modProfile,mesProfile,modProfileExtend,mesProfileExtend;
	private String outputXML;
		
	@ComplexDataInput(identifier="modProfile", abstrakt="Reference to a GMLCov file (WCS getCovergae) with modelled profile data", binding=GMLCovBinding.class)
    public void setModelProfile(GridCoverageDocument gmlcov) {
    	this.modProfile = gmlcov;
    }
	
	@ComplexDataInput(identifier="mesProfile", abstrakt="Reference to a GMLCov file (WCS getCovergae) with measured profile data", binding=GMLCovBinding.class)
    public void setMesProfile(GridCoverageDocument gmlcov) {
    	this.mesProfile = gmlcov;
    }
	
	@ComplexDataInput(identifier="modProfileExtend", abstrakt="Reference to a GMLCov file (WCS getCovergae) with modelled profile extend data", binding=GMLCovBinding.class)
    public void setModelProfileExtend(GridCoverageDocument gmlcov) {
    	this.modProfileExtend = gmlcov;
    }
	
	@ComplexDataInput(identifier="mesProfileExtend", abstrakt="Reference to a GMLCov file (WCS getCovergae) with measured profile extend data", binding=GMLCovBinding.class)
    public void setMesProfileExtend(GridCoverageDocument gmlcov) {
    	this.mesProfileExtend = gmlcov;
    }
	
	@LiteralDataOutput(identifier = "outputXML", abstrakt="Difference of input profiles, GMLCov-XML", binding=LiteralStringBinding.class)
	public String getOutputXML() {
	 return this.outputXML;
	}
	
	@Execute
	public void executeChain(){
		
		//GMLCov XML wird in String gewandelt
		String modProfileStr = new GMLCovBuilder().docToString(this.modProfile);
		String mesProfileStr = new GMLCovBuilder().docToString(this.mesProfile);
		String modProfileExtendStr = new GMLCovBuilder().docToString(this.modProfileExtend);
		String mesProfileExtendStr = new GMLCovBuilder().docToString(this.mesProfileExtend);

		//Das gemessene Profil wird auf das modellierte abgebildet
		HashMap<String, Object> inputsMatch = new HashMap<String, Object>();
		inputsMatch.put("mesProfile",mesProfileStr);
		inputsMatch.put("modProfileExtend", modProfileExtendStr);
		inputsMatch.put("mesProfileExtend", mesProfileExtendStr);
		String matchedData[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.aws2.MatchProfiles",inputsMatch, "GC");
				
		//Das modellierte Profil wird mit dem interpolierten verglichen
		HashMap<String, Object> inputsCompare = new HashMap<String, Object>();
		inputsCompare.put("modProfile",modProfileStr);
		inputsCompare.put("mesProfile", matchedData[0]);
		String comparedData[] = prepareExecute("http://kfkiserver:8080/wps/WebProcessingService","de.baw.wps.aws2.CompareProfiles",inputsCompare, "GC");
					
		this.outputXML=comparedData[0];
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
                	if(inputType.equals("GC")){
                    	InputStream stream = null;
                		try {
                			stream = new ByteArrayInputStream(inputValue.toString().getBytes("UTF-8"));
                		} catch (UnsupportedEncodingException e) {
                			e.printStackTrace();
                		}
                		
                		IData data = new GenericFileDataBinding(new GenericFileData(stream,"text/xml"));
                       
                		executeBuilder.addComplexData(inputName,data,"http://schemas.opengis.net/gmlcov/1.0/gmlcovAll.xsd","UTF-8","text/xml");
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
    
}
