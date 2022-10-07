package com.example.reciclo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.example.reciclo.databinding.ActivityMapsClientBinding
import com.example.reciclo.model.Client
import com.example.reciclo.model.Worker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.card.MaterialCardView


class MapsActivityClient : AppCompatActivity(), OnMapReadyCallback {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsClientBinding
    private var client :Client? = null
    private var worker :Worker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val addButton = findViewById<Button>(R.id.add_button)
        val expandButton = findViewById<Button>(R.id.expand_button)
        val startButton = findViewById<Button>(R.id.start_button)
        val newButton = findViewById<Button>(R.id.new_button)
        val backButton = findViewById<Button>(R.id.back_button)
        val loginNome = findViewById<EditText>(R.id.login_nome)
        val editNome = findViewById<EditText>(R.id.edit_nome)
        val editEnd = findViewById<EditText>(R.id.edit_end)
        val buttonLabel = findViewById<TextView>(R.id.button_label)
        val cardModal = findViewById<CardView>(R.id.card_modal)
        val cardViewEnd = findViewById<MaterialCardView>(R.id.cardview_end)
        val timeLabel = findViewById<MaterialCardView>(R.id.time_label)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        var clients:ArrayList<Client>? = intent.getParcelableArrayListExtra("CLIENTS")

        if(clients == null){
            clients = ArrayList()
        }

        disable(loginNome,editNome,editEnd,addButton,backButton)

        addButton.setOnClickListener{
            if(editEnd.text.isNotBlank() && editNome.text.isNotBlank()){
                if(client!=null){
                    client!!.name = editNome.text.toString()
                    client!!.address = editEnd.text.toString()
                    setPage(editNome.text.toString(),editEnd.text.toString(),buttonLabel,
                        addButton,expandButton,editNome,cardViewEnd,mapFragment)
                    client!!.latitude = latitude
                    client!!.longitude = longitude
                }else{
                    setPage(editNome.text.toString(),editEnd.text.toString(),buttonLabel,
                        addButton,expandButton,editNome,cardViewEnd,mapFragment)
                    client = Client(editNome.text.toString(),editEnd.text.toString(),latitude,longitude)
                    clients.add(client!!)
                }

            }
        }

        expandButton.setOnClickListener{
            addButton.visibility = View.VISIBLE
            expandButton.visibility = View.INVISIBLE

            editNome.setMargins(
                left = 82
            )
            editNome.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            editNome.isFocusableInTouchMode = true
            editNome.isEnabled = true
            editNome.isCursorVisible = true

            cardViewEnd.visibility = View.VISIBLE
            editEnd.setText("")

            val params: ViewGroup.LayoutParams = mapFragment.view!!.layoutParams
            params.height = 950
            mapFragment.view?.layoutParams = params
        }

        startButton.setOnClickListener{
            if(clients.size > 0){
                worker = intent.getSerializableExtra("WORKER") as? Worker
                try{
                    client = clients.single { c -> c.name.equals(loginNome.text.toString()) }
                    if(client!=null){
                        enable(editNome,editEnd,addButton,backButton)
                        cardModal.visibility = View.INVISIBLE
                        setPage(
                            client!!.name, client!!.address,buttonLabel,
                            addButton,expandButton,editNome,cardViewEnd,mapFragment)
                        editNome.setText(client!!.name)

                        if(worker!=null){
                            timeLabel.visibility = View.VISIBLE
                        }
                    }
                }catch(e:NoSuchElementException){
                    loginNome.setText("")
                }
            }
        }

        newButton.setOnClickListener{
            worker = intent.getSerializableExtra("WORKER") as? Worker
            enable(editNome,editEnd,addButton,backButton)
            cardModal.visibility = View.INVISIBLE
        }

        backButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("WORKER", worker)
            intent.putParcelableArrayListExtra("CLIENTS",
                clients)
            loginNome.setText("")
            editNome.setText("")
            editEnd.setText("")
            startActivity(intent)
        }

    }

    
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map)
        mMap.setMapStyle(mapStyleOptions)

        // Add a marker and move the camera
        val address = getLocationFromAddress(this, "Praia de Copacabana")
        address?.let { CameraUpdateFactory.newLatLngZoom(it,1f) }?.let { mMap.moveCamera(it) }

    }

    private fun setPage(name: String?,address: String?,buttonLabel:TextView?,
                                addButton:Button?,expandButton:Button?,
                        editNome:EditText?,cardViewEnd:MaterialCardView?,
                        mapFragment:SupportMapFragment?){
        var add = getLocationFromAddress(this, address)
        var marker = add?.let { MarkerOptions().position(it)
            .title("Endereço de $name")
            .snippet(address)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.user))}
        if (marker != null) {
            mMap.clear()
            mMap.addMarker(marker)?.showInfoWindow()

            if(worker!=null && client!=null){
                add = getLocationFromAddress(this, worker!!.location)
                marker = add?.let { MarkerOptions().position(it)
                    .title("Localização atual do caminhão")
                    .snippet(worker!!.location)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck))}
                if (marker != null) {
                    mMap.addMarker(marker)
                }
            }

        }
        add?.let { CameraUpdateFactory.newLatLngZoom(it,15f) }?.let { mMap.moveCamera(it) }
        if (buttonLabel != null) {
            buttonLabel.text = "alterar"
        }
        if (addButton != null) {
            addButton.visibility = View.INVISIBLE
        }
        if (expandButton != null) {
            expandButton.visibility = View.VISIBLE
        }

        if (editNome != null) {
            editNome.setMargins(
                left = 0
            )
            editNome.textAlignment = View.TEXT_ALIGNMENT_CENTER
            editNome.isFocusable = false
            editNome.isEnabled = false
            editNome.isCursorVisible = false
        }

        if (cardViewEnd != null) {
            cardViewEnd.visibility = View.INVISIBLE
        }


        if (mapFragment != null) {
            val params: ViewGroup.LayoutParams = mapFragment.view!!.layoutParams
            params.height = 1150
            mapFragment.view?.layoutParams = params
        }



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
            p1 = LatLng(location.latitude, location.longitude)
            latitude = location.latitude
            longitude = location.longitude
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return p1
    }

    private fun disable(loginNome:EditText?,editNome:EditText?, editEnd:EditText?,
                addButton:Button?, backButton:Button?){
        loginNome?.setBackgroundColor(Color.TRANSPARENT)

        if (editNome != null) {
            editNome.setBackgroundColor(Color.TRANSPARENT);
            editNome.isFocusable = false
            editNome.isEnabled = false
            editNome.isCursorVisible = false
        }

        if (editEnd != null) {
            editEnd.setBackgroundColor(Color.TRANSPARENT);
            editEnd.isFocusable = false
            editEnd.isEnabled = false
            editEnd.isCursorVisible = false
        }

        if (addButton != null) addButton.isEnabled = false
        if (backButton != null) backButton.isEnabled = false


    }

    private fun enable(editNome:EditText?, editEnd:EditText?,
                        addButton:Button?, backButton:Button?){
        if (editNome != null) {
            editNome.isFocusableInTouchMode = true
            editNome.isEnabled = true
            editNome.isCursorVisible = true
        }

        if (editEnd != null) {
            editEnd.isFocusableInTouchMode = true
            editEnd.isEnabled = true
            editEnd.isCursorVisible = true
        }

        if (addButton != null) addButton.isEnabled = true
        if (backButton != null) backButton.isEnabled = true
    }

    private fun View.setMargins(
        left: Int = this.marginLeft,
        top: Int = this.marginTop,
        right: Int = this.marginRight,
        bottom: Int = this.marginBottom,
    ) {
        layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
            setMargins(left, top, right, bottom)
        }
    }

}