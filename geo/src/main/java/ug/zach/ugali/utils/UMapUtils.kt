package ug.zach.ugali.utils

import android.content.Context
import android.graphics.*
import android.util.Log
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil

object UMapUtils{

    private var geotype = "polygon"
    lateinit var map:GoogleMap
    var canDraw = false
    var color:Int= Color.YELLOW
    var width:Float=4f


    var listMarkers = arrayListOf<Marker>()

    var lastMarker:Marker?=null

    private var listToRedo = arrayListOf<Marker>()


    var listPolygons = arrayListOf<Polygon>()
    lateinit var polygon:Polygon

    var listPolylines = arrayListOf<Polyline>()
    lateinit var polyline:Polyline

    fun edit(){

    }

    fun initialize(geotype: String) {
        UMapUtils.geotype = geotype
        canDraw = false


        //canDraw = false

        clear()

        map.setOnMapClickListener {
            Log.d("ZAKA", "$canDraw")
            if (canDraw){
                drawPoly(it)
            }
        }

        map.setOnMarkerClickListener { p0 ->
            lastMarker = p0
            p0.showInfoWindow()
            true
        }

        map.setOnPolygonClickListener {

        }


        var oldMarker:Marker? = null
        map.setOnMarkerDragListener(object :GoogleMap.OnMarkerClickListener,
            GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(p0: Marker) {

            }
            override fun onMarkerDrag(p0: Marker) {
                var position = 0
                var count = 0
                listMarkers.forEach {
                    if (it==oldMarker){
                        position=count
                        return@forEach
                    }
                    count++
                }
                listMarkers.remove(oldMarker)
                listMarkers.add(position,p0)
                oldMarker = p0

                redrawPolygon()
            }

            override fun onMarkerDragEnd(p0: Marker) {
                //listMarkers.add(p0)
                //redrawPolygon()
            }

            override fun onMarkerClick(p0: Marker): Boolean {
                oldMarker = p0
                return true
            }

        })


    }

    fun drawPoly(it: LatLng) {
        addMarker(it)
        val listLatLng = arrayListOf<LatLng>()
        listMarkers.forEach { marker-> listLatLng.add(marker.position) }
        drawPolygon(listLatLng)
    }

    fun drawLine(it: LatLng) {
        addMarker(it)
        val listLatLng = arrayListOf<LatLng>()
        listMarkers.forEach { marker-> listLatLng.add(marker.position) }
        drawPolyline(listLatLng)
    }

    fun addMarker(it: LatLng) {
        try {
            val marker = map.addMarker(
                MarkerOptions()
                    .title("${it.latitude},${it.longitude}")
                    .position(it)
                    .draggable(true)
            )!!
            listMarkers.add(marker)
            lastMarker = marker
        }catch (e:Exception){
            e.message?.let { it1 -> Log.d("ZAKA", it1) }
        }
    }

    private fun drawPolygon(listLatLong: ArrayList<LatLng>) {
        try {
            if (listLatLong.size>0){
                if (listLatLong[0]!=listLatLong[listLatLong.size-1]){
                    listLatLong.add(listLatLong[0])
                }
            }
            if (listPolygons.size>0){
                listPolygons.forEach { it.remove() }
            }
            val polygon = map.addPolygon(PolygonOptions()
                .addAll(listLatLong)
                .strokeWidth(width)
                .strokeColor(color)
            )
            UMapUtils.polygon = polygon

            listPolygons.add(polygon)


        }catch (e:Exception){
            e.message?.let { it1 -> Log.d("ZAKA", it1) }
        }
    }

    fun undo(){
        if (listMarkers.size>0){
            val marker = listMarkers.last()
            listToRedo.add(marker)
            deleteMarker(marker)
        }
    }

    fun redo(){
        if (listToRedo.size > 0){
            val marker = listMarkers.first()
            listMarkers.add(marker)
            listToRedo.remove(marker)

            redrawPolygon()
        }
    }

    fun done(): Boolean {

        when(geotype){
            "polygon"->{
                if (polygon.points.size>0){
                    softClear()
                    return true
                }
            }
            "polyline"->{
                if (polyline.points.size>0){
                    softClear()
                    return true
                }
            }
            else->{
                if (lastMarker !=null){
                    softClear()
                    return true
                }
            }
        }

        return false
    }

    private fun softClear(){
        if (listMarkers.size>2){
            canDraw =false
            listMarkers.forEach { it.remove() }
            //polygon = listPolygons[0]
            listMarkers.clear()
            listPolygons.clear()
            listToRedo.clear()
        }
    }


    fun deleteMarker(marker:Marker?= lastMarker):Boolean{
        return try {
            if (marker!=null){
                listMarkers.remove(marker)
                marker.remove()
                redrawPolygon()
            }
            true
        }catch (e:Exception){
            false
        }
    }

    fun clear(mapClear:Boolean=true) {
        listMarkers.forEach { it.remove() }
        listPolygons.forEach { it.remove() }
        listToRedo.forEach { it.remove() }

        listMarkers.clear()
        listPolygons.clear()
        listToRedo.clear()

        try {
            polyline.remove()
            polygon.remove()
        }catch (e:Exception){}
        if (mapClear){
            map.clear()
        }




    }

    fun redrawPolygon() {
        val listLatLong: ArrayList<LatLng> = arrayListOf()
        listMarkers.forEach { listLatLong.add(it.position) }

        if (geotype =="polygon"){
            drawPolygon(listLatLong)
        }else{
            drawPolyline(listLatLong)
        }

    }



















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


    fun getGeom(poly: String): ArrayList<LatLng> {
        val lst_points = arrayListOf<LatLng>()
        val point =poly.lowercase().removePrefix("polygon").removePrefix("polyline")
            .removePrefix("point")
            .replace("(","").replace(")","")

        try {
            if (point.length > 10) {
                val points = point.split(",")
                points.forEach { poin ->
                    val ltln = poin.trim().split(" ")
                    lst_points.add(LatLng(ltln[1].toDouble(), ltln[0].toDouble()))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.let { Log.d("ZAKA", it) }
        }
        return lst_points
    }

    fun set_geom(lst_points: MutableList<LatLng>,type:String): String {
        val init = when(type){
            "polygon"->{
                "POLYGON"
            }else->{
                "POLYLINE"
            }
        }
        return if (lst_points.size>1){
            var pol = "$init(("
            lst_points.forEach { p -> pol += "${p.longitude} ${p.latitude}," }
            pol.trim().substring(0, pol.length - 1) + "))"
        }else{
            var pol = "POINT("
            val p = lst_points[0]
            pol += "${p.longitude} ${p.latitude}"
            pol.trim().substring(0, pol.length - 1) + ")"
        }
    }


    fun getSQM(poly: String?): String {
        if (geotype =="polygon"){
            return "%.1f".format(SphericalUtil.computeArea(getGeom(poly!!)))
        }
        return "%.1f".format(SphericalUtil.computeLength(getGeom(poly!!)))
    }

    fun outputPolygon(): String {
        return when(geotype){
            "polygon"-> set_geom(polygon.points, geotype)
            "line"-> set_geom(polyline.points, geotype)
            else-> set_geom(arrayListOf(lastMarker?.position!!), geotype)
        }
    }




    private fun drawPolyline(listLatLong: ArrayList<LatLng>) {
        try {
            listPolylines.forEach { it.remove() }

            val line = map.addPolyline(PolylineOptions()
                .addAll(listLatLong)
                .width(width)
                .width(width)
                .color(color)
            )

            polyline = line
            listPolylines.add(line)


        }catch (e:Exception){
            e.message?.let { it1 -> Log.d("ZAKA", it1) }
        }
    }

    fun drawGeom(poly: ArrayList<LatLng>, color: Int= UMapUtils.color, width:Float= UMapUtils.width, onWork:Boolean=true) {
        try {
            if(poly.size>1){
                if (poly[0]==poly[poly.size-1]){
                    val polygon = map.addPolygon(
                        PolygonOptions()
                            .addAll(poly)
                            .strokeWidth(width)
                            .strokeColor(color)
                    )
                   if (onWork){
                       var i = 0
                       poly.forEach {
                           if (i<poly.size-1){ drawPoly(it) }
                           i++
                       }
                       listPolygons.add(polygon)
                   }
                }else{
                    val polygline = map.addPolyline(
                        PolylineOptions()
                            .addAll(poly)
                            .width(width)
                            .color(color)
                    )
                    if (onWork){
                        var i = 0
                        poly.forEach {
                            if (i<poly.size-1){ drawLine(it) }
                            i++
                        }
                        listPolylines.add(polygline)
                    }
                }
            }else{
                if (onWork){
                    addMarker(poly[0])
                }else{
                    val it = poly[0]
                    map.addMarker(
                        MarkerOptions()
                            .title("${it.latitude},${it.longitude}")
                            .position(it)
                            .draggable(true)
                    )!!
                }
            }
        }catch (e:Exception){
            e.message?.let { Log.d("ZAKA", it) }
        }
    }


}