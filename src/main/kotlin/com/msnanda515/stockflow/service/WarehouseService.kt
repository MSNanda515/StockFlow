package com.msnanda515.stockflow.service

import com.msnanda515.stockflow.exception.*
import com.msnanda515.stockflow.model.*
import com.msnanda515.stockflow.repository.ItemRepository
import com.msnanda515.stockflow.repository.WarehouseRepository
import org.springframework.stereotype.Service

@Service
class WarehouseService(
        val warehouseRepository: WarehouseRepository,
        val itemRepository: ItemRepository,
    ) {
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
        return ware.getAvailablePalletPos(noPallets)
    }

    /**
     * Deletes the pallets for the given items for all warehouses
     */
    fun deletePalletsForItem(item: Item) {
        val wares = warehouseRepository.findAll()
        if (item.pallets.isEmpty()) {
            return
        }
        wares.forEach {
            // delete all pallets for the item
            it.pallets = it.pallets.filter { p -> p.itemNo!=item.itemNo } as MutableList<Pallet>
        }
        warehouseRepository.saveAll(wares)
    }

    /**
     * Edits the warehouse;
     * @throws DoesNotExistsException if warehouse does not exist
     * @throws WarehouseCapacityException if new capacity is invalid
     */
    fun editWarehouse(wareVm: WarehouseVM): Warehouse {
        val wares = warehouseRepository.findByWareNo(wareVm.wareNo)
        if (wares.isEmpty()) {
            throw DoesNotExistsException("Warehouse with ${wareVm.wareNo} does not exist")
        }

        val ware = wares[0]
        ware.name = wareVm.name
        ware.location = wareVm.location
        val newCapacity = WarehouseCapacity(wareVm.aisle, wareVm.section, wareVm.level)
        val oldCapacity = ware.capacity
        if (!ware.capacity.equals(newCapacity)) {
            ware.changeCapacity(newCapacity)
        }
        warehouseRepository.save(ware)

        if (!oldCapacity.isStrictlySmaller(newCapacity)) {
            // change items pallet info warehouse resized
            editItemsForWarehouse(ware)
        }
        return ware
    }

    /**
     * Edits the item according to the change in the warehouse capacity
     */
    fun editItemsForWarehouse(ware: Warehouse) {
        val palletMap = ware.pallets.associateBy { it.id }
        val items = itemRepository.findAllByStatus(ItemStatus.ACTIVE)
        items.forEach {
            it.pallets.forEach {p ->
                run {
                    if (palletMap.contains(p.id)) {
                        p.palletLoc = palletMap[p.id]!!.palletLoc
                    }
                }
            }
        }
        itemRepository.saveAll(items)
    }
}