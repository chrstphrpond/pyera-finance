package com.pyera.app.ui.insights.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.pyera.app.domain.analysis.CategoryInsight
import com.pyera.app.domain.analysis.DailySpending
import com.pyera.app.domain.analysis.MonthlySpending
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Line chart showing spending trends over time
 */
@Composable
fun SpendingTrendChart(
    data: List<DailySpending>,
    modifier: Modifier = Modifier,
    title: String = "Spending Trend"
) {
    if (data.isEmpty()) {
        EmptyChartState(title = title, message = "No data available")
        return
    }

    val modelProducer = remember { ChartEntryModelProducer() }
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    val labels = data.map { dateFormat.format(Date(it.date)) }

    LaunchedEffect(data) {
        val entries = data.mapIndexed { index, item ->
            entryOf(index.toFloat(), item.amount.toFloat())
        }
        modelProducer.setEntries(entries)
    }

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            val axisLabel = textComponent(color = MaterialTheme.colorScheme.onSurfaceVariant)
            val axisLine = lineComponent(color = ColorBorder, thickness = 1.dp)

            Chart(
                chart = lineChart(
                    lines = listOf(
                        lineSpec(lineColor = ColorTokens.Primary500)
                    )
                ),
                chartModelProducer = modelProducer,
                startAxis = rememberStartAxis(
                    label = axisLabel,
                    axis = axisLine,
                    guideline = lineComponent(color = ColorBorder.copy(alpha = 0.3f), thickness = 1.dp)
                ),
                bottomAxis = rememberBottomAxis(
                    label = axisLabel,
                    axis = axisLine,
                    guideline = lineComponent(color = ColorBorder.copy(alpha = 0.2f), thickness = 1.dp),
                    valueFormatter = AxisValueFormatter { value, _ ->
                        labels.getOrNull(value.toInt()) ?: ""
                    },
                    labelRotationDegrees = 45f
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

/**
 * Bar chart comparing spending between months
 */
@Composable
fun MonthlyComparisonChart(
    data: List<MonthlySpending>,
    modifier: Modifier = Modifier,
    title: String = "Monthly Comparison"
) {
    if (data.isEmpty()) {
        EmptyChartState(title = title, message = "No data available")
        return
    }

    val modelProducer = remember { ChartEntryModelProducer() }
    val spendingValues = data.map { it.totalSpending.toFloat() }
    val incomeValues = data.map { it.totalIncome.toFloat() }
    val labels = data.map { it.monthName }

    LaunchedEffect(data) {
        val spendingEntries = spendingValues.mapIndexed { index, value ->
            entryOf(index.toFloat(), value)
        }
        val incomeEntries = incomeValues.mapIndexed { index, value ->
            entryOf(index.toFloat(), value)
        }
        modelProducer.setEntries(spendingEntries, incomeEntries)
    }

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = ColorTokens.Error500, label = "Spending")
                Spacer(modifier = Modifier.width(SpacingTokens.Medium))
                ChartLegendItem(color = ColorTokens.Success500, label = "Income")
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Small))

            val axisLabel = textComponent(color = MaterialTheme.colorScheme.onSurfaceVariant)
            val axisLine = lineComponent(color = ColorBorder, thickness = 1.dp)

            Chart(
                chart = columnChart(
                    columns = listOf(
                        lineComponent(color = ColorTokens.Error500, thickness = SpacingTokens.Medium),
                        lineComponent(color = ColorTokens.Success500, thickness = SpacingTokens.Medium)
                    )
                ),
                chartModelProducer = modelProducer,
                startAxis = rememberStartAxis(
                    label = axisLabel,
                    axis = axisLine,
                    guideline = lineComponent(color = ColorBorder.copy(alpha = 0.3f), thickness = 1.dp)
                ),
                bottomAxis = rememberBottomAxis(
                    label = axisLabel,
                    axis = axisLine,
                    guideline = lineComponent(color = ColorBorder.copy(alpha = 0.2f), thickness = 1.dp),
                    valueFormatter = AxisValueFormatter { value, _ ->
                        labels.getOrNull(value.toInt()) ?: ""
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

/**
 * Donut/Pie chart showing category breakdown
 */
@Composable
fun CategoryBreakdownChart(
    categories: List<CategoryInsight>,
    modifier: Modifier = Modifier,
    title: String = "Spending by Category"
) {
    if (categories.isEmpty() || categories.all { it.currentPeriodSpending <= 0 }) {
        EmptyChartState(title = title, message = "No spending data")
        return
    }

    val sortedCategories = categories
        .filter { it.currentPeriodSpending > 0 }
        .sortedByDescending { it.currentPeriodSpending }
        .take(6) // Show top 6 categories

    val totalSpending = sortedCategories.sumOf { it.currentPeriodSpending }

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            // Custom donut chart using simple composables
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    segments = sortedCategories.map {
                        DonutSegment(
                            value = it.currentPeriodSpending.toFloat(),
                            color = Color(it.categoryColor),
                            label = it.categoryName
                        )
                    },
                    totalValue = totalSpending.toFloat(),
                    modifier = Modifier.size(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            // Category legend
            sortedCategories.forEach { category ->
                val percentage = if (totalSpending > 0) {
                    (category.currentPeriodSpending / totalSpending * 100)
                } else 0.0

                CategoryLegendItem(
                    color = Color(category.categoryColor),
                    label = category.categoryName,
                    amount = category.currentPeriodSpending,
                    percentage = percentage.toFloat()
                )
            }
        }
    }
}

/**
 * Simple donut chart implementation
 */
@Suppress("UNUSED_PARAMETER")
@Composable
private fun DonutChart(
    segments: List<DonutSegment>,
    totalValue: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Simple visual representation using stacked boxes
        // For a real implementation, you would use Canvas
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = CurrencyFormatter.formatShort(totalValue.toDouble()),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

private data class DonutSegment(
    val value: Float,
    val color: Color,
    val label: String
)

/**
 * Category list item with progress bar for category breakdown
 */
@Composable
private fun CategoryLegendItem(
    color: Color,
    label: String,
    amount: Double,
    percentage: Float
) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
            ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )

        // Category name
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        // Percentage
        Text(
            text = "${String.format("%.1f", percentage)}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        // Amount
        Text(
            text = CurrencyFormatter.formatShort(amount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Empty state for charts when no data is available
 */
@Composable
private fun EmptyChartState(
    title: String,
    message: String
) {
    PyeraCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.MediumLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

/**
 * Heat map showing daily spending intensity
 */
@Composable
fun SpendingHeatMap(
    data: List<DailySpending>,
    modifier: Modifier = Modifier,
    title: String = "Daily Spending Pattern"
) {
    if (data.isEmpty()) {
        EmptyChartState(title = title, message = "No data available")
        return
    }

    val maxAmount = data.maxOfOrNull { it.amount } ?: 1.0
    val minAmount = data.minOfOrNull { it.amount } ?: 0.0

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            // Simple heat map representation
            // In a real implementation, you'd use a grid layout
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                data.chunked(7).forEach { weekData ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        weekData.forEach { day ->
                            val intensity = if (maxAmount > minAmount) {
                                ((day.amount - minAmount) / (maxAmount - minAmount)).toFloat()
                            } else 0f

                            Box(
                                modifier = Modifier
                                    .size(SpacingTokens.ExtraLarge)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        ColorTokens.Primary500.copy(
                                            alpha = 0.2f + (intensity * 0.8f)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day.amount > 0) {
                                    Text(
                                        text = "${String.format("%.0f", day.amount / 1000)}k",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (intensity > 0.5f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Small))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Low",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    ColorTokens.Primary500.copy(alpha = 0.2f),
                                    ColorTokens.Primary500
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "High",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
    }
}


