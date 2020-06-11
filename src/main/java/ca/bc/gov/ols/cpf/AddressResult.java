package ca.bc.gov.ols.cpf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import ca.bc.gov.ols.geocoder.api.data.AddressMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocodeMatch;
import ca.bc.gov.ols.geocoder.api.data.GeocoderAddress;
import ca.bc.gov.ols.geocoder.api.data.IntersectionMatch;
import ca.bc.gov.ols.geocoder.api.data.SearchResults;
import ca.bc.gov.ols.geocoder.config.GeocoderConfig;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocalityType;
import ca.bc.gov.ols.geocoder.data.enumTypes.LocationDescriptor;
import ca.bc.gov.ols.geocoder.data.enumTypes.MatchPrecision;
import ca.bc.gov.ols.geocoder.data.enumTypes.PhysicalStatus;
import ca.bc.gov.ols.geocoder.data.enumTypes.PositionalAccuracy;
import ca.bc.gov.ols.geocoder.util.GeocoderUtil;
import ca.bc.gov.open.cpf.plugin.api.GeometryConfiguration;
import ca.bc.gov.open.cpf.plugin.api.GeometryFactory;
import ca.bc.gov.open.cpf.plugin.api.ResultAttribute;

import com.vividsolutions.jts.geom.Point;

/**
 * AddressResult adapts from the deeply structured GeocodeMatch to a flatter structure more
 * compatible with the cpf.
 * 
 * @author chodgson
 */

public class AddressResult {
	private GeocodeMatch match;
	private GeocoderConfig config;
	private SearchResults searchResults;
	
	// private String debugInfo;
	
	public AddressResult(GeocodeMatch match, SearchResults searchResults, GeocoderConfig config) {
		this.match = match;
		this.searchResults = searchResults;
		this.config = config;
	}
	
	@ResultAttribute(index = 10, description = "A unique identifier you have assigned to an address occupant.")
	public String getYourId() {
		return deNullify(match.getYourId());
	}
	
	@ResultAttribute(index = 20, description = "The complete address string in canonical form.")
	public String getFullAddress() {
		return deNullify(match.getAddressString());
	}
	
