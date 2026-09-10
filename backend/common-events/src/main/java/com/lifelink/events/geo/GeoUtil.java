package com.lifelink.events.geo;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal geo helpers shared across services: haversine distance and a
 * base32 geohash encoder. matching-service keys its KTables by geohash
 * prefix so it only has to join within a neighborhood cell instead of
 * scanning every surplus/need pair.
 */
public final class GeoUtil {

    private static final double EARTH_RADIUS_KM = 6371.0088;
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    private GeoUtil() {
    }

    public static double distanceKm(GeoPoint a, GeoPoint b) {
        double lat1 = Math.toRadians(a.lat());
        double lat2 = Math.toRadians(b.lat());
        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLon = Math.toRadians(b.lon() - a.lon());

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Precision 5 ~= 4.9km x 4.9km cell — coarse enough to group "nearby"
     * providers/requesters, fine enough to keep join fan-out small.
     */
    public static String geohash(GeoPoint point, int precision) {
        double latMin = -90, latMax = 90;
        double lonMin = -180, lonMax = 180;
        StringBuilder hash = new StringBuilder();
        boolean evenBit = true;
        int bit = 0, ch = 0;

        while (hash.length() < precision) {
            if (evenBit) {
                double mid = (lonMin + lonMax) / 2;
                if (point.lon() >= mid) {
                    ch |= (1 << (4 - bit));
                    lonMin = mid;
                } else {
                    lonMax = mid;
                }
            } else {
                double mid = (latMin + latMax) / 2;
                if (point.lat() >= mid) {
                    ch |= (1 << (4 - bit));
                    latMin = mid;
                } else {
                    latMax = mid;
                }
            }
            evenBit = !evenBit;
            if (bit < 4) {
                bit++;
            } else {
                hash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }
        return hash.toString();
    }

    /**
     * The point's own geohash cell plus its 8 neighbors, so callers can find
     * everything within roughly one cell-width of a point without scanning
     * the whole table. Neighbor cells are approximated by offsetting the
     * point by one cell-width in each direction and re-hashing — simpler
     * than decoding geohash bit-neighbors and accurate enough given the
     * exact haversine filter callers apply afterward.
     */
    public static List<String> neighboringCells(GeoPoint center, int precision) {
        double stepDeg = 0.045; // ~5km at the equator, matches a precision-5 cell
        List<String> cells = new ArrayList<>(9);
        for (int dLat = -1; dLat <= 1; dLat++) {
            for (int dLon = -1; dLon <= 1; dLon++) {
                double lat = clamp(center.lat() + dLat * stepDeg, -90, 90);
                double lon = wrapLongitude(center.lon() + dLon * stepDeg);
                String cell = geohash(new GeoPoint(lat, lon), precision);
                if (!cells.contains(cell)) {
                    cells.add(cell);
                }
            }
        }
        return cells;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double wrapLongitude(double lon) {
        double wrapped = ((lon + 180) % 360 + 360) % 360 - 180;
        return wrapped;
    }
}
