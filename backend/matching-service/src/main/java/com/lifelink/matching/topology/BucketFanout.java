package com.lifelink.matching.topology;

import com.lifelink.events.geo.GeoPoint;
import com.lifelink.events.geo.GeoUtil;
import com.lifelink.events.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Expands a surplus/need location into the geohash buckets used for KTable
 * joins — the item's own cell plus neighboring cells so matches are not
 * missed at geohash boundaries.
 */
final class BucketFanout {

    private static final int GEOHASH_PRECISION = 5;

    private BucketFanout() {
    }

    static List<String> bucketKeys(Category category, GeoPoint location, String geohash5) {
        GeoPoint center = location != null ? location : new GeoPoint(0, 0);
        List<String> cells = GeoUtil.neighboringCells(center, GEOHASH_PRECISION);
        List<String> keys = new ArrayList<>(cells.size());
        for (String cell : cells) {
            keys.add(category.name() + "|" + cell);
        }
        if (geohash5 != null && !geohash5.isBlank()) {
            String own = category.name() + "|" + geohash5;
            if (!keys.contains(own)) {
                keys.add(own);
            }
        }
        return keys;
    }
}
