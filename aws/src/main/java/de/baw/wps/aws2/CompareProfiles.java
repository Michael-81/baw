package de.baw.wps.aws2;

import net.opengis.gmlcov.x10.GridCoverageDocument;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import de.baw.wps.binding.GMLCovBinding;
import de.baw.xml.GMLCovBuilder;
import de.baw.xml.GMLCovData;
import de.baw.xml.GMLCovExplorer;

@Algorithm(version = "1.0.0", abstrakt="Reads and compares profile data")
public class CompareProfiles extends AbstractAnnotatedAlgorithm{
	
	 protected static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CompareProfiles.class);
	
	private GridCoverageDocument modelProfile,mesProfile,modelProfileExtend,mesProfileExtend;
	private String outputXML;
	
	@ComplexDataInput(identifier="modelProfile", abstrakt="Reference to a GMLCov file (WCS getCovergae) with modelled profile data", binding=GMLCovBinding.class)
    public void setModelProfile(GridCoverageDocument gmlcov) {
    	this.modelProfile = gmlcov;
    }
	
	@ComplexDataInput(identifier="mesProfile", abstrakt="Reference to a GMLCov file (WCS getCovergae) with measured profile data", binding=GMLCovBinding.class)
    public void setMesProfile(GridCoverageDocument gmlcov) {
    	this.mesProfile = gmlcov;
    }
	
	@ComplexDataInput(identifier="modelProfileExtend", abstrakt="Reference to a GMLCov file (WCS getCovergae) with modelled profile data", binding=GMLCovBinding.class)
    public void setModelProfileExtend(GridCoverageDocument gmlcov) {
    	this.modelProfileExtend = gmlcov;
    }
	
	@ComplexDataInput(identifier="mesProfileExtend", abstrakt="Reference to a GMLCov file (WCS getCovergae) with measured profile data", binding=GMLCovBinding.class)
    public void setMesProfileExtend(GridCoverageDocument gmlcov) {
    	this.mesProfileExtend = gmlcov;
    }
	
	@LiteralDataOutput(identifier = "outputXML", abstrakt="Difference of input profiles, GMLCov-XML", binding=LiteralStringBinding.class)
	public String getResult() {
		return this.outputXML;
	}
	
	@Execute
	public void execute(){
		
		String modelProfileValues = GMLCovExplorer.getValues(modelProfile);
		String mesProfileValues = GMLCovExplorer.getValues(mesProfile);
		String modelProfileLatLon = GMLCovExplorer.getLatLon(modelProfileExtend);
		String mesProfileLatLon = GMLCovExplorer.getLatLon(mesProfileExtend);
		String modelDepth = GMLCovExplorer.getValues(modelProfileExtend);
		String mesDepth = GMLCovExplorer.getValues(mesProfileExtend);
		
		double[][] coordModArray = getCoordArray(modelProfileLatLon);
		double[][] coordMesArray = getCoordArray(mesProfileLatLon);
		double[] depthModArray = getDepthArray(modelDepth);
		double[] depthMesArray = getDepthArray(mesDepth);
		double[][] valueModArray = getValueArray(modelProfileValues);
		double[][] valueMesArray = getValueArray(mesProfileValues);
				
		double[][] valueMesArray_depthFiltered = getNearestDepths(depthModArray,depthMesArray,valueMesArray);
		
		double[][] valueMesArray_posFiltered = getNearestCoords(coordModArray,coordMesArray,valueMesArray_depthFiltered);
		
		double[][] valueDifArray = compareValueArrays(valueModArray,valueMesArray_posFiltered);		
		
        GMLCovData gcd = new GMLCovData();
        gcd.setId("Difference Profile Values");
        gcd.setAxisLabelX("width");
        gcd.setAxisLabelY("depth");
        gcd.setDescription("desc");
        gcd.setHigherLat("2");
        gcd.setLowerLat("1");
        gcd.setHigherLon("2");
        gcd.setLowerLon("1");
        gcd.setMax(100);
        gcd.setMin(0);
        gcd.setTupleList(arrayToString(valueDifArray));
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
				
				if(index > 0){
					value1 = valueMesArray[index-1][k];
					divider = divider +1;
				}
				if(index < valueMesArray.length-1){
					value3 = valueMesArray[index+1][k];		
					divider = divider +1;
				}
				
				valueMesArray_filtered[i][k] = (value1+value2+value3)/divider;
			}
						
		}
			
		return valueMesArray_filtered;
	}
	
	private double[][] compareValueArrays(double[][] valueModArray, double[][] valueMesArray){
		double[][] valueDifArray= new double[valueModArray.length][valueModArray[0].length];
		
		for(int i = 0; i < valueModArray.length; i++){
			for(int j = 0; j < valueModArray[0].length; j++){
				valueDifArray[i][j] = valueModArray[i][j] - valueMesArray[i][j];
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
