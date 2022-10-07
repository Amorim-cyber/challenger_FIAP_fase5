package com.example.reciclo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.reciclo.BuildConfig.MAPS_API_KEY

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.reciclo.databinding.ActivityMapsWorkerBinding
import com.example.reciclo.model.Client
import com.example.reciclo.model.Worker
import com.example.reciclo.util.MapData
import com.google.android.gms.maps.model.*
import com.google.android.material.card.MaterialCardView
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import kotlinx.coroutines.*

class MapsActivityWorker : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsWorkerBinding
    private var routeButton:Button? = null
    private var routeLabel:TextView?=null
    private var routeCardView:MaterialCardView?=null
    private var worker:Worker? = Worker()
    private var clients:ArrayList<Client>? = null
    private var pointerAddress:String = ""

    private var address: String? = null
    private var add: LatLng? = null
    private var marker: MarkerOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nomeLocal = findViewById<EditText>(R.id.nome_local)
        nomeLocal.setBackgroundColor(Color.TRANSPARENT);

        val updateButton = findViewById<Button>(R.id.update_button)
        val backButton = findViewById<Button>(R.id.back_button)

        routeButton = findViewById(R.id.route_button)
        routeLabel = findViewById(R.id.route_label)
        routeCardView = findViewById(R.id.route_card_view)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        worker = intent.getSerializableExtra("WORKER") as? Worker
        clients = intent.getParcelableArrayListExtra("CLIENTS")

        backButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("WORKER", worker)
            intent.putParcelableArrayListExtra("CLIENTS",
                clients)
            startActivity(intent)
        }

        updateButton.setOnClickListener{

            if(nomeLocal.text.isNotBlank()){
                val address = nomeLocal.text.toString()
                worker = Worker(address)
                val add = getLocationFromAddress(this, address)
                val marker = add?.let { MarkerOptions().position(it)
                    .title("Localização atual")
                    .snippet(address)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck))}
                if (marker != null) {
                    mMap.addMarker(marker)
                }
                add?.let { CameraUpdateFactory.newLatLngZoom(it,15f) }?.let { mMap.moveCamera(it) }
                nomeLocal.setText("")
            }

            setPage()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map)
        mMap.setMapStyle(mapStyleOptions)

        setPage()

        mMap.setOnMapClickListener {

            routeButton?.visibility  = View.INVISIBLE
            routeCardView?.visibility = View.INVISIBLE
        }
        mMap.setOnMarkerClickListener { marker ->
            pointerAddress = ""
            if(marker.snippet!= worker?.location && worker!=null){
                routeButton?.visibility = View.VISIBLE
                routeCardView?.visibility = View.VISIBLE
            }
            pointerAddress = marker.snippet.toString()
            marker.showInfoWindow()
            true
        }

    }

    fun setRoute(v:View){
        val client = getClientByAddress(pointerAddress)
        worker?.let { worker!!.latitude?.let { it1 -> worker!!.longitude?.let { it2 ->
            client.latitude?.let { it3 ->
                client.longitude?.let { it4 ->
                    renderizarRota(it1,
                        it2, it3, it4
                    )
                }
            }
        } } }
    }

    private fun setPage(){

        mMap.clear()

        if(worker?.location != null){
            address = worker!!.location
            add = getLocationFromAddress(this, address)
            marker = add?.let { MarkerOptions().position(it)
                .title("Localização atual")
                .snippet(address)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck))}
            if (marker != null) {
                mMap.addMarker(marker!!)
            }
            add?.let { CameraUpdateFactory.newLatLngZoom(it,15f) }?.let { mMap.moveCamera(it) }
        }else{
            add = getLocationFromAddress(this, "Praia de Copacabana")
            add?.let { CameraUpdateFactory.newLatLngZoom(it,1f) }?.let { mMap.moveCamera(it) }
        }

        if(clients != null){
            for(client in clients!!){
                address = client.address
                add = getLocationFromAddress(this, address)
                marker = add?.let { MarkerOptions().position(it)
                    .title("Endereço de ${client.name}")
                    .snippet(client.address)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.trash))}
                if (marker != null) {
                    mMap.addMarker(marker!!)
                }
            }
            if(worker?.location != null){
                address = worker!!.location
                add = getLocationFromAddress(this, address)
                add?.let { CameraUpdateFactory.newLatLngZoom(it,15f) }?.let { mMap.moveCamera(it) }}
            else{
                add?.let { CameraUpdateFactory.newLatLngZoom(it,15f) }?.let { mMap.moveCamera(it) }
            }

        }
    }

    private fun getClientByAddress(address:String):Client{
        return clients!!.single { c -> c.address.equals(address) }
    }

    private fun getLocationFromAddress(context: Context?, strAddress: String?): LatLng? {
        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: LatLng? = null
        try {
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            val location: Address = address[0]
            location.latitude
            location.longitude
            worker?.latitude = location.latitude
            worker?.longitude = location.longitude
            Log.i("WORKER",worker.toString())
            p1 = LatLng(location.latitude, location.longitude)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return p1
    }

    private fun renderizarRota(latOrig: Double,lngOrig: Double,latDest: Double,lngDest: Double){
        CoroutineScope(Dispatchers.IO).launch{
            val resultado = getPolylines(latOrig,lngOrig,latDest,lngDest)
            withContext(Dispatchers.Main){
                val lineOption = PolylineOptions()
                for(i in resultado.indices){
                    lineOption.add(resultado[i])
                    lineOption.color(Color.parseColor("#2C4E72"))
                }
                mMap.addPolyline(lineOption)
                latDest?.let { lngDest?.let { it1 -> LatLng(it, it1) } }
                    ?.let { CameraUpdateFactory.newLatLngZoom(it,15F) }?.let { mMap.animateCamera(it) }
            }

        }

    }

    private fun getPolylines(latOrig: Double,lngOrig: Double,latDest: Double,lngDest: Double): ArrayList<LatLng> {
        val data = requestLocationData("https://maps.googleapis.com/maps/api/directions/json?origin=${latOrig},${lngOrig}&destination=${latDest},${lngDest}&sensor=false&mode=driving&key=${MAPS_API_KEY}")
        Log.e("URL","https://maps.googleapis.com/maps/api/directions/json?origin=${latOrig},${lngOrig}&destination=${latDest},${lngDest}&sensor=false&mode=driving&key=${MAPS_API_KEY}")
        val resultado = ArrayList<LatLng>()

        val responseObject = Gson().fromJson(data, MapData::class.java)
        Log.e("OBJECT", responseObject.toString())
        for(i in 0 until responseObject.routes[0].legs[0].steps.size){
            resultado.addAll(decodificarPolylines(responseObject.routes[0].legs[0].steps[i].polyline.points))
        }
        return resultado
    }

    private fun decodificarPolylines(points: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = points.length
        var lat = 0
        var lng = 0
        while (index < len){
            var b: Int
            var shift = 0
            var result = 0
            do{
                b = points[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = points[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

    private fun requestLocationData(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val data = response.body!!.string()
        Log.e("JSON_DATA",data)
        return data
    }

}

