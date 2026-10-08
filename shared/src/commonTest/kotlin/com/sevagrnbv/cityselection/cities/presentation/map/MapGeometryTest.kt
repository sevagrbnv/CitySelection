package com.sevagrnbv.cityselection.cities.presentation.map

import kotlin.test.*

class MapGeometryTest {
    @Test
    fun distanceCrossesAntimeridianByShortestArc() {
        assertEquals(222_390.0, distanceMeters(0.0, 179.0, 0.0, -179.0), 1.0)
        assertEquals(0.0, distanceMeters(55.0, 37.0, 55.0, 37.0), 0.001)
    }
    @Test
    fun radiusCoversEveryCornerAndNeverRoundsDown() {
        val camera = MapCamera(0.0, 0.0)
        val corners = listOf(1.0 to 1.0, -2.0 to 1.0, 1.0 to -1.0, -2.0 to -1.0)
        val radius = viewportRadius(camera, corners)
        assertTrue(corners.all { (lat, lon) -> distanceMeters(0.0, 0.0, lat, lon) <= radius })
        assertEquals(1, viewportRadius(camera, listOf(0.0 to 0.0)))
    }
}
