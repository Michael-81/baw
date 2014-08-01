package de.baw.xml;

public class GMLCovData {

	private String id;
	private String tupleList;
	private int lowGridX;
	private int lowGridY;
	private int highGridX;
	private int highGridY;
	private String lowerLat;
	private String lowerLon;
	private String higherLat;
	private String higherLon;
	private String axisLabelX;
	private String axisLabelY;
	private String varName;
	private String description;
	private String uom;
	private float min;
	private float max;
	private String coordList;
	
	public String getTupleList() {
		return tupleList;
	}
	public void setTupleList(String tupleList) {
		this.tupleList = tupleList;
	}
	public int getLowGridX() {
		return lowGridX;
	}
	public void setLowGridX(int lowGridX) {
		this.lowGridX = lowGridX;
	}
	public int getLowGridY() {
		return lowGridY;
	}
	public void setLowGridY(int lowGridY) {
		this.lowGridY = lowGridY;
	}
	public int getHighGridX() {
		return highGridX;
	}
	public void setHighGridX(int highGridX) {
		this.highGridX = highGridX;
	}
	public int getHighGridY() {
		return highGridY;
	}
	public void setHighGridY(int highGridY) {
		this.highGridY = highGridY;
	}
	public String getLowerLat() {
		return lowerLat;
	}
	public void setLowerLat(String lowerLat) {
		this.lowerLat = lowerLat;
	}
	public String getLowerLon() {
		return lowerLon;
	}
	public void setLowerLon(String lowerLon) {
		this.lowerLon = lowerLon;
	}
	public String getHigherLat() {
		return higherLat;
	}
	public void setHigherLat(String higherLat) {
		this.higherLat = higherLat;
	}
	public String getHigherLon() {
		return higherLon;
	}
	public void setHigherLon(String higherLon) {
		this.higherLon = higherLon;
	}
	public String getAxisLabelX() {
		return axisLabelX;
	}
	public void setAxisLabelX(String axisLabelX) {
		this.axisLabelX = axisLabelX;
	}
	public String getAxisLabelY() {
		return axisLabelY;
	}
	public void setAxisLabelY(String axisLabelY) {
		this.axisLabelY = axisLabelY;
	}
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUom() {
		return uom;
	}
	public void setUom(String uom) {
		this.uom = uom;
	}
	public double getMin() {
		return min;
	}
	public void setMin(float min) {
		this.min = min;
	}
	public double getMax() {
		return max;
	}
	public void setMax(float max) {
		this.max = max;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCoordList() {
		return coordList;
	}
	public void setCoordList(String coordList) {
		this.coordList = coordList;
	}
}

