package de.baw.xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;

import net.opengis.gml.x32.CodeType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.PointPropertyType;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePositionType;
import net.opengis.om.x20.NamedValuePropertyType;
import net.opengis.om.x20.NamedValueType;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.om.x20.TimeObjectPropertyType;
import net.opengis.swe.x20.CountPropertyType;
import net.opengis.swe.x20.CountType;
import net.opengis.swe.x20.DataArrayPropertyType;
import net.opengis.swe.x20.DataArrayType;
import net.opengis.swe.x20.UnitReference;
import net.opengis.swe.x20.DataArrayType.ElementType;



public final class OMexplorer {
	private static String gmlNamespaceDecl = "declare namespace gml='http://www.opengis.net/gml/3.2'; ";
	private static String sweNamespaceDecl = "declare namespace swe='http://www.opengis.net/swe/2.0'; ";
	
	private OMexplorer(){
		
	}
	
	public static String getTitle(OMObservationDocument om){
		CodeType name = om.getOMObservation().getNameArray(0);
		
		return name.getStringValue();
	}
	
	public static String getBegin(OMObservationDocument om){

		TimeObjectPropertyType phenomenonTime= om.getOMObservation().getPhenomenonTime();
		XmlObject[] timePeriodProperty = phenomenonTime.selectPath(gmlNamespaceDecl + "$this//gml:TimePeriod/gml:beginPosition");
		TimePositionType beginPosition= (TimePositionType)timePeriodProperty[0];

		return beginPosition.getStringValue();
	}
	
	public static String getEnd(OMObservationDocument om){
		TimeObjectPropertyType phenomenonTime= om.getOMObservation().getPhenomenonTime();
		XmlObject[] timePeriodProperty = phenomenonTime.selectPath(gmlNamespaceDecl + "$this//gml:TimePeriod/gml:endPosition");
		TimePositionType endPosition= (TimePositionType)timePeriodProperty[0];

		return endPosition.getStringValue();
	}
	
	public static String getResultTime(OMObservationDocument om){
		TimeInstantPropertyType resultTimeProperty = om.getOMObservation().getResultTime();
		TimeInstantType timeInstant = resultTimeProperty.getTimeInstant();
		TimePositionType timePosition = timeInstant.getTimePosition();

		return timePosition.getStringValue();
	}
	
	public static String getProcedure(OMObservationDocument om){
		OMProcessPropertyType procedure = om.getOMObservation().getProcedure();
		return procedure.getHref();
	}
	
	public static String getFeatureOfInterest(OMObservationDocument om){
		FeaturePropertyType foi = om.getOMObservation().getFeatureOfInterest();
		return foi.getHref();
	}
	
	public static String getObservedProperty(OMObservationDocument om){
		ReferenceType observedProperty = om.getOMObservation().getObservedProperty();
		return observedProperty.getHref();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Double> getPosition(OMObservationDocument om){
		
		NamedValuePropertyType[] parameters = om.getOMObservation().getParameterArray();
		List<Double> posValues = null;
		for(int i = 0; i < parameters.length; i++){
			NamedValueType namedValue = parameters[i].getNamedValue();
			try{
				PointPropertyType pointProperty =(PointPropertyType)namedValue.getValue();
				PointType point = pointProperty.getPoint();
				DirectPositionType pos = point.getPos();
				posValues = pos.getListValue();
				
			}catch(Exception e){
				
			}
			
		}
	
		return posValues;
	}
	
	public static List<String> getMetadataHref(OMObservationDocument om){
		List<String> metadataHref= new ArrayList<String>();
		NamedValuePropertyType[] parameters = om.getOMObservation().getParameterArray();
		
		for(int i = 0; i < parameters.length; i++){
			NamedValueType namedValue = parameters[i].getNamedValue();
			try{
				ReferenceType hrefProperty =(ReferenceType)namedValue.getValue();
				metadataHref.add(hrefProperty.getHref());				
			}catch(Exception e){				
			}			
		}
		return metadataHref;
	}
	public static String getParameterName(OMObservationDocument om){
		ReferenceType observedProperty = om.getOMObservation().getObservedProperty();
		return observedProperty.getHref();
	}
	
	public static String getModelName(OMObservationDocument om){
		FeaturePropertyType featureOfInterest = om.getOMObservation().getFeatureOfInterest();
		return featureOfInterest.getHref();
	}
	public static BigInteger getCount(OMObservationDocument om){
		DataArrayPropertyType dataArrayProperty = (DataArrayPropertyType) om.getOMObservation().getResult();
		DataArrayType dataArray = dataArrayProperty.getDataArray1();
		CountPropertyType elementCount = dataArray.getElementCount();
		CountType count = elementCount.getCount();	
		return count.getValue();
	}
	public static String getUnits(OMObservationDocument om){
		DataArrayPropertyType dataArrayProperty = (DataArrayPropertyType) om.getOMObservation().getResult();
		DataArrayType dataArray = dataArrayProperty.getDataArray1();
		ElementType elementTypeProperty = dataArray.getElementType();
				
		XmlObject[] unitReference = elementTypeProperty.selectPath(sweNamespaceDecl + "$this//swe:DataRecord/swe:field/swe:Quantity/swe:uom");
		UnitReference uom =(UnitReference)unitReference[0];
			
		return uom.getCode();
	}
	
	public static String getValues(OMObservationDocument om){
		DataArrayPropertyType dataArrayProperty = (DataArrayPropertyType) om.getOMObservation().getResult();
		
		XmlObject[] simpleValue = dataArrayProperty.selectPath(sweNamespaceDecl + "$this//swe:values");
		
		return ((SimpleValue)simpleValue[0]).getStringValue();
	}

}

