package com.edulearn.progress.pdf;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generates portrait A4 certificate PDFs in 6 distinct visual templates.
 * Templates are inspired by botanical, formal, and vintage design aesthetics.
 */
@Component
public class CertificatePdfGenerator {

    private static final float PAGE_W = PDRectangle.A4.getWidth();   // 595.28
    private static final float PAGE_H = PDRectangle.A4.getHeight();  // 841.89

    private static final String TEXT_CERTIFICATE = "CERTIFICATE";
    private static final String TEXT_APPRECIATION = "OF APPRECIATION";
    private static final String TEXT_EULEARN = "EduLearn";

    private static final String[] MOTIVATION_QUOTES = {
            "Excellence is not a skill; it is an attitude.",
            "Learning is a treasure that follows its owner everywhere.",
            "Success is the sum of small efforts, repeated day after day.",
            "Education is the passport to the future.",
            "Commitment to growth opens every door.",
            "Your dedication today shapes achievements tomorrow."
    };

    @Value("${certificate.output.path}")
    private String certificateOutputPath;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ────────────────────────────────────────────────────────────────────────────
    //  Public entry point
    // ────────────────────────────────────────────────────────────────────────────

    public String generate(CertificatePdfTemplate template, Long studentId, Long courseId,
            String verificationCode, String courseName, String studentName) {
        try {
            File certDir = new File(certificateOutputPath);
            if (!certDir.exists()) {
                certDir.mkdirs();
            }

            String fileName  = "cert_" + studentId + "_" + courseId + ".pdf";
            String filePath  = certificateOutputPath + fileName;

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    switch (template) {
                        case SAGE_BOTANICAL_BLOOM  -> drawSageBotanicalBloom(cs, verificationCode, courseName, studentName);
                        case WATERCOLOR_BOTANICAL  -> drawWatercolorBotanical(cs, verificationCode, courseName, studentName);
                        case MIDNIGHT_BOTANICAL    -> drawMidnightBotanical(cs, verificationCode, courseName, studentName);
                        case FORMAL_NAVY_ORNATE    -> drawFormalNavyOrnate(cs, verificationCode, courseName, studentName);
                        case VINTAGE_CRIMSON_SEAL  -> drawVintageCrimsonSeal(cs, verificationCode, courseName, studentName);
                        case TEAL_GUILLOCHE_AWARD  -> drawTealGuillocheAward(cs, verificationCode, courseName, studentName);
                    }
                }
                document.save(filePath);
            }
            return filePath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate certificate PDF for student " + studentId, e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    //  Shared drawing primitives
    // ────────────────────────────────────────────────────────────────────────────

    /** Draw a filled circle approximated with a polygon. */
    private static void fillCircle(PDPageContentStream c, float cx, float cy, float r) throws IOException {
        final int segments = 48;
        for (int i = 0; i <= segments; i++) {
            double ang = 2 * Math.PI * i / segments;
            float x = cx + r * (float) Math.cos(ang);
            float y = cy + r * (float) Math.sin(ang);
            if (i == 0) c.moveTo(x, y); else c.lineTo(x, y);
        }
        c.fill();
    }

    /** Draw a stroked circle. */
    private static void strokeCircle(PDPageContentStream c, float cx, float cy, float r) throws IOException {
        final int segments = 48;
        for (int i = 0; i <= segments; i++) {
            double ang = 2 * Math.PI * i / segments;
            float x = cx + r * (float) Math.cos(ang);
            float y = cy + r * (float) Math.sin(ang);
            if (i == 0) c.moveTo(x, y); else c.lineTo(x, y);
        }
        c.stroke();
    }

    /** Draw a filled ellipse. */
    private static void fillEllipse(PDPageContentStream c, float cx, float cy, float rx, float ry) throws IOException {
        final int segments = 48;
        for (int i = 0; i <= segments; i++) {
            double ang = 2 * Math.PI * i / segments;
            float x = cx + rx * (float) Math.cos(ang);
            float y = cy + ry * (float) Math.sin(ang);
            if (i == 0) c.moveTo(x, y); else c.lineTo(x, y);
        }
        c.fill();
    }

