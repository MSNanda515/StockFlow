package com.msnanda515.stockflow.service

import com.msnanda515.stockflow.exception.*
import com.msnanda515.stockflow.model.*
import com.msnanda515.stockflow.repository.ItemRepository
import com.msnanda515.stockflow.repository.WarehouseRepository
import org.bson.types.ObjectId
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
     * Gets the active items present in the warehouse
     * Note: Does not include those which are in receiving
     */
    fun getActiveItemsInWarehouse(wareNo: Long): List<Item> {
        val items = getAllActiveItems()
        val resItems: MutableList<Item> = mutableListOf()
        // filter those items which have units in the warehouse
        for (item in items) {
            var palletsInWare = item.pallets.filter { it.palletLoc.wareNo == wareNo && !it.isPalletInShipping() }
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
        // verify item does not already exist
        var itemExists = itemRepository.findAllByItemNo(itemVm.itemNo).isNotEmpty()
        if (itemExists) {
            throw AlreadyExistsException("Item with Item No ${itemVm.itemNo} already exists")
        }

        // prepare the item from the VM
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
        // store the pallet info
        item.pallets.addAll(pallets)
        addPalletsToWarehouse(wareNo, pallets)
    }

    /**
     * Adds pallets to the warehouse
     * @throws DoesNotExistsException if warehouse with wareNo does not exist
     */
    fun addPalletsToWarehouse(wareNo: Long, pallets: List<Pallet>) {
        // verify that the warehouse exist
        val wares = warehouseRepository.findByWareNo(wareNo)
        if (wares.isEmpty()) {
            throw DoesNotExistsException("Warehouse with $wareNo does not exist")
        }
        // add the pallets to the warehouse
        val ware = wares[0]
        ware.pallets.addAll(pallets)
        warehouseRepository.save(ware)
    }

    /**
     * Returns an available item no to be configured as the default value for the form
     */
    fun getNextItemNo(): Long {
        // find the maximum item no used so far, and increments it
        // TODO: Can avoid db call by caching maximum id
        return (itemRepository.findTopByOrderByItemNoDesc()?.itemNo ?: 0) + 1
    }

    /**
     * gets the item asked for
     * @throws DoesNotExistsException if item with itemNo does not exist
     */
    fun getItem(itemNo: Long): Item {
        // verify item exists
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
        // verify item exists
        val items = itemRepository.findAllByItemNo(itemVm.itemNo)
        if (items.isEmpty()) {
            throw DoesNotExistsException("Item with ${itemVm.itemNo} does not exist")
        }
        val item = items[0]
        // not allowed to change department of item, pallet capacity correlation
        if (item.department != itemVm.department) {
            throw DoesNotMatchException("Item's department does not match, expected ${item.department}" +
                    ", got ${itemVm.department}")
        }

        // save the new information
        item.name = itemVm.name
        item.description = itemVm.description
        itemRepository.save(item)
    }

    /**
     * Deletes item
     * @throws DoesNotExistsException if item does not exist
     */
    fun deleteItem(itemVm: ItemVM) {
        // verify item exists
        val items = itemRepository.findAllByItemNo(itemVm.itemNo)
        if (items.isEmpty()) {
            throw DoesNotExistsException("Item with ${itemVm.itemNo} does not exist")
        }
        val item = items[0]

        // delete pallets for the item and set the item to inactive
        warehouseService.deletePalletsForItem(item)
        item.status = ItemStatus.INACTIVE
        itemRepository.save(item)
    }

    /**
     * Ships items, update pallets in warehouse as needed
     * @throws InvalidRequestException if the no of item units does not match with the ids
     */
    fun shipItems(shipmentVm: ShipmentVM) {
        // get the items ids and units
        val shippedItems: Map<Long, Int>
        val shippedItemIds = shipmentVm.itemNos.split(",").map { it.toLong() }
        val shippedItemUnits = shipmentVm.itemUnits.split(",").map { it.toInt() }
        if (shippedItemIds.size != shippedItemUnits.size) {
            throw InvalidRequestException("Requested item units (${shippedItemUnits.size}) and " +
                    "ids (${shippedItemIds.size}) does not match")
        }
        // prepare a map for shipped items (id -> units) for efficient lookup
        shippedItems = shippedItemIds.zip(shippedItemUnits).toMap()

        // get the shipped items from db
        var items = itemRepository.findAll()
        items = items.filter { shippedItems.containsKey(it.itemNo) }
        // gather the shipment info
        val tempShipment = Shipment(from = shipmentVm.from, to = shipmentVm.to, units = 0)
        val deletePalletsWare = mutableSetOf<ObjectId>() // delete pallets from shippedFrom ware
        val updatePalletsWare = mutableMapOf<ObjectId, Int>() // update pallets in shippedFrom ware
        items.forEach {
            // ship the required units for item
            val (delPal, updPal) = it.shipItem(shippedItems[it.itemNo]!!, tempShipment)
            // append to the sets and update warehouse later to optimize process
            deletePalletsWare += delPal
            updatePalletsWare += updPal
        }
        // update the pallets info in shippedFrom ware
        // pallets in shippedTo updated at delivery for distributed response time
        warehouseService.shipItemsWarehouse(tempShipment.from, deletePalletsWare, updatePalletsWare)
        itemRepository.saveAll(items)
    }

    /**
     * Gets the items for a warehouse in shipping
     * Returns:
     *  - List<ReceivingVM>: list of shipments including the items in those shipments
     */
    fun getItemsInReceivingForWarehouse(wareNo: Long): List<ReceivingVM> {
        val receivingMap: MutableMap<ObjectId, ReceivingVM> = mutableMapOf()
        val items = itemRepository.findAllByStatus(ItemStatus.ACTIVE)
        for (it in items) {
            for (p in it.pallets) {
                // find pallets being shipped to selected warehouse
                if (p.isPalletInShipping() && p.shipment != null && p.shipment!!.to == wareNo) {
                    val shipment = p.shipment!!
                    // if first pallet from particular shipment, prepare the receiving vm for shipment
                    if (!receivingMap.containsKey(shipment.id)) {
                        receivingMap[shipment.id] = ReceivingVM(shipment.id, shipment.from, shipment.to)
                    }
                    val shipFromMap = receivingMap[shipment.id]!!

                    // if first pallet for this item in shipment, prepare the item vm for receiving vm
                    if (shipFromMap.items.isEmpty() || shipFromMap.items.last().itemNo != it.itemNo) {
                        val itemVm = ItemVM(it.itemNo, it.name, it.description, it.department, wareNo, 0)
                        shipFromMap.items.add(itemVm)
                    }
                    // add units to prepared VMs
                    shipFromMap.items.last().units += p.units
                }
            }
        }

        return receivingMap.values.toList()
    }

    /**
     * Receives the selected shipments for the user
     * @throws InvalidRequestException if the parameters are not valid
     * todo: add the shipments to history os shipments for items for record
     */
    fun receiveShipment(shipmentIdsStr: String, wareNo: Long) {
        // get the shipment ids to receive for the selected warehouse
        val shipmentIds = shipmentIdsStr.split(",").toSet()
        // find all pallets which are shipped
        val palletsShipped = mutableListOf<Pallet>()
        val items = itemRepository.findAllByStatus(ItemStatus.ACTIVE)
        for (it in items) {
            for (p in it.pallets) {
                // if pallet is the shipment being processed
                if (p.isPalletInShipping() && p.shipment != null && shipmentIds.contains(p.shipment!!.id.toString())){
                    // and pallet is going to the correct warehouse
                    if (p.shipment!!.to != wareNo) {
                        throw InvalidRequestException("The shipment no does not match with the records")
                    }
                    // receive pallet
                    palletsShipped.add(p)
                }
            }
        }

        // update warehouse receiving shipment
        val wares = warehouseRepository.findByWareNo(wareNo)
        if (wares.isEmpty()) {
            throw InvalidRequestException("The requested warehouse does not exist")
        }
        val ware = wares[0]
        // assign the received pallets to valid positions in warehouse
        val palletLocs = ware.getAvailablePalletPos(palletsShipped.size)
        for (i in palletsShipped.indices) {
            palletsShipped[i].receivePallet(palletLocs[i])
        }

        // add the pallets to the warehouse
        ware.addNewPallets(palletsShipped)
        warehouseRepository.save(ware)
        itemRepository.saveAll(items)
    }
}