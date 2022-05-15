package go.lands.ilmis.utils.ugali

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil


object UgMap {
    fun getCentroid(latLngList: ArrayList<LatLng>): LatLng {
        val centroid = doubleArrayOf(0.0, 0.0)
        for (i in latLngList.indices) {
            centroid[0] += latLngList[i].latitude
            centroid[1] += latLngList[i].longitude
        }
        val totalPoints = latLngList.size
        return LatLng(centroid[0] / totalPoints, centroid[1] / totalPoints)
    }

    fun addText(
        context: Context?, map: GoogleMap?,
        location: LatLng?, text: String?, padding: Int=0,
        fontSize: Int=14,
        color:String = "#ffff00"
    ): Marker? {
        var marker: Marker? = null

        if (context == null || map == null || location == null || text == null
            || fontSize <= 0
        ) {
            return marker
        }

        val textView = TextView(context)
        textView.text = text
        textView.textSize = fontSize.toFloat()

        val paintText = textView.paint

        val boundsText = Rect()
        paintText.getTextBounds(text, 0, textView.length(), boundsText)
        paintText.textAlign = Paint.Align.CENTER

        val conf = Bitmap.Config.ARGB_8888
        val bmpText = Bitmap.createBitmap(
            boundsText.width() + 2 * padding,
            boundsText.height() + 2 * padding,
            conf
        )

        val canvasText = Canvas(bmpText)
        paintText.color = Color.parseColor(color)

        canvasText.drawText(
            text, (canvasText.width / 2).toFloat(),
            (canvasText.height - padding - boundsText.bottom).toFloat(), paintText
        )

        val markerOptions = MarkerOptions()
            .position(location)
            .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
            .anchor(0.5f, 1f)

        marker = map.addMarker(markerOptions)

        return marker
    }

    fun setExtent(mMap: GoogleMap, lst_latlng: ArrayList<LatLng>) {
        try {
            val builder = LatLngBounds.Builder()
            lst_latlng.forEach { p -> builder.include(p) }
            val cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 16)
            mMap.animateCamera(cu)
        } catch (e: Exception) {
            e.message?.let { Log.d("ZAKA", it) }
        }
    }

    fun updateMyLocation(context: AppCompatActivity, gmap: GoogleMap) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            ActivityCompat.requestPermissions(
                context, arrayListOf(
                    android.Manifest.permission
                        .ACCESS_FINE_LOCATION
                ).toTypedArray(), 1
            )
        }
        gmap.isMyLocationEnabled = true
        gmap.uiSettings?.isMyLocationButtonEnabled = true
    }

    fun getDeviceLocation(context: AppCompatActivity, gmap: GoogleMap) {
        val mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            ActivityCompat.requestPermissions(
                context, arrayListOf(
                    android.Manifest.permission
                        .ACCESS_FINE_LOCATION
                ).toTypedArray(), 1
            )
        }

        val locationResult = mFusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("ZAKA", "${it.result?.latitude} ${it.result?.longitude}")
                gmap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.result?.latitude!!, it.result?.longitude!!), 21.0f
                    )
                )
                Log.d("ZAKA", it.result!!.accuracy.toString())
            }
        }
    }

    fun showCurrentPlace(context: AppCompatActivity) {
        val mPlaceDetectionClient = Places.getPlaceDetectionClient(context)
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            ActivityCompat.requestPermissions(
                context, arrayListOf(
                    android.Manifest.permission
                        .ACCESS_FINE_LOCATION
                ).toTypedArray(), 1
            )
        }
        val placeResult = mPlaceDetectionClient.getCurrentPlace(null)
        placeResult.addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.count!! > 0) {
                    it.result?.forEach { pl ->
                        Log.d("ZAKA", pl.place?.phoneNumber.toString())
                    }
                }
            }
        }
    }

    fun get_poly(poly: String): ArrayList<LatLng> {
        val lst_points = arrayListOf<LatLng>()
        try {
            val point = poly.replace("POLYGON((", "").replace("))", "")
            if (point.length > 10) {
                val points = point.split(",")
                points.forEach { poin ->
                    val ltln = poin.trim().split(" ")
                    lst_points.add(LatLng(ltln[1].toDouble(), ltln[0].toDouble()))
                }
            }
        } catch (e: Exception) {
            e.message?.let { Log.d("ZAKA", it) }
        }
        return lst_points
    }

    fun set_poly(lst_points: MutableList<LatLng>): String {
        var pol = "POLYGON(("
        lst_points.forEach { p -> pol += "${p.longitude} ${p.latitude}," }
        return pol.trim().substring(0, pol.length - 1) + "))"
    }

    fun getHactor(sqr: Double): String {
        return "%.1f".format(sqr / 10000)
    }

    fun getHactorFromAcres(acres: Double): String {
        return "%.1f".format(acres * 0.404686)
    }

    fun getAcres(sqr: Double): String {
        return "%.1f".format(sqr * 0.000247105)
    }


    fun getSQM(poly: String?): String {
        return "%.1f".format(SphericalUtil.computeArea(get_poly(poly!!)))
    }

}