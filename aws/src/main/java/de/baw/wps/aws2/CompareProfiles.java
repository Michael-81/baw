package de.baw.wps.aws2;

import net.opengis.gmlcov.x10.GridCoverageDocument;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import de.baw.xml.GMLCovBuilder;
import de.baw.xml.GMLCovData;
import de.baw.xml.GMLCovExplorer;

@Algorithm(version = "1.0.0", abstrakt="Compares profile data. Dimensions must be equal.")
public class CompareProfiles extends AbstractAnnotatedAlgorithm{
	
	 protected static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CompareProfiles.class);
	
	private String modProfile,mesProfile;
	private String outputXML;
		
	@LiteralDataInput(identifier="modProfile", abstrakt="GMLCov String with modelled profile data")
    public void setModProfile(String gmlcov) {
    	this.modProfile = gmlcov;
    }
	
	@LiteralDataInput(identifier="mesProfile", abstrakt="GMLCov String with measured profile data")
    public void setMesProfile(String gmlcov) {
    	this.mesProfile = gmlcov;
    }
		
	@LiteralDataOutput(identifier = "outputXML", abstrakt="Difference of input profiles, GMLCov-XML", binding=LiteralStringBinding.class)
	public String getResult() {
		return this.outputXML;
	}
	
	@Execute
	public void execute(){
		
		GMLCovBuilder gcb = new GMLCovBuilder();
		
		//aus dem XML-String wird ein Document-Object geparst
		GridCoverageDocument modProfileDoc=gcb.stringToDoc(modProfile);
		GridCoverageDocument mesProfileDoc=gcb.stringToDoc(mesProfile);
		
		String modelProfileValues = GMLCovExplorer.getValues(modProfileDoc);
		String mesProfileValues = GMLCovExplorer.getValues(mesProfileDoc);

		double[][] valueModArray = getValueArray(modelProfileValues);
		double[][] valueMesArray_posFiltered = getValueArray(mesProfileValues);

		
		double[][] valueDifArray = compareValueArrays(valueModArray,valueMesArray_posFiltered);		
		
        GMLCovData gcd = new GMLCovData();
        gcd.setId("Difference Profile Values");
        gcd.setAxisLabelX("width");
        gcd.setAxisLabelY("depth");
        gcd.setDescription("desc");
        gcd.setHighGridX(valueDifArray.length);
        gcd.setLowGridX(1);
        gcd.setHighGridY(valueDifArray[0].length);
        gcd.setLowGridY(1);
        gcd.setMax(100);
        gcd.setMin(0);
        gcd.setTupleList(arrayToString(valueDifArray));
        gcd.setUom("m");
        gcd.setVarName("Velocity_Magnitude");
        gcd.setCoordList("");
        
        this.outputXML = GMLCovBuilder.encode(gcd,"profile");
	}
		
	private double[][] compareValueArrays(double[][] valueModArray, double[][] valueMesArray){
		double[][] valueDifArray= new double[valueModArray.length][valueModArray[0].length];
		
		for(int i = 0; i < valueModArray.length; i++){
			for(int j = 0; j < valueModArray[0].length; j++){
				if(valueModArray[i][j] == 0.0 || valueMesArray[i][j] == 0.0){
					valueDifArray[i][j] = 0;
				}else{
					valueDifArray[i][j] = Math.abs(valueModArray[i][j] - valueMesArray[i][j]);
				}
			}
		}
		
		return valueDifArray;
	}
	
	private double[][] getValueArray(String valueList){
		String[] split = valueList.split(";");
		String[] splittmp = split[0].split(",");
		double[][] valueArray= new double[splittmp.length][split.length];
		
		for(int i = 0; i < split.length; i++){
			String[] split2 = split[i].split(",");
			for(int j = 0; j < split2.length; j++){
				valueArray[j][i]=Double.parseDouble(split2[j]);
				valueArray[j][i]=Double.parseDouble(split2[j]);	
			}
		}
		
		return valueArray;
	}
		
	private String arrayToString(double[][] valueArray){
    	String cov ="";
	    	
		for (int i=0;i<valueArray[0].length;i++) {  
			for (int j=0;j<valueArray.length;j++) {
				
    			cov += valueArray[j][i]+",";
			}
			cov = cov.substring(0,cov.length()-1);
			cov += ";";   	
    	}
 	
    	cov = cov.substring(0,cov.length()-1);
        return cov;
	}
}
