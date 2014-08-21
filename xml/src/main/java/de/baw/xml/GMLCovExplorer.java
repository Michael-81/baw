package de.baw.xml;

import org.apache.xmlbeans.XmlObject;

import net.opengis.gml.x32.CoordinatesType;
import net.opengis.gml.x32.DataBlockType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.DomainSetType;
import net.opengis.gml.x32.RangeSetType;
import net.opengis.gmlcov.x10.AbstractDiscreteCoverageType;
import net.opengis.gmlcov.x10.GridCoverageDocument;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.DataRecordPropertyType;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.QuantityType;


public class GMLCovExplorer {
	private static String gmlNamespaceDecl = "declare namespace gml='http://www.opengis.net/gml/3.2'; ";
	private static String sweNamespaceDecl = "declare namespace swe='http://www.opengis.net/swe/2.0'; ";
		
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
	
	public static String getDescription(GridCoverageDocument gmlcovdoc){
		AbstractDiscreteCoverageType gmlcov = (AbstractDiscreteCoverageType)gmlcovdoc.getAbstractCoverage();
		
		DataRecordPropertyType rangeType = gmlcov.getRangeType();
		DataRecordType dataRecord = rangeType.getDataRecord();
		Field field = dataRecord.getFieldArray(0);
		
		XmlObject[] quantityXML = field.selectPath(sweNamespaceDecl + "$this//swe:Quantity");
		QuantityType quantity= (QuantityType)quantityXML[0];
		
		return quantity.getDescription();
	}
	
	public static String getDefinition(GridCoverageDocument gmlcovdoc){
		AbstractDiscreteCoverageType gmlcov = (AbstractDiscreteCoverageType)gmlcovdoc.getAbstractCoverage();
		
		DataRecordPropertyType rangeType = gmlcov.getRangeType();
		DataRecordType dataRecord = rangeType.getDataRecord();
		Field field = dataRecord.getFieldArray(0);
		
		XmlObject[] quantityXML = field.selectPath(sweNamespaceDecl + "$this//swe:Quantity");
		QuantityType quantity= (QuantityType)quantityXML[0];
		
		return quantity.getDefinition();
	}
	
	public static String getUom(GridCoverageDocument gmlcovdoc){
		AbstractDiscreteCoverageType gmlcov = (AbstractDiscreteCoverageType)gmlcovdoc.getAbstractCoverage();
		
		DataRecordPropertyType rangeType = gmlcov.getRangeType();
		DataRecordType dataRecord = rangeType.getDataRecord();
		Field field = dataRecord.getFieldArray(0);
		
		XmlObject[] quantityXML = field.selectPath(sweNamespaceDecl + "$this//swe:Quantity");
		QuantityType quantity= (QuantityType)quantityXML[0];
		
		return quantity.getUom().getCode();
	}
}
