package com.msnanda515.stockflow.model

import org.bson.types.ObjectId

data class Shipment(
    val from: ObjectId?,
    val to: ObjectId?,
    val units: Int?,
)