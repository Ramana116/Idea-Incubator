/**
 * pdfExport.js
 * 
 * Defines a structural utility function mapping AI-generated business plan JSON state
 * to a formatted high-quality PDF report layout using the jsPDF library.
 */

/**
 * Maps the JSON business plan state into a styled PDF output layout.
 * 
 * @param {Object} businessPlan - The JSON document containing section contents.
 * @param {Object} doc - An instance of jsPDF.
 * @returns {Object} The updated jsPDF instance.
 */
export function mapBusinessPlanToPdf(businessPlan, doc) {
    if (!doc) {
        throw new Error("A valid jsPDF instance is required for export.");
    }

    // Default configuration and theme dimensions (A4 layout margins in pt)
    const margin = 40;
    const pageWidth = doc.internal.pageSize.getWidth();
    const contentWidth = pageWidth - (margin * 2);
    let currentY = 50;

    // UI Theme Palette
    const COLORS = {
        primary: { r: 10, g: 132, b: 255 }, // Electric Blue Accent
        secondary: { r: 100, g: 116, b: 139 }, // Slate Secondary
        darkText: { r: 33, g: 37, b: 41 }, // Dark grey/charcoal body
        divider: { r: 226, g: 232, b: 240 } // Light grey line
    };

    /**
     * Helper to render dynamic multi-line wrapped text blocks with auto-paging
     */
    function renderSection(title, content) {
        if (!content || content.trim() === "") return;

        // Space validation - prevent heading orphans at page end
        if (currentY > doc.internal.pageSize.getHeight() - 80) {
            doc.addPage();
            currentY = 40;
        }

        // Section Title Styling
        doc.setFont("helvetica", "bold");
        doc.setFontSize(14);
        doc.setTextColor(COLORS.primary.r, COLORS.primary.g, COLORS.primary.b);
        doc.text(title.toUpperCase(), margin, currentY);
        currentY += 6;

        // Custom Blue Accent Underline Indicator
        doc.setDrawColor(COLORS.primary.r, COLORS.primary.g, COLORS.primary.b);
        doc.setLineWidth(1.5);
        doc.line(margin, currentY, margin + 40, currentY);
        currentY += 10;

        // Body Content Wrapping and Layout Flow
        doc.setFont("helvetica", "normal");
        doc.setFontSize(10);
        doc.setTextColor(COLORS.darkText.r, COLORS.darkText.g, COLORS.darkText.b);

        const paragraphs = content.split('\n');
        paragraphs.forEach(para => {
            const lines = doc.splitTextToSize(para.trim(), contentWidth);
            lines.forEach(line => {
                if (currentY > doc.internal.pageSize.getHeight() - 40) {
                    doc.addPage();
                    currentY = 40;
                }
                doc.text(line, margin, currentY);
                currentY += 15; // standard spacing
            });
            currentY += 5; // Paragraph spacing
        });
        currentY += 10; // Extra spacing between blocks
    }

    // --- Document Cover Header ---
    doc.setFont("helvetica", "bold");
    doc.setFontSize(22);
    doc.setTextColor(COLORS.primary.r, COLORS.primary.g, COLORS.primary.b);
    doc.text(businessPlan.name || "AI INCUBATOR BUSINESS PLAN", margin, currentY);
    currentY += 10;

    // Subtitle Description
    doc.setFont("helvetica", "normal");
    doc.setFontSize(11);
    doc.setTextColor(COLORS.secondary.r, COLORS.secondary.g, COLORS.secondary.b);
    doc.text("STRUCTURED STRATEGIC ADVISORY BLUEPRINT", margin, currentY);
    currentY += 15;

    // Header Horizontal Line Divider
    doc.setDrawColor(COLORS.primary.r, COLORS.primary.g, COLORS.primary.b);
    doc.setLineWidth(2.5);
    doc.line(margin, currentY, pageWidth - margin, currentY);
    currentY += 25;

    // --- Dynamic Content Sections Extraction ---
    const roadmap = businessPlan.businessPlan || businessPlan;

    renderSection("1. Executive Summary", roadmap.executiveSummary);
    renderSection("2. Deep Market Sizing (TAM/SAM/SOM)", roadmap.marketAnalysisDetail);
    renderSection("3. Customer Segment Profiles", roadmap.customerSegments);
    renderSection("4. Revenue Engineering & Monitization", roadmap.revenueStreams);
    renderSection("5. Operational Cost Structures", roadmap.costStructure);
    renderSection("6. Brand & Acquisition Strategy", roadmap.marketingPlan);
    renderSection("7. Scaling Matrix & Growth Benchmarks", roadmap.growthStrategy);

    // --- Document Footer Integration (For all pages) ---
    const totalPages = doc.internal.getNumberOfPages();
    for (let i = 1; i <= totalPages; i++) {
        doc.setPage(i);
        doc.setFont("helvetica", "italic");
        doc.setFontSize(8);
        doc.setTextColor(COLORS.secondary.r, COLORS.secondary.g, COLORS.secondary.b);
        
        // Footer divider
        doc.setDrawColor(COLORS.divider.r, COLORS.divider.g, COLORS.divider.b);
        doc.setLineWidth(0.5);
        doc.line(margin, doc.internal.pageSize.getHeight() - 30, pageWidth - margin, doc.internal.pageSize.getHeight() - 30);
        
        // Footer Text Labels
        doc.text("AI Idea Validator — Full Business Report", margin, doc.internal.pageSize.getHeight() - 20);
        doc.text(`Page ${i} of ${totalPages}`, pageWidth - margin - 30, doc.internal.pageSize.getHeight() - 20);
    }

    return doc;
}
