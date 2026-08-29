package com.landradar.android.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.landradar.android.R
import com.landradar.android.data.Property
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
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

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
            map.overlays.clear()
            val points = properties.map { GeoPoint(it.latitude, it.longitude) }
            properties.forEach { property ->
                Marker(map).apply {
                    position = GeoPoint(property.latitude, property.longitude)
                    title = property.title
                    snippet = property.district + ", " + property.province
                    icon = createMarkerIcon(context.resources.displayMetrics.density)
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
                    val bounds = BoundingBox.fromGeoPoints(points)
                    map.zoomToBoundingBox(bounds, true, 96)
                    map.controller.setCenter(bounds.centerWithDateLine)
                    if (map.zoomLevelDouble < 6.2) map.controller.setZoom(6.2)
                    map.invalidate()
                }
            }
                map.setMinZoomLevel(5.0)
                map.setMaxZoomLevel(19.0)
                map.invalidate()
            }
        )
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = { mapView.controller.zoomIn() },
                modifier = Modifier.size(42.dp)
            ) { Text("+", style = MaterialTheme.typography.titleLarge) }
            FilledTonalIconButton(
                onClick = { mapView.controller.zoomOut() },
                modifier = Modifier.size(42.dp)
            ) { Text("−", style = MaterialTheme.typography.titleLarge) }
        }
    }
}

private fun createMarkerIcon(density: Float): BitmapDrawable {
    val width = (40 * density).toInt().coerceAtLeast(40)
    val height = (48 * density).toInt().coerceAtLeast(48)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val cx = width / 2f
    val radius = width * 0.42f

    paint.color = Color.rgb(23, 107, 58)
    val pin = Path().apply {
        moveTo(cx, height.toFloat())
        cubicTo(width * 0.34f, height * 0.72f, width * 0.08f, height * 0.52f, width * 0.08f, radius)
        arcTo(width * 0.08f, 0f, width * 0.92f, radius * 2f, 180f, 180f, false)
        cubicTo(width * 0.92f, height * 0.52f, width * 0.66f, height * 0.72f, cx, height.toFloat())
        close()
    }
    canvas.drawPath(pin, paint)
    paint.color = Color.WHITE
    canvas.drawCircle(cx, radius, radius * 0.52f, paint)
    paint.color = Color.rgb(23, 107, 58)
    canvas.drawCircle(cx, radius, radius * 0.22f, paint)
    return BitmapDrawable(null, bitmap)
}
