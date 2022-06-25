package com.msnanda515.stockflow.model

data class Location(
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var country: String = "",
    var lat: Double = 0.0,
    var lon: Double = 0.0,
) {

    companion object {
        fun createLocation(wareVm: WarehouseVM): Location {
            return Location(
                wareVm.address,
                wareVm.city,
                wareVm.state,
                wareVm.country,
                wareVm.lat,
                wareVm.lng,
            )
        }
    }

    override fun toString(): String {
        return address
    }
}