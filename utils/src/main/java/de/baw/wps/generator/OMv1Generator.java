package de.baw.wps.generator;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.opengis.om.x10.ObservationDocument;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

import de.baw.wps.binding.OMv1Binding;

public class OMv1Generator extends AbstractGenerator{
	
	public OMv1Generator() {
		super();
		supportedIDataTypes.add(OMv1Binding.class);
	}

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		
		File tempFile = null;
		InputStream stream = null;

		try {
			tempFile = File.createTempFile("om1", "xml");
			this.finalizeFiles.add(tempFile);
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			this.writeToStream(data, outputStream);
			outputStream.flush();
			outputStream.close();
	
			stream = new FileInputStream(tempFile);
		} catch (IOException e){
			throw new IOException("Unable to generate OM");
		}

		return stream;
	}
	
	public void writeToStream(IData coll, OutputStream os) {
		OutputStreamWriter w = new OutputStreamWriter(os);
		write (coll, w);	
	}
	
	public void write(IData coll, Writer writer) {
		ObservationDocument doc = ((OMv1Binding)coll).getPayload();
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			doc.save(bufferedWriter);
			bufferedWriter.close();
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
