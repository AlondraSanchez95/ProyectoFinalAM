package com.example.proyectofinal.utils

private val monthNames = listOf(
    "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
    "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"
)

private val bimesters = listOf("ENE-FEB", "MAR-ABR", "MAY-JUN", "JUL-AGO", "SEP-OCT", "NOV-DIC")
private val trimesters = listOf("ENE-MAR", "ABR-JUN", "JUL-SEP", "OCT-DIC")
private val semesters = listOf("ENE-JUN", "JUL-DIC")

fun monthNumberFromDate(date: String): Int? {
    val match = Regex("""^\s*(\d{1,2})[/-](\d{1,2})[/-](\d{4})\s*$""").matchEntire(date)
        ?: Regex("""^\s*(\d{4})-(\d{1,2})-(\d{1,2}).*$""").matchEntire(date)
    val month = match?.groupValues?.get(2)?.toIntOrNull()
    return month?.takeIf { it in 1..12 }
}

fun monthAbbreviation(monthNumber: Int): String? = monthNames.getOrNull(monthNumber - 1)

fun yearFromDate(date: String): String? {
    val yearFirst = Regex("""^\s*(\d{4})[-/]\d{1,2}[-/]\d{1,2}""").find(date)?.groupValues?.get(1)
    if (yearFirst != null) return yearFirst
    return Regex("""^\s*\d{1,2}[-/]\d{1,2}[-/](\d{4})\s*$""")
        .find(date)
        ?.groupValues
        ?.get(1)
}

fun periodForMonth(monthNumber: Int, groupSize: Int): String? {
    if (monthNumber !in 1..12) return null
    val periods = when (groupSize) {
        1 -> monthNames
        2 -> bimesters
        3 -> trimesters
        6 -> semesters
        else -> return null
    }
    return periods.getOrNull((monthNumber - 1) / groupSize)
}

fun monthNumbersForPeriod(period: String): Set<Int> {
    val normalized = period.uppercase()
    when (normalized) {
        "ENE-FEB" -> return setOf(1, 2)
        "MAR-ABR" -> return setOf(3, 4)
        "MAY-JUN" -> return setOf(5, 6)
        "JUL-AGO" -> return setOf(7, 8)
        "SEP-OCT" -> return setOf(9, 10)
        "NOV-DIC" -> return setOf(11, 12)
        "ENE-MAR" -> return setOf(1, 2, 3)
        "ABR-JUN" -> return setOf(4, 5, 6)
        "JUL-SEP" -> return setOf(7, 8, 9)
        "OCT-DIC" -> return setOf(10, 11, 12)
        "ENE-JUN" -> return (1..6).toSet()
        "JUL-DIC" -> return (7..12).toSet()
    }
    val listedMonths = normalized.split("-")
        .mapNotNull { month -> monthNames.indexOf(month).takeIf { it >= 0 }?.plus(1) }
        .toSet()
    if (listedMonths.isNotEmpty()) return listedMonths

    return emptySet()
}

fun belongsToPeriod(date: String, fallbackPeriod: String, selectedPeriod: String): Boolean {
    if (selectedPeriod == "TODOS") return true
    val monthNumber = monthNumberFromDate(date)
    val selectedMonths = monthNumbersForPeriod(selectedPeriod)
    return if (monthNumber != null) {
        monthNumber in selectedMonths
    } else {
        monthNumbersForPeriod(fallbackPeriod).any { it in selectedMonths }
    }
}
