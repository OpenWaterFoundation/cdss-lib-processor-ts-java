package rti.tscommandprocessor.commands.nrcs.awdb;

import gov.usda.nrcs.wcc.ns.awdbwebservice.AwdbWebService;
import gov.usda.nrcs.wcc.ns.awdbwebservice.AwdbWebService_Service;
import gov.usda.nrcs.wcc.ns.awdbwebservice.Data;
import gov.usda.nrcs.wcc.ns.awdbwebservice.DataSource;
import gov.usda.nrcs.wcc.ns.awdbwebservice.Duration;
import gov.usda.nrcs.wcc.ns.awdbwebservice.Element;
import gov.usda.nrcs.wcc.ns.awdbwebservice.HeightDepth;
import gov.usda.nrcs.wcc.ns.awdbwebservice.StationElement;
import gov.usda.nrcs.wcc.ns.awdbwebservice.StationMetaData;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import riverside.datastore.AbstractWebServiceDataStore;

import RTi.TS.TS;
import RTi.TS.TSUtil;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
Data store for NRCS AWDB web service.  This class maintains the web service information in a general way.
This data store was created from a previous "input type" convention.  Consequently, this code is mainly a
wrapper for code that previously was developed.
*/
public class NrcsAwdbDataStore extends AbstractWebServiceDataStore
{

/**
ColoradoWaterSMS instance used as SOAP API.
*/
private AwdbWebService __awdbWebService = null;

/**
The list of network codes, listed here:  http://www.wcc.nrcs.usda.gov/web_service/AWDB_Web_Service_Reference.htm
*/
private List<NrcsAwdbNetworkCode> __networkCodeList = new Vector();

/**
The list of element codes, listed here:  http://www.wcc.nrcs.usda.gov/web_service/AWDB_Web_Service_Reference.htm
*/
private List<Element> __elementList = new Vector();
    
/**
Constructor for web service.
*/
public NrcsAwdbDataStore ( String name, String description, URI serviceRootURI, PropList props )
throws URISyntaxException, IOException
{
    setName ( name );
    setDescription ( description );
    setServiceRootURI ( serviceRootURI );
    setProperties ( props );
    
    // The following as per the AWDB tutorial:
    // http://www.wcc.nrcs.usda.gov/web_service/AWDB_Web_Service_Tutorial.htm
    AwdbWebService_Service lookup = new AwdbWebService_Service(serviceRootURI.toString());
    setAwdbWebService ( lookup.getAwdbWebServiceImplPort() );
    
    // Initialize static data
    // Initialize the network codes - this may be available as a service at some point but for now inline
    __networkCodeList.add ( new NrcsAwdbNetworkCode("BOR","Any Bureau of Reclamation reservoir stations plus other non-BOR reservoir stations"));
    __networkCodeList.add ( new NrcsAwdbNetworkCode("CLMIND","Used to store climate indices (such as Southern Oscillation Index or Trans-Nino Index)"));
    __networkCodeList.add ( new NrcsAwdbNetworkCode("COOP","National Weather Service COOP stations"));
    __networkCodeList.add ( new NrcsAwdbNetworkCode("MPRC","Manual precipitation sites"));
    __networkCodeList.add ( new NrcsAwdbNetworkCode("MSNT","Manual SNOTEL non-telemetered, non-real time sites"));
    __networkCodeList.add ( new NrcsAwdbNetworkCode("SNOW","NRCS Snow Course Sites"));
    __networkCodeList.add ( new NrcsAwdbNetworkCode("SNTL","NWCC SNOTEL and SCAN stations"));
    __networkCodeList.add ( new NrcsAwdbNetworkCode("USGS","Any USGS station, but also other non-USGS streamflow stations"));
    // Initialize the element codes from web service
    readElements();
}

/**
Factory method to construct a data store connection from a properties file.
@param filename name of file containing property strings
*/
public static NrcsAwdbDataStore createFromFile ( String filename )
throws IOException, Exception
{
    // Read the properties from the file
    PropList props = new PropList ("");
    props.setPersistentName ( filename );
    props.readPersistent ( false );
    props.set("DataStoreConfigFile",filename);
    String name = IOUtil.expandPropertyForEnvironment("Name",props.getValue("Name"));
    String description = IOUtil.expandPropertyForEnvironment("Description",props.getValue("Description"));
    String serviceRootURI = IOUtil.expandPropertyForEnvironment("ServiceRootURI",props.getValue("ServiceRootURI"));
    
    // Get the properties and create an instance

    NrcsAwdbDataStore ds = new NrcsAwdbDataStore( name, description, new URI(serviceRootURI), props );
    return ds;
}

/**
Format a date/time to the interval precision being queried.
@return the formatted date/time or null if requested period is null.
*/
private String formatDateTime ( DateTime dt, TimeInterval interval )
{
    if ( dt == null ) {
        return null;
    }
    int intervalBase = interval.getBase();
    if ( intervalBase == TimeInterval.YEAR ) {
        return dt.toString(DateTime.FORMAT_YYYY);
    }
    else if ( intervalBase == TimeInterval.MONTH ) {
        return dt.toString(DateTime.FORMAT_YYYY_MM);
    }
    else if ( intervalBase == TimeInterval.DAY ) {
        return dt.toString(DateTime.FORMAT_YYYY_MM_DD);
    }
    else if ( intervalBase == TimeInterval.IRREGULAR ) {
        return dt.toString(DateTime.FORMAT_YYYY_MM_DD_HH_mm);
    }
    else {
        return null;
    }
}

/**
Return the AwdbWebService instance used by the data store.
@return the AwdbWebService instance used by the data store
*/
public AwdbWebService getAwdbWebService ()
{
    return __awdbWebService;
}

/**
Return the list of elements that are available.  This returns the element code and optionally
the name.  Duplicates in the table are ignored.
@param includeName whether to include the description (use " - " as separator).
*/
public List<String> getElementStrings ( boolean includeName )
{   List<String> elementList = new Vector();
    for ( Element el: __elementList ) {
        if ( includeName ) {
            elementList.add( "" + el.getElementCd() + " - " + el.getName() );
        }
        else {
            elementList.add( "" + el.getElementCd() );
        }
    }
    return elementList;
}

/**
Return the list of network that are available.  This returns the network code and optionally
the description.  Duplicates in the table are ignored.
@param includeDesc whether to include the description (use " - " as separator).
*/
public List<String> getNetworkStrings ( boolean includeDesc )
{   List<String> networkList = new Vector();
    for ( NrcsAwdbNetworkCode param: __networkCodeList ) {
        if ( includeDesc ) {
            networkList.add( "" + param.getCode() + " - " + param.getDescription() );
        }
        else {
            networkList.add( "" + param.getCode() );
        }
    }
    return networkList;
}

/**
Lookup the AWDB duration from the internal TS interval.
*/
private Duration lookupDurationFromInterval ( TimeInterval interval )
{
    if ( interval.getBase() == TimeInterval.YEAR ) {
        return Duration.ANNUAL;
    }
    else if ( interval.getBase() == TimeInterval.MONTH ) {
        return Duration.MONTHLY;
    }
    else if ( interval.getBase() == TimeInterval.DAY ) {
        return Duration.DAILY;
    }
    else if ( interval.getBase() == TimeInterval.IRREGULAR ) {
        return Duration.INSTANTANEOUS;
    }
    else {
        return null;
    }
}

/**
Lookup the TimeInterval from the AWDB duration.
*/
private int lookupIntervalFromDuration ( Duration duration )
{
    if ( duration == Duration.ANNUAL ) {
        return TimeInterval.YEAR;
    }
    else if ( duration == Duration.MONTHLY ) {
        return TimeInterval.MONTH;
    }
    else if ( duration == Duration.DAILY ) {
        return TimeInterval.DAY;
    }
    else if ( duration == Duration.INSTANTANEOUS ) {
        return TimeInterval.IRREGULAR;
    }
    else {
        return TimeInterval.UNKNOWN;
    }
}

/**
Parse an AWDB date/time string and return a DateTime instance.
*/
private DateTime parseDateTime ( String s )
{
    return DateTime.parse(s);
}

/**
Parse the network code from the station triplet ("StationID:State:Network")
*/
private String parseNetworkCodeFromTriplet(String stationTriplet)
{
    String [] parts = stationTriplet.split(":");
    return parts[2];
}

/**
Parse the state abbreviation from the station triplet ("StationID:State:Network")
*/
private String parseStateFromTriplet(String stationTriplet)
{
    String [] parts = stationTriplet.split(":");
    return parts[1];
}

/**
Parse the station ID from the station triplet ("StationID:State:Network")
*/
private String parseStationIDFromTriplet(String stationTriplet)
{
    String [] parts = stationTriplet.split(":");
    return parts[0];
}

/**
Read the available elements from the web service and cache for further use.
*/
private void readElements ()
{
    // First read the stations that match the basic criteria
    AwdbWebService ws = getAwdbWebService ();
    __elementList = ws.getElements();
}

/**
Read a list of time series from the web service, using query parameters that are supported for the web service.
@param elementListReq list of requested element codes to match stations -
if null then the element list is queried for each station as processed
*/
public List<TS> readTimeSeriesList ( List<String> stationIdList, List<String> stateList,
    List<NrcsAwdbNetworkCode>networkList, List<String> hucList, double [] boundingBox,
    List<String> countyList, List<Element> elementListReq, Double elevationMin, Double elevationMax,
    TimeInterval interval, DateTime readStartReq, DateTime readEndReq, boolean readData )
{   String routine = getClass().getName() + ".readTimeSeriesList";
    List<TS> tsList = new Vector();
    // First read the stations that match the basic criteria
    AwdbWebService ws = getAwdbWebService ();
    List<String> stationIds = stationIdList;
    List<String> stateCds = stateList;
    List<String> networkCds = new Vector();
    for ( NrcsAwdbNetworkCode n: networkList ) {
        networkCds.add(n.getCode());
    }
    List<String> hucs = hucList;
    List<String> countyNames = countyList;
    BigDecimal minLatitude = null;
    BigDecimal maxLatitude = null;
    BigDecimal minLongitude = null;
    BigDecimal maxLongitude = null;
    if ( boundingBox != null ) {
        minLatitude = new BigDecimal(boundingBox[1]);
        maxLatitude = new BigDecimal(boundingBox[3]);
        minLongitude = new BigDecimal(boundingBox[0]);
        maxLongitude = new BigDecimal(boundingBox[2]);
    }
    BigDecimal minElevation = null;
    if ( elevationMin != null ) {
        minElevation = new BigDecimal(elevationMin);
    }
    BigDecimal maxElevation = null;
    if ( elevationMax != null ) {
        maxElevation = new BigDecimal(elevationMax);
    }
    List<String> elementCds = new Vector();
    if ( elementListReq != null ) {
        for ( Element el: elementListReq ) {
            elementCds.add ( el.getElementCd() );
        }
    }
    List<Integer> ordinals = new Vector<Integer>();
    List<HeightDepth> heightDepths = new Vector<HeightDepth>();
    boolean logicalAnd = true;
    List<String> stationTriplets = ws.getStations(stationIds, stateCds, networkCds, hucs, countyNames,
        minLatitude, maxLatitude, minLongitude, maxLongitude, minElevation, maxElevation,
        elementCds, ordinals, heightDepths, logicalAnd );
    int nStations = 0;
    if ( stationTriplets != null ) {
        nStations = stationTriplets.size();
    }
    Message.printStatus(2,routine,"Read " + nStations + " stations from NRCS AWDB getStations(...) request." );
    if ( nStations == 0 ) {
        return tsList;
    }
    // Now get the list of station metadata for the stations
    List<StationMetaData> stationMetaData = ws.getStationMetadataMultiple(stationTriplets);
    TS ts;
    String tsid;
    String stationTriplet;
    String state;
    String stationID;
    String networkCode;
    String elementCode;
    int ordinal = 1;
    HeightDepth heightDepth = null;
    boolean getFlags = true;
    Duration duration = lookupDurationFromInterval ( interval );
    String beginDateString = null; // Null means no requested period so read all
    String endDateString = null;
    // Date to read data and also to allocate time series.
    // May be reset below if no requested period.
    if ( readStartReq != null ) {
        beginDateString = formatDateTime(readStartReq,interval);
    }
    if ( readEndReq != null ) {
        endDateString = formatDateTime(readEndReq,interval);
    }
    int iMeta = -1;
    // Loop through the stations and then the elements for each station
    for ( StationMetaData meta: stationMetaData ) {
        ++iMeta;
        stationTriplet = meta.getStationTriplet();
        state = parseStateFromTriplet(stationTriplet);
        stationID = parseStationIDFromTriplet(stationTriplet);
        networkCode = parseNetworkCodeFromTriplet(stationTriplet);
        String text;
        BigDecimal bd;
        int intervalBase, intervalMult;
        // Get the elements that are valid for the station, specifying null for dates
        List<StationElement> stationElementList = ws.getStationElements(stationTriplet, null, null);
        // Now cut back the list to elements and intervals that were originally requested
        for ( int iEl = 0; iEl < stationElementList.size(); iEl++ ) {
            boolean elementFound = false;
            boolean intervalFound = false;
            StationElement sel = stationElementList.get(iEl);
            if ( elementListReq != null ) {
                // Check for requested elements...
                for ( Element el : elementListReq ) {
                    if ( sel.getElementCd().equalsIgnoreCase(el.getElementCd())) {
                        // Matched the element
                        elementFound = true;
                        break;
                    }
                }
            }
            // Check for requested interval
            if ( lookupIntervalFromDuration(sel.getDuration()) == interval.getBase() ) {
                intervalFound = true;
            }
            // If data from web service does not match the requested values, remove from the list so
            // the StationElement does not get processed below.
            if ( !elementFound || !intervalFound ) {
                // Available element/interval was not requested so remove from list
                stationElementList.remove(iEl);
                --iEl;
            }
        }
        // Process the remaining StationElement items...
        for ( StationElement sel: stationElementList ) {
            // Process each element code that applies to the station
            elementCode = sel.getElementCd();
            tsid = state + "-" + stationID + "." + networkCode + "." + elementCode + "." + interval;
            try {
                ts = TSUtil.newTimeSeries(tsid,true);
                ts.setIdentifier(tsid);
                intervalBase = ts.getDataIntervalBase();
                intervalMult = ts.getDataIntervalMult();
                ts.setMissing(Double.NaN);
                ts.setDescription(meta.getName());
                ts.setDate1Original(parseDateTime(sel.getBeginDate())); // Sensor install date
                ts.setDate2Original(parseDateTime(sel.getEndDate())); // Sensor end, or 2100-01-01 00:00 if active
                // The following will be reset if reading data but are OK for discovery mode...
                if ( readStartReq != null ) {
                    ts.setDate1(readStartReq);
                }
                else {
                    // Set the period to read from the data
                    ts.setDate1(ts.getDate1Original());
                }
                if ( readEndReq != null ) {
                    ts.setDate2(readEndReq);
                }
                else {
                    // Set the period to read from the data
                    ts.setDate2(ts.getDate2Original());
                }
                ts.setDataUnits(sel.getStoredUnitCd());
                ts.setDataUnitsOriginal(sel.getOriginalUnitCd());
                // Also set properties by passing through XML elements
                boolean setPropertiesFromMetadata = true;
                if ( setPropertiesFromMetadata ) {
                    // Set time series properties from the timeSeries elements
                    ts.setProperty("stationTriplet", (stationTriplet == null) ? "" : stationTriplet );
                    text = meta.getName();
                    ts.setProperty("name", (text == null) ? "" : text );
                    text = meta.getActonId();
                    ts.setProperty("actonId", (text == null) ? "" : text );
                    text = meta.getShefId();
                    ts.setProperty("shefId", (text == null) ? "" : text );
                    text = meta.getBeginDate(); // Date station installed
                    ts.setProperty("beginDate", (text == null) ? "" : text );
                    text = meta.getEndDate(); // Date station discontinued
                    ts.setProperty("endDate", (text == null) ? "" : text );
                    text = meta.getCountyName();
                    ts.setProperty("countyName", (text == null) ? "" : text );
                    bd = meta.getElevation();
                    ts.setProperty("elevation", (bd == null) ? null : bd.doubleValue() );
                    text = meta.getFipsCountyCd();
                    ts.setProperty("fipsCountyCd", (text == null) ? "" : text );
                    text = meta.getFipsStateNumber();
                    ts.setProperty("fipsStateNumber", (text == null) ? "" : text );
                    text = meta.getFipsCountryCd();
                    ts.setProperty("fipsCountryCd", (text == null) ? "" : text );
                    text = meta.getHuc();
                    ts.setProperty("huc", (text == null) ? "" : text );
                    bd = meta.getLongitude();
                    ts.setProperty("longitude", (bd == null) ? null : bd.doubleValue() );
                    bd = meta.getLatitude();
                    ts.setProperty("latitude", (bd == null) ? null : bd.doubleValue() );
                    Integer i = sel.getDataPrecision();
                    ts.setProperty("dataPrecision", (i == null) ? null : i );
                    DataSource s = sel.getDataSource();
                    ts.setProperty("dataSource", (i == null) ? null : "" + s );
                    int i2 = sel.getOrdinal();
                    ts.setProperty("ordinal", new Integer(i2) );
                    HeightDepth hd = sel.getHeightDepth();
                    if ( hd == null ) {
                        ts.setProperty("heighDepthValue", null );
                        ts.setProperty("heightDepthUnitCd", null );
                    }
                    else {
                        ts.setProperty("heighDepthValue", (hd.getValue() == null) ? null : new Double(hd.getValue().doubleValue()));
                        ts.setProperty("heightDepthUnitCd", (hd.getUnitCd() == null) ? "" : hd.getUnitCd() );
                    }
                    bd = meta.getStationDataTimeZone();
                    ts.setProperty("stationDataTimeZone", (bd == null) ? null : bd.doubleValue() );
                    bd = meta.getStationTimeZone();
                    ts.setProperty("stationTimeZone", (bd == null) ? null : bd.doubleValue() );
                }
            }
            catch ( Exception e ) {
                continue;
            }
            if ( readData ) {
                // Reset the date to read based on the full period available from the StationElement
                if ( readStartReq == null ) {
                    beginDateString = sel.getBeginDate();
                }
                if ( readEndReq == null ) {
                    endDateString = sel.getEndDate();
                }
                Message.printStatus(2, routine, "Getting data values for triplet ("+ iMeta + " of " +
                    stationMetaData.size() + ")=\"" + stationTriplet +
                    "\" elementCode="+elementCode + " duration=" + duration + " beginDate=" + beginDateString +
                    " endDate=" + endDateString);
                try {
                    // Get the data values for the list of station triplets.
                    // Since only one triplet is processed here, the data array will have one element.
                    List<Data> dataList = ws.getData(Arrays.asList(stationTriplet), elementCode, ordinal, heightDepth,
                        sel.getDuration(), getFlags, beginDateString, endDateString);
                    if ( dataList.size() == 1 ) {
                        // Have data values for the requested station triplet and element code
                        Data data = dataList.get(0);
                        List<BigDecimal> values = data.getValues();
                        List<String> flags = data.getFlags();
                        int nValues = values.size();
                        Message.printStatus(2, routine, "Have " + nValues + " data values for triplet " + stationTriplet );
                        // If a period is not requested, set to the available StationElement data period
                        if ( readStartReq == null ) {
                            DateTime readStart = DateTime.parse(beginDateString);
                            ts.setDate1(readStart);
                        }
                        if ( readEndReq == null ) {
                            DateTime readEnd = new DateTime(ts.getDate1());
                            readEnd = TimeUtil.addIntervals(readEnd,intervalBase,intervalMult,(nValues - 1));
                            ts.setDate2(readEnd);
                        }
                        ts.allocateDataSpace();
                        // Loop through the data values and set the values and the flag
                        DateTime dt = new DateTime(ts.getDate1());
                        BigDecimal value;
                        String flag;
                        for ( int i = 0; i < nValues; i++, dt.addInterval(intervalBase,intervalMult) ) {
                            value = values.get(i);
                            flag = flags.get(i);
                            if ( value == null ) {
                                // Might still have a flag
                                if ( (flag != null) && !flag.equals("") ) {
                                    ts.setDataValue(dt,ts.getMissing(),flag,0);
                                }
                            }
                            else {
                                // Value is not missing but flag may be null
                                if ( flag == null ) {
                                    ts.setDataValue(dt, value.doubleValue());
                                }
                                else {
                                    ts.setDataValue(dt, value.doubleValue(),flag,0);
                                }
                            }
                        }
                    }
                }
                catch ( Exception e ) {
                    Message.printWarning(3, routine, "Error getting data values (" + e + ").");
                    Message.printWarning(3, routine, e);
                }
            }
            tsList.add(ts);
        }
    }
    return tsList;
}

/**
Set the AwdbWebService instance that is used as the API
*/
private void setAwdbWebService ( AwdbWebService awdbWebService )
{
    __awdbWebService = awdbWebService;
}

}