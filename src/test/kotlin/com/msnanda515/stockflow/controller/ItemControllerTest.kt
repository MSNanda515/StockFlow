package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.model.Department
import com.msnanda515.stockflow.model.Item
import com.msnanda515.stockflow.model.ItemStatus
import com.msnanda515.stockflow.repository.ItemRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
internal class ItemControllerTest @Autowired constructor(
    val itemRepository: ItemRepository,
    val mockMvc: MockMvc
) {

    val baseUrl = "/api/v1/items"
    val defaultItem = Item(
        itemNo = 123,
        name = "def",
        description = "def",
        status = ItemStatus.ACTIVE,
        department = Department.MISC
    )

    /**
     * creates the default item in the database
     */
    fun createDefItem() {
        itemRepository.save(defaultItem)
    }

    /**
     * Deletes the default item from the database
     */
    fun deleteDefItem() {
        itemRepository.delete(defaultItem)
    }

    @Nested
    @DisplayName("GET /api/items")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetItems {
        @Test
        fun `should return all banks`() {
            // given
            createDefItem()
            // when/then
            mockMvc.get(baseUrl)
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("\$[?(@.itemNo == ${defaultItem.itemNo})].name")
                        { value(defaultItem.name) }
                }
            deleteDefItem()
        }
    }

}