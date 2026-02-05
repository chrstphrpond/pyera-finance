package com.pyera.app.ui.insights.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.pyera.app.domain.analysis.CategoryInsight
import com.pyera.app.domain.analysis.DailySpending
import com.pyera.app.domain.analysis.MonthlySpending
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.*
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

    val modelProducer = remember { CartesianChartModelProducer.build() }
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    // Prepare data for Vico
    val xValues = data.mapIndexed { index, _ -> index.toFloat() }
    val yValues = data.map { it.amount.toFloat() }
    val labels = data.map { dateFormat.format(Date(it.date)) }

    modelProducer.tryRunTransaction {
        lineSeries { series(xValues, yValues) }
    }

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.CardPadding)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.Medium))

            ProvideVicoTheme(
                vicoTheme.copy(
                    textColor = TextPrimary.hashCode()
                )
            ) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(
                            fill = fill(NeonYellow.copy(alpha = 0.2f)),
                            strokeFill = fill(NeonYellow)
                        ),
                        startAxis = rememberStartAxis(
                            label = rememberTextComponent(color = TextSecondary),
                            line = rememberLineComponent(color = ColorBorder)
                        ),
                        bottomAxis = rememberBottomAxis(
                            label = rememberTextComponent(color = TextSecondary),
                            line = rememberLineComponent(color = ColorBorder),
                            valueFormatter = { value, _, _ ->
                                labels.getOrNull(value.toInt()) ?: ""
                            },
                            labelRotationDegrees = 45f
                        ),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
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

    val modelProducer = remember { CartesianChartModelProducer.build() }

    val xValues = data.mapIndexed { index, _ -> index.toFloat() }
    val spendingValues = data.map { it.totalSpending.toFloat() }
    val incomeValues = data.map { it.totalIncome.toFloat() }
    val labels = data.map { it.monthName }

    modelProducer.tryRunTransaction {
        columnSeries {
            series(xValues, spendingValues)
            series(xValues, incomeValues)
        }
    }

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.CardPadding)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.Medium))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = ColorError, label = "Spending")
                Spacer(modifier = Modifier.width(Spacing.Large))
                ChartLegendItem(color = ColorSuccess, label = "Income")
            }

            Spacer(modifier = Modifier.height(Spacing.Small))

            ProvideVicoTheme(
                vicoTheme.copy(
                    textColor = TextPrimary.hashCode()
                )
            ) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                listOf(
                                    rememberLineComponent(fill = fill(ColorError), thickness = 16.dp),
                                    rememberLineComponent(fill = fill(ColorSuccess), thickness = 16.dp)
                                )
                            )
                        ),
                        startAxis = rememberStartAxis(
                            label = rememberTextComponent(color = TextSecondary),
                            line = rememberLineComponent(color = ColorBorder)
                        ),
                        bottomAxis = rememberBottomAxis(
                            label = rememberTextComponent(color = TextSecondary),
                            line = rememberLineComponent(color = ColorBorder),
                            valueFormatter = { value, _, _ ->
                                labels.getOrNull(value.toInt()) ?: ""
                            }
                        ),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
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
        Column(modifier = Modifier.padding(Spacing.CardPadding)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.Medium))

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

            Spacer(modifier = Modifier.height(Spacing.Medium))

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
                text = "₱${String.format("%,.0f", totalValue)}",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
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
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
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
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        // Percentage
        Text(
            text = "${String.format("%.1f", percentage)}%",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )

        // Amount
        Text(
            text = "₱${String.format("%,.0f", amount)}",
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
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
            color = TextSecondary
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
                .padding(Spacing.XLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.Medium))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
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
        Column(modifier = Modifier.padding(Spacing.CardPadding)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.Medium))

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
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        NeonYellow.copy(
                                            alpha = 0.2f + (intensity * 0.8f)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day.amount > 0) {
                                    Text(
                                        text = "${String.format("%.0f", day.amount / 1000)}k",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (intensity > 0.5f) DarkGreen else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Small))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Low",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
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
                                    NeonYellow.copy(alpha = 0.2f),
                                    NeonYellow
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "High",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}
