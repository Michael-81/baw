package de.baw.xml;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import net.opengis.gml.x32.BoundingShapeType;
import net.opengis.gml.x32.CoordinatesType;
import net.opengis.gml.x32.CurveDocument;
import net.opengis.gml.x32.CurveSegmentArrayPropertyType;
import net.opengis.gml.x32.CurveType;
import net.opengis.gml.x32.DataBlockType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.DomainSetType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.GridDocument;
import net.opengis.gml.x32.GridEnvelopeType;
import net.opengis.gml.x32.GridLimitsType;
import net.opengis.gml.x32.GridType;
import net.opengis.gml.x32.LineStringSegmentDocument;
import net.opengis.gml.x32.LineStringSegmentType;
import net.opengis.gml.x32.RangeSetType;
import net.opengis.gmlcov.x10.AbstractDiscreteCoverageType;
import net.opengis.gmlcov.x10.GridCoverageDocument;
import net.opengis.swe.x20.AllowedValuesPropertyType;
import net.opengis.swe.x20.AllowedValuesType;
import net.opengis.swe.x20.DataRecordPropertyType;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.QuantityDocument;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.RealPair;
import net.opengis.swe.x20.UnitReference;

public class GMLCovBuilder {
	
	public static String encode(GMLCovData gcd, String type){
		GridCoverageDocument gmlcovDoc = GridCoverageDocument.Factory.newInstance();
		AbstractDiscreteCoverageType gmlcov = gmlcovDoc.addNewGridCoverage();
		
		XmlCursor cursor= gmlcov.newCursor();
		cursor.toNextToken();
		cursor.insertNamespace("gml", "http://www.opengis.net/gml/3.2");
		cursor.insertNamespace("swe", "http://www.opengis.net/swe/2.0");
		cursor.insertAttributeWithValue("schemaLocation","http://www.opengis.net/gmlcov/1.0","http://schemas.opengis.net/gmlcov/1.0/gmlcovAll.xsd");
		cursor.dispose();
						
		BoundingShapeType boundedBy = gmlcov.addNewBoundedBy();
		EnvelopeType envelope = boundedBy.addNewEnvelope();
		envelope.setSrsName("http://www.opengis.net/def/crs/EPSG/0/4326");
		
		List<String> labelsValues = new ArrayList<String>();
		labelsValues.add("Lat");
		labelsValues.add("Lon");
		
		envelope.setAxisLabels(labelsValues);
		
		List<String> uomValues = new ArrayList<String>();
		uomValues.add("deg");
		uomValues.add("deg");
		
		envelope.setUomLabels(uomValues);
		
		envelope.setSrsDimension(BigInteger.valueOf(2));
		
		List<String> lowerValues = new ArrayList<String>();
		lowerValues.add(gcd.getLowerLat());
		lowerValues.add(gcd.getLowerLon());
		
		List<String> upperValues = new ArrayList<String>();
		upperValues.add(gcd.getHigherLat());
		upperValues.add(gcd.getHigherLon());
		
		DirectPositionType lowerCorner = envelope.addNewLowerCorner();
		lowerCorner.setListValue(lowerValues);
		
		DirectPositionType upperCorner = envelope.addNewUpperCorner();
		upperCorner.setListValue(upperValues);
		

		DomainSetType domainSet = gmlcov.addNewDomainSet();
		
		if(type.equals("LatLon")){
			CurveDocument curveDoc = CurveDocument.Factory.newInstance();
			CurveType curve = curveDoc.addNewCurve();
			curve.setAxisLabels(labelsValues);
			curve.setSrsName("http://www.opengis.net/def/crs/EPSG/0/4326");
			CurveSegmentArrayPropertyType segment = curve.addNewSegments();
			LineStringSegmentDocument lineStringSegmentDoc = LineStringSegmentDocument.Factory.newInstance();
			LineStringSegmentType lineStringSegment=lineStringSegmentDoc.addNewLineStringSegment();
			DirectPositionListType posList = lineStringSegment.addNewPosList();
			//posList.setStringValue(gcd.getCoordList());
			String[] split = gcd.getCoordList().split(";");
			List<String> list = new ArrayList<String>();
			for(int i = 0; i < split.length; i++){
				String[] splitPos = split[i].split(",");
				list.add(splitPos[0]);
				list.add(splitPos[1]);
			}
			posList.setListValue(list);
			segment.set(lineStringSegmentDoc);
			domainSet.set(curveDoc);
		}else if(type.equals("profile")){
			GridDocument gridDoc = GridDocument.Factory.newInstance();
			GridType grid= gridDoc.addNewGrid();
			grid.setId(gcd.getId());
			grid.setDimension(BigInteger.valueOf(2));
			GridLimitsType limits = grid.addNewLimits();
			GridEnvelopeType gridEnvelope = limits.addNewGridEnvelope();
			
			List<Integer> lowValues = new ArrayList<Integer>();
			lowValues.add(gcd.getLowGridX());
			lowValues.add(gcd.getLowGridY());
			
			List<Integer> highValues = new ArrayList<Integer>();
			highValues.add(gcd.getHighGridX());
			highValues.add(gcd.getHighGridY());
			
			gridEnvelope.setHigh(highValues);
			gridEnvelope.setLow(lowValues);
			
			List<String> lablesValues = new ArrayList<String>();
			lablesValues.add(gcd.getAxisLabelX());
			lablesValues.add(gcd.getAxisLabelY());
			
			grid.setAxisLabels(lablesValues);
			
			domainSet.set(gridDoc);
		}
				
		RangeSetType rangeSet = gmlcov.addNewRangeSet();
		DataBlockType dataBlock = rangeSet.addNewDataBlock();
		CoordinatesType tupleList = dataBlock.addNewTupleList();
		tupleList.setStringValue(gcd.getTupleList());
		
		DataRecordPropertyType rangeType = gmlcov.addNewRangeType();
		DataRecordType dataRecord = rangeType.addNewDataRecord();
		Field field = dataRecord.addNewField();
		
		QuantityDocument quantityDoc = QuantityDocument.Factory.newInstance();							
		QuantityType quantity = quantityDoc.addNewQuantity();
		quantity.setDefinition(gcd.getVarName());
		quantity.setDescription(gcd.getDescription());
		UnitReference uom = quantity.addNewUom();

		uom.setCode(gcd.getUom());
		AllowedValuesPropertyType constraint = quantity.addNewConstraint();
		AllowedValuesType allowedValues = constraint.addNewAllowedValues();
		RealPair interval = allowedValues.addNewInterval();
		interval.setStringValue(gcd.getMin()+" "+gcd.getMax());
		allowedValues.setSignificantFigures(BigInteger.valueOf(4));
		
		field.set(quantityDoc);
		field.setName("Profile");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			gmlcovDoc.save(baos, getXmlOptions());
		} catch (Exception e){
			e.printStackTrace();
		}
		

