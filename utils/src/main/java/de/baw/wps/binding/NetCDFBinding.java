package de.baw.wps.binding;

import org.n52.wps.io.data.IComplexData;
import ucar.nc2.NetcdfFile;

public class NetCDFBinding implements IComplexData{
	
	private static final long serialVersionUID = -868454895827379449L;
	protected transient NetcdfFile netCdfFile;
	
	public NetCDFBinding(NetcdfFile payload) {
		this.netCdfFile = payload;
	}
	
	@Override
	public NetcdfFile getPayload() {
		return netCdfFile;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getSupportedClass() {
		return NetcdfFile.class;
	}

	//@Override
	public void dispose() {
		
	}

}

