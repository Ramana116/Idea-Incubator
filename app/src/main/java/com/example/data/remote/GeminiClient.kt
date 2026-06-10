package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.*
import com.squareup.moshi.Moshi

object GeminiClient {

    private val moshi = Moshi.Builder().build()
    private val reportAdapter = moshi.adapter(StartupReport::class.java)

    /**
     * Call Gemini to analyze the startup idea.
     * Includes fallback mock generator in case of network issues or empty API key with detailed user guidance.
     */
    suspend fun validateStartupIdea(
        name: String,
        description: String,
        industry: String,
        targetAudience: String
    ): StartupReport {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Emulate fallback high-quality analytics mock if key is default/empty, so the UI is fully functional
            // but inject a warning about configuring the key in the Secrets panel.
            return generateMockReport(name, description, industry, targetAudience, isMock = true)
        }

        val prompt = """
            Determine validation details for a startup idea:
            Name: $name
            Description: $description
            Industry Segment: $industry
            Target Audience User Group: $targetAudience

            Please run a comprehensive multi-agent startup simulation to generate a full analysis report.
            Act as:
            1. Market Research Agent (Analyzes TAM/SAM/SOM, trends, growth)
            2. Competitor Agent (Locates actual competitors, strengths, weaknesses)
            3. Gap Finder Agent (Identifies what competitors lacks and score opportunity)
            4. Financial Agent (Predicts conservative, expected, and optimistic models)
            5. Risk Detection Agent (Checks regulatory, technical, marketing risks)
            6. Business Planner Agent (Writes the executive summary and marketing plans)
            7. Pitch Deck Agent (Prepares investor presentation structure)

            Your response must be a single RAW JSON object that maps perfectly to this structure. Do not wrap in markdown tags like ```json:
            {
              "name": "$name",
              "description": "$description",
              "industry": "$industry",
              "targetAudience": "$targetAudience",
              "detectedIndustry": "The specific primary industry (e.g. EdTech, FinTech, DeepTech)",
              "businessCategory": "e.g. AI SaaS, marketplace, hardware-enabled B2B",
              "productType": "e.g. mobile app, platform service, browser extension",
              "targetUsers": "Describe primary user segments clearly",
              "potentialMarkets": "The regional/global areas with high growth",
              "demandScore": 0 to 100 integer,
              "demandLabel": "A short slogan indicating market opportunity level, e.g. 'Excellent Market Opportunity' or 'Moderate Demand with High Niche Potential'",
              "tamValue": "TAM estimate with currency (e.g. ₹5,000 Cr or $12B)",
              "samValue": "SAM estimate",
              "somValue": "SOM estimate",
              "yearlyGrowthRate": "Market CAGR percentage (e.g. 14.5% YoY)",
              "competitors": [
                {
                  "name": "Real or prominent competitor name (e.g. LeetCode, ChatGPT, etc.)",
                  "pricing": "pricing summary",
                  "features": ["feature 1", "feature 2"],
                  "strengths": "competitor strength",
                  "weaknesses": "competitor weakness"
                }
              ],
              "currentOfferings": ["What competitors typically offer standard"],
              "competitorGaps": ["What competitors fail to offer, showing clear empty markets (e.g. lack of emotional building, offline localizations)"],
              "opportunityScore": 0 to 100 integer,
              "opportunitySuggestions": ["Unique feature suggestion 1", "Unique feature suggestion 2"],
              "validationScore": 0 to 100 integer representing the final aggregate validation rating,
              "validationRecommendation": "Executive summarization recommending whether to build or pivot",
              "revenuePredictions": [
                {
                  "year": "Year 1",
                  "conservative": "conservative estimate (e.g. ₹10 Lakhs)",
                  "expected": "expected estimate",
                  "optimistic": "optimistic estimate"
                },
                {
                  "year": "Year 3",
                  "conservative": "conservative estimate (e.g. ₹60 Lakhs)",
                  "expected": "expected estimate",
                  "optimistic": "optimistic estimate"
                },
                {
                  "year": "Year 5",
                  "conservative": "conservative estimate (e.g. ₹3 Crores)",
                  "expected": "expected estimate",
                  "optimistic": "optimistic estimate"
                }
              ],
              "recommendedModel": "Recommended business model (e.g. Freemium Subscription, Licensing, Commission)",
              "suggestedPricing": "Recommended actual pricing tier (e.g. ₹299/mo or $15/mo)",
              "pricingStructureDetails": "Explain the pricing tiers rationale",
              "strengths": ["SWOT Strength 1", "Strength 2"],
              "weaknesses": ["SWOT Weakness 1", "Weakness 2"],
              "opportunities": ["SWOT Opportunity 1", "Opportunity 2"],
              "threats": ["SWOT Threat 1", "Threat 2"],
              "investorReadinessScore": 0 to 100 integer,
              "investorReadinessDetails": "Explain investor metrics evaluation",
              "risks": [
                {
                  "riskName": "A critical potential threat",
                  "category": "Technical, Financial, Legal, Market, or Operational",
                  "severity": "High, Medium, or Low",
                  "solution": "Actionable strategy / mitigation plan"
                }
              ],
              "businessPlan": {
                "executiveSummary": "Comprehensive summary of the concept and path to market",
                "marketAnalysisDetail": "Deep dive analysis of the competition and customer demand",
                "customerSegments": "Categorized target audience profiles",
                "revenueStreams": "Explanation of streams and premium monetizations",
                "costStructure": "Expected server, API, marketing, and developmental support costs",
                "marketingPlan": "Detailed growth-hacking and digital positioning strategy",
                "growthStrategy": "Plan to scale operations from Year 1 to Year 5"
              },
              "pitchDeck": [
                {
                  "title": "Problem Statement",
                  "bullets": ["State bullet 1", "State bullet 2"],
                  "footnote": "Supporting data / quote"
                },
                {
                  "title": "The Solution",
                  "bullets": ["State core product solutions", "How it resolves the friction"],
                  "footnote": "Value proposition"
                },
                {
                  "title": "Market Size & Opportunity",
                  "bullets": ["Detail TAM, SAM, SOM metrics", "Market expansion trends"],
                  "footnote": "CAGR statistics"
                },
                {
                  "title": "Competitor Intelligence & Competitive Advantage",
                  "bullets": ["Acknowledge competitors", "Point out our distinct gaps"],
                  "footnote": "The Unfair Advantage"
                },
                {
                  "title": "Revenue & Business Model",
                  "bullets": ["How we charge customers", "Financial predictions summary"],
                  "footnote": "SaaS Monetizations"
                }
              ],
              "fundingRoadmap": [
                {
                  "stage": "Ideation / Pre-Seed",
                  "source": "F&F, Incubators, Grants",
                  "recommendation": "Identify a baseline recommendation for pre-seed actions."
                },
                {
                  "stage": "MVP / Seed",
                  "source": "Angel Investors, Micro-VCs",
                  "recommendation": "MVP development funding strategy."
                }
              ],
              "successProbability": 0 to 100 integer representing statistical chance of succeeding based on competitive gaps
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No analytical report returned.")
            reportAdapter.fromJson(jsonText) ?: throw Exception("Failed to parse startup report JSON.")
        } catch (e: Exception) {
            e.printStackTrace()
            // If API fails due to bad key or network, return high fidelity custom simulated analysis with warning
            generateMockReport(name, description, industry, targetAudience, isMock = false, errorMsg = e.localizedMessage)
        }
    }

    /**
     * Standard Chat Grounding with the generated Startup Report context.
     */
    suspend fun chatWithMentor(
        report: StartupReport,
        history: List<ChatMessage>,
        newMessage: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val systemPrompt = """
            You are an elite, practical venture capitalist & startup incubator mentor coaching a startup on their validated idea.
            Here is the complete validated evaluation of their idea:
            Startup Name: ${report.name}
            Description: ${report.description}
            Industry: ${report.detectedIndustry} (${report.businessCategory})
            Target Users: ${report.targetUsers}
            Opportunity Score: ${report.opportunityScore}%
            Gaps Detected: ${report.competitorGaps.joinToString(", ")}
            Revenue Recommended Model: ${report.recommendedModel} at Pricing: ${report.suggestedPricing}
            SWOT Strengths: ${report.strengths.joinToString(", ")}
            SWOT Weaknesses: ${report.weaknesses.joinToString(", ")}
            SWOT Opportunities: ${report.opportunities.joinToString(", ")}
            SWOT Threats: ${report.threats.joinToString(", ")}
            Investor Readiness Rating: ${report.investorReadinessScore}%

