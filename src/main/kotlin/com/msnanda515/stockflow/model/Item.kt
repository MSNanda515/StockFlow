package com.msnanda515.stockflow.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.msnanda515.stockflow.exception.InvalidRequestException
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import kotlin.math.ceil

/**
 * Represents the model for an item in the warehouse
 */
@Document
data class Item(
    var itemNo: Long,
    var name: String,
    var description: String,
    var department: Department,
    var status: ItemStatus,
    @Id
    val id: ObjectId = ObjectId.get(),
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val modifiedDate: LocalDateTime = LocalDateTime.now(),

    var pallets: MutableList<Pallet> = mutableListOf(),
    val shipments: MutableList<Shipment> = mutableListOf(),
) {
    /**
     * Gets the number of pallets required to store the item
     */
    fun palletsRequired(units: Int): Int =
        ceil(1.0 * units / department.palleteCap).toInt()

    fun getDisplayStr(): String {
        return "$itemNo (Name: $name, Department: ${department.disp}, Pallet Cap: ${department.palleteCap})"
    }

    /**
     * Ships pallets from one warehouse to another
     */
    fun shipItem(units: Int, tempShipment: Shipment): Pair<MutableSet<ObjectId>, MutableMap<ObjectId, Int>> {
        if (getUnitsInWare(tempShipment.from) < units) {
            throw InvalidRequestException("Not enough units in warehouse")
        }
        val shipment = Shipment(tempShipment.from, tempShipment.to, units, tempShipment.id)
        val deletePalletsWare = mutableSetOf<ObjectId>() // to delete the pallet from the warehouse collection
        val palletsUpdated = mutableMapOf<ObjectId, Int>() // to update the pallet in the warehouse collection
        val palletsAdded = mutableListOf<Pallet>() // to add the new pallets to the list of pallets
        var itemsAlloc = 0

        for (i in 0 until pallets.size) {
            val it = pallets[i]
            if (it.palletLoc.wareNo == shipment.from && !it.isPalletInShipping()) {
                if (itemsAlloc == units) {
                    // all items allocated
                    break
                }
                if (it.units <= (units-itemsAlloc)) {
                    shipPallet(it, shipment)
                    deletePalletsWare.add(it.id)
                    itemsAlloc += it.units
                } else {
                    // units are greater, pallet needs to be partially emptied
                    // create a new pallet for shipping, update the units on original pallet
                    val newPalletForShipping = Pallet(itemNo, (units-itemsAlloc),
                        PalletLoc(shipment.to, -1, 0, 0), shipment, PalletStatus.TRAN)
                    it.units -= (units-itemsAlloc)
                    palletsAdded.add(newPalletForShipping)
                    palletsUpdated[it.id] = it.units
                    break
                }
            }
        }

        pallets.addAll(palletsAdded)
        return Pair(deletePalletsWare, palletsUpdated)
    }

    /**
     * Ships the given pallet in the mentioned shipment
     */
    fun shipPallet(pallet: Pallet, shipment: Shipment) {
        pallet.status = PalletStatus.TRAN
        pallet.palletLoc.wareNo = shipment.to
        pallet.palletLoc.aisle = -1
        pallet.shipment = shipment
    }

    /**
     * Gets the number of units for the item present in the warehouse
     */
    fun getUnitsInWare(wareNo: Long): Int {
        return pallets.fold(0) {sum, p ->
            if (p.palletLoc.wareNo == wareNo && !p.isPalletInShipping()) (p.units + sum) else sum
        }
    }

    companion object {
        /**
         * Create an item object from the item view model
         */
        fun createItem(itemVm: ItemVM): Item {
            var item = Item(
                itemVm.itemNo, itemVm.name, itemVm.description, itemVm.department,
                ItemStatus.ACTIVE
            )
            return item
        }
    }
}

class ItemVM(
    @field:Min(1)
    var itemNo: Long,
    @field:NotBlank
    var name: String,
    @field:NotBlank
    var description: String,
    @field:NotNull
    var department: Department,
    var wareNo: Long = 1,
    @field:Min(0)
    var units: Int = 0,
    var pallets: MutableList<Pallet> = mutableListOf()
) {
    companion object {
        fun createItem(): ItemVM {
            return ItemVM(
                itemNo = 1,
                name = "Name",
                description = "Desc",
                department = Department.MISC,
            )
        }

        fun initItemVm(item: Item): ItemVM {
            return ItemVM(
                itemNo = item.itemNo,
                name = item.name,
                description = item.description,
                department = item.department,
                pallets = item.pallets
            )
        }

        /**
         * Prepares the view model from the Item object
         */
        fun prepareVM(item: Item): ItemVM {
            val itemVm = initItemVm(item)
            itemVm.units = item.pallets.fold(0) {sum, p -> sum + p.units}
            return itemVm
        }

        /**
         * Prepares the view model from the item object taking the warehouse number into account
         * @return ItemVM prepared view model for the item
         */
        fun prepareVMForWarehouse(item: Item, wareNo: Long): ItemVM {
            val itemVm = initItemVm(item)
            // Find the number of units in the warehouse
            itemVm.units = item.pallets.fold(0) {sum, p ->
                if (p.palletLoc.wareNo == wareNo && !p.isPalletInShipping()) sum + p.units else sum
            }
            return itemVm
        }
    }

    fun getDisplayStr(): String {
        return "Item No: $itemNo, Name: $name, Desc: $description, Dep: $department, Units: $units"
    }
}

/**
 * Used to create inventory of an item
 */
data class ItemInventoryVM(
    @field:Min(1)
    var itemNo: Long,
    @field:Min(1)
    var wareNo: Long,
    @field:Min(1)
    var units: Int,
)

/**
 * Represents the status code for item
 */
enum class ItemStatus(disp: String) {
    @JsonProperty("active")  ACTIVE("Active"),
    @JsonProperty("inactive") INACTIVE("Inactive"),
}

/**
 * Represents the departments to group
 * items
 */
enum class Department(val disp: String, val palleteCap: Int) {
    @JsonProperty("grocery") GRCY("Grocery", 50), // Grocery
    @JsonProperty("electric") ELEC("Electric", 10), // Electric
    @JsonProperty("household") HSLD("Household", 1), // Household
    @JsonProperty("stationary") STRY("Stationary", 50), // Stationary
    @JsonProperty("automobile") ATMB("Automobile", 1), // Automobile
    @JsonProperty("misc") MISC("Miscellaneous", 10), // Miscellaneous
}