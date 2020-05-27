package ca.bc.gov.ols.cpf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import ca.bc.gov.open.cpf.plugin.impl.BusinessApplicationPluginExecutor;

public class GeocoderPluginTest {
	private static BusinessApplicationPluginExecutor bape;
	
	@BeforeClass
	public static void setUp() {
		bape = new BusinessApplicationPluginExecutor();
	}
	
	@Test
	public void testAddress() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("addressString", "1207 Douglas");
		List<Map<String, Object>> results = bape.executeList(GeocoderPlugin.PLUGIN_NAME, params);
		System.out.println(results.toString());
	}
	
	@Test
	public void testExtrapolate() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("addressString", "525 Superior St, Victoria, BC");
		params.put("extrapolate", Boolean.TRUE);
		params.put("parcelPoint", "SRID=4326;POINT(-123.370780 48.417926)");
		List<Map<String, Object>> results = bape.executeList(GeocoderPlugin.PLUGIN_NAME, params);
		System.out.println(results.toString());
	}
	
	@Test
	public void testAccents() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("addressString",
				"Mount Douglas Cross (Ã€Ã Ã‚Ã¢Ã†Ã¦Ã‡Ã§Ã‰Ã©ÃˆÃ¨ÃŠÃªÃ‹Ã«ÃŽÃ®Ã�Ã­Ã�Ã¯Ã”Ã´Å’Å“Ã™Ã¹Ã›Ã»ÃœÃ¼ÃƒÅ¸Ã¿) rd saanich bc");
		List<Map<String, Object>> results = bape.executeList(GeocoderPlugin.PLUGIN_NAME, params);
		System.out.println(results.toString());
	}
	
	@Test
	public void testLocality() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("addressString", "Victoria");
		List<Map<String, Object>> results = bape.executeList(GeocoderPlugin.PLUGIN_NAME, params);
		System.out.println(results.toString());
	}
}
