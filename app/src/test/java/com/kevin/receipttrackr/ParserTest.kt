package com.kevin.receipttrackr

import com.kevin.receipttrackr.domain.Categorizer
import com.kevin.receipttrackr.domain.Parser
import com.kevin.receipttrackr.settings.SettingsStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ParserTest {
    private lateinit var parser: Parser
    private lateinit var categorizer: Categorizer
    private lateinit var settingsStore: SettingsStore

    @Before
    fun setup() {
        settingsStore = mock(SettingsStore::class.java)
        `when`(settingsStore.keywordRules).thenReturn(flowOf(emptyMap()))
        categorizer = Categorizer(settingsStore)
        parser = Parser(categorizer)
    }

    @Test
    fun `test grocery receipt parsing`() = runTest {
        val receipt = """
            WHOLE FOODS MARKET
            Date: 15/03/2024
            Milk 2L                  4.99
            Bread Whole Wheat        3.50
            Eggs Dozen               5.25
            Cheese Cheddar           7.99
            
            Subtotal                21.73
            Tax                      1.74
            Total                   23.47
        """.trimIndent()

        val result = parser.parse(receipt)

        assertEquals("WHOLE FOODS MARKET", result.merchant)
        assertNotNull(result.dateEpochMs)
        assertEquals(2173, result.subtotalCents)
        assertEquals(174, result.taxCents)
        assertEquals(2347, result.totalCents)
        assertEquals(4, result.items.size)
        
        val milk = result.items.find { it.name.contains("Milk", ignoreCase = true) }
        assertNotNull(milk)
        assertEquals(499, milk!!.amountCents)
        assertEquals("Groceries", milk.category)
    }

    @Test
    fun `test cafe receipt parsing`() = runTest {
        val receipt = """
            COFFEE BEAN CAFE
            2024-01-20
            
            Cappuccino Large         5.50
            Latte Medium             4.75
            Croissant                3.25
            Muffin Blueberry         3.50
            
            Subtotal                17.00
            Tax                      1.36
            Total                   18.36
        """.trimIndent()

        val result = parser.parse(receipt)

        assertEquals("COFFEE BEAN CAFE", result.merchant)
        assertEquals(1700, result.subtotalCents)
        assertEquals(136, result.taxCents)
        assertEquals(1836, result.totalCents)
        assertTrue(result.items.size >= 2)
        
        val cappuccino = result.items.find { it.name.contains("Cappuccino", ignoreCase = true) }
        assertNotNull(cappuccino)
        assertEquals("Food & Drink", cappuccino!!.category)
    }

    @Test
    fun `test pharmacy receipt parsing`() = runTest {
        val receipt = """
            HEALTH PHARMACY
            Date: 10 Mar 2024
            
            Aspirin 100mg            12.99
            Vitamin C Tablets        15.50
            Band-Aids Large          8.75
            
            Subtotal                37.24
            Tax                      2.98
            Total                   40.22
        """.trimIndent()

        val result = parser.parse(receipt)

        assertEquals("HEALTH PHARMACY", result.merchant)
        assertEquals(3724, result.subtotalCents)
        assertEquals(298, result.taxCents)
        assertEquals(4022, result.totalCents)
        assertEquals(3, result.items.size)
        
        val vitamin = result.items.find { it.name.contains("Vitamin", ignoreCase = true) }
        assertNotNull(vitamin)
        assertEquals("Health", vitamin!!.category)
    }

    @Test
    fun `test date parsing formats`() = runTest {
        val formats = listOf(
            "Date: 2024-03-15" to "2024-03-15",
            "15/03/2024" to "15/03/2024",
            "Mar 15 2024" to "Mar 15 2024"
        )

        formats.forEach { (text, _) ->
            val receipt = """
                Test Merchant
                $text
                Item                    10.00
                Total                   10.00
            """.trimIndent()

            val result = parser.parse(receipt)
            assertNotNull("Date should be parsed for: $text", result.dateEpochMs)
        }
    }

    @Test
    fun `test price parsing with comma separators`() = runTest {
        val receipt = """
            Expensive Store
            Big Item            1,250.99
            Total               1,250.99
        """.trimIndent()

        val result = parser.parse(receipt)
        assertEquals(125099, result.totalCents)
    }
}
