package com.msnanda515.stockflow.service

import com.msnanda515.stockflow.exception.*
import com.msnanda515.stockflow.model.*
import com.msnanda515.stockflow.repository.ItemRepository
import com.msnanda515.stockflow.repository.WarehouseRepository
import org.springframework.stereotype.Service

@Service
class ItemService(
    val itemRepository: ItemRepository,
    val warehouseRepository: WarehouseRepository,
    val warehouseService: WarehouseService,
) {
    /**
     * Gets all active items
     */
    fun getAllActiveItems(): List<Item> {
        return itemRepository.findAllByStatus(ItemStatus.ACTIVE)
    }

    /**
     * Active items, assumes that all pallets in the warehouse are for active items
     */
    fun getActiveItemsInWarehouse(wareNo: Long): List<Item> {
        val items = getAllActiveItems()
        val resItems: MutableList<Item> = mutableListOf()
        // filter those items which have units in the warehouse
        for (item in items) {
            var palletsInWare = item.pallets.filter { it.palletLoc.wareNo == wareNo }
            if (palletsInWare.isNotEmpty()) {
                item.pallets = palletsInWare as MutableList<Pallet>
                resItems.add(item)
            }
        }
        return resItems
    }

    /**
     * Creates and persists an item using the details from itemVM
     */
    fun createItem(itemVm: ItemVM) {
        var itemExists = itemRepository.findAllByItemNo(itemVm.itemNo).isNotEmpty()
        if (itemExists) {
            throw AlreadyExistsException("Item with Item No ${itemVm.itemNo} already exists")
        }

        var item = Item.createItem(itemVm)
        // get the pallets required for the item
        if (itemVm.units > 0) {
            addInventoryToItem(item, itemVm.units, itemVm.wareNo)
        }
        itemRepository.save(item)
    }

    /**
     * Creates and persists inventory for an existing item
     * @throws DoesNotExistsException if either the warehouse or the item does not exist with given Ids
     * @throws OutOfCapacityException if not enough capacity in the warehouse
     */
    fun createInventory(inventoryVM: ItemInventoryVM) {
        val items = itemRepository.findAllByItemNo(inventoryVM.itemNo)
        // check if item exists
        val itemExits = items.isNotEmpty()
        if (!itemExits) {
            throw DoesNotExistsException("Item with ${inventoryVM.itemNo} does not exist")
        }
        val item = items[0]
        addInventoryToItem(item, inventoryVM.units, inventoryVM.wareNo)
        itemRepository.save(item)
    }

    /**
     * Adds inventory units to an item
     * Also records the pallet information in the warehouse document
     * Note: Does not persist the item
     * @throws DoesNotExistsException if warehouse with wareNo does not exist
     * @throws OutOfCapacityException if not enough capacity in the warehouse
     */
    fun addInventoryToItem(item: Item, units: Int, wareNo: Long) {
        // find the pallet info for storing the inventory in the warehouse
        val palletsRequired = item.palletsRequired(units)
        val palletLocs = warehouseService.getAvailablePalletPos(palletsRequired, wareNo)
        val pallets = Pallet.createPalletsForItem(item.itemNo, units, palletLocs, palletsRequired,
            item.department.palleteCap)
        // store the pallet ifo
        item.pallets.addAll(pallets)
        addPalletsToWarehouse(wareNo, pallets)
    }

    /**
     * Adds pallets to the warehouse
     * @throws DoesNotExistsException if warehouse with wareNo does not exist
     */
    fun addPalletsToWarehouse(wareNo: Long, pallets: List<Pallet>) {
        val wares = warehouseRepository.findByWareNo(wareNo)
        if (wares.isEmpty()) {
            throw DoesNotExistsException("Warehouse with $wareNo does not exist")
        }
        val ware = wares[0]
        ware.pallets.addAll(pallets)
        warehouseRepository.save(ware)
    }

    /**
     * Returns an available item no to be configured as the default value for the form
     */
    fun getNextItemNo(): Long {
        return (itemRepository.findTopByOrderByItemNoDesc()?.itemNo ?: 0) + 1
    }

    /**
     * gets the item asked for
     * @throws DoesNotExistsException if item with itemNo does not exist
     */
    fun getItem(itemNo: Long): Item {
        val items = itemRepository.findAllByItemNo(itemNo)
        if (items.isEmpty()) {
            throw DoesNotExistsException("Item with $itemNo does not exist")
        }
        return items[0]
    }

    /**
     * Edit the item
     * @throws DoesNotExistsException if the item to be edited does not exist
     * @throws DoesNotMatchException if the item's department does not match with expected
     */
    fun editItem(itemVm: ItemVM) {
        val items = itemRepository.findAllByItemNo(itemVm.itemNo)
        if (items.isEmpty()) {
            throw DoesNotExistsException("Item with ${itemVm.itemNo} does not exist")
        }
        val item = items[0]
        if (item.department != itemVm.department) {
            throw DoesNotMatchException("Item's department does not match, expected ${item.department}" +
                    ", got ${itemVm.department}")
        }

        item.name = itemVm.name
        item.description = itemVm.description
        itemRepository.save(item)
    }

    fun deleteItem(itemVm: ItemVM) {
        val items = itemRepository.findAllByItemNo(itemVm.itemNo)
        if (items.isEmpty()) {
            throw DoesNotExistsException("Item with ${itemVm.itemNo} does not exist")
        }

    }

}