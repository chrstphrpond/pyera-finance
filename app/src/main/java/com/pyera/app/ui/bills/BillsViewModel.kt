package com.pyera.app.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.BillEntity
import com.pyera.app.domain.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillsViewModel @Inject constructor(
    private val billRepository: BillRepository
) : ViewModel() {

    val bills: StateFlow<List<BillEntity>> = billRepository.getAllBills()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBill(name: String, amount: Double, dueDate: Long, frequency: String) {
        viewModelScope.launch {
            billRepository.addBill(
                BillEntity(name = name, amount = amount, dueDate = dueDate, frequency = frequency)
            )
        }
    }

    fun markAsPaid(bill: BillEntity) {
        viewModelScope.launch {
            // Logic for recurring bills:
            // If recurring, create a new "upcoming" bill for next cycle and mark current as paid (or just update date)
            // For MVP simplicity, let's just update the due date to next month if monthly.
            
            if (bill.frequency == "MONTHLY") {
                val nextDueDate = bill.dueDate + 30L * 24 * 60 * 60 * 1000 // Approx 1 month
                billRepository.updateBill(bill.copy(dueDate = nextDueDate, isPaid = false))
            } else if (bill.frequency == "YEARLY") {
                val nextDueDate = bill.dueDate + 365L * 24 * 60 * 60 * 1000 // Approx 1 year
                billRepository.updateBill(bill.copy(dueDate = nextDueDate, isPaid = false))
            } else {
                 // Warning: "ONE_TIME" frequency logic is not handled here, user may expect it to be gone or marked as paid.
                 // For now, let's just mark it as paid so it can be filtered out or shown in history.
                 billRepository.updateBill(bill.copy(isPaid = true))
            }
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.deleteBill(bill)
        }
    }
}
