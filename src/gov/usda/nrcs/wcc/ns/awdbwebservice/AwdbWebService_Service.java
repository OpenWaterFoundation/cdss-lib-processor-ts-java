
package gov.usda.nrcs.wcc.ns.awdbwebservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "AwdbWebService", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", wsdlLocation = "http://www.wcc.nrcs.usda.gov/awdbWebService/services?WSDL")
public class AwdbWebService_Service
    extends Service
{

    private final static URL AWDBWEBSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(gov.usda.nrcs.wcc.ns.awdbwebservice.AwdbWebService_Service.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = gov.usda.nrcs.wcc.ns.awdbwebservice.AwdbWebService_Service.class.getResource(".");
            url = new URL(baseUrl, "http://www.wcc.nrcs.usda.gov/awdbWebService/services?WSDL");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'http://www.wcc.nrcs.usda.gov/awdbWebService/services?WSDL', retrying as a local file");
            logger.warning(e.getMessage());
        }
        AWDBWEBSERVICE_WSDL_LOCATION = url;
    }
    
    /**
    Construct a SOAP object to allow API interaction.
    This version is called when constructing a data store.
    @param wsdlLocation the WSDL location for the AwdbWebService web service
    @throws MalformedURLException
    */
    public AwdbWebService_Service(String wsdlLocation)
    throws MalformedURLException
    {
        super(new URL(wsdlLocation), new QName("http://www.wcc.nrcs.usda.gov/ns/awdbWebService", "AwdbWebService"));
    }

    public AwdbWebService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public AwdbWebService_Service() {
        super(AWDBWEBSERVICE_WSDL_LOCATION, new QName("http://www.wcc.nrcs.usda.gov/ns/awdbWebService", "AwdbWebService"));
    }

    /**
     * 
     * @return
     *     returns AwdbWebService
     */
    @WebEndpoint(name = "AwdbWebServiceImplPort")
    public AwdbWebService getAwdbWebServiceImplPort() {
        return super.getPort(new QName("http://www.wcc.nrcs.usda.gov/ns/awdbWebService", "AwdbWebServiceImplPort"), AwdbWebService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns AwdbWebService
     */
    @WebEndpoint(name = "AwdbWebServiceImplPort")
    public AwdbWebService getAwdbWebServiceImplPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://www.wcc.nrcs.usda.gov/ns/awdbWebService", "AwdbWebServiceImplPort"), AwdbWebService.class, features);
    }

}
