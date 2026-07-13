package com.lysanderuy.tulogs.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.lysanderuy.tulogs.R

/**
 * TuLogs type tokens.
 *
 * Two families used with restraint, plus one supporting mono:
 *  - Outfit            → the one thing you're meant to actually read at a glance
 *                         (headline status text, big numbers). Rounded, upright,
 *                         no thin hairlines — chosen specifically to stay legible
 *                         to someone half-asleep.
 *  - Plus Jakarta Sans  → body/UI text, nav labels.
 *  - IBM Plex Mono      → small supporting labels only (date stamp, BEDTIME /
 *                         WAKE / QUALITY labels). Never used for full sentences.
 *
 * Fonts are wired via the Downloadable Fonts API (Google Play Services),
 * so no .ttf files need to be bundled into the APK and no extra runtime
 * permission is required — consistent with the app's minimal-permissions
 * principle.
 *
 * SETUP REQUIRED (one-time):
 * 1. Add the dependency:
 *      implementation("androidx.compose.ui:ui-text-google-fonts:<version matching your Compose BOM>")
 * 2. Add res/values/font_certs.xml containing the standard
 *    `com_google_android_gms_fonts_certs` array — copy this verbatim from
 *    Android's official Downloadable Fonts guide, don't hand-type it:
 *    https://developer.android.com/develop/ui/compose/text/fonts#downloadable-fonts
 */

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private fun googleFont(name: String, weight: FontWeight) = Font(
    googleFont = GoogleFont(name),
    fontProvider = fontProvider,
    weight = weight
)

val OutfitFamily = FontFamily(
    googleFont("Outfit", FontWeight.Normal),
    googleFont("Outfit", FontWeight.Medium),
    googleFont("Outfit", FontWeight.SemiBold)
)

val JakartaFamily = FontFamily(
    googleFont("Plus Jakarta Sans", FontWeight.Normal),
    googleFont("Plus Jakarta Sans", FontWeight.Medium),
    googleFont("Plus Jakarta Sans", FontWeight.SemiBold)
)

val MonoFamily = FontFamily(
    googleFont("IBM Plex Mono", FontWeight.Medium)
)

/**
 * Named text styles used directly by screens — mirrors the CSS approach
 * (design tokens as named constants) rather than overriding Material's
 * default type scale, since most of the app's UI is custom-styled.
 */
object TuLogsType {

    /** BEDTIME / WAKE / QUALITY / date-stamp labels — always short, never a sentence. */
    val monoLabel = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.1.em,
        color = Mist600
    )

    /** The one big glanceable line on Home — "Tapped in at 11:02 PM." */
    val statusHeadline = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.01).em,
        color = Mist200
    )

    /** Bold word inside statusHeadline (the actual time/fact) — apply Paper50 + SemiBold via SpanStyle. */
    val statusHeadlineEmphasisWeight = FontWeight.SemiBold

    val statusSub = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Mist600
    )

    /** Bedtime / Wake / Quality numbers in the last-night stat row. */
    val factValue = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = (-0.01).em,
        color = Paper50
    )

    val captionText = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        color = Mist600
    )

    val navLabel = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
        color = Mist600
    )
}
