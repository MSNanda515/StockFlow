package com.msnanda515.stockflow.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import kotlin.math.min

enum class PalletStatus {
    @JsonProperty("station") STAT,
    @JsonProperty("transit") TRAN,
}

@Document
data class Pallet(
    var itemNo: Long,
    var units: Int,
    var palletLoc: PalletLoc,
    var shipment: Shipment?,
    var status: PalletStatus = PalletStatus.STAT,
    @Id
    var id: ObjectId = ObjectId.get(),
    var createdDate: LocalDateTime = LocalDateTime.now(),
    var modifiedDate: LocalDateTime = LocalDateTime.now()
): Comparable<Pallet> {
    override fun compareTo(other: Pallet): Int {
        return palletLoc.compareTo(other.palletLoc)
    }

    fun getDisplayStr(): String {
        val pId = id.toString().takeLast(6)
        return "PalletId: $pId, Warehouse: ${palletLoc.wareNo}, Aisle: ${palletLoc.aisle}, " +
                "Section: ${palletLoc.section}, Level: ${palletLoc.level}, Units: ${units}"
    }

    /**
     * Returns true if item is in shipment
     * Item in shipment when status set to PalletStatus.Tran
     */
    fun isPalletInShipping(): Boolean {
        return status == PalletStatus.TRAN
    }

    fun receivePallet(newPalletLoc: PalletLoc) {
        if (isPalletInShipping()) {
            status = PalletStatus.STAT
            palletLoc = newPalletLoc
            shipment = null
        }
    }

    companion object {
        /**
         * Creates the pallets for an item
         */
        fun createPalletsForItem(itemNo: Long, units: Int, palletLocs: List<PalletLoc>, noPallets: Int,
            palletCap: Int): List<Pallet> {
            if (palletLocs.size != noPallets) {
                throw RuntimeException("palletLocs size (${palletLocs.size}) doesn't match with noPallets ${noPallets}")
            }
            val pallets = mutableListOf<Pallet>()
            var unitsLeft = units
            for (palletLoc in palletLocs) {
                val unitsPallet = min(unitsLeft, palletCap)
                pallets.add(Pallet(itemNo, unitsPallet, palletLoc, null))
                unitsLeft -= unitsPallet
            }
            return pallets
        }
    }
}

data class PalletLoc(
    var wareNo: Long,
    var aisle: Int,
    var section: Int,
    var level: Int,
): Comparable<PalletLoc> {
    /**
     * Default comparator to sort pallets according to their loc
     */
    override fun compareTo(other: PalletLoc): Int {
        return if (this.wareNo != other.wareNo) {
            this.wareNo.compareTo(other.wareNo)
        } else if (this.aisle != other.aisle) {
            this.aisle.compareTo(other.aisle)
        } else if (this.section != other.section) {
            this.section.compareTo(other.section)
        } else {
            this.level.compareTo(other.level)
        }
    }

    override fun toString(): String {
        return "(${wareNo},${aisle},${section},${level})"
    }
}

