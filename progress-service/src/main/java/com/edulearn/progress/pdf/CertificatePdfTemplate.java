package com.edulearn.progress.pdf;

import java.security.SecureRandom;

/**
 * Visual templates for course completion PDF certificates.
 * One variant is chosen at random when a certificate is first issued.
 * All templates render vertical (portrait) A4 pages.
 */
public enum CertificatePdfTemplate {

    /** Sage green with organic botanical blob shapes and yellow flower accents — matches reference 1 */
    SAGE_BOTANICAL_BLOOM,

    /** Cream background with watercolor botanical border in teal, pink, and gold — matches reference 2 */
    WATERCOLOR_BOTANICAL,

    /** Midnight dark background with colorful botanical left panel in gold/teal/rust — matches reference 3 */
    MIDNIGHT_BOTANICAL,

    /** Light grey with navy ornate floral side panel, ribbon seal — matches reference 4 */
    FORMAL_NAVY_ORNATE,

    /** Ivory parchment with red ornate border and wax seal — matches reference 5 */
    VINTAGE_CRIMSON_SEAL,

    /** Teal-sage with guilloche wave pattern border, best award medallion — matches reference 6 */
    TEAL_GUILLOCHE_AWARD;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static CertificatePdfTemplate random() {
        CertificatePdfTemplate[] values = values();
        return values[SECURE_RANDOM.nextInt(values.length)];
    }
}