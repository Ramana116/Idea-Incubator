package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StartupReport(
    val name: String,
    val description: String,
    val industry: String,
    val targetAudience: String,
    
    // 1. AI Idea Analyzer
    val detectedIndustry: String,
    val businessCategory: String,
    val productType: String,
    val targetUsers: String,
    val potentialMarkets: String,
    
    // 2. Market Demand Analysis
    val demandScore: Int, // 0-100
    val demandLabel: String, // e.g. "Excellent Market Opportunity"
    val tamValue: String, // e.g. "$12B" or "₹5,000 Cr"
    val samValue: String,
    val somValue: String,
    val yearlyGrowthRate: String, // e.g. "18.5%"
    
    // 3. Competitor Intelligence System
    val competitors: List<Competitor>,
    
    // 4. Gap Finder
    val currentOfferings: List<String>,
    val competitorGaps: List<String>,
    val opportunityScore: Int,
    val opportunitySuggestions: List<String>,
    
    // 5. Startup Validation Score
    val validationScore: Int, // 0-100
    val validationRecommendation: String, // e.g. "Highly Recommended"
    
    // 6. Revenue Prediction Engine
    val revenuePredictions: List<RevenueYearPrediction>,
    
    // 7. Business Model Generator
    val recommendedModel: String, // e.g. "SaaS Subscription"
    val suggestedPricing: String, // e.g. "₹299/month" or "$29/month"
    val pricingStructureDetails: String,
    
    // 8. SWOT Analysis Generator
    val strengths: List<String>,
    val weaknesses: List<String>,
    val opportunities: List<String>,
    val threats: List<String>,
    
    // 9. Investor Readiness Score
    val investorReadinessScore: Int, // Percent
    val investorReadinessDetails: String,
    
    // 10. Startup Risk Detection
    val risks: List<StartupRisk>,
    
    // 11. Business Plan
    val businessPlan: BusinessPlan,
    
    // 12. Pitch Deck
    val pitchDeck: List<PitchSlide>,
    
    // 14. Funding Recommendation Engine
    val fundingRoadmap: List<FundingRoute>,
    
    // 15. Startup Success Probability Predictor
    val successProbability: Int // Percent
)

@JsonClass(generateAdapter = true)
data class Competitor(
    val name: String,
    val pricing: String,
    val features: List<String>,
    val strengths: String,
    val weaknesses: String
)

@JsonClass(generateAdapter = true)
data class RevenueYearPrediction(
    val year: String, // e.g., "Year 1", "Year 3", "Year 5"
    val conservative: String,
    val expected: String,
    val optimistic: String
)

@JsonClass(generateAdapter = true)
data class StartupRisk(
    val riskName: String,
    val category: String, // Technical, Financial, Legal, Market, Operational
    val severity: String, // High, Medium, Low
    val solution: String
)

@JsonClass(generateAdapter = true)
data class BusinessPlan(
    val executiveSummary: String,
    val marketAnalysisDetail: String,
    val customerSegments: String,
    val revenueStreams: String,
    val costStructure: String,
    val marketingPlan: String,
    val growthStrategy: String
)

@JsonClass(generateAdapter = true)
data class PitchSlide(
    val title: String,
    val bullets: List<String>,
    val footnote: String? = null
)

@JsonClass(generateAdapter = true)
data class FundingRoute(
    val stage: String,
    val source: String,
    val recommendation: String
)

data class StartupIdea(
    val id: Int = 0,
    val name: String,
    val description: String,
    val industry: String,
    val targetAudience: String,
    val timestamp: Long,
    val report: StartupReport?
)
