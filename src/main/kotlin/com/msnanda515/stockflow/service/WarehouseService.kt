package com.msnanda515.stockflow.service

import com.msnanda515.stockflow.exception.AlreadyExistsException
import com.msnanda515.stockflow.exception.DoesNotExistsException
import com.msnanda515.stockflow.exception.OutOfCapacityException
import com.msnanda515.stockflow.model.*
import com.msnanda515.stockflow.repository.WarehouseRepository
import org.springframework.stereotype.Service

@Service
class WarehouseService(val warehouseRepository: WarehouseRepository) {
    /**
     * Creates and Persists a warehouse in the database
     * @param wareVm the view model of the warehouse to create
     */
    fun createWarehouse(wareVm: WarehouseVM) {
        // only store the warehouse if warehouse No is unique
        val wareExists = warehouseRepository.findByWareNo(wareVm.wareNo).isNotEmpty()
        if (wareExists) {
            throw AlreadyExistsException("Warehouse with id ${wareVm.wareNo} already exists")
        }
        val ware = Warehouse.createWarehouse(wareVm)
        warehouseRepository.save(ware)
    }

    /**
     * Gets all the warehouses
     */
    fun getAllWarehouses(): List<Warehouse> {
        return warehouseRepository.findAll()
    }

    fun getWarehouse(wareNo: Long) {

    }

    /**
     * Finds the next available warehouse No
     */
    fun getNextWarehouseNo(): Long {
        return (warehouseRepository.findTopByOrderByWareNoDesc()
            ?.wareNo ?: 0)+ 1
    }

    /**
     * Gets the available pallet locations in the warehouse
     * @throws OutOfCapacityException if not enough capacity in the warehouse
     */
    fun getAvailablePalletPos(noPallets: Int, wareNo: Long): List<PalletLoc> {
        val wares = warehouseRepository.findByWareNo(wareNo)
        if (wares.isEmpty()) {
            throw DoesNotExistsException("Warehouse with ${wareNo} Id does not exist")
        }
        val ware = wares[0]
        val warePallets = ware.pallets.map { it.palletLoc.toString() }.toSet()
        val palletLocs = mutableListOf<PalletLoc>()
        var assPallets = 0

        // find an available slot in the warehouse
        for (a in 1..ware.capacity.aisle) {
            for (s in 1..ware.capacity.section) {
                for (l in 1..ware.capacity.level) {
                    if ( !warePallets.contains("(${ware.wareNo},${a},${s},${l})") ) {
                        palletLocs.add(PalletLoc(ware.wareNo, a, s, l))
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
                "Capacity ${ware.capacity.getCapacity()}: ${ware.capacity.toString()} ")
        return palletLocs
    }


}