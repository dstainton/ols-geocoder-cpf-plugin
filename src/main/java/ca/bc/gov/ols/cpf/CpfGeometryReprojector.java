package ca.bc.gov.ols.cpf;

import ca.bc.gov.ols.geocoder.api.GeometryReprojector;
import ca.bc.gov.open.cpf.plugin.api.GeometryFactory;

import org.locationtech.jts.geom.Geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class CpfGeometryReprojector implements GeometryReprojector {
	
	private org.locationtech.jts.geom.GeometryFactory gf;
	
	public CpfGeometryReprojector(org.locationtech.jts.geom.GeometryFactory gf) {
		this.gf = gf;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Geometry> T reproject(T geom, int toSRSCode) {
		if(geom.getSRID() != toSRSCode) {
			Point p = GeometryFactory.getFactory(toSRSCode).createPoint(new Coordinate(((org.locationtech.jts.geom.Point)geom).getX(), ((org.locationtech.jts.geom.Point)geom).getY()));
			return (T)gf.createPoint(new org.locationtech.jts.geom.Coordinate(p.getX(), p.getY()));
		}
		return geom;
	}
	
}
