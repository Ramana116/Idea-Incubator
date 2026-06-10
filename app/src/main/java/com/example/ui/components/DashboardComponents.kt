package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Competitor
import com.example.data.model.FundingRoute
import com.example.data.model.RevenueYearPrediction
import com.example.data.model.StartupRisk
import com.example.ui.theme.*

/**
 * A beautiful, premium material circular gauge with canvas strokes and gradients.
 */
@Composable
fun CircularScoreGauge(
    score: Int,
    label: String,
    modifier: Modifier = Modifier,
    maxScore: Int = 100,
    gaugeColor: Color = ElectricSky,
    size: Dp = 150.dp,
    strokeWidth: Dp = 12.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score.toFloat() / maxScore,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "gaugeAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gaugeGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Render Circular Arc and Glowing Shadows
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            // Draw background track
            drawCircle(
                color = Slate700.copy(alpha = 0.5f),
                style = Stroke(width = strokeWidth.toPx())
            )

            // Draw animated ambient glowing outer ring
            drawCircle(
                color = gaugeColor.copy(alpha = glowAlpha),
                radius = (size.toPx() / 2f) - (strokeWidth.toPx() / 2f) + (4.dp.toPx() * glowAlpha * 5f),
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw active progress sweep
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        gaugeColor.copy(alpha = 0.6f),
                        gaugeColor,
                        gaugeColor.copy(alpha = 0.6f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Inner text contents
        val currentVal = (animatedProgress * score).toInt()
        val textScale = 0.9f + 0.1f * animatedProgress

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "$currentVal",
                fontSize = ((size.value * 0.22) * textScale).sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "/100",
                fontSize = (size.value * 0.09).sp,
                color = TextSecondary,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = (size.value * 0.08).sp,
                color = gaugeColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }
    }
}

/**
 * SWOT analysis 2x2 Grid card with custom symbols and descriptions.
 */
@Composable
fun SwotGridWidget(
    strengths: List<String>,
    weaknesses: List<String>,
    opportunities: List<String>,
    threats: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SwotItemCard(
                title = "STRENGTHS",
                items = strengths,
                color = MintFlame,
                icon = Icons.Default.ThumbUp,
                modifier = Modifier.weight(1f)
            )
            SwotItemCard(
                title = "WEAKNESSES",
                items = weaknesses,
                color = SeverityHigh,
                icon = Icons.Default.Warning,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SwotItemCard(
                title = "OPPORTUNITIES",
                items = opportunities,
                color = ElectricSky,
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
            SwotItemCard(
                title = "THREATS",
                items = threats,
                color = SeverityMedium,
                icon = Icons.Default.Info,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SwotItemCard(
    title: String,
    items: List<String>,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .heightIn(min = 160.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Slate800,
                        Slate900.copy(alpha = 0.95f),
                        color.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { bullet ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = bullet,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Competitors intelligence card carousel or list row
 */
@Composable
fun CompetitorsListWidget(
    competitors: List<Competitor>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        competitors.forEach { comp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comp.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricSky
                        )
                        Box(
                            modifier = Modifier
                                .background(IndigoDusk.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .border(1.dp, IndigoDusk.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = comp.pricing,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IndigoDusk
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Core Offerings:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    comp.features.forEach { ft ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MintFlame, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = ft, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "STRENGTH", fontSize = 10.sp, color = MintFlame, fontWeight = FontWeight.Bold)
                            Text(text = comp.strengths, fontSize = 12.sp, color = TextPrimary, lineHeight = 15.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "WEAKNESS", fontSize = 10.sp, color = SeverityHigh, fontWeight = FontWeight.Bold)
                            Text(text = comp.weaknesses, fontSize = 12.sp, color = TextPrimary, lineHeight = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Gap Finder analysis visual table
 */
@Composable
fun GapFinderWidget(
    currentOfferings: List<String>,
    gaps: List<String>,
    opportunityScore: Int,
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ElectricSky.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gaps Assessment & Unfair Advantages",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Box(
                    modifier = Modifier
                        .background(MintFlame.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Opp Score: $opportunityScore%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintFlame
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "STANDARD COMPETITORS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    currentOfferings.forEach { offering ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Check, contentDescription = "Standard", tint = TextSecondary, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = offering, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
                Box(modifier = Modifier.width(1.dp).height(120.dp).background(Slate700))
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = "OUR IDENTIFIED GAPS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MintFlame,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    gaps.forEach { gap ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Text(text = "✗", color = SeverityHigh, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
                            Text(text = gap, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate700)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "AI Suggested Strategy:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricSky,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            suggestions.forEach { sug ->
                Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Suggestion",
                        tint = ElectricSky,
                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = sug, fontSize = 12.sp, color = TextPrimary)
                }
            }
        }
    }
}

/**
 * Revenue Predictions Engine Component with clean bar charts
 */
@Composable
fun RevenuePredictionsWidget(
    predictions: List<RevenueYearPrediction>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Revenue Projections Engine (Conservative vs Max Expected)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(12.dp))

            predictions.forEach { pred ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(
                        text = pred.year,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricSky
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Conservative", fontSize = 9.sp, color = TextSecondary)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate700, RoundedCornerShape(4.dp))
                                    .padding(vertical = 5.dp, horizontal = 8.dp)
                            ) {
                                Text(pred.conservative, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text("Expected Model", fontSize = 9.sp, color = MintFlame)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MintFlame.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, MintFlame.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(vertical = 5.dp, horizontal = 8.dp)
                            ) {
                                Text(pred.expected, fontSize = 11.sp, color = MintFlame, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Optimistic", fontSize = 9.sp, color = IndigoDusk)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(IndigoDusk.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(vertical = 5.dp, horizontal = 8.dp)
                            ) {
                                Text(pred.optimistic, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Risks with dynamic severity tags
 */
@Composable
fun RisksWidget(
    risks: List<StartupRisk>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        risks.forEach { risk ->
            val severityColor = when (risk.severity.lowercase()) {
                "high" -> SeverityHigh
                "medium", "moderate" -> SeverityMedium
                else -> SeverityLow
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, severityColor.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Risk", tint = severityColor, modifier = Modifier.size(16.dp))
                            Text(
                                text = risk.riskName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(severityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, severityColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${risk.category} : ${risk.severity.uppercase()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = severityColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AI Mitigation Strategy:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = risk.solution,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Funding roadmap
 */
@Composable
fun FundingRoadmapWidget(
    routes: List<FundingRoute>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Investor Funding Roadmap",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(12.dp))
            routes.forEachIndexed { idx, route ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(IndigoDusk, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        if (idx < routes.size - 1) {
                            Box(modifier = Modifier.width(2.dp).height(40.dp).background(Slate700))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(route.stage, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricSky)
                        Text("Suggested sources: ${route.source}", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(route.recommendation, fontSize = 11.sp, color = TextPrimary, lineHeight = 14.sp)
                    }
                }
            }
        }
    }
}
