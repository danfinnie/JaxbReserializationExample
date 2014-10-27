import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringWriter;
import java.io.Writer;

public class Marshaller {
    private final JAXBContext jaxbContext;

    public Marshaller(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public String marshal(Object input) {
        Writer jsonWriter = new StringWriter();

        try {
            jaxbContext.createMarshaller().marshal(input, jsonWriter);
            return jsonWriter.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
