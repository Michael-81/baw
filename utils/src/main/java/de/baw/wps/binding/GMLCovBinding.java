package de.baw.wps.binding;

import java.io.IOException;
import java.io.StringWriter;

import net.opengis.gmlcov.x10.GridCoverageDocument;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.io.data.IComplexData;

import de.baw.wps.generator.GMLCovGenerator;

public class GMLCovBinding implements IComplexData{
	private static final long serialVersionUID = -868454895827379449L;
	protected transient GridCoverageDocument gmlcovFile;
	
	public GMLCovBinding(GridCoverageDocument payload) {
		this.gmlcovFile = payload;
	}
	
	@Override
	public GridCoverageDocument getPayload() {
		return gmlcovFile;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getSupportedClass() {
		return GridCoverageDocument.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException{
		StringWriter buffer = new StringWriter();
		GMLCovGenerator generator = new GMLCovGenerator();
		generator.write(this, buffer);
		oos.writeObject(buffer.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException, XmlException{
		
		GridCoverageDocument gmlcovFile = GridCoverageDocument.Factory.parse(ois);

		GMLCovBinding data = new GMLCovBinding(gmlcovFile);		
		
		this.gmlcovFile = data.getPayload();
	}

	//@Override
	public void dispose() {
		
	}
}
