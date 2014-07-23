package de.baw.xml;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.math.BigInteger;

import org.apache.xmlbeans.*;

import net.opengis.gml.x32.*;
import net.opengis.om.x20.*;
import net.opengis.swe.x20.*;
import net.opengis.swe.x20.CountPropertyType;
import net.opengis.swe.x20.DataArrayType.ElementType;
import net.opengis.swe.x20.DataArrayType.Encoding;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.QuantityDocument;
import net.opengis.swe.x20.TimeType;

public class OMdocBuilder {
	
	private String title="";
	private String begin="";
	private String end="";
	private String now="";
	private String procChainLink="";
	private String parameterName="";
	private String observedProperty="";
	private String units="";
	private String blockSeperator=";";
	private String decimalSeperator=".";
	private String tokenSeperator=",";
	private String value="";
	private String modelLongName="";
	private String featureOfInterest="";
	private List<String> metadataList = new ArrayList<String>();
	private double lon=0.0;
	private double lat=0.0;
	private BigInteger number;
	
	
	private XmlOptions getXmlOptions(){
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSavePrettyPrintIndent(3);
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setCharacterEncoding("UTF-8");
		HashMap<String, String> nsMap = new HashMap<String, String>();

		nsMap.put("http://www.opengis.net/om/2.0", "om");
		nsMap.put("http://www.opengis.net/gml/3.2", "gml");
		nsMap.put("http://www.w3.org/1999/xlink", "xlink");
		nsMap.put("http://www.opengis.net/swe/2.0", "swe");

		xmlOptions.setSaveSuggestedPrefixes(nsMap);
		
		return xmlOptions;
		
	}
	
	public String encode(){
				
		OMObservationDocument omd = OMObservationDocument.Factory.newInstance();
		OMObservationType om = omd.addNewOMObservation();
		
		XmlCursor cursor= om.newCursor();
		cursor.toNextToken();
		cursor.insertNamespace("swe", "http://www.opengis.net/swe/2.0");
		cursor.insertNamespace("gml", "http://www.opengis.net/gml/3.2");
		cursor.insertAttributeWithValue("schemaLocation","http://www.w3.com/2001/XMLSchema-instance", "http://schemas.opengis.net/om/2.0/observation.xsd");
		cursor.dispose();
				
		//gml:name
		CodeType ct = om.addNewName();
		ct.setStringValue(this.title);
		
		//om:type
		ReferenceType rt = om.addNewType();
		rt.setHref("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_SWEObservation");
		
		//om:phenomenonTime
		TimeObjectPropertyType phenomenonTime= om.addNewPhenomenonTime();
		TimePeriodPropertyType timePeriodProperty = TimePeriodPropertyType.Factory.newInstance();
		TimePeriodType timePeriod = timePeriodProperty.addNewTimePeriod();
		timePeriod.setId(""+ getGmlId());
		TimePositionType beginPosition= timePeriod.addNewBeginPosition();
		beginPosition.setStringValue(this.begin);
		TimePositionType endPosition= timePeriod.addNewEndPosition();
		endPosition.setStringValue(this.end);
		phenomenonTime.set(timePeriodProperty);
		
		//om:resultTime
		TimeInstantPropertyType resultTimeProperty = om.addNewResultTime();
		TimeInstantType timeInstant = resultTimeProperty.addNewTimeInstant();
		timeInstant.setId(""+getGmlId());
		TimePositionType timePosition = timeInstant.addNewTimePosition();
		timePosition.setStringValue(this.now);
		
		//om:procedure
		OMProcessPropertyType procedure = om.addNewProcedure();
		procedure.setHref(this.procChainLink);
		
		//om:parameter Position
		NamedValuePropertyType parameter = om.addNewParameter();
		NamedValueType namedValue = parameter.addNewNamedValue();
		ReferenceType name = namedValue.addNewName();
		name.setHref("http://www.opengis.net/def/param-name/OGC-OM/2.0/samplingGeometry");
		PointPropertyType pointProperty = PointPropertyType.Factory.newInstance();
		PointType point = pointProperty.addNewPoint();
		point.setId(""+getGmlId());
		DirectPositionType pos = point.addNewPos();
		pos.setSrsName("urn:ogc:def:crs:EPSG:4326");
		List<Double> posValues = new ArrayList<Double>();
		posValues.add(this.lat);
		posValues.add(this.lon);
		pos.setListValue(posValues);
		namedValue.setValue(pointProperty);
		
		//om:parameter Metadata
		for(int i = 0; i < metadataList.size(); i++){
			NamedValuePropertyType parameterMD = om.addNewParameter();
			NamedValueType namedValueMD = parameterMD.addNewNamedValue();
			ReferenceType nameMD = namedValueMD.addNewName();
			nameMD.setHref("http://mdi-dienste.baw.de/def/param-name/OGC-OM/2.0/MetadataLink");
			ReferenceType metadataLink = ReferenceType.Factory.newInstance();
			metadataLink.setHref(metadataList.get(i));
			namedValueMD.setValue(metadataLink);
		}
		
		//om:observedProperty
		ReferenceType observedProperty = om.addNewObservedProperty();
		observedProperty.setHref(this.observedProperty);
		
		//om:featureOfInterest
		FeaturePropertyType featureOfInterest = om.addNewFeatureOfInterest();
		featureOfInterest.setHref(this.featureOfInterest);
		featureOfInterest.setRole("baw:def:model:mesh_location");
		
		//om:result
		DataArrayPropertyType dataArrayProperty = DataArrayPropertyType.Factory.newInstance();
		
		//swe:ElementCount
		DataArrayType dataArray = dataArrayProperty.addNewDataArray1();
		CountPropertyType elementCount = dataArray.addNewElementCount();
		CountType count = elementCount.addNewCount();	
		count.setValue(this.number);	
		
		//swe:ElementType
		ElementType elementTypeProperty = dataArray.addNewElementType();
		elementTypeProperty.setName("ModelDataType");
		DataRecordDocument dataRecordDoc = DataRecordDocument.Factory.newInstance();
		DataRecordType dataRecord = dataRecordDoc.addNewDataRecord();
		dataRecord.setId(this.modelLongName);
		
		//swe:Field Parameter
		Field field = dataRecord.addNewField();
		QuantityDocument quantityDoc = QuantityDocument.Factory.newInstance();
		QuantityType quantity = quantityDoc.addNewQuantity();
		quantity.setDefinition(this.observedProperty);
		UnitReference uom = quantity.addNewUom();
		uom.setCode(this.units);
		quantity.setLabel(this.units);
		field.set(quantityDoc);
		field.setName(this.parameterName);
		
		//swe:Field Zeit
		Field fieldTime = dataRecord.addNewField();
		TimeDocument timeDoc = TimeDocument.Factory.newInstance();
		TimeType time = timeDoc.addNewTime();
		time.setDefinition("http://www.opengis.net/def/property/OGC/0/SamplingTime");
		UnitReference uomTime = time.addNewUom();
		uomTime.setCode("http://www.opengis.net/def/uom/ISO-8601/0/Gregorian");
		time.setLabel("Modellzeit");
		fieldTime.set(timeDoc);
		fieldTime.setName("time");
		
		
		
		elementTypeProperty.set(dataRecordDoc);
		
		//swe:TextEncoding
		Encoding encoding = dataArray.addNewEncoding();
		TextEncodingDocument textBlockDoc = TextEncodingDocument.Factory.newInstance();
		TextEncodingType textBlock = textBlockDoc.addNewTextEncoding();
		textBlock.setBlockSeparator(this.blockSeperator);
		textBlock.setDecimalSeparator(this.decimalSeperator);
		textBlock.setTokenSeparator(this.tokenSeperator);	
		encoding.set(textBlockDoc);
		

		//swe:values
		EncodedValuesPropertyType values = dataArray.addNewValues();
		XmlString xbValueString = XmlString.Factory.newInstance();
        xbValueString.setStringValue(this.value);
		values.set(xbValueString);
		
		om.setResult(dataArrayProperty);
		
		return docToString(omd);		
	}
	
