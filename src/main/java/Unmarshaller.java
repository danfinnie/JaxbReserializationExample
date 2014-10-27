import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class Unmarshaller {
    private final JAXBContext jaxbContext;

    public Unmarshaller(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public <T> T unmarshal(String string, Class<T> clazz) {
        return unmarshal(IOUtils.toInputStream(string), clazz);
    }

    public <T> T unmarshal(InputStream inputStream, Class<T> clazz) {
        try {
            return jaxbContext.createUnmarshaller().unmarshal(new StreamSource(inputStream), clazz).getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
