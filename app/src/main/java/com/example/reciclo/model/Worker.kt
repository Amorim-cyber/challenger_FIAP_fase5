package com.example.reciclo.model

import java.io.Serializable

class Worker(var location: String? = null): Serializable{

    var longitude: Double? = 0.0
    var latitude: Double? = 0.0
    override fun toString(): String {
        return "Worker(location=$location, longitude=$longitude, latitude=$latitude)"
    }


}