    /** Wrap text to fit within maxWidth at given font/size. */
    private static List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth)
            throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) return lines;
        String[] words = text.trim().split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String w : words) {
            String trial = current.isEmpty() ? w : current + " " + w;
            float tw = font.getStringWidth(trial) / 1000f * fontSize;
            if (tw > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(w);
            } else {
                current = new StringBuilder(trial);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    /** Draw centered text at a given Y. Returns the font width used. */
    private static float drawCenteredText(PDPageContentStream c, String text, PDType1Font font, float fontSize,
            float cx, float y, float[] rgb) throws IOException {
        c.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
        c.setFont(font, fontSize);
        float w = font.getStringWidth(text) / 1000f * fontSize;
        c.beginText();
        c.newLineAtOffset(cx - w / 2f, y);
        c.showText(text);
        c.endText();
        return w;
    }

    /** Draw centered wrapped text block; returns the Y of the last line rendered. */
    private static float drawCenteredWrapped(PDPageContentStream c, String text, PDType1Font font,
            float fontSize, float[] pos, float[] wrapParams,
            float[] rgb) throws IOException {
        float cx = pos[0];
        float startY = pos[1];
        float maxWidth = wrapParams[0];
        float leading = wrapParams[1];
        List<String> lines = wrapText(text, font, fontSize, maxWidth);
        c.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
        c.setFont(font, fontSize);
        float y = startY;
        for (String line : lines) {
            float lw = font.getStringWidth(line) / 1000f * fontSize;
            c.beginText();
            c.newLineAtOffset(cx - lw / 2f, y);
            c.showText(line);
            c.endText();
            y -= leading;
        }
        return y;
    }

    /** Thin horizontal divider line with optional diamond accent in the center. */
    private static void drawDividerLine(PDPageContentStream c, float x1, float x2, float y,
            float[] rgb, float lineWidth) throws IOException {
        c.setStrokingColor(rgb[0], rgb[1], rgb[2]);
        c.setLineWidth(lineWidth);
        c.moveTo(x1, y);
        c.lineTo(x2, y);
        c.stroke();
    }

    /** Small diamond shape at (cx, cy). */
    private static void fillDiamond(PDPageContentStream c, float cx, float cy, float s,
            float r, float g, float b) throws IOException {
        c.setNonStrokingColor(r, g, b);
        c.moveTo(cx, cy + s);
        c.lineTo(cx + s, cy);
        c.lineTo(cx, cy - s);
        c.lineTo(cx - s, cy);
        c.closePath();
        c.fill();
    }

    /** Verification footer — small centered italic line. */
    private static void drawVerifyFooter(PDPageContentStream c, String code,
            float r, float g, float b, float y) throws IOException {
        c.setNonStrokingColor(r, g, b);
        c.setFont(PDType1Font.HELVETICA_OBLIQUE, 7.5f);
        String line = "Verify at edulearn.com/verify  ·  Code: " + code;
        float w = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(line) / 1000f * 7.5f;
        c.beginText();
        c.newLineAtOffset((PAGE_W - w) / 2f, y);
        c.showText(line);
        c.endText();
    }

    /** Date left + signature line right row. */
    private static void drawDateSignatureRow(PDPageContentStream c,
            float leftX, float rightEndX, float y, float r, float g, float b) throws IOException {
        String date = LocalDate.now().toString();
        c.setNonStrokingColor(r, g, b);
        c.setFont(PDType1Font.HELVETICA, 9);
        c.beginText();
        c.newLineAtOffset(leftX, y);
        c.showText("Date: " + date);
        c.endText();

        float sigStart = leftX + (rightEndX - leftX) * 0.55f;
        c.setStrokingColor(r, g, b);
        c.setLineWidth(0.6f);
        c.moveTo(sigStart, y + 10);
        c.lineTo(rightEndX, y + 10);
        c.stroke();

        c.setFont(PDType1Font.HELVETICA, 8);
        c.setNonStrokingColor(r, g, b);
        String sig = "Authorised Signature";
        float sw = PDType1Font.HELVETICA.getStringWidth(sig) / 1000f * 8;
        c.beginText();
        c.newLineAtOffset(sigStart + (rightEndX - sigStart - sw) / 2f, y - 2);
        c.showText(sig);
        c.endText();
    }

    private static String randomQuote() {
        return MOTIVATION_QUOTES[SECURE_RANDOM.nextInt(MOTIVATION_QUOTES.length)];
    }

    private static String trimToWidth(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEMPLATE 1 — SAGE BOTANICAL BLOOM
    //  Mint/sage background, large ORGANIC AMOEBA blob shapes (bezier curves),
    //  yellow daisy flowers, outline leaf sprigs — matches reference exactly
    // ════════════════════════════════════════════════════════════════════════════

    private void drawSageBotanicalBloom(PDPageContentStream c, String code,
            String courseName, String studentName) throws IOException {

        float cx = PAGE_W / 2f;

        // ── Background ──────────────────────────────────────────────────────────
        c.setNonStrokingColor(0.882f, 0.910f, 0.863f); // soft mint-sage
        c.addRect(0, 0, PAGE_W, PAGE_H);
        c.fill();

        // ── ORGANIC BLOB SHAPES using Bézier curves ─────────────────────────────
        // Each blob is a closed path of curveTo segments — amoeba/kidney shapes
        // like the reference image.

        // Blob 1: top-left large rounded shape (medium sage)
        c.setNonStrokingColor(0.502f, 0.612f, 0.459f);
        c.moveTo(30, PAGE_H - 30);
        c.curveTo(130, PAGE_H + 20, 220, PAGE_H - 60, 200, PAGE_H - 150);
        c.curveTo(185, PAGE_H - 220, 80, PAGE_H - 200, 30, PAGE_H - 140);
        c.curveTo(-20, PAGE_H - 90, -20, PAGE_H - 50, 30, PAGE_H - 30);
        c.fill();

        // Blob 2: top-center-right large (darker sage) — the big dominant top blob
        c.setNonStrokingColor(0.435f, 0.541f, 0.392f);
        c.moveTo(PAGE_W * 0.45f, PAGE_H - 10);
        c.curveTo(PAGE_W * 0.65f, PAGE_H + 30, PAGE_W + 20, PAGE_H - 40, PAGE_W + 10, PAGE_H - 160);
        c.curveTo(PAGE_W, PAGE_H - 260, PAGE_W * 0.78f, PAGE_H - 230, PAGE_W * 0.62f, PAGE_H - 190);
        c.curveTo(PAGE_W * 0.50f, PAGE_H - 160, PAGE_W * 0.38f, PAGE_H - 120, PAGE_W * 0.45f, PAGE_H - 10);
        c.fill();

        // Blob 3: middle-left sweeping diagonal blob (medium-light sage)
        c.setNonStrokingColor(0.541f, 0.651f, 0.502f);
        c.moveTo(-10, PAGE_H * 0.62f);
        c.curveTo(60, PAGE_H * 0.70f, 200, PAGE_H * 0.68f, 260, PAGE_H * 0.58f);
        c.curveTo(320, PAGE_H * 0.48f, 280, PAGE_H * 0.40f, 180, PAGE_H * 0.38f);
        c.curveTo(80, PAGE_H * 0.36f, -20, PAGE_H * 0.44f, -10, PAGE_H * 0.62f);
        c.fill();

        // Blob 4: center-right medium blob
        c.setNonStrokingColor(0.478f, 0.588f, 0.435f);
        c.moveTo(PAGE_W * 0.55f, PAGE_H * 0.55f);
        c.curveTo(PAGE_W * 0.72f, PAGE_H * 0.60f, PAGE_W * 0.92f, PAGE_H * 0.52f, PAGE_W * 0.90f, PAGE_H * 0.40f);
        c.curveTo(PAGE_W * 0.88f, PAGE_H * 0.30f, PAGE_W * 0.68f, PAGE_H * 0.28f, PAGE_W * 0.58f, PAGE_H * 0.36f);
        c.curveTo(PAGE_W * 0.45f, PAGE_H * 0.44f, PAGE_W * 0.42f, PAGE_H * 0.52f, PAGE_W * 0.55f, PAGE_H * 0.55f);
        c.fill();

        // Blob 5: bottom-left large blob
        c.setNonStrokingColor(0.459f, 0.569f, 0.420f);
        c.moveTo(-15, PAGE_H * 0.20f);
        c.curveTo(40, PAGE_H * 0.28f, 150, PAGE_H * 0.26f, 180, PAGE_H * 0.18f);
        c.curveTo(210, PAGE_H * 0.10f, 140, PAGE_H * 0.02f, 60, PAGE_H * 0.04f);
        c.curveTo(-10, PAGE_H * 0.06f, -30, PAGE_H * 0.14f, -15, PAGE_H * 0.20f);
        c.fill();

        // Blob 6: bottom-right large blob
        c.setNonStrokingColor(0.502f, 0.612f, 0.459f);
        c.moveTo(PAGE_W * 0.58f, PAGE_H * 0.18f);
        c.curveTo(PAGE_W * 0.72f, PAGE_H * 0.24f, PAGE_W + 20, PAGE_H * 0.22f, PAGE_W + 10, PAGE_H * 0.10f);
        c.curveTo(PAGE_W, PAGE_H * 0.00f, PAGE_W * 0.75f, PAGE_H * -0.02f, PAGE_W * 0.62f, PAGE_H * 0.02f);
        c.curveTo(PAGE_W * 0.46f, PAGE_H * 0.06f, PAGE_W * 0.44f, PAGE_H * 0.14f, PAGE_W * 0.58f, PAGE_H * 0.18f);
        c.fill();

        // Small accent blob top-center (light sage, partially behind title)
        c.setNonStrokingColor(0.588f, 0.698f, 0.549f);
        c.moveTo(cx + 60, PAGE_H - 20);
        c.curveTo(cx + 110, PAGE_H - 30, cx + 130, PAGE_H - 80, cx + 90, PAGE_H - 110);
        c.curveTo(cx + 50, PAGE_H - 135, cx + 10, PAGE_H - 110, cx + 20, PAGE_H - 75);
        c.curveTo(cx + 30, PAGE_H - 40, cx + 20, PAGE_H - 20, cx + 60, PAGE_H - 20);
        c.fill();

        // ── OUTLINE LEAF SPRIGS ─────────────────────────────────────────────────
        drawDetailedLeafSprig(c, 18, PAGE_H * 0.68f, false);   // left side mid
        drawDetailedLeafSprig(c, PAGE_W - 18, PAGE_H * 0.22f, true); // right side lower
        // Small wildflower sprigs (outline only)
        drawWildflowerSprig(c, 60, PAGE_H * 0.14f);
        drawWildflowerSprig(c, PAGE_W - 50, PAGE_H * 0.58f);

        // ── YELLOW DAISY FLOWERS ────────────────────────────────────────────────
        // Big yellow daisy — left side
        drawDaisy(c, 52, PAGE_H * 0.42f, 22, 0.929f, 0.784f, 0.255f);
        // Medium daisies
        drawDaisy(c, 75, PAGE_H - 62, 15, 0.929f, 0.784f, 0.255f);
        drawDaisy(c, PAGE_W - 52, PAGE_H * 0.30f, 20, 0.929f, 0.784f, 0.255f);
        drawDaisy(c, PAGE_W - 75, PAGE_H - 55, 13, 0.929f, 0.784f, 0.255f);
        // Small gold-orange star flowers (6-petal)
        drawStarFlower(c, 110, PAGE_H - 145, 9, 0.820f, 0.569f, 0.173f);
        drawStarFlower(c, PAGE_W * 0.52f, PAGE_H - 25, 8, 0.820f, 0.569f, 0.173f);
        drawStarFlower(c, PAGE_W - 90, PAGE_H - 165, 8, 0.820f, 0.569f, 0.173f);

        // ── TEXT CONTENT ────────────────────────────────────────────────────────
        drawCenteredText(c, TEXT_CERTIFICATE, PDType1Font.TIMES_BOLD, 36,
                cx, 698, new float[]{0.082f, 0.110f, 0.082f});

        drawCenteredText(c, TEXT_APPRECIATION, PDType1Font.TIMES_ROMAN, 12,
                cx, 668, new float[]{0.180f, 0.210f, 0.180f});

        drawCenteredText(c, "THIS CERTIFICATE IS PROUDLY PRESENTED FOR HONORABLE ACHIEVEMENT TO",
                PDType1Font.HELVETICA, 7.5f, cx, 635, new float[]{0.240f, 0.260f, 0.240f});

        // Student name — dark green italic, large
        drawCenteredText(c, studentName, PDType1Font.TIMES_BOLD_ITALIC, 36,
                cx, 590, new float[]{0.106f, 0.333f, 0.188f});

        // Underline beneath name
        c.setStrokingColor(0.150f, 0.150f, 0.150f);
        c.setLineWidth(0.6f);
        float nameW = PDType1Font.TIMES_BOLD_ITALIC.getStringWidth(studentName) / 1000f * 36;
        float lineHalf = Math.min(nameW / 2f + 10, 180);
        c.moveTo(cx - lineHalf, 576); c.lineTo(cx + lineHalf, 576); c.stroke();

        drawCenteredText(c, "This certificate recognizes the successful completion of",
                PDType1Font.TIMES_ROMAN, 11, cx, 538, new float[]{0.240f, 0.240f, 0.240f});

        drawCenteredText(c, trimToWidth(courseName, 52), PDType1Font.TIMES_BOLD, 18,
                cx, 510, new float[]{0.082f, 0.200f, 0.110f});

        drawCenteredText(c, "EduLearn — " + LocalDate.now().getYear(),
                PDType1Font.TIMES_ROMAN, 11, cx, 478, new float[]{0.300f, 0.300f, 0.300f});

        // Quote
        drawCenteredWrapped(c, "\"" + randomQuote() + "\"",
                PDType1Font.TIMES_ITALIC, 10, new float[]{cx, 370}, new float[]{PAGE_W - 130, 14}, new float[]{0.380f, 0.380f, 0.380f});

        // Date / Signature
        drawDateSignatureRow(c, 72, PAGE_W - 72, 185, 0.200f, 0.200f, 0.200f);

        // Verify
        drawVerifyFooter(c, code, 0.380f, 0.380f, 0.380f, 58);
    }

    /**
     * Draw a detailed outlined leaf sprig (no fill — just strokes).
     * @param flipped if true, mirror horizontally for right-side sprigs
     */
    private static void drawDetailedLeafSprig(PDPageContentStream c,
            float rootX, float rootY, boolean flipped) throws IOException {
        float s = flipped ? -1f : 1f;
        c.setStrokingColor(0.200f, 0.310f, 0.188f);
        c.setLineWidth(0.75f);

        // Main vertical stem
        c.moveTo(rootX, rootY - 80);
        c.curveTo(rootX + s * 8, rootY - 40, rootX + s * 5, rootY - 20, rootX, rootY + 10);
        c.stroke();

        // Leaves branching off the stem — filled outline leaves
        float[][] leaves = {
            { rootX, rootY - 20,  s * 36, 12 },
            { rootX + s*4, rootY - 40, s * 30, -10 },
            { rootX + s*2, rootY - 58, s * 28, 14 },
            { rootX - s*2, rootY - 70, s * 22, -8 },
        };
        for (float[] lf : leaves) {
            float lx = lf[0];
            float ly = lf[1];
            float lw = lf[2];
            float lh = lf[3];
            // Leaf: teardrop shape
            c.moveTo(lx, ly);
            c.curveTo(lx + lw * 0.4f, ly + lh * 1.2f, lx + lw, ly + lh * 0.5f, lx + lw, ly);
            c.curveTo(lx + lw, ly - lh * 0.4f, lx + lw * 0.4f, ly - lh * 0.8f, lx, ly);
            c.stroke();
            // Center vein
            c.moveTo(lx, ly);
            c.lineTo(lx + lw * 0.85f, ly + lh * 0.15f);
            c.stroke();
        }
    }

    /** Tiny wildflower sprig — small stem with bud circles at top. */
    private static void drawWildflowerSprig(PDPageContentStream c, float x, float y) throws IOException {
        c.setStrokingColor(0.220f, 0.330f, 0.200f);
        c.setLineWidth(0.65f);
        // Three stalks
        for (int i = -1; i <= 1; i++) {
            float tx = x + i * 12;
            c.moveTo(x, y);
            c.curveTo(x + i * 6, y + 20, tx + i * 4, y + 36, tx, y + 50);
            c.stroke();
            // Tiny bud at tip
            c.setNonStrokingColor(0.560f, 0.650f, 0.510f);
            fillCircle(c, tx, y + 52, 4);
            c.setStrokingColor(0.220f, 0.330f, 0.200f);
        }
    }

    /**
     * Draw a daisy flower with pointed oval petals and a round center.
     * Much more detailed than the old fillCircle version.
     */
    private static void drawDaisy(PDPageContentStream c, float cx, float cy, float r,
            float pr, float pg, float pb) throws IOException {
        int numPetals = 8;
        c.setNonStrokingColor(pr, pg, pb);
        for (int i = 0; i < numPetals; i++) {
            double a = Math.PI * 2 * i / numPetals;
            float tipX = cx + r * 1.35f * (float) Math.cos(a);
            float tipY = cy + r * 1.35f * (float) Math.sin(a);
            double aL = a - 0.28;
            double aR = a + 0.28;
            float baseLX = cx + r * 0.45f * (float) Math.cos(aL);
            float baseLY = cy + r * 0.45f * (float) Math.sin(aL);
            float baseRX = cx + r * 0.45f * (float) Math.cos(aR);
            float baseRY = cy + r * 0.45f * (float) Math.sin(aR);
            // Petal shape: two curves from base sides to tip
            c.moveTo(baseLX, baseLY);
            c.curveTo(baseLX + (tipX - baseLX) * 0.3f, baseLY + (tipY - baseLY) * 0.3f,
                      tipX - (float)Math.cos(a)*r*0.2f, tipY - (float)Math.sin(a)*r*0.2f,
                      tipX, tipY);
            c.curveTo(tipX - (float)Math.cos(a)*r*0.2f, tipY - (float)Math.sin(a)*r*0.2f,
                      baseRX + (tipX - baseRX) * 0.3f, baseRY + (tipY - baseRY) * 0.3f,
                      baseRX, baseRY);
            c.closePath();
            c.fill();
        }
        // White center ring
        c.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        fillCircle(c, cx, cy, r * 0.42f);
        // Gold center dot
        c.setNonStrokingColor(pr * 0.75f, pg * 0.60f, pb * 0.15f);
        fillCircle(c, cx, cy, r * 0.26f);
        fillCircle(c, cx, cy, r * 0.22f);
    }

    /** 6-pointed star flower (small decorative accent). */
    private static void drawStarFlower(PDPageContentStream c, float cx, float cy, float r,
            float pr, float pg, float pb) throws IOException {
        c.setNonStrokingColor(pr, pg, pb);
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6;
            float px = cx + r * (float) Math.cos(a);
            float py = cy + r * (float) Math.sin(a);
            fillCircle(c, px, py, r * 0.40f);
        }
        c.setNonStrokingColor(pr * 0.65f, pg * 0.55f, pb * 0.10f);
        fillCircle(c, cx, cy, r * 0.30f);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEMPLATE 2 — WATERCOLOR BOTANICAL
    //  Cream/off-white background, colourful botanical corner decorations
    //  Inspired by reference image 2 (floral corners, cream background)
    // ════════════════════════════════════════════════════════════════════════════

    private void drawWatercolorBotanical(PDPageContentStream c, String code,
            String courseName, String studentName) throws IOException {

        // ── Background ──────────────────────────────────────────────────────────
        c.setNonStrokingColor(0.976f, 0.969f, 0.949f); // warm cream
        c.addRect(0, 0, PAGE_W, PAGE_H);
        c.fill();

        // Subtle inner border
        c.setStrokingColor(0.82f, 0.78f, 0.72f);
        c.setLineWidth(0.5f);
        c.addRect(22, 22, PAGE_W - 44, PAGE_H - 44);
        c.stroke();

        // ── Botanical corner clusters ───────────────────────────────────────────
        // Top-left: teal/green leaves
        drawBotanicalCornerTL(c);
        // Top-right: pink/green mix
        drawBotanicalCornerTR(c);
        // Bottom-left: purple/green
        drawBotanicalCornerBL(c);
        // Bottom-right: teal flowers
        drawBotanicalCornerBR(c);

        // ── Text ────────────────────────────────────────────────────────────────
        float cx = PAGE_W / 2f;

        drawCenteredText(c, "Certificate", PDType1Font.TIMES_BOLD_ITALIC, 46,
                cx, 670, new float[]{0.094f, 0.094f, 0.094f});

        drawCenteredText(c, TEXT_APPRECIATION, PDType1Font.HELVETICA, 11,
                cx, 640, new float[]{0.300f, 0.300f, 0.300f});

        drawDividerLine(c, cx - 80, cx + 80, 631, new float[]{0.600f, 0.600f, 0.580f}, 0.5f);

        drawCenteredText(c, "Awarded to", PDType1Font.TIMES_ITALIC, 13,
                cx, 595, new float[]{0.350f, 0.350f, 0.350f});

        drawCenteredText(c, studentName, PDType1Font.TIMES_BOLD, 28,
                cx, 555, new float[]{0.094f, 0.094f, 0.094f});

        drawDividerLine(c, cx - 160, cx + 160, 542, new float[]{0.700f, 0.680f, 0.640f}, 0.5f);

        drawCenteredText(c, "in recognition of", PDType1Font.TIMES_ITALIC, 12,
                cx, 510, new float[]{0.350f, 0.350f, 0.350f});

        drawCenteredText(c, "successful completion of", PDType1Font.TIMES_ROMAN, 11,
                cx, 487, new float[]{0.300f, 0.300f, 0.300f});

        drawCenteredText(c, trimToWidth(courseName, 55), PDType1Font.TIMES_BOLD, 16,
                cx, 460, new float[]{0.180f, 0.380f, 0.320f}); // teal-ish accent

        drawCenteredText(c, "through EduLearn Platform", PDType1Font.TIMES_ROMAN, 11,
                cx, 432, new float[]{0.350f, 0.350f, 0.350f});

        // Quote
        drawCenteredWrapped(c, "\"" + randomQuote() + "\"",
                PDType1Font.TIMES_ITALIC, 10, new float[]{cx, 330}, new float[]{PAGE_W - 150, 14}, new float[]{0.440f, 0.440f, 0.440f});

        // Date / Signature
        drawDateSignatureRow(c, 80, PAGE_W - 80, 168, 0.250f, 0.250f, 0.250f);

        drawVerifyFooter(c, code, 0.450f, 0.450f, 0.450f, 55);
    }

    private static void drawBotanicalCornerTL(PDPageContentStream c) throws IOException {
        // Teal big leaf
        c.setNonStrokingColor(0.376f, 0.588f, 0.502f);
        c.moveTo(10, PAGE_H - 10); c.curveTo(80, PAGE_H - 30, 50, PAGE_H - 80, 20, PAGE_H - 130);
        c.lineTo(10, PAGE_H - 10); c.fill();
        c.setNonStrokingColor(0.282f, 0.510f, 0.431f);
        c.moveTo(25, PAGE_H - 10); c.curveTo(110, PAGE_H - 60, 70, PAGE_H - 110, 10, PAGE_H - 160);
        c.lineTo(25, PAGE_H - 10); c.fill();
        // Small gold-green leaf
        c.setNonStrokingColor(0.596f, 0.651f, 0.310f);
        fillEllipse(c, 70, PAGE_H - 55, 28, 14);
        // Pink accent flower
        c.setNonStrokingColor(0.918f, 0.631f, 0.667f);
        fillCircle(c, 95, PAGE_H - 100, 12);
        c.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        fillCircle(c, 95, PAGE_H - 100, 5);
        // Beige dry sprig
        c.setNonStrokingColor(0.722f, 0.667f, 0.576f);
        fillEllipse(c, 50, PAGE_H - 140, 10, 20);
        fillEllipse(c, 35, PAGE_H - 165, 8, 18);
    }

    private static void drawBotanicalCornerTR(PDPageContentStream c) throws IOException {
        float rx = PAGE_W;
        c.setNonStrokingColor(0.529f, 0.729f, 0.588f);
        c.moveTo(rx, PAGE_H - 10); c.curveTo(rx - 80, PAGE_H - 40, rx - 40, PAGE_H - 100, rx - 10, PAGE_H - 140);
        c.lineTo(rx, PAGE_H - 10); c.fill();
        c.setNonStrokingColor(0.400f, 0.631f, 0.506f);
        c.moveTo(rx - 20, PAGE_H - 10); c.curveTo(rx - 120, PAGE_H - 70, rx - 65, PAGE_H - 130, rx - 12, PAGE_H - 175);
        c.lineTo(rx - 20, PAGE_H - 10); c.fill();
        // beige sprig top right
        c.setNonStrokingColor(0.761f, 0.698f, 0.604f);
        fillEllipse(c, rx - 70, PAGE_H - 50, 12, 22);
        fillEllipse(c, rx - 50, PAGE_H - 88, 8, 18);
        // Pink flower
        c.setNonStrokingColor(0.918f, 0.631f, 0.667f);
        fillCircle(c, rx - 108, PAGE_H - 110, 11);
        c.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        fillCircle(c, rx - 108, PAGE_H - 110, 4);
        // Gold leaf
        c.setNonStrokingColor(0.714f, 0.620f, 0.290f);
        fillEllipse(c, rx - 145, PAGE_H - 65, 22, 11);
    }

    private static void drawBotanicalCornerBL(PDPageContentStream c) throws IOException {
        c.setNonStrokingColor(0.376f, 0.604f, 0.510f);
        c.moveTo(10, 10); c.curveTo(80, 40, 45, 90, 15, 140); c.lineTo(10, 10); c.fill();
        c.setNonStrokingColor(0.282f, 0.510f, 0.420f);
        c.moveTo(25, 10); c.curveTo(110, 65, 72, 120, 8, 170); c.lineTo(25, 10); c.fill();
        // purple sprig
        c.setNonStrokingColor(0.502f, 0.392f, 0.620f);
        fillEllipse(c, 70, 60, 10, 22);
        fillEllipse(c, 55, 88, 8, 18);
        c.setNonStrokingColor(0.400f, 0.310f, 0.510f);
        fillCircle(c, 70, 60, 8);
        // teal flower
        c.setNonStrokingColor(0.310f, 0.549f, 0.580f);
        fillCircle(c, 100, 105, 11);
        c.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        fillCircle(c, 100, 105, 4);
        // gold-green
        c.setNonStrokingColor(0.596f, 0.651f, 0.310f);
        fillEllipse(c, 115, 60, 22, 11);
    }

    private static void drawBotanicalCornerBR(PDPageContentStream c) throws IOException {
        float rx = PAGE_W;
        c.setNonStrokingColor(0.529f, 0.729f, 0.588f);
        c.moveTo(rx, 10); c.curveTo(rx - 80, 40, rx - 42, 95, rx - 12, 145); c.lineTo(rx, 10); c.fill();
        c.setNonStrokingColor(0.376f, 0.620f, 0.498f);
        c.moveTo(rx - 18, 10); c.curveTo(rx - 118, 68, rx - 70, 130, rx - 10, 175); c.lineTo(rx - 18, 10); c.fill();
        // teal flower cluster
        c.setNonStrokingColor(0.310f, 0.549f, 0.620f);
        fillCircle(c, rx - 85, 100, 12); fillCircle(c, rx - 105, 70, 9);
        c.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        fillCircle(c, rx - 85, 100, 5);
        // rust-orange sprig
        c.setNonStrokingColor(0.714f, 0.392f, 0.298f);
        fillEllipse(c, rx - 60, 55, 9, 20); fillEllipse(c, rx - 48, 82, 7, 16);
        // gold leaf
        c.setNonStrokingColor(0.714f, 0.620f, 0.290f);
        fillEllipse(c, rx - 130, 60, 24, 12);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEMPLATE 3 — MIDNIGHT BOTANICAL
    //  Very dark teal/navy background, rich botanical left panel, gold serif text
    //  Inspired by reference image 3 (dark elegant floral)
    // ════════════════════════════════════════════════════════════════════════════

    private void drawMidnightBotanical(PDPageContentStream c, String code,
            String courseName, String studentName) throws IOException {

        // Full dark background
        c.setNonStrokingColor(0.063f, 0.090f, 0.094f); // #101720 near-black teal
        c.addRect(0, 0, PAGE_W, PAGE_H);
        c.fill();

        // Left botanical strip (~38% width)
        float stripW = PAGE_W * 0.38f;
        drawRichBotanicalStrip(c, stripW);

        // Thin gold vertical rule at strip edge
        c.setStrokingColor(0.749f, 0.616f, 0.376f);
        c.setLineWidth(1.2f);
        c.moveTo(stripW, 0); c.lineTo(stripW, PAGE_H); c.stroke();
        c.setStrokingColor(0.380f, 0.310f, 0.192f);
        c.setLineWidth(0.3f);
        c.moveTo(stripW + 2f, 0); c.lineTo(stripW + 2f, PAGE_H); c.stroke();

        // ── Text panel (right side) ──────────────────────────────────────────────
        float textLeft = stripW + 32;
        float contentW  = PAGE_W - textLeft - 28;
        float cx        = textLeft + contentW / 2f;

        // Gold "Certificate"
        drawCenteredText(c, "Certificate", PDType1Font.TIMES_BOLD, 38,
                cx, 700, new float[]{0.839f, 0.698f, 0.424f});

        // Spaced caps subtitle
        drawCenteredText(c, "O F   A P P R E C I A T I O N",
                PDType1Font.TIMES_BOLD, 10, cx, 665, new float[]{0.839f, 0.698f, 0.424f});

        // Horizontal gold rule
        c.setStrokingColor(0.580f, 0.460f, 0.260f);
        c.setLineWidth(0.5f);
        c.moveTo(textLeft, 655); c.lineTo(textLeft + contentW, 655); c.stroke();

        drawCenteredText(c, "Awarded to", PDType1Font.HELVETICA, 9,
                cx, 630, new float[]{0.780f, 0.780f, 0.800f});

        drawCenteredText(c, studentName, PDType1Font.TIMES_BOLD, 24,
                cx, 596, new float[]{0.839f, 0.698f, 0.424f});

        c.setStrokingColor(0.480f, 0.360f, 0.200f);
        c.setLineWidth(0.4f);
        c.moveTo(textLeft + 10, 583); c.lineTo(textLeft + contentW - 10, 583); c.stroke();

        drawCenteredText(c, "in recognition of", PDType1Font.TIMES_ITALIC, 10,
                cx, 558, new float[]{0.720f, 0.720f, 0.740f});

        drawCenteredText(c, "successful completion of", PDType1Font.TIMES_ROMAN, 9,
                cx, 535, new float[]{0.680f, 0.680f, 0.700f});

        drawCenteredText(c, trimToWidth(courseName, 38), PDType1Font.TIMES_BOLD, 14,
                cx, 508, new float[]{0.839f, 0.698f, 0.424f});

        drawCenteredText(c, "and demonstrating excellence through EduLearn.",
                PDType1Font.TIMES_ROMAN, 9, cx, 485, new float[]{0.640f, 0.640f, 0.660f});

        // Quote
        drawCenteredWrapped(c, "\"" + randomQuote() + "\"",
                PDType1Font.TIMES_ITALIC, 9.5f, new float[]{cx, 400}, new float[]{contentW, 13},
                new float[]{0.680f, 0.580f, 0.380f});

        // Date / Signature in gold tones
        drawDateSignatureRow(c, textLeft, textLeft + contentW, 168, 0.600f, 0.500f, 0.300f);

        drawVerifyFooter(c, code, 0.420f, 0.380f, 0.260f, 52);
    }

    /**
     * Rich botanical left strip for midnight template.
     * Multi-layer circles in teal, gold, rust, cream to mimic watercolor leaves & blooms.
     */
    private static void drawRichBotanicalStrip(PDPageContentStream c, float w) throws IOException {
        // Dark strip background
        c.setNonStrokingColor(0.078f, 0.110f, 0.114f);
        c.addRect(0, 0, w, PAGE_H);
        c.fill();

        // Large teal/green leaf masses
        c.setNonStrokingColor(0.196f, 0.376f, 0.345f);
        fillCircle(c, w * 0.20f, PAGE_H * 0.78f, 68);
        c.setNonStrokingColor(0.145f, 0.298f, 0.298f);
        fillCircle(c, w * 0.55f, PAGE_H * 0.72f, 75);
        c.setNonStrokingColor(0.255f, 0.431f, 0.400f);
        fillCircle(c, w * 0.38f, PAGE_H * 0.88f, 50);

        // Gold/amber leaf shapes
        c.setNonStrokingColor(0.588f, 0.482f, 0.318f);
        fillEllipse(c, w * 0.30f, PAGE_H * 0.42f, 42, 28);
        c.setNonStrokingColor(0.698f, 0.580f, 0.345f);
        fillEllipse(c, w * 0.68f, PAGE_H * 0.52f, 30, 20);

        // Rust-brown masses
        c.setNonStrokingColor(0.525f, 0.286f, 0.216f);
        fillCircle(c, w * 0.18f, PAGE_H * 0.55f, 28);
        c.setNonStrokingColor(0.459f, 0.251f, 0.188f);
        fillCircle(c, w * 0.72f, PAGE_H * 0.65f, 22);

        // Cream/white flower highlights
        c.setNonStrokingColor(0.949f, 0.918f, 0.859f);
        fillCircle(c, w * 0.42f, PAGE_H * 0.62f, 18);
        fillCircle(c, w * 0.58f, PAGE_H * 0.35f, 14);
        fillCircle(c, w * 0.25f, PAGE_H * 0.28f, 10);

        // Small teal berry dots
        c.setNonStrokingColor(0.310f, 0.608f, 0.580f);
        fillCircle(c, w * 0.80f, PAGE_H * 0.18f, 6);
        fillCircle(c, w * 0.65f, PAGE_H * 0.12f, 8);
        fillCircle(c, w * 0.45f, PAGE_H * 0.08f, 5);

        // Gold highlight dots
        c.setNonStrokingColor(0.839f, 0.698f, 0.424f);
        fillCircle(c, w * 0.25f, PAGE_H * 0.15f, 9);
        fillCircle(c, w * 0.75f, PAGE_H * 0.28f, 7);

        // Stem lines
        c.setStrokingColor(0.376f, 0.298f, 0.196f);
        c.setLineWidth(0.8f);
        c.moveTo(w * 0.72f, PAGE_H * 0.02f);
        c.curveTo(w * 0.35f, PAGE_H * 0.28f, w * 0.55f, PAGE_H * 0.52f, w * 0.42f, PAGE_H * 0.96f);
        c.stroke();
        c.moveTo(w * 0.22f, PAGE_H * 0.06f);
        c.curveTo(w * 0.48f, PAGE_H * 0.22f, w * 0.18f, PAGE_H * 0.48f, w * 0.52f, PAGE_H * 0.70f);
        c.stroke();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEMPLATE 4 — FORMAL NAVY ORNATE
    //  Light grey/cream, navy ornate floral right side panel, ribbon seal
    //  Inspired by reference image 4 (formal navy botanical)
    // ════════════════════════════════════════════════════════════════════════════

    private void drawFormalNavyOrnate(PDPageContentStream c, String code,
            String courseName, String studentName) throws IOException {

        // ── Background ──────────────────────────────────────────────────────────
        c.setNonStrokingColor(0.949f, 0.945f, 0.937f); // light warm grey
        c.addRect(0, 0, PAGE_W, PAGE_H);
        c.fill();

        // Right ornate panel (~32% width)
        float panelX = PAGE_W * 0.68f;
        float panelW = PAGE_W - panelX;
        c.setNonStrokingColor(0.949f, 0.945f, 0.937f);
        c.addRect(panelX, 0, panelW, PAGE_H);
        c.fill();
        drawNavyOrnatePanel(c, panelX, panelW);

        // Outer double border on the left text area
        c.setStrokingColor(0.110f, 0.165f, 0.275f);
        c.setLineWidth(1.8f);
        c.addRect(18, 18, panelX - 28, PAGE_H - 36);
        c.stroke();
        c.setLineWidth(0.5f);
        c.addRect(26, 26, panelX - 44, PAGE_H - 52);
        c.stroke();

        // ── Text ────────────────────────────────────────────────────────────────
        float cx = (panelX) / 2f;

        drawCenteredText(c, TEXT_CERTIFICATE, PDType1Font.TIMES_BOLD, 34,
                cx, 710, new float[]{0.110f, 0.165f, 0.275f});

        drawCenteredText(c, TEXT_APPRECIATION, PDType1Font.TIMES_BOLD, 14,
                cx, 680, new float[]{0.110f, 0.165f, 0.275f});

        // Decorative rule
        c.setStrokingColor(0.110f, 0.165f, 0.275f);
        c.setLineWidth(0.7f);
        c.moveTo(50, 668); c.lineTo(panelX - 40, 668); c.stroke();
        c.setLineWidth(0.3f);
        c.moveTo(50, 664); c.lineTo(panelX - 40, 664); c.stroke();

        drawCenteredText(c, "We hereby present this certificate to",
                PDType1Font.TIMES_ITALIC, 12, cx, 630, new float[]{0.300f, 0.300f, 0.300f});

        drawCenteredText(c, studentName, PDType1Font.TIMES_BOLD_ITALIC, 30,
                cx, 585, new float[]{0.094f, 0.094f, 0.094f});

        c.setStrokingColor(0.250f, 0.250f, 0.250f);
        c.setLineWidth(0.6f);
        c.moveTo(55, 572); c.lineTo(panelX - 45, 572); c.stroke();

        drawCenteredWrapped(c, "for successful completion of " + trimToWidth(courseName, 50)
                + " and demonstrating great commitment through EduLearn.",
                PDType1Font.TIMES_ROMAN, 12, new float[]{cx, 530}, new float[]{panelX - 110, 16},
                new float[]{0.200f, 0.200f, 0.200f});

        // Quote
        drawCenteredWrapped(c, "\"" + randomQuote() + "\"",
                PDType1Font.TIMES_ITALIC, 10, new float[]{cx, 340}, new float[]{panelX - 110, 14},
                new float[]{0.430f, 0.430f, 0.430f});

        // Date / Signature
        drawDateSignatureRow(c, 50, panelX - 40, 170, 0.200f, 0.200f, 0.200f);

        // Navy seal / ribbon badge at top-right of text area
        drawNavyRibbonSeal(c, panelX - 58, PAGE_H - 58);

        drawVerifyFooter(c, code, 0.420f, 0.420f, 0.420f, 48);
    }

    private static void drawNavyOrnatePanel(PDPageContentStream c, float px, float pw) throws IOException {
        // Dark navy panel base
        c.setNonStrokingColor(0.110f, 0.165f, 0.275f);
        c.addRect(px, 0, pw, PAGE_H);
        c.fill();

        // Ornate floral decoration in lighter navy tones
        c.setNonStrokingColor(0.196f, 0.259f, 0.400f);
        fillCircle(c, px + pw * 0.5f, PAGE_H * 0.75f, 55);
        fillCircle(c, px + pw * 0.2f, PAGE_H * 0.60f, 40);
        fillCircle(c, px + pw * 0.8f, PAGE_H * 0.45f, 38);

        c.setNonStrokingColor(0.165f, 0.231f, 0.365f);
        fillEllipse(c, px + pw * 0.5f, PAGE_H * 0.30f, 50, 65);
        fillEllipse(c, px + pw * 0.3f, PAGE_H * 0.15f, 35, 50);
        fillEllipse(c, px + pw * 0.7f, PAGE_H * 0.85f, 40, 55);

        // Light navy highlight veins/stems
        c.setStrokingColor(0.280f, 0.349f, 0.482f);
        c.setLineWidth(0.7f);
        for (int i = 0; i < 8; i++) {
            float sy = PAGE_H * (0.05f + i * 0.12f);
            c.moveTo(px, sy);
            c.curveTo(px + pw * 0.6f, sy + 30, px + pw * 0.4f, sy + 60, px + pw, sy + 20);
            c.stroke();
        }

        // Cream dots for flower centers
        c.setNonStrokingColor(0.918f, 0.898f, 0.863f);
        fillCircle(c, px + pw * 0.5f, PAGE_H * 0.75f, 8);
        fillCircle(c, px + pw * 0.2f, PAGE_H * 0.60f, 6);
        fillCircle(c, px + pw * 0.5f, PAGE_H * 0.30f, 7);
    }

    private static void drawNavyRibbonSeal(PDPageContentStream c, float cx, float cy) throws IOException {
        // Outer ring
        c.setNonStrokingColor(0.718f, 0.718f, 0.718f);
        fillCircle(c, cx, cy, 30);
        c.setNonStrokingColor(0.949f, 0.945f, 0.937f);
        fillCircle(c, cx, cy, 25);
        c.setNonStrokingColor(0.110f, 0.165f, 0.275f);
        fillCircle(c, cx, cy, 20);

        // Ribbon tails
        c.setNonStrokingColor(0.110f, 0.165f, 0.275f);
        c.moveTo(cx - 8, cy - 20); c.lineTo(cx - 14, cy - 48); c.lineTo(cx, cy - 42); c.closePath(); c.fill();
        c.moveTo(cx + 8, cy - 20); c.lineTo(cx + 14, cy - 48); c.lineTo(cx, cy - 42); c.closePath(); c.fill();

        // Text inside seal
        c.setNonStrokingColor(0.918f, 0.898f, 0.863f);
        c.setFont(PDType1Font.HELVETICA_BOLD, 5);
        c.beginText(); c.newLineAtOffset(cx - 10, cy + 3); c.showText(TEXT_CERTIFICATE); c.endText();
        c.beginText(); c.newLineAtOffset(cx - 5, cy - 5); c.showText(TEXT_EULEARN); c.endText();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEMPLATE 5 — VINTAGE CRIMSON SEAL
    //  Warm ivory parchment, red ornate border, gold corner scrollwork, wax seal
    //  Inspired by reference image 5 (red/gold ornate border)
    // ════════════════════════════════════════════════════════════════════════════

    private void drawVintageCrimsonSeal(PDPageContentStream c, String code,
            String courseName, String studentName) throws IOException {

        // ── Background ──────────────────────────────────────────────────────────
        c.setNonStrokingColor(0.976f, 0.961f, 0.902f); // warm ivory
        c.addRect(0, 0, PAGE_W, PAGE_H);
        c.fill();

        // Ornate border system (crimson + gold)
        drawCrimsonOrnanteBorder(c);

        // ── Text ────────────────────────────────────────────────────────────────
        float cx = PAGE_W / 2f;

        drawCenteredText(c, TEXT_CERTIFICATE, PDType1Font.TIMES_BOLD, 36,
                cx, 700, new float[]{0.224f, 0.063f, 0.063f});

        drawCenteredText(c, "of Recognition", PDType1Font.TIMES_BOLD_ITALIC, 18,
                cx, 668, new float[]{0.380f, 0.380f, 0.380f});

        c.setStrokingColor(0.690f, 0.490f, 0.090f);
        c.setLineWidth(0.8f);
        c.moveTo(cx - 115, 658); c.lineTo(cx + 115, 658); c.stroke();

        drawCenteredText(c, "This Certificate is proudly presented to",
                PDType1Font.TIMES_ITALIC, 12, cx, 625, new float[]{0.400f, 0.400f, 0.400f});

        drawCenteredText(c, studentName, PDType1Font.TIMES_BOLD_ITALIC, 32,
                cx, 580, new float[]{0.094f, 0.094f, 0.094f});

        c.setStrokingColor(0.200f, 0.200f, 0.200f);
        c.setLineWidth(0.5f);
        c.moveTo(cx - 175, 567); c.lineTo(cx + 175, 567); c.stroke();

        drawCenteredWrapped(c,
                "This certificate recognises the successful completion of "
                        + trimToWidth(courseName, 54) + " through EduLearn.",
                PDType1Font.TIMES_ROMAN, 12, new float[]{cx, 525}, new float[]{PAGE_W - 180, 16}, new float[]{0.300f, 0.300f, 0.300f});

        // EduLearn year
        drawCenteredText(c, "EduLearn  ·  " + LocalDate.now().getYear(),
                PDType1Font.TIMES_BOLD, 13, cx, 430, new float[]{0.600f, 0.050f, 0.050f});

        // Quote
        drawCenteredWrapped(c, "\"" + randomQuote() + "\"",
                PDType1Font.TIMES_ITALIC, 10.5f, new float[]{cx, 330}, new float[]{PAGE_W - 180, 14},
                new float[]{0.430f, 0.430f, 0.430f});

        // Wax seal (right-bottom area)
        drawWaxSeal(c, cx + 140, 215);

        // Date / Signature (left + center)
        drawDateSignatureRow(c, 75, cx + 60, 170, 0.200f, 0.200f, 0.200f);

        drawVerifyFooter(c, code, 0.450f, 0.450f, 0.450f, 52);
    }

    private static void drawCrimsonOrnanteBorder(PDPageContentStream c) throws IOException {
        float m1 = 15;
        float m2 = 22;
        float m3 = 30;

        // Crimson outer thick border
        c.setStrokingColor(0.600f, 0.020f, 0.040f);
        c.setLineWidth(3.5f);
        c.addRect(m1, m1, PAGE_W - 2 * m1, PAGE_H - 2 * m1);
        c.stroke();

        // Gold mid border
        c.setStrokingColor(0.720f, 0.520f, 0.100f);
        c.setLineWidth(1.0f);
        c.addRect(m2, m2, PAGE_W - 2 * m2, PAGE_H - 2 * m2);
        c.stroke();

        // Thin inner crimson
        c.setStrokingColor(0.700f, 0.020f, 0.040f);
        c.setLineWidth(0.5f);
        c.addRect(m3, m3, PAGE_W - 2 * m3, PAGE_H - 2 * m3);
        c.stroke();

        // Corner ornaments (gold scroll-like)
        drawScrollCorner(c, m3 + 4, PAGE_H - m3 - 4, 1, 1);    // TL
        drawScrollCorner(c, PAGE_W - m3 - 4, PAGE_H - m3 - 4, -1, 1);  // TR
        drawScrollCorner(c, m3 + 4, m3 + 4, 1, -1);             // BL
        drawScrollCorner(c, PAGE_W - m3 - 4, m3 + 4, -1, -1);  // BR
    }

    private static void drawScrollCorner(PDPageContentStream c,
            float x, float y, float sx, float sy) throws IOException {
        c.setStrokingColor(0.700f, 0.510f, 0.110f);
        c.setNonStrokingColor(0.700f, 0.510f, 0.110f);
        c.setLineWidth(1.1f);
        // L-bracket
        c.moveTo(x, y); c.lineTo(x + sx * 32, y); c.stroke();
        c.moveTo(x, y); c.lineTo(x, y - sy * 32); c.stroke();
        // Small decorative circles at corner
        fillCircle(c, x + sx * 10, y - sy * 10, 3);
        fillCircle(c, x + sx * 22, y, 2);
        fillCircle(c, x, y - sy * 22, 2);
    }

    private static void drawWaxSeal(PDPageContentStream c, float cx, float cy) throws IOException {
        // Outer starburst rays
        c.setNonStrokingColor(0.690f, 0.490f, 0.090f);
        for (int i = 0; i < 16; i++) {
            double a1 = Math.PI * 2 * i / 16;
            double a2 = Math.PI * 2 * (i + 0.5) / 16;
            float x1o = cx + 38 * (float) Math.cos(a1);
            float y1o = cy + 38 * (float) Math.sin(a1);
            float x2o = cx + 32 * (float) Math.cos(a2);
            float y2o = cy + 32 * (float) Math.sin(a2);
            float x3o = cx + 38 * (float) Math.cos(a2 + Math.PI * 2 / 16);
            float y3o = cy + 38 * (float) Math.sin(a2 + Math.PI * 2 / 16);
            c.moveTo(cx, cy);
            c.lineTo(x1o, y1o);
            c.lineTo(x2o, y2o);
            c.lineTo(x3o, y3o);
            c.closePath();
            c.fill();
        }
        // Main wax circle
        c.setNonStrokingColor(0.720f, 0.090f, 0.090f);
        fillCircle(c, cx, cy, 28);
        // Inner ring
        c.setNonStrokingColor(0.800f, 0.130f, 0.130f);
        fillCircle(c, cx, cy, 22);
        // Laurel wreath lines (simplified)
        c.setStrokingColor(0.900f, 0.720f, 0.400f);
        c.setLineWidth(0.5f);
        strokeCircle(c, cx, cy, 18);
        // Center text
        c.setNonStrokingColor(0.976f, 0.860f, 0.500f);
        c.setFont(PDType1Font.HELVETICA_BOLD, 7);
        c.beginText(); c.newLineAtOffset(cx - 10, cy + 3); c.showText("BEST"); c.endText();
        c.beginText(); c.newLineAtOffset(cx - 12, cy - 6); c.showText("AWARD"); c.endText();
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEMPLATE 6 — TEAL GUILLOCHE AWARD
    //  Sage/teal background, guilloche wave-pattern border, "Best Award" medallion
    //  Inspired by reference image 6 (teal guilloche engraved border)
    // ════════════════════════════════════════════════════════════════════════════

    private void drawTealGuillocheAward(PDPageContentStream c, String code,
            String courseName, String studentName) throws IOException {

        // ── Background ──────────────────────────────────────────────────────────
        c.setNonStrokingColor(0.863f, 0.882f, 0.820f); // sage-cream
        c.addRect(0, 0, PAGE_W, PAGE_H);
        c.fill();

        // Guilloche outer band (teal)
        drawGuillocheOrnamentBand(c, 0.165f, 0.337f, 0.306f);

        // ── Text ────────────────────────────────────────────────────────────────
        float cx = PAGE_W / 2f;

        drawCenteredText(c, TEXT_CERTIFICATE, PDType1Font.TIMES_BOLD, 36,
                cx, 700, new float[]{0.118f, 0.251f, 0.227f});

        drawCenteredText(c, "OF ACHIEVEMENT", PDType1Font.TIMES_BOLD, 14,
                cx, 668, new float[]{0.165f, 0.310f, 0.282f});

        // Decorative divider with diamond
        float lx1 = cx - 175;
        float lx2 = cx - 18;
        float rx1 = cx + 18;
        float rx2 = cx + 175;
        c.setStrokingColor(0.165f, 0.310f, 0.282f);
        c.setLineWidth(0.8f);
        c.moveTo(lx1, 657); c.lineTo(lx2, 657); c.stroke();
        c.moveTo(rx1, 657); c.lineTo(rx2, 657); c.stroke();
        fillDiamond(c, cx, 657, 6, 0.165f, 0.310f, 0.282f);

        drawCenteredText(c, studentName, PDType1Font.TIMES_BOLD_ITALIC, 30,
                cx, 614, new float[]{0.094f, 0.094f, 0.094f});

        // Underline with diamond decoration
        c.setStrokingColor(0.165f, 0.310f, 0.282f);
        c.setLineWidth(0.7f);
        c.moveTo(cx - 160, 600); c.lineTo(cx - 14, 600); c.stroke();
        c.moveTo(cx + 14, 600); c.lineTo(cx + 160, 600); c.stroke();
        fillDiamond(c, cx, 600, 5, 0.165f, 0.310f, 0.282f);

        drawCenteredWrapped(c,
                "For successful completion of " + trimToWidth(courseName, 55)
                        + " through EduLearn, demonstrating outstanding commitment and excellence.",
                PDType1Font.TIMES_ROMAN, 11, new float[]{cx, 558}, new float[]{PAGE_W - 160, 16}, new float[]{0.180f, 0.180f, 0.180f});

        // Best Award medallion (center-bottom of text area)
        drawGuillocheAwardMedallion(c, cx, 365);

        // Date / Signature
        drawDateSignatureRow(c, 75, cx - 30, 170, 0.200f, 0.200f, 0.200f);
        // Right-side signature too
        c.setStrokingColor(0.200f, 0.200f, 0.200f);
        c.setLineWidth(0.6f);
        c.moveTo(cx + 40, 180); c.lineTo(PAGE_W - 75, 180); c.stroke();
        c.setNonStrokingColor(0.200f, 0.200f, 0.200f);
        c.setFont(PDType1Font.HELVETICA, 8);
        String sig = "Authorised Signature";
        float sw = PDType1Font.HELVETICA.getStringWidth(sig) / 1000f * 8;
        c.beginText(); c.newLineAtOffset(cx + 40 + ((PAGE_W - 75 - cx - 40) - sw) / 2f, 168);
        c.showText(sig); c.endText();

        drawVerifyFooter(c, code, 0.350f, 0.380f, 0.320f, 52);
    }

    /**
     * Draws a simplified guilloche ornament band around the page border.
     * Uses repeating sinusoidal wave strokes in the border band area.
     */
    private static void drawGuillocheOrnamentBand(PDPageContentStream c,
            float r, float g, float b) throws IOException {
        float bandW = 38f;

        drawGuillocheBase(c, r, g, b, bandW);
        drawGuillocheStripes(c, r, g, b, bandW);
        drawGuillocheCorners(c, bandW);
        drawGuillocheWaveLines(c, r, g, b, bandW);

        // Inner thin border lines
        c.setStrokingColor(r, g, b);
        c.setLineWidth(0.7f);
        float inn = bandW + 10;
        c.addRect(inn, inn, PAGE_W - 2 * inn, PAGE_H - 2 * inn);
        c.stroke();
        c.setLineWidth(0.3f);
        float inn2 = inn + 6;
        c.addRect(inn2, inn2, PAGE_W - 2 * inn2, PAGE_H - 2 * inn2);
        c.stroke();
    }

    private static void drawGuillocheBase(PDPageContentStream c, float r, float g, float b, float bandW) throws IOException {
        c.setNonStrokingColor(r, g, b);
        c.addRect(0, 0, PAGE_W, bandW);              // bottom
        c.fill();
        c.addRect(0, PAGE_H - bandW, PAGE_W, bandW); // top
        c.fill();
        c.addRect(0, 0, bandW, PAGE_H);              // left
        c.fill();
        c.addRect(PAGE_W - bandW, 0, bandW, PAGE_H); // right
        c.fill();
    }

    private static void drawGuillocheStripes(PDPageContentStream c, float r, float g, float b, float bandW) throws IOException {
        c.setNonStrokingColor(r * 1.18f, g * 1.18f, b * 1.15f);
        float m2 = bandW + 4;
        c.addRect(m2, m2, PAGE_W - 2 * m2, 3);
        c.fill();
        c.addRect(m2, PAGE_H - m2 - 3, PAGE_W - 2 * m2, 3);
        c.fill();
        c.addRect(m2, m2, 3, PAGE_H - 2 * m2);
        c.fill();
        c.addRect(PAGE_W - m2 - 3, m2, 3, PAGE_H - 2 * m2);
        c.fill();
    }

    private static void drawGuillocheCorners(PDPageContentStream c, float bandW) throws IOException {
        float cm = bandW / 2f;
        drawRosetteCorner(c, cm, cm, 0.863f, 0.882f, 0.820f);
        drawRosetteCorner(c, PAGE_W - cm, cm, 0.863f, 0.882f, 0.820f);
        drawRosetteCorner(c, cm, PAGE_H - cm, 0.863f, 0.882f, 0.820f);
        drawRosetteCorner(c, PAGE_W - cm, PAGE_H - cm, 0.863f, 0.882f, 0.820f);
    }

    private static void drawGuillocheWaveLines(PDPageContentStream c, float r, float g, float b, float bandW) throws IOException {
        c.setStrokingColor(r * 0.75f, g * 0.75f, b * 0.75f);
        c.setLineWidth(0.4f);
        float waveAmp = 5f;
        float waveLen = 18f;

        drawHorizontalGuillocheWaves(c, bandW, waveAmp, waveLen);
        drawVerticalGuillocheWaves(c, bandW, waveAmp, waveLen);
    }

    private static void drawHorizontalGuillocheWaves(PDPageContentStream c, float bandW, float waveAmp, float waveLen) throws IOException {
        for (int pass = 0; pass < 2; pass++) {
            float baseY = (pass == 0) ? 12f : PAGE_H - 12f;
            float startX = bandW;
            boolean first = true;
            for (float x = startX; x <= PAGE_W - bandW; x += 2f) {
                float y = baseY + waveAmp * (float) Math.sin(2 * Math.PI * x / waveLen + pass * Math.PI);
                if (first) {
                    c.moveTo(x, y);
                    first = false;
                } else {
                    c.lineTo(x, y);
                }
            }
            c.stroke();
        }
    }

    private static void drawVerticalGuillocheWaves(PDPageContentStream c, float bandW, float waveAmp, float waveLen) throws IOException {
        for (int pass = 0; pass < 2; pass++) {
            float baseX = (pass == 0) ? 12f : PAGE_W - 12f;
            float startY = bandW;
            boolean first = true;
            for (float y = startY; y <= PAGE_H - bandW; y += 2f) {
                float x = baseX + waveAmp * (float) Math.sin(2 * Math.PI * y / waveLen + pass * Math.PI);
                if (first) {
                    c.moveTo(x, y);
                    first = false;
                } else {
                    c.lineTo(x, y);
                }
            }
            c.stroke();
        }
    }

    private static void drawRosetteCorner(PDPageContentStream c, float cx, float cy,
            float r, float g, float b) throws IOException {
        c.setNonStrokingColor(r, g, b);
        fillCircle(c, cx, cy, 9);
        c.setNonStrokingColor(r * 0.75f, g * 0.75f, b * 0.75f);
        fillCircle(c, cx, cy, 5);
        c.setNonStrokingColor(r, g, b);
        fillCircle(c, cx, cy, 3);
    }

    private static void drawGuillocheAwardMedallion(PDPageContentStream c, float cx, float cy) throws IOException {
        // Outer starburst
        c.setNonStrokingColor(0.165f, 0.310f, 0.282f);
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2 * i / 12;
            float x1 = cx + 30 * (float) Math.cos(a);
            float y1 = cy + 30 * (float) Math.sin(a);
            double a2 = a + Math.PI / 12;
            float x2 = cx + 25 * (float) Math.cos(a2);
            float y2 = cy + 25 * (float) Math.sin(a2);
            double a3 = a + Math.PI * 2 / 12;
            float x3 = cx + 30 * (float) Math.cos(a3);
            float y3 = cy + 30 * (float) Math.sin(a3);
            c.moveTo(cx, cy); c.lineTo(x1, y1); c.lineTo(x2, y2); c.lineTo(x3, y3); c.closePath(); c.fill();
        }
        // Inner circle
        c.setNonStrokingColor(0.863f, 0.882f, 0.820f);
        fillCircle(c, cx, cy, 22);
        c.setNonStrokingColor(0.165f, 0.310f, 0.282f);
        fillCircle(c, cx, cy, 18);

        // Text
        c.setNonStrokingColor(0.863f, 0.882f, 0.820f);
        c.setFont(PDType1Font.HELVETICA_BOLD, 6);
        c.beginText(); c.newLineAtOffset(cx - 8, cy + 7); c.showText("* * *"); c.endText();
        c.beginText(); c.newLineAtOffset(cx - 9, cy + 1); c.showText("BEST"); c.endText();
        c.beginText(); c.newLineAtOffset(cx - 11, cy - 6); c.showText("AWARD"); c.endText();
        c.beginText(); c.newLineAtOffset(cx - 8, cy - 12); c.showText("* * *"); c.endText();
    }
}