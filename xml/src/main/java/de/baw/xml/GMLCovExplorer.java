package de.baw.xml;

import org.apache.xmlbeans.XmlObject;

import net.opengis.gml.x32.CoordinatesType;
import net.opengis.gml.x32.DataBlockType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.DomainSetType;
import net.opengis.gml.x32.RangeSetType;
import net.opengis.gmlcov.x10.AbstractDiscreteCoverageType;
import net.opengis.gmlcov.x10.GridCoverageDocument;


public class GMLCovExplorer {
	private static String gmlNamespaceDecl = "declare namespace gml='http://www.opengis.net/gml/3.2'; ";
	
	public static String getValues(GridCoverageDocument gmlcovdoc){
		AbstractDiscreteCoverageType gmlcov = (AbstractDiscreteCoverageType)gmlcovdoc.getAbstractCoverage();
		RangeSetType rangeSet = gmlcov.getRangeSet();
		DataBlockType dataBlock = rangeSet.getDataBlock();
		CoordinatesType tupleList = dataBlock.getTupleList();
						
		return tupleList.getStringValue();
	}
	
	public static String getLatLon(GridCoverageDocument gmlcovdoc){
		AbstractDiscreteCoverageType gmlcov = (AbstractDiscreteCoverageType)gmlcovdoc.getAbstractCoverage();
		
		DomainSetType domainSet = gmlcov.getDomainSet();
		
		XmlObject[] directPositionListType = domainSet.selectPath(gmlNamespaceDecl + "$this//gml:Curve/gml:segments/gml:LineStringSegment/gml:posList");
		
		DirectPositionListType posList = (DirectPositionListType)directPositionListType[0];
				
		return posList.getStringValue();
	}
}
