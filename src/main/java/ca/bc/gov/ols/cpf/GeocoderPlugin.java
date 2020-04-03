package ca.bc.gov.ols.cpf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import com.vividsolutions.jts.geom.Point;

import ca.bc.gov.ols.geocoder.DummyGeocoder;
import ca.bc.gov.ols.geocoder.GeocoderDataStore;
import ca.bc.gov.ols.geocoder.IGeocoder;
import ca.bc.gov.ols.geocoder.api.GeocodeQuery;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.Interpolation;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.util.GeomParseUtil;
import ca.bc.gov.open.cpf.plugin.api.AllowedValues;
import ca.bc.gov.open.cpf.plugin.api.BusinessApplicationPlugin;
import ca.bc.gov.open.cpf.plugin.api.DefaultValue;
import ca.bc.gov.open.cpf.plugin.api.GeometryConfiguration;
import ca.bc.gov.open.cpf.plugin.api.GeometryFactory;
import ca.bc.gov.open.cpf.plugin.api.JobParameter;
import ca.bc.gov.open.cpf.plugin.api.RequestParameter;
import ca.bc.gov.open.cpf.plugin.api.ResultList;

@BusinessApplicationPlugin(
		name = GeocoderPlugin.PLUGIN_NAME,
		title = "Batch Geocoder",
		version = GeocoderConfig.VERSION,
		description = "Batch Geocoder Service",
		maxConcurrentRequests = 16,
		numRequestsPerWorker = 1000)
public class GeocoderPlugin {
	
	public static final String PLUGIN_NAME = "geocoder";