	@ResultAttribute(index = 30, description = "A common language description of the intersection. (eg. Howe St and Robson St")
	public String getIntersectionName() {
		String result = null;
		if(match instanceof IntersectionMatch) {
			result = ((IntersectionMatch)match).getAddress().getName();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 40, length = 3, scale = 0, description = "An indication of the quality of the match of the returned address, between 0 and 100; 100 is a perfect match")
	public int getScore() {
		return match.getScore();
	}
	
	@ResultAttribute(index = 50, length = 25, description = "An indication of what level the address was matched on (UNIT, SITE, CIVIC_NUMBER, BLOCK, STREET, LOCALITY, or PROVINCE)")
	public MatchPrecision getMatchPrecision() {
		return match.getPrecision();
	}
	
	@ResultAttribute(index = 60, length = 3, scale = 0, description = "The associated highest possible score of a match with this matches given match precision.")
	public int getPrecisionPoints() {
		return match.getPrecisionPoints();
	}
	
	@ResultAttribute(index = 70, description = "The full list of elements that did not match the query and their associated penalty points that affect the score of the match.")
	public String getFaults() {
		return match.getFaults().toString();
	}
	
	@ResultAttribute(index = 80)
	public String getSiteName() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getSiteName();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 90, length = 25)
	public String getUnitDesignator() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getUnitDesignator();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 100, length = 25)
	public String getUnitNumber() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getUnitNumber();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 110, length = 25)
	public String getUnitNumberSuffix() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getUnitNumberSuffix();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 120, length = 9, scale = 0)
	public String getCivicNumber() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = GeocoderUtil.formatCivicNumber(
					((AddressMatch)match).getAddress().getCivicNumber());
		}
		return result;
	}
	
	@ResultAttribute(index = 130, length = 25)
	public String getCivicNumberSuffix() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getCivicNumberSuffix();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 140, length = 50)
	public String getStreetName() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getStreetName();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 150, length = 25)
	public String getStreetType() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getStreetType();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 155, description = "True if the street type is a prefix to the street name, such as Hwy 1, false if the street type is a suffix such as 1 St.")
	public Boolean getIsStreetTypePrefix() {
		Boolean result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().isStreetTypePrefix();
		}
		if(result == null) {
			result = false;
		}
		return result;
	}
	
	@ResultAttribute(index = 160, length = 2)
	public String getStreetDirection() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getStreetDirection();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 163, description = "True if the street direction is a prefix to the street name, such as West 1 Ave, false if the street direction is a suffix such as 1 Ave West.")
	public Boolean getIsStreetDirectionPrefix() {
		Boolean result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().isStreetDirectionPrefix();
		}
		if(result == null) {
			result = false;
		}
		return result;
	}
	
	@ResultAttribute(index = 167, length = 25)
	public String getStreetQualifier() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getStreetQualifier();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 170, length = 50)
	public String getLocalityName() {
		return deNullify(match.getLocalityName());
	}
	
	@ResultAttribute(index = 180, length = 50, description = "(e.g., municipality, community, Indian reservation, subdivision, regional district, aboriginal lands, landmark, or natural feature)")
	public LocalityType getLocalityType() {
		return match.getLocalityType();
	}

	@ResultAttribute(index = 185, length = 50, description = "The Electoral Area the address is in, if the addresses locality is unincorporated.")
	public String getElectoralArea() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getElectoralArea();
		}
		return result;
	}

	@ResultAttribute(index = 190, length = 2)
	public String getProvinceCode() {
		return deNullify(match.getStateProvTerr());
	}
	
	@ResultAttribute(index = 200, description = "point geometry assigned to this address")
	@GeometryConfiguration(
			srid = 3005,
			numAxis = 2,
			// scaleFactorXy=1000,
			primaryGeometry = true)
	public Point getLocation() {
		org.locationtech.jts.geom.Point p = match.getLocation();
		return GeocoderPlugin.GEOMETRY_FACTORY.createPoint(p.getX(), p.getY());
	}
	
	@ResultAttribute(index = 210, length = 25, description = "Coarse - Street, Locality, or Province level match; Low - digitized or interpolated along an address range; Medium - interpolated within a Parcel; High - observed using GPS or survey instruments")
	public PositionalAccuracy getLocationPositionalAccuracy() {
		return match.getAddress().getLocationPositionalAccuracy();
	}
	
	@ResultAttribute(index = 220, length = 25, description = "An explanation of what is represented by the location returned. Will be the same as was requested unless that is not available, in which case the closest available location will be returned. (e.g., accessPoint, frontDoorPoint, localityPoint, parcelPoint, provincePoint, rooftopPoint, routingPoint, streetPoint)")
	public LocationDescriptor getLocationDescriptor() {
		return match.getAddress().getLocationDescriptor();
	}
	
	@ResultAttribute(index = 230, length = 36, description = "A unique identifier to identify the specific site.")
	public String getSiteID() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getSiteID();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 240, length = 9, scale = 0, description = "A unique identifier for the street block. ")
	public Integer getBlockID() {
		Integer result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getStreetSegmentID();
		}
		return result;
	}
	
	@ResultAttribute(index = 250, length = 36, description = "A unique identifier for this particular intersection.")
	public String getIntersectionID() {
		String result = null;
		if(match instanceof IntersectionMatch) {
			result = ((IntersectionMatch)match).getAddress().getID();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 260, description = "A full descripion of the site.")
	public String getFullSiteDescriptor() {
		String result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getFullSiteDescriptor();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 270, description = "Directions to the site, including notes on accessibility.")
	public String getAccessNotes() {
		String result = null;
		if(match instanceof AddressMatch) {
			// intentionally outputting narrativeLocation as AccessNotes
			result = ((AddressMatch)match).getAddress().getNarrativeLocation();
		}
		return deNullify(result);
	}
	
	@ResultAttribute(index = 290, description = "(e.g., proposed, active, retired)")
	public PhysicalStatus getSiteStatus() {
		PhysicalStatus result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getSiteStatus();
		}
		if(result == null) {
			result = PhysicalStatus.ACTIVE;
		}
		return result;
	}
	
	@ResultAttribute(index = 300, description = "The date the site was set to a state where it was no longer considered an active or reliable site.")
	public LocalDate getSiteRetireDate() {
		LocalDate result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getSiteRetireDate();
		}
		// if(result == null) {
		// result = new Date(); // TODO end of time
		// }
		return result;
	}
	
	@ResultAttribute(index = 310, description = "The date the site address was last changed")
	public LocalDate getChangeDate() {
		LocalDate result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getSiteChangeDate();
		}
		// if(result == null) {
		// result = new Date(); // TODO end of time
		// }
		return result;
	}
	
	@ResultAttribute(index = 320, description = "true if this is the official address for site; false otherwise")
	public Boolean getIsOfficial() {
		if(match instanceof AddressMatch) {
			return ((AddressMatch)match).getAddress().isPrimary();
		}
		return true;
	}
	
	@ResultAttribute(index = 330, length = 2, scale = 0, description = "The number of road segments which meet at this intersection.")
	public Integer getDegree() {
		Integer result = null;
		if(match instanceof IntersectionMatch) {
			result = ((IntersectionMatch)match).getAddress().getDegree();
		}
		return result;
	}
	
	@ResultAttribute(index = 340, length = 9, scale = 3, description = "The time taken to execute the geocode for this request, in milliseconds (this value is duplicated for all results from one request)")
	public BigDecimal getExecutionTime() {
		return searchResults.getExecutionTime();
	}

	@ResultAttribute(index = 350, length = 10, scale = 0, description = "Internal use.")
	public Integer getSid() {
		Integer result = null;
		if(match instanceof AddressMatch) {
			result = ((AddressMatch)match).getAddress().getSID();
		}
		return result;
	}

	/*
	 * @ResultAttribute(index = 360) public String getDebugInfo() { return debugInfo; }
	 * 
	 * public void setDebugInfo(String debugInfo) { this.debugInfo = debugInfo; }
	 */
	
	public Map<String, Object> getCustomizationProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		GeocoderAddress addr = null;
		if(match instanceof AddressMatch) {
			addr = ((AddressMatch)match).getAddress();
		} else if(match instanceof IntersectionMatch) {
			addr = ((IntersectionMatch)match).getAddress();
		}
		if(addr != null && config != null) {
			properties.put("kmlStyleUrl",
					config.getKmlStylesUrl() + "#geocoded_" + addr.getLocationDescriptor() + "_"
							+ addr.getLocationPositionalAccuracy());
		}
		properties.put("kmlPlaceMarkNameAttribute", "fullAddress");
		if(config != null) {
			properties.put("kmlLookAtMinRange", config.getDefaultLookAtRange());
			properties.put("kmlLookAtMaxRange", config.getDefaultLookAtRange());
		}
		properties.put("kmlSnippet", "Score: " + match.getScore() +
				"  Precision: " + match.getPrecision());
		return properties;
	}
	
	private String deNullify(String s) {
		if(s == null) {
			return "";
		}
		return s;
	}
}
