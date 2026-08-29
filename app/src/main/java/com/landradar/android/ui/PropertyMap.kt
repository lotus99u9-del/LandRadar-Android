package com.landradar.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.landradar.android.data.Property
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun PropertyMap(
    properties: List<Property>,
    onPropertyClick: (Property) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            controller.setZoom(5.5)
            controller.setCenter(GeoPoint(13.7367, 100.5231))
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.overlays.clear()
            val points = properties.map { GeoPoint(it.latitude, it.longitude) }
            properties.forEach { property ->
                Marker(map).apply {
                    position = GeoPoint(property.latitude, property.longitude)
                    title = property.title
                    snippet = property.district + ", " + property.province
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { marker, _ ->
                        marker.showInfoWindow()
                        onPropertyClick(property)
                        true
                    }
                    map.overlays.add(this)
                }
            }
            when (points.size) {
                0 -> {
                    map.controller.setZoom(5.5)
                    map.controller.setCenter(GeoPoint(13.7367, 100.5231))
                }
                1 -> {
                    map.controller.setZoom(15.0)
                    map.controller.setCenter(points.first())
                }
                else -> map.post {
                    map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), true, 72)
                }
            }
            map.invalidate()
        }
    )
}
