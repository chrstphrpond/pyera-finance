package com.pyera.app.ui.transaction

import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.repository.CategoryRepository
import com.pyera.app.data.repository.OcrRepository
import com.pyera.app.data.repository.TransactionRepository
import com.pyera.app.domain.smart.SmartCategorizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.ResultHandlingTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private lateinit var viewModel: TransactionViewModel
    private val transactionRepository: TransactionRepository = mock()
    private val categoryRepository: CategoryRepository = mock()
    private val ocrRepository: OcrRepository = mock()
    private val smartCategorizer: SmartCategorizer = mock()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TransactionViewModel(
            transactionRepository,
            categoryRepository,
            ocrRepository,
            smartCategorizer
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addTransaction triggers auto-categorization when categoryId is 0`() = runTest {
        // Given
        val transaction = TransactionEntity(
            amount = 10.0,
            note = "Uber",
            date = 123L,
            type = "EXPENSE",
            categoryId = 0 // Invalid/Unselected
        )
        val predictedCategory = "Transport"
        val transportCategory = CategoryEntity(id = 5, name = "Transport", icon = "", color = 0, type = "EXPENSE")

        whenever(smartCategorizer.predict("Uber")).thenReturn(predictedCategory)
        whenever(categoryRepository.getCategoryByName(predictedCategory)).thenReturn(transportCategory)

        // When
        viewModel.addTransaction(transaction)

        // Then
        // Verify that insertTransaction was called with the PREDICTED category ID (5), not 0
        verify(transactionRepository).insertTransaction(
            org.mockito.kotlin.check {
                 assert(it.categoryId == 5L)
            }
        )
    }

    @Test
    fun `addTransaction uses provided categoryId if valid`() = runTest {
        // Given
        val transaction = TransactionEntity(
            amount = 10.0,
            note = "Uber",
            date = 123L,
            type = "EXPENSE",
            categoryId = 2 // Valid
        )

        // When
        viewModel.addTransaction(transaction)

        // Then
        // Verify that insertTransaction was called with the ORIGINAL category ID (2)
        verify(transactionRepository).insertTransaction(
             org.mockito.kotlin.check {
                 assert(it.categoryId == 2L)
            }
        )
    }
}
