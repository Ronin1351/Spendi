package com.kevin.receipttrackr.domain

import com.kevin.receipttrackr.debug.Logger
import com.kevin.receipttrackr.settings.SettingsStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Categorizer @Inject constructor(
    private val settingsStore: SettingsStore
) {
    private val tag = "Categorizer"

    private val builtInRules = mapOf(
        // Groceries
        "bread" to "Groceries", "milk" to "Groceries", "eggs" to "Groceries",
        "cheese" to "Groceries", "butter" to "Groceries", "yogurt" to "Groceries",
        "vegetables" to "Groceries", "fruit" to "Groceries", "meat" to "Groceries",
        "chicken" to "Groceries", "fish" to "Groceries", "rice" to "Groceries",
        "pasta" to "Groceries", "cereal" to "Groceries",

        // Food & Drink
        "coffee" to "Food & Drink", "latte" to "Food & Drink", "cappuccino" to "Food & Drink",
        "espresso" to "Food & Drink", "tea" to "Food & Drink", "juice" to "Food & Drink",
        "soda" to "Food & Drink", "water" to "Food & Drink", "beer" to "Food & Drink",
        "wine" to "Food & Drink", "cocktail" to "Food & Drink", "burger" to "Food & Drink",
        "pizza" to "Food & Drink", "sandwich" to "Food & Drink", "salad" to "Food & Drink",
        "cafe" to "Food & Drink", "restaurant" to "Food & Drink",

        // Transport
        "uber" to "Transport", "lyft" to "Transport", "taxi" to "Transport",
        "grab" to "Transport", "fare" to "Transport", "ride" to "Transport",
        "bus" to "Transport", "train" to "Transport", "metro" to "Transport",
        "parking" to "Transport", "toll" to "Transport", "gas" to "Transport",
        "fuel" to "Transport",

        // Health
        "pharmacy" to "Health", "medicine" to "Health", "drug" to "Health",
        "clinic" to "Health", "hospital" to "Health", "doctor" to "Health",
        "prescription" to "Health", "vitamin" to "Health", "supplement" to "Health",

        // Bills & Utilities
        "electric" to "Bills & Utilities", "water" to "Bills & Utilities",
        "internet" to "Bills & Utilities", "phone" to "Bills & Utilities",
        "bill" to "Bills & Utilities", "utility" to "Bills & Utilities",

        // Shopping
        "clothing" to "Shopping", "shoes" to "Shopping", "shirt" to "Shopping",
        "pants" to "Shopping", "dress" to "Shopping", "electronics" to "Shopping",
        "book" to "Shopping", "toy" to "Shopping"
    )

    suspend fun categorize(itemName: String): String {
		val lowerName = itemName.lowercase()

		// First check user-defined rules (higher priority)
		val userRules = settingsStore.keywordRules.first()
		for ((keyword, category) in userRules) {
			if (lowerName.contains(keyword)) {
				Logger.d(tag, "User rule matched: '$keyword' -> $category")
				return category
			}
		}

		// Then check built-in rules
		for ((keyword, category) in builtInRules) {
			if (lowerName.contains(keyword)) {
				Logger.d(tag, "Built-in rule matched: '$keyword' -> $category")
				return category
			}
		}

		Logger.d(tag, "No match for '$itemName', using Other")
		return "Other"
	}
}
