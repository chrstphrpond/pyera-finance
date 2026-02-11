package com.pyera.app.ui.transaction

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.domain.nlp.NaturalLanguageParser
import com.pyera.app.ui.theme.tokens.ColorTokens
import java.util.Locale

@Composable
fun NaturalLanguageTransactionInput(
    onParsed: (NaturalLanguageParser.ParsedTransaction) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by rememberSaveable { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val parserViewModel: NaturalLanguageViewModel = hiltViewModel()

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull().orEmpty()
            if (text.isNotBlank()) {
                input = text
            }
        }
    }

    val launchSpeechToText = {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your transaction")
            }
            speechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            onError("Speech recognition is not available on this device")
        }
    }

    val triggerParse: () -> Unit = triggerParse@{
        if (input.isBlank() || isProcessing) return@triggerParse
        isProcessing = true
        parserViewModel.parse(input) { result ->
            isProcessing = false
            result
                .onSuccess {
                    onParsed(it)
                    input = ""
                }
                .onFailure { error ->
                    onError(error.message ?: "Parse failed")
                }
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., Coffee at Starbucks 250") },
            leadingIcon = {
                IconButton(onClick = launchSpeechToText) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = ColorTokens.Primary500
                    )
                }
            },
            trailingIcon = {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = { triggerParse() },
                        enabled = input.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, "Parse", tint = ColorTokens.Primary500)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { triggerParse() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTokens.Primary500,
                cursorColor = ColorTokens.Primary500
            )
        )

        Text(
            text = "Try: \"Salary 50000 yesterday\" or \"Grab ride 150\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