	public String docToString(OMObservationDocument omd){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			omd.save(baos, getXmlOptions());
		} catch (Exception e){
			e.printStackTrace();
		}
		

		return new String (baos.toByteArray());	
	}
	
	public OMObservationDocument stringToDoc(String omdString){
		OMObservationDocument omd = null;
		try {
			omd = OMObservationDocument.Factory.parse(omdString);
		} catch (XmlException e) {
			e.printStackTrace();
		}
		return omd;
	}
	
	private int getGmlId() {
		return (int) (Math.random()*100000);
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setBegin(String begin){
		this.begin = begin;
	}
	
	public void setEnd(String end){
		this.end = end;
	}
	
	public void setNow(String now){
		this.now = now;
	}
	
	public void setProcChainLink(String procChainLink){
		this.procChainLink = procChainLink;
	}
	
	public void setParameterName(String parameterName){
		this.parameterName = parameterName;
	}
	
	public void setObservedProperty(String observedProperty){
		this.observedProperty = observedProperty;
	}
	
	public void setUnits(String units){
		this.units = units;
	}
	
	public void setBlockSeperator(String blockSeperator){
		this.blockSeperator = blockSeperator;
	}
	
	public void setDecimalSeperator(String decimalSeperator){
		this.decimalSeperator = decimalSeperator;
	}
	
	public void setTokenSeperator(String tokenSeperator){
		this.tokenSeperator = tokenSeperator;
	}
	
	public void setNumber(BigInteger number){
		this.number = number;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public void setModelLongName(String modelLongName){
		this.modelLongName = modelLongName;
	}
	
	public void setFeatureOfInterest(String featureOfInterest){
		this.featureOfInterest = featureOfInterest;
	}
	
	public void setLon(double lon){
		this.lon = lon;
	}
	
	public void setLat(double lat){
		this.lat = lat;
	}
	
	public void setMetadataList(List<String> metadataList){
		this.metadataList = metadataList;
	}
	
}