		return new String (baos.toByteArray());	
	}
	
	private static XmlOptions getXmlOptions(){
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSavePrettyPrintIndent(3);
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setCharacterEncoding("UTF-8");
		HashMap<String, String> nsMap = new HashMap<String, String>();

		nsMap.put("http://www.opengis.net/gmlcov/1.0", "gmlcov");
		nsMap.put("http://www.w3.org/1999/xlink", "xlink");
		nsMap.put("http://www.opengis.net/swe/2.0", "swe");
		nsMap.put("http://www.opengis.net/gml/3.2", "gml");
		nsMap.put("http://www.w3.com/2001/XMLSchema-instance", "xsi");
		

		xmlOptions.setSaveSuggestedPrefixes(nsMap);
		
		return xmlOptions;
		
	}
	
	public GridCoverageDocument stringToDoc(String gcdString){
		GridCoverageDocument gcd = null;
		try {
			gcd = GridCoverageDocument.Factory.parse(gcdString);
		} catch (XmlException e) {
			e.printStackTrace();
		}
		return gcd;
	}
	
	public String docToString(GridCoverageDocument gcd){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			gcd.save(baos, getXmlOptions());
		} catch (Exception e){
			e.printStackTrace();
		}
		

		return new String (baos.toByteArray());	
	}

}
