package de.baw.wps.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import de.baw.wps.binding.NetCDFBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.io.datahandler.parser.AbstractParser;

import ucar.nc2.NetcdfFile;

/**
 * This parser is able to read netCDF-Files into ucar.nc2.NetcdfFile objects
 * @author woessner
 *
 */
public class NetCDFParser extends AbstractParser{
	
	private static Logger LOGGER = LoggerFactory.getLogger(NetCDFParser.class);
	
	public NetCDFParser() {
		super();
		supportedIDataTypes.add(NetCDFBinding.class);
	}

	@Override
	public NetCDFBinding parse(InputStream input, String mimeType, String schema) {
		File tempFile;
		
		// Create temporary file 
		try {
            tempFile = File.createTempFile("netcdf" + UUID.randomUUID(),"tmp");
            finalizeFiles.add(tempFile);
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			byte buf[] = new byte[4096];
			int len;
			while ((len = input.read(buf)) > 0) {
				outputStream.write(buf, 0, len);
			}
			outputStream.flush();
			outputStream.close();
			input.close();
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage());
			throw new RuntimeException(e1);
		}
		
		// Parse the nc file
		return parseNetCDF(tempFile);
	}

	private NetCDFBinding parseNetCDF(File file) {
		NetcdfFile ncFile;

		try {
			ncFile = NetcdfFile.open(file.getAbsolutePath());
			return new NetCDFBinding(ncFile);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException();
		}
	}
	
}
