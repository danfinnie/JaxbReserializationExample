import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ValidationResponseSerializationTest {
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    public static class VendorResponse {
        public Response response;
        public String id;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Response {
        @XmlAnyElement
        public List<Object> elements;
    }

    @Test
    public void serializesArbitraryFieldsJson() throws Exception {
        String input = "" +
                "{" +
                "   \"id\": \"yolokitten\"," +
                "   \"response\":" +
                "   {" +
                "       \"integer\": 42," +
                "       \"string\": \"42\"," +
                "       \"nested\": {" +
                "           \"integer\": 10," +
                "           \"string\": \"10\"" +
                "       }" +
                "   }" +
                "}";

        VendorResponse vendorResponse = new Unmarshaller(getJsonJaxbContext()).unmarshal(input, VendorResponse.class);
        String output = new Marshaller(getJsonJaxbContext()).marshal(vendorResponse);

        System.out.println(output);
        ReadContext context = JsonPath.parse(output);
        assertThat((String) context.read("id"), equalTo("yolokitten"));
        assertThat((String) context.read("response.string"), equalTo("42"));
        assertThat((Integer) context.read("response.integer"), equalTo(42));
        assertThat((String) context.read("response.nested.string"), equalTo("10"));
        assertThat((Integer) context.read("response.nested.integer"), equalTo(10));
    }

    @Test
    public void serializesArbitraryFieldsXml() throws Exception {
        String input = "" +
                "<vendorResponse>\n" +
                    "<id>yolokitten</id>\n" +
                    "<response>\n" +
                        "<integer>42</integer>\n" +
                        "<string>42</string>\n" +
                        "<nested>\n" +
                            "<integer>10</integer>\n" +
                            "<string>10</string>\n" +
                        "</nested>\n" +
                    "</response>\n" +
                "</vendorResponse>";

        VendorResponse vendorResponse = new Unmarshaller(getXmlJaxbContext()).unmarshal(input, VendorResponse.class);
        String output = new Marshaller(getXmlJaxbContext()).marshal(vendorResponse);

        System.out.println(output);
        assertThat(xpath("/vendorResponse/id", output), equalTo("yolokitten"));
        assertThat(xpath("/vendorResponse/response/string", output), equalTo("42"));
        assertThat(xpath("/vendorResponse/response/integer", output), equalTo("42"));
        assertThat(xpath("/vendorResponse/response/nested/string", output), equalTo("10"));
        assertThat(xpath("/vendorResponse/response/nested/integer", output), equalTo("10"));
    }

    private JAXBContext getJsonJaxbContext() throws JAXBException {
        Map<String, Object> jaxbProperties = new HashMap<String, Object>(2);
        jaxbProperties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
        jaxbProperties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
        jaxbProperties.put(JAXBContextProperties.JSON_ATTRIBUTE_PREFIX, "@");

        Class[] classes = {
                VendorResponse.class
        };
        return JAXBContextFactory.createContext(classes, jaxbProperties);
    }

    private JAXBContext getXmlJaxbContext() throws JAXBException {
        Map<String, Object> jaxbProperties = new HashMap<String, Object>(2);
        Class[] classes = {
                VendorResponse.class
        };
        return JAXBContextFactory.createContext(classes, jaxbProperties);
    }

    private String xpath(String xpathStr, String xml) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        XPathExpression expr = xpath.compile(xpathStr);
        return (String) expr.evaluate(doc, XPathConstants.STRING);
    }
}