	public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.getFactory(3005, 2);
	private static final org.locationtech.jts.geom.GeometryFactory LT_GEOMETRY_FACTORY = new org.locationtech.jts.geom.GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(1000), 3005);
	private static final CpfGeometryReprojector REPROJECTOR = new CpfGeometryReprojector(LT_GEOMETRY_FACTORY);
	
	private IGeocoder geocoder;
	
	private GeocodeQuery query = new GeocodeQuery();
	private List<AddressResult> results;	
	
	public void setGeocoder(IGeocoder geocoder) {
		this.geocoder = geocoder;
	}
	
	@ResultList
	public List<AddressResult> getResults() {
		return results;
	}
	
	@RequestParameter(index = 1, description = " Example 1: 525 Superior St, Victoria, BC Example 2: Yates and Douglas, Victoria, BC")
	public void setAddressString(String addressString) {
		query.setAddressString(addressString);
	}
	
	@JobParameter
	@DefaultValue("1")
	@RequestParameter(index = 2, minValue = "1", maxValue = "1000", description = "Maximum number of matched addresses to return for each input address")
	public void setMaxResults(int maxResults) {
		query.setMaxResults(maxResults);
	}
	
	@JobParameter
	@DefaultValue("0")
	@RequestParameter(index = 3, minValue = "0", maxValue = "100", description = "Between 0 and 100; the higher the score, the better the match")
	public void setMinScore(int minScore) {
		query.setMinScore(minScore);
	}
	
	@JobParameter
	@DefaultValue("0")
	@RequestParameter(index = 4, minValue = "0", maxValue = "1000", description = "In metres from curb and away from street")
	public void setSetBack(int setBack) {
		query.setSetBack(setBack);
	}
	
	@JobParameter
	@RequestParameter(index = 5, description = "Comma-delimited list of acceptable MatchPrecision values")
	public void setMatchPrecision(String matchPrecision) {
		query.setMatchPrecision(MatchPrecision.parseList(matchPrecision));
	}
	
	@JobParameter
	@RequestParameter(index = 6, description = "Comma-delimited list of unacceptable MatchPrecision values")
	public void setMatchPrecisionNot(String matchPrecisionNot) {
		query.setMatchPrecisionNot(MatchPrecision.parseList(matchPrecisionNot));
	}
	
	@JobParameter
	@RequestParameter(index = 7, description = "Comma-delimited list of acceptable locality names; matches not in a locality in this list will not be returned.")
	public void setLocalities(String localities) {
		if(localities != null && !localities.isEmpty()) {
			query.setLocalities(Arrays.asList(localities.toLowerCase().split(",")));
		}
	}
	
	@JobParameter
	@RequestParameter(index = 8, description = "Comma-delimited list of unacceptable locality names; matches in a locality in this list will not be returned.")
	public void setNotLocalities(String notLocalities) {
		if(notLocalities != null && !notLocalities.isEmpty()) {
			query.setNotLocalities(Arrays.asList(notLocalities.toLowerCase().split(",")));
		}
	}
	
	@JobParameter
	@RequestParameter(index = 14, description = "Used with maxDistance to define a circular spatial filter for results")
	public void setCentre(String centre) {
		if(centre != null && !centre.isEmpty()) {
			query.setCentre(GeomParseUtil.parseDoubleArray(centre));
		}
	}
	
	@JobParameter
	@RequestParameter(index = 15, minValue = "0", description = "Maximum distance from the centre point, in meters, defines a circular spatial filter for results")
	public void setMaxDistance(Integer maxDistance) {
		if(maxDistance != null) {
			query.setMaxDistance(maxDistance);
		}
	}
	
	@JobParameter
	@RequestParameter(index = 16, description = "Bounding box used to spatially filter results, specified as <xmin>,<ymin>,<xmax>,<ymax> in the same spatial reference system as is select for the output.")
	public void setBbox(String bbox) {
		if(bbox != null && !bbox.isEmpty()) {
			query.setBbox(GeomParseUtil.parseDoubleArray(bbox));
		}
	}
	
	@JobParameter
	@DefaultValue("true")
	@RequestParameter(index = 17, description = "Include unmatched address details such as site name in results")
	public void setEcho(boolean echo) {
		query.setEcho(echo);
	}
	
	@JobParameter
	@AllowedValues(value = {"adaptive", "linear", "none"})
	@DefaultValue("adaptive")
	@RequestParameter(index = 18, description = "Specifies the type of interpolation to use for results. None will only return known site matches.")
	public void setInterpolation(String interpolation) {
		query.setInterpolation(Interpolation.convert(interpolation));
	}
	
	@JobParameter
	@AllowedValues(value = {"any", "accessPoint", "frontDoorPoint", "parcelPoint", "rooftopPoint",
			"routingPoint"})
	@DefaultValue("any")
	@RequestParameter(index = 19, description = "Specifies your preference of what the returned location should represent")
	public void setLocationDescriptor(String locationDescriptor) {
		query.setLocationDescriptor(LocationDescriptor.convert(locationDescriptor));
	}
	
	@RequestParameter(index = 20, description = "(e.g., Victoria General Hospital)")
	public void setSiteName(String siteName) {
		query.setSiteName(siteName);
	}
	
	@RequestParameter(index = 21, description = " (e.g., Unit, Apt, Suite)")
	public void setUnitDesignator(String unitDesignator) {
		query.setUnitDesignator(unitDesignator);
	}
	
	@RequestParameter(index = 22, description = "(e.g., 1 as in Unit 1, B as in Apt B)")
	public void setUnitNumber(String unitNumber) {
		query.setUnitNumber(unitNumber);
	}
	
	@RequestParameter(index = 23, description = " (e.g., A as in Unit 13A)")
	public void setUnitNumberSuffix(String unitNumberSuffix) {
		query.setUnitNumberSuffix(unitNumberSuffix);
	}
	
	@RequestParameter(index = 24, description = " (e.g., the 525 in 525 Superior St)")
	public void setCivicNumber(String civicNumber) {
		query.setCivicNumber(civicNumber);
	}
	
	@RequestParameter(index = 25, description = " (e.g., the A in 14A Main St)")
	public void setCivicNumberSuffix(String civicNumberSuffix) {
		query.setCivicNumberSuffix(civicNumberSuffix);
	}
	
	@RequestParameter(index = 26, description = "(e.g., the Gorge in 414 Gorge Rd E)")
	public void setStreetName(String streetName) {
		query.setStreetName(streetName);
	}
	
	@RequestParameter(index = 27, description = "(e.g., the Rd in 414 Gorge Rd E); typical values are St, Rd, Ave")
	public void setStreetType(String streetType) {
		query.setStreetType(streetType);
	}
	
	@RequestParameter(index = 28, description = "(e.g., the E in 414 Gorge Rd E); typical values are N,S,E,W,NE,NW,SE,SW")
	@DefaultValue("")
	public void setStreetDirection(String streetDirection) {
		query.setStreetDirection(streetDirection);
	}
	
	@RequestParameter(index = 29, description = "(e.g., the Bridge in Johnson St Bridge, Victoria, BC)")
	public void setStreetQualifier(String streetQualifier) {
		query.setStreetQualifier(streetQualifier);
	}
	
	@RequestParameter(index = 30, description = "(e.g., Comox, Shearwater)")
	public void setLocalityName(String localityName) {
		query.setLocalityName(localityName);
	}
	
	@DefaultValue("BC")
	@RequestParameter(index = 31, description = "(e.g., BC)")
	public void setProvinceCode(String province) {
		query.setStateProvTerr(province);
	}
	
	@RequestParameter(index = 32, description = "Unique identifier you have assigned to an address occupant")
	public void setYourId(String yourId) {
		query.setYourId(yourId);
	}
	
	@JobParameter
	@DefaultValue("false")
	@RequestParameter(index = 33, description = "Forces matched site to return an accessPoint that is the nearest curb point to site location")
	public void setExtrapolate(boolean extrapolate) {
		query.setExtrapolate(extrapolate);
	}
	
	@RequestParameter(index = 34, description = "The location to extrapolate from.")
	@GeometryConfiguration(srid = 3005,
			numAxis = 2,
			scaleFactorXy = 1000,
			validate = true,
			primaryGeometry = true)
	public void setParcelPoint(Point parcelPoint) {
		if(parcelPoint != null) {
			query.setParcelPointGeom(LT_GEOMETRY_FACTORY.createPoint(new org.locationtech.jts.geom.Coordinate(parcelPoint.getX(), parcelPoint.getY())));
		}
	}
	
	public void execute() {
		query.resolveAndValidate(geocoder.getConfig(), LT_GEOMETRY_FACTORY, REPROJECTOR);
		SearchResults sr = geocoder.geocode(query);
		results = new ArrayList<AddressResult>();
		GeocoderConfig config = null;
		if(geocoder.getDatastore() != null) {
			config = geocoder.getDatastore().getConfig();
		}
		for(GeocodeMatch match : sr.getMatches()) {
			AddressResult result = new AddressResult(match, sr, config);
			results.add(result);
		}
	}
	
	public void testExecute() {
		query.resolveAndValidate(geocoder.getDatastore().getConfig(), LT_GEOMETRY_FACTORY, REPROJECTOR);
		results = new ArrayList<AddressResult>();
		SearchResults sr = DummyGeocoder.getDummyResults(query, GeocoderDataStore.getGeometryFactory());
		for(GeocodeMatch match : sr.getMatches()) {
			AddressResult result = new AddressResult(match, sr, null);
			results.add(result);
		}
	}
	
	public Map<String, Object> getCustomizationProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("kmlWriteNulls", true);
		return properties;
	}
	
	@PreDestroy
	public void clearGeocoder() {
		geocoder = null;
	}
}