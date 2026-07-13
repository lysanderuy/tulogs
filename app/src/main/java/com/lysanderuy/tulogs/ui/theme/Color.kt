package com.lysanderuy.tulogs.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * TuLogs color tokens.
 *
 * Single dark "night sky" theme — the app is mostly used in dark rooms,
 * so there is deliberately no light variant for MVP.
 *
 * Naming mirrors the CSS custom properties used in the HTML design
 * iterations, so this file should stay in sync with any future
 * tulogs_tokens.css if one gets generated.
 */

// ---- ink / night (backgrounds & surfaces) ----
val Ink950 = Color(0xFF0A0E17) // app background
val Ink900 = Color(0xFF111726) // raised surface
val Ink800 = Color(0xFF1B2233) // card / row background
val Ink700 = Color(0xFF2A3348) // borders, dividers, unfilled marks

// ---- text ----
val Paper50 = Color(0xFFF6F3EC) // primary text (warm white, not pure white)
val Mist200 = Color(0xFFC7CDDC) // secondary text
val Mist400 = Color(0xFF98A0B5) // muted text
val Mist600 = Color(0xFF6B7288) // faint labels, disabled/pending state

// ---- accents ----
val Amber500 = Color(0xFFE4A855) // tap / confirmed / primary accent
val Amber300 = Color(0xFFF0C687) // amber, lighter tint
val Periwinkle400 = Color(0xFF8991E0) // secondary / "night" accent

// ---- semantic ----
val Success500 = Color(0xFF6FBF8B)
val Warning500 = Color(0xFFE0A63E)
val Error500 = Color(0xFFD9695F)
