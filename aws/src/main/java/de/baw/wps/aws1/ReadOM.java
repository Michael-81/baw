package de.baw.wps.aws1;

import de.baw.wps.binding.OMBinding;
import de.baw.xml.OMdocBuilder;
import net.opengis.om.x20.OMObservationDocument;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

@Algorithm(version = "1.0.0", abstrakt = "Liest Daten aus der uebergebenen NetCDF Datei ein und gibt sie als O&M XML aus")
public class ReadOM extends AbstractAnnotatedAlgorithm {

    String outputXML;
    OMObservationDocument inputOM;

    @ComplexDataInput(identifier = "inputOM", abstrakt = "Reference to a O&M file (SOS getObservation)", binding = OMBinding.class)
    public void setModellNetCDF(OMObservationDocument om) {
        this.inputOM = om;
    }

    @LiteralDataOutput(identifier = "outputXML", abstrakt = "Extracted time series. O&M-XML", binding = LiteralStringBinding.class)
    public String getResult() {
        return this.outputXML;
    }

    @Execute
    public void execute() {

        OMdocBuilder omb = new OMdocBuilder();

        this.outputXML = omb.docToString(inputOM);

    }

}
