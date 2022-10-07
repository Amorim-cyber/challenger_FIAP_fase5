package com.example.reciclo.util

class MapData {
    var routes = ArrayList<Routes>()
    override fun toString(): String {
        return "MapData(routes=$routes)"
    }
}

class Routes {
    var legs = ArrayList<Legs>()
    override fun toString(): String {
        return "Routes(legs=$legs)"
    }
}

class Legs {
    var distance = Distance()
    var duration = Duration()
    var end_address = ""
    var start_address = ""
    var end_location = Location()
    var start_location = Location()
    var steps = ArrayList<Steps>()
    override fun toString(): String {
        return "Legs(distance=$distance, duration=$duration, end_address='$end_address', start_address='$start_address', end_location=$end_location, start_location=$start_location, steps=$steps)"
    }
}

class Steps {
    var distance = Distance()
    var duration = Duration()
    var end_address = ""
    var start_address = ""
    var end_location = Location()
    var start_location = Location()
    var polyline = Polyline()
    var travel_mode = ""
    var maneuver = ""
    override fun toString(): String {
        return "Steps(distance=$distance, duration=$duration, end_address='$end_address', start_address='$start_address', end_location=$end_location, start_location=$start_location, polyline=$polyline, travel_mode='$travel_mode', maneuver='$maneuver')"
    }
}

class Duration {
    var text = ""
    var value = 0
    override fun toString(): String {
        return "Duration(text='$text', value=$value)"
    }
}

class Distance {
    var text = ""
    var value = 0
    override fun toString(): String {
        return "Distance(text='$text', value=$value)"
    }
}

class Polyline{
    var points = ""
    override fun toString(): String {
        return "Polyline(points='$points')"
    }
}

class Location{
    var lat = ""
    var lng = ""
    override fun toString(): String {
        return "Location(lat='$lat', lng='$lng')"
    }
}