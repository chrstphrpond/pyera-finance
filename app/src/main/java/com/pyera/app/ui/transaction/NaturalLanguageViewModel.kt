package com.pyera.app.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.nlp.NaturalLanguageParser
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NaturalLanguageViewModel @Inject constructor(
    private val parser: NaturalLanguageParser,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    fun parse(
        input: String,
        onResult: (Result<NaturalLanguageParser.ParsedTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            val result = parser.parse(input)
            onResult(result)
        }
    }

    fun saveParsedTransaction(
        parsed: NaturalLanguageParser.ParsedTransaction,
        accountId: Long?
    ): Flow<Result<Long>> = flow {
        try {
            val amount = parsed.amount
            if (amount == null || amount <= 0) {
                emit(Result.failure(Exception("Amount is required")))
                return@flow
            }

            if (accountId == null || accountId <= 0) {
                emit(Result.failure(Exception("Account is required")))
                return@flow
            }

            val userId = authRepository.currentUser?.uid.orEmpty()
            val transaction = TransactionEntity(
                amount = amount,
                note = parsed.description.trim(),
                type = parsed.type,
                categoryId = parsed.categoryId?.toInt(),
                accountId = accountId,
                userId = userId,
                date = parsed.date ?: System.currentTimeMillis()
            )

            val id = transactionRepository.insertTransactionAndReturnId(transaction)
            emit(Result.success(id))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
