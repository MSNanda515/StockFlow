package com.msnanda515.stockflow.service

import com.msnanda515.stockflow.exception.AlreadyExistsException
import com.msnanda515.stockflow.exception.DoesNotExistsException
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
     */
    fun getAvailablePalletPos(noPallets: Int, wareNo: Long): List<PalletLoc> {
        val wares = warehouseRepository.findByWareNo(wareNo)
        if (wares.isEmpty()) {
            throw DoesNotExistsException("Warehouse with ${wareNo} Id does not exist")
        }
        val ware = wares[0]
        val warePallets = ware.pallets.map { it.palletLoc.toString() }.toSet()
        val palletLocs = mutableListOf<PalletLoc>()
        var a = 0
        var s = 0
        var l = 0
        var assPallets = 0

        // find an available slot in the warehouse
        while (a++ <= 200) {
            while (s++ <= 10) {
                while (l++ <= 3) {
                    if ("(${ware.wareNo},${a},${s},${l})" !in warePallets) {
                        palletLocs.add(PalletLoc(ware.wareNo, a, s, l))
                        assPallets++
                        if (assPallets == noPallets) {
                            return palletLocs
                        }
                    }
                }
            }
        }
        return palletLocs
    }

    fun addPalletsToWarehouse(wareNo: Long, pallets: List<Pallet>) {
        val wares = warehouseRepository.findByWareNo(wareNo)
        if (wares.isEmpty()) {
            throw DoesNotExistsException("Warehouse with $wareNo does not exist")
        }
        val ware = wares[0]
        ware.pallets.addAll(pallets)
        warehouseRepository.save(ware)
    }
}