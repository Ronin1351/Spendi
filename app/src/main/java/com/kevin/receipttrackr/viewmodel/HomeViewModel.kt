package com.kevin.receipttrackr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.receipttrackr.data.db.CategoryTotal
import com.kevin.receipttrackr.data.db.Receipt
import com.kevin.receipttrackr.data.repo.ReceiptRepository
import com.kevin.receipttrackr.util.Formatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ReceiptRepository
) : ViewModel() {

    val allReceipts: StateFlow<List<Receipt>> = repository.getAllReceipts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyCategoryTotals: StateFlow<List<CategoryTotal>> = repository.getMonthlyCategoryTotals(
        startMs = Formatters.getMonthStartMs(),
        endMs = Formatters.getMonthEndMs()
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedCategories()
        }
    }
}
