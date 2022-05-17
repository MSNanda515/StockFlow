package com.msnanda515.stockflow.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

enum class PalletStatus {
    @JsonProperty("station") STAT,
    @JsonProperty("transit") TRAN,
}

@Document
data class Pallet(
    var itemNo: Long,
    var warehouseId: ObjectId,
    var units: Int,
    var status: PalletStatus = PalletStatus.STAT,
    var shipment: Shipment?,
    @Id
    var id: ObjectId = ObjectId.get(),
    var createdDate: LocalDateTime = LocalDateTime.now(),
    var modifiedDate: LocalDateTime = LocalDateTime.now()
)