            Speak with practical wisdom, sharp business acumen, realistic warnings, yet supportive excitement! Answer questions in short, clear paragraphs utilizing bullet points. Offer constructive, tough, actionable suggestions.
        """.trimIndent()

        // Gather chat history context
        val contextParts = mutableListOf<Part>()
        contextParts.add(Part(text = "Grounding Context: $systemPrompt"))
        history.forEach { msg ->
            val prefix = if (msg.sender == MessageSender.USER) "User Question: " else "Mentor Expert Answer: "
            contextParts.add(Part(text = prefix + msg.text))
        }
        contextParts.add(Part(text = "User Question: $newMessage"))

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = contextParts)),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Emulate conversational startup consulting offline
            return getSimulatedMentorReply(newMessage, report)
        }

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, my analytical engines are experiencing a brief disconnect. What other aspects of your ${report.name} layout can we drill into?"
        } catch (e: Exception) {
            e.printStackTrace()
            getSimulatedMentorReply(newMessage, report) + "\n\n(Local Advisor Offline Fallback: ${e.localizedMessage})"
        }
    }

    /**
     * Generates extremely high-quality simulated reports when API is offline/unconfigured
     * to keep prototype 100% stable and fully interactive.
     */
    private fun generateMockReport(
        name: String,
        description: String,
        industry: String,
        targetAudience: String,
        isMock: Boolean = false,
        errorMsg: String? = null
    ): StartupReport {
        val warningHeader = if (isMock) {
            "[DEMO / OFFLINE MODE] (Set your personal GEMINI_API_KEY in the Secrets panel in AI Studio for live multi-agent AI verification!)."
        } else {
            "[FALLBACK MODE] Analytical engine loaded locally. Connection message: $errorMsg"
        }

        val parsedIndustry = if (industry.isNotBlank()) industry else "General Tech SaaS"
        val audience = if (targetAudience.isNotBlank()) targetAudience else "General Consumers"

        val mockDemandScore = 82
        val mockValidationScore = 79
        val mockSuccessProbability = 72
        val mockOpportunityScore = 88

        return StartupReport(
            name = name,
            description = "$warningHeader\n\n$description",
            industry = parsedIndustry,
            targetAudience = audience,
            detectedIndustry = parsedIndustry,
            businessCategory = "Integrated AI Service Layout",
            productType = "Cross-platform Cloud SaaS",
            targetUsers = "Primary Focus on $audience with professional demands.",
            potentialMarkets = "Tier 1 Metro hubs, North America, India, and European remote systems.",
            demandScore = mockDemandScore,
            demandLabel = "Excellent Industry Velocity with strong greenfield segments",
            tamValue = "$18.4 Billion global aggregate",
            samValue = "$2.1 Billion adressable SaaS market",
            somValue = "$240 Million specialized target footprint",
            yearlyGrowthRate = "18.6% CAGR estimated 2026-2031",
            competitors = listOf(
                Competitor(
                    name = "Legacy Inc.",
                    pricing = "$49/developer/month",
                    features = listOf("Basic analytical reports", "CSV exports", "Custom dashboards"),
                    strengths = "High brand equity, established corporate relationships",
                    weaknesses = "Outdated clunky web interface, slow manual processes, lacks generative recommendations"
                ),
                Competitor(
                    name = "SaaS Automate",
                    pricing = "Freemium scaling up to $199/month",
                    features = listOf("Template configurations", "Limited automation macros"),
                    strengths = "Very affordable starter tiers, basic dashboard layouts",
                    weaknesses = "Zero domain specific models, low data security, lacks chat grounding or SWOT summaries"
                )
            ),
            currentOfferings = listOf(
                "Standard data dashboard views",
                "CSV/PDF static downloads"
            ),
            competitorGaps = listOf(
                "Lacks deep emotion validation or Confidence Coaching systems",
                "No localized regional language configurations",
                "Lacks actionable sequential roadmapping"
            ),
            opportunityScore = mockOpportunityScore,
            opportunitySuggestions = listOf(
                "Create a step-by-step interactive AI feedback roadmap for onboarding",
                "Provide confidence coaching speech simulations with automated analysis feedback",
                "Implement a dynamic localized localization framework to hit non-English speaking markets first!"
            ),
            validationScore = mockValidationScore,
            validationRecommendation = "Strong candidate for rapid prototyping. The competitive landscape shows solid mature players, but they are highly static. Focus on the core gaps ($audience localized roadmaps) to establish a fast market entry footprint.",
            revenuePredictions = listOf(
                RevenueYearPrediction("Year 1", "₹8 Lakhs / $10k", "₹15 Lakhs / $18k", "₹30 Lakhs / $36k"),
                RevenueYearPrediction("Year 3", "₹45 Lakhs / $54k", "₹90 Lakhs / $108k", "₹1.8 Crores / $220k"),
                RevenueYearPrediction("Year 5", "₹2.2 Crores / $260k", "₹5 Crores / $600k", "₹12 Crores / $1.4M")
            ),
            recommendedModel = "B2C / B2B Hybrid Freemium SaaS subscription",
            suggestedPricing = "₹349/month or $9.99/month for Pro tools",
            pricingStructureDetails = "Freemium tier supports 2 validations. Pro tier unlocks SWOT, deep business planning modules, unlimited presentations, and direct grounded chat mentorship. B2B enterprise volume scaling starts at 10 user blocks.",
            strengths = listOf("Agile product core", "Highly customizable localized coaching engines", "Very low operational overhead with standard LLM calls"),
            weaknesses = listOf("Dependent on external API speeds", "Low starting brand awareness"),
            opportunities = listOf("Rapid AI market validation adoption", "Growing gig economy and freelance demands"),
            threats = listOf("Big cloud tech players releasing bundled simple utilities"),
            investorReadinessScore = 84,
            investorReadinessDetails = "Scored highly due to strong opportunity score (88%) and clear unmet competitor gaps. Investor focus will center on product acceleration velocity.",
            risks = listOf(
                StartupRisk(
                    riskName = "API price scaling & speed",
                    category = "Technical",
                    severity = "Medium",
                    solution = "Implement smart caching of reports in a local Room database and fallback local engines when necessary."
                ),
                StartupRisk(
                    riskName = "User trust & credibility",
                    category = "Market",
                    severity = "Medium",
                    solution = "Offer detailed citations, transparent formulas, and solid initial validation transparency."
                )
            ),
            businessPlan = BusinessPlan(
                executiveSummary = "The startup solves the primary bottleneck for $audience who need high-velocity preparation or evaluation of technical concepts without spending $1000s in consulting or mentorship. By executing an automated agent validation matrix, they get a custom roadmap in 30 seconds rather than waiting months.",
                marketAnalysisDetail = "Market CAGR is 18.6%. Gaps in competitor offerings center on dynamic conversational coaching. Core audience suffers high anxiety and needs immediate actionable tools.",
                customerSegments = "Ambitious students, tech freelancers, fresh engineering graduates, and corporate entry candidates.",
                revenueStreams = "SaaS monthly subscription, institutional team licensing, and certified mock report exports.",
                costStructure = "Low starting hosting. LLM API tokens average ₹4-₹8 per validation. Low support expense with built-in mentor chat.",
                marketingPlan = "Leverage viral campus marketing, organic LinkedIn posts comparing AI mock results, and strategic university partnerships.",
                growthStrategy = "Establish university pilot integrations in Q1, expand to regional hubs in Q3, and introduce advanced automated audio/video coaching avatars by Year 2."
            ),
            pitchDeck = listOf(
                PitchSlide(
                    title = "The Problem",
                    bullets = listOf(
                        "90% of fresh individuals fail to pass technical screens due to anxiety and bad prep structures.",
                        "Mentorship guides are static, expensive, and completely unscalable."
                    ),
                    footnote = "Source: Industry Startup Research, 2026"
                ),
                PitchSlide(
                    title = "The AI Solution",
                    bullets = listOf(
                        "An automated, 24/7 hyper-personalized simulator simulating authentic domain evaluations.",
                        "Generates custom regionalized roadmaps to focus improvement points securely."
                    ),
                    footnote = "Idea Validator Product Demo"
                ),
                PitchSlide(
                    title = "Market Size",
                    bullets = listOf(
                        "Total Addressable Market reaches $18.4 Billion globally.",
                        "SOM targets an initially addressable regional student cohort representing a $240M opportunity."
                    ),
                    footnote = "TAM calculated on global EdTech CAGR"
                ),
                PitchSlide(
                    title = "Unfair Gaps & Strategy",
                    bullets = listOf(
                        "Current engines provide static algorithms (LeetCode).",
                        "We target emotional coaching, regional localizations, and structured PDF execution roadmaps."
                    ),
                    footnote = "Competitor Intelligence Gap Assessment"
                ),
                PitchSlide(
                    title = "Financial Model",
                    bullets = listOf(
                        "Targeting Year 1 scaling of ₹15 Lakhs and scaling rapidly to ₹5 Crores expected inside Year 5.",
                        "Highly resilient unit economics with 88% gross software profit margins."
                    ),
                    footnote = "Expected conservative-to-optimistic projection metrics"
                )
            ),
            fundingRoadmap = listOf(
                FundingRoute(
                    stage = "Ideation / Pre-Seed",
                    source = "Grants, University Incubators",
                    recommendation = "Utilize free cloud credits and local student incubation grants to build out the MVP core."
                ),
                FundingRoute(
                    stage = "MVP Launch / Seed",
                    source = "Angel Syndicates, Micro-VCs",
                    recommendation = "Raise ₹40L to hire 2 core engineers and launch campus Ambassador growth programs to reach 10k users."
                )
            ),
            successProbability = mockSuccessProbability
        )
    }

    private fun getSimulatedMentorReply(newMessage: String, report: StartupReport): String {
        val lower = newMessage.lowercase()
        return when {
            lower.contains("pricing") || lower.contains("price") -> {
                "For ${report.name}, establishing a premium tier at ${report.suggestedPricing} is extremely smart. To optimize this, I recommend a tiered structure: keep a free starter token that validates 1 report, then charge a monthly subscription for dynamic SWOT and the interactive Business Plan modules. Do you think B2B schools would buy this as a package?"
            }
            lower.contains("target") || lower.contains("student") || lower.contains("customer") -> {
                "Targeting ${report.targetAudience} is a massive strategic play. They are highly active online, have clear pain points, and talk to each other. Your first priority should be user acquisition via micro-influencers and student networks. Do not try to target everyone at once; dominate this core group first!"
            }
            lower.contains("improve") || lower.contains("better") -> {
                "To improve ${report.name} immediately, focus on executing the identified gaps: ${report.competitorGaps.firstOrNull() ?: "the unique emotional coaching engine"}. Competitors are big and slow, they won't build this overnight. Build a simple interactive prototype highlighting this exact feature!"
            }
            else -> {
                "Excellent focus! For ${report.name} (${report.detectedIndustry}), I strongly suggest prioritizing product velocity over complex features. Focus on customer feedback loops. What is your plan for the first 100 beta testing users in your target audience?"
            }
        }
    }
}
