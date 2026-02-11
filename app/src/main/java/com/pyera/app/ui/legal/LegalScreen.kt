package com.pyera.app.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.util.pyeraBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    title: String,
    body: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(SpacingTokens.MediumLarge),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Last updated: 2026-02-07",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(SpacingTokens.Small))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
        }
    }
}

val TermsOfServiceText = """
Welcome to Pyera Finance. By using the app, you agree to the terms below.

1. Use of the app
You are responsible for the accuracy of the information you enter. Pyera provides tools for personal finance tracking and does not offer financial or legal advice.

2. Accounts and security
Keep your device and authentication credentials secure. You are responsible for all activity under your account.

3. Data and storage
Your data may be stored locally on your device and, if enabled, synced to cloud services. You control what you enter and how it is used within the app.

4. Availability
We strive to keep Pyera available, but the service is provided "as is" without warranties of any kind.

5. Changes
We may update these terms. Continued use of the app means you accept the updated terms.

If you have questions, contact support from the app settings.
""".trim()

val PrivacyPolicyText = """
Pyera Finance respects your privacy. This policy explains what data we collect and how it is used.

1. Data you provide
We store the financial data you enter, such as transactions, budgets, and goals. This data is used to power app features.

2. Device data
Basic device information may be used for diagnostics and app stability.

3. Cloud sync (if enabled)
If you enable sync, your data may be stored in cloud services you configure. We do not sell your data.

4. Security
We use modern security practices to protect your data, including encryption where supported.

5. Your choices
You can delete your data by clearing app storage or removing your account. Export is available from Profile.

If you have questions, contact support from the app settings.
""".trim()
