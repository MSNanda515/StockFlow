package com.msnanda515.stockflow.model

import com.msnanda515.stockflow.exception.InvalidCapacityException
import com.msnanda515.stockflow.exception.OutOfCapacityException
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

const val MIN_AISLE = 200L
const val MIN_SECTION = 10L
const val MIN_LEVEL = 4L

@Document
data class Warehouse(
    var wareNo: Long,
    var name: String,
    var location: String,
    var pallets: MutableList<Pallet> = mutableListOf(),
    var capacity: WarehouseCapacity = WarehouseCapacity(),
    @Id
    val id: ObjectId = ObjectId.get(),
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val modifiedDate: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        /**
         * Create a warehouse object from its view model
         */
        fun createWarehouse(wareVm: WarehouseVM): Warehouse {
            return Warehouse(
                wareNo = wareVm.wareNo,
                name = wareVm.name,
                location = wareVm.location,
                capacity = WarehouseCapacity(wareVm.aisle, wareVm.section, wareVm.level)
            )
        }
    }

    override fun toString(): String {
        return "$wareNo ($name)"
    }

    fun isCapacityValid(cap: WarehouseCapacity): Boolean {
        // check min values
        if (cap.aisle < MIN_AISLE || cap.section < MIN_SECTION || cap.level < MIN_LEVEL) {
            return false
        }
        // check if new values can hold all pallets
        if (cap.getCapacity() < this.pallets.size) {
            return false
        }
        return true
    }

    /**
     * Changes the capacity according to new capacity
     * @throws OutOfCapacityException if invalid capacity
     * @throws InvalidCapacityException if invalid capacity
     */
    fun changeCapacity(newCapacity: WarehouseCapacity) {
        if (!isCapacityValid(newCapacity)) {
            throw InvalidCapacityException("Warehouse requires ${pallets.size} capacity at least;\n Minimum " +
                    "$MIN_AISLE aisles, $MIN_SECTION sections, $MIN_LEVEL levels")
        }
        if (capacity.isStrictlySmaller(newCapacity)) {
            // nothing needs to be arranged if all dimensions are still valid
            capacity = newCapacity
            return
        }
        capacity = newCapacity
        assignPalletsAccNewCapacity()
    }

    /**
     * Gets the requested available pallet locations
     * @throws OutOfCapacityException if not enough capacity in warehouse
     */
    fun getAvailablePalletPos(noPallets: Int): List<PalletLoc> {
        val warePallets = pallets.map { it.palletLoc.toString() }.toSet()
        val palletLocs = mutableListOf<PalletLoc>()
        var assPallets = 0

        // find an available slot in the warehouse
        for (a in 1..capacity.aisle) {
            for (s in 1..capacity.section) {
                for (l in 1..capacity.level) {
                    if ( !warePallets.contains("(${wareNo},${a},${s},${l})") ) {
                        palletLocs.add(PalletLoc(wareNo, a, s, l))
                        assPallets++
                        if (assPallets == noPallets) {
                            return palletLocs
                        }
                    }
                }
            }
        }
        // if more pallets required, out of capacity
        throw OutOfCapacityException("Warehouse $wareNo out of Capacity, has ${warePallets.size} pallets, " +
                "Capacity ${capacity.getCapacity()}: $capacity ")
    }

    fun assignPalletsAccNewCapacity() {
        // temporarily copy reference to object to find pallet locations as if warehouse is empty
        val palletsCopy = pallets
        pallets = mutableListOf()
        val availLocs = getAvailablePalletPos(palletsCopy.size)
        pallets = palletsCopy
        for (i in pallets.indices) {
            pallets[i].palletLoc = availLocs[i]
        }
        println("got pos")
        println("Pallets reassigned")
    }

    /**
     * Adds the given pallets to the warehouse
     */
    fun addNewPallets(pallets: MutableList<Pallet>) {
        this.pallets.addAll(pallets)
    }
}

class WarehouseVM(
    @field:Min(value = 1)
    var wareNo: Long,
    @field:NotBlank
    var name: String,
    @field:NotBlank
    var location: String,
    @field:Min(MIN_AISLE)
    var aisle: Int = 200,
    @field:Min(MIN_SECTION)
    var section: Int = 10,
    @field:Min(MIN_LEVEL)
    var level: Int = 4,
) {
    companion object {
        /**
         * Creates an empty View Model
         */
        fun createVM(): WarehouseVM {
            return WarehouseVM(
                wareNo = 1,
                name = "Name",
                location = "Location"
            )
        }

        /**
         * Prepares a view model from the warehouse object
         */
        fun prepareVM(ware: Warehouse): WarehouseVM {
            return WarehouseVM(
                wareNo = ware.wareNo,
                name = ware.name,
                location = ware.location,
                aisle = ware.capacity.aisle,
                section = ware.capacity.section,
                level = ware.capacity.level
            )
        }
    }

    fun setDefaultValues(wareNo: Long) {
        this.wareNo = wareNo
        this.name = "Ware $wareNo"
        this.location = "Loc $wareNo"
    }

    fun getDisplayStr(): String {
        return "Warehouse No: $wareNo, Name: $name, Location: $location, Capacity: ($aisle, $section, $level)"
    }
}


/**
 * Defines the max capacity of a warehouse
 */
data class WarehouseCapacity(
    var aisle: Int = 300,
    var section: Int = 10,
    var level: Int = 3,
) {
    /**
     * Gets the pallets allowed in the warehouse
     */
    fun getCapacity(): Long = 1L * aisle * section * level

    /**
     * Returns true if all dimensions of new capacity are >= to the original capacity
     */
    fun isStrictlySmaller(capacity: WarehouseCapacity): Boolean {
        if (aisle <= capacity.aisle && section <= capacity.section && level <= capacity.level) {
            return true
        }
        return false
    }
}


