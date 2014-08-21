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

@Algorithm(version = "1.0.0", abstrakt="Matches the Measured Profile on to the modelled Profile")
public class MatchProfiles extends AbstractAnnotatedAlgorithm{
	
	 protected static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MatchProfiles.class);
	
	private String mesProfile,modProfileExtend,mesProfileExtend;
	private String outputXML;
	
	@LiteralDataInput(identifier="mesProfile", abstrakt="GMLCov String with measured profile data")
    public void setMesProfile(String gmlcov) {
    	this.mesProfile = gmlcov;
    }
	
	@LiteralDataInput(identifier="modProfileExtend", abstrakt="GMLCov String with modelled profile extend data")
    public void setModelProfileExtend(String gmlcov) {
    	this.modProfileExtend = gmlcov;
    }
	
	@LiteralDataInput(identifier="mesProfileExtend", abstrakt="GMLCov String with with measured profile extend data")
    public void setMesProfileExtend(String gmlcov) {
    	this.mesProfileExtend = gmlcov;
    }
	
	@LiteralDataOutput(identifier = "outputXML", abstrakt="Interpolated Profile", binding=LiteralStringBinding.class)
	public String getResult() {
		return this.outputXML;
	}
	
	@Execute
	public void execute(){
		
		GMLCovBuilder gcb = new GMLCovBuilder();
		
		//aus dem XML-String wird ein Document-Object geparst
		GridCoverageDocument mesProfileDoc=gcb.stringToDoc(mesProfile);
		GridCoverageDocument modProfileExtendDoc=gcb.stringToDoc(modProfileExtend);
		GridCoverageDocument mesProfileExtendDoc=gcb.stringToDoc(mesProfileExtend);
		
		String mesProfileValues = GMLCovExplorer.getValues(mesProfileDoc);
		String modelProfileLatLon = GMLCovExplorer.getLatLon(modProfileExtendDoc);
		String mesProfileLatLon = GMLCovExplorer.getLatLon(mesProfileExtendDoc);
		String modelDepth = GMLCovExplorer.getValues(modProfileExtendDoc);
		String mesDepth = GMLCovExplorer.getValues(mesProfileExtendDoc);
		
		double[][] coordModArray = getCoordArray(modelProfileLatLon);
		double[][] coordMesArray = getCoordArray(mesProfileLatLon);
		double[] depthModArray = getDepthArray(modelDepth);
		double[] depthMesArray = getDepthArray(mesDepth);
		double[][] valueMesArray = getValueArray(mesProfileValues);
				
		double[][] valueMesArray_depthFiltered = getNearestDepths(depthModArray,depthMesArray,valueMesArray);
		
		double[][] valueMesArray_posFiltered = getNearestCoords(coordModArray,coordMesArray,valueMesArray_depthFiltered);
				
        GMLCovData gcd = new GMLCovData();
        gcd.setId("Difference Profile Values");
        gcd.setAxisLabelX("width");
        gcd.setAxisLabelY("depth");
        gcd.setDescription("desc");
        gcd.setHighGridX(valueMesArray_posFiltered.length);
        gcd.setLowGridX(1);
        gcd.setHighGridY(valueMesArray_posFiltered[0].length);
        gcd.setLowGridY(1);
        gcd.setMax(100);
        gcd.setMin(0);
        gcd.setTupleList(arrayToString(valueMesArray_posFiltered));
        gcd.setUom("m");
        gcd.setVarName("Velocity_Magnitude");
        gcd.setCoordList("");
        
        this.outputXML = GMLCovBuilder.encode(gcd,"profile");
	}
	
	private double[][] getNearestDepths(double[] depthModArray, double[] depthMesArray, double[][] valueMesArray){
		double[][] valueMesArray_filtered= new double[valueMesArray.length][depthModArray.length];
		
		for(int i = 0; i < depthModArray.length; i++){
			double diff = 99999;
			for(int j = 0; j < depthMesArray.length; j++){
				if(Math.abs(depthModArray[i]-depthMesArray[j])<diff){
					for(int k = 0; k < valueMesArray.length; k++){
						valueMesArray_filtered[k][i]=valueMesArray[k][j];
					}
					
					diff = Math.abs(depthModArray[i]-depthMesArray[j]);
				}				
			}
			
		}
				
		return valueMesArray_filtered;
	}
	
	private double[][] getNearestCoords(double[][] coordModArray, double[][] coordMesArray, double[][] valueMesArray){
		double[][] valueMesArray_filtered= new double[coordModArray.length][valueMesArray[0].length];
		
		for(int i = 0; i < coordModArray.length; i++){
			double entf = 99999999;
			int index = 0;
			for(int j = 0; j < coordMesArray.length; j++){
				double dif1 = coordModArray[i][0] - coordMesArray[j][0];
				double dif2 = coordModArray[i][1] - coordMesArray[j][1];
				double entf_it = Math.sqrt(Math.pow(dif1, 2)+Math.pow(dif2, 2));
				
				if(entf_it < entf){
					entf = entf_it;
					index = j;					
				}
				
			}
			
			for(int k = 0; k < valueMesArray[0].length; k++){
				double value1 = 0;
				double value2 = valueMesArray[index][k];
				double value3 = 0;
				
				double divider = 1;
				
				if(value2 != 0.0){
					if(index > 0){
						value1 = valueMesArray[index-1][k];
						divider = divider +1;
					}
					if(index < valueMesArray.length-1){
						value3 = valueMesArray[index+1][k];		
						divider = divider +1;
					}
				}
				valueMesArray_filtered[i][k] = (value1+value2+value3)/divider;
			}
						
		}
			
		return valueMesArray_filtered;
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
	
	private double[] getDepthArray(String depthList){
		String[] split = depthList.split(",");
		double[] depthArray= new double[split.length];
		
		for(int i = 0; i < split.length; i++){
			depthArray[i]=Double.parseDouble(split[i]);	
		}
		
		return depthArray;
		
	}
	
	private double[][] getCoordArray(String coordList){
		String[] split = coordList.split(" ");
		double[][] coordArray= new double[split.length/2][2];
		
		for(int i = 0; i < split.length/2; i++){
			coordArray[i][0]=Double.parseDouble(split[i*2]);
			coordArray[i][1]=Double.parseDouble(split[(i*2)+1]);		
		}
		
		return coordArray;
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