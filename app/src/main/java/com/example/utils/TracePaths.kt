package com.example.utils

data class GuidePoint(val x: Float, val y: Float, val strokeIndex: Int, val isStartOfSegment: Boolean = false)

data class TraceCharacter(
    val id: String,
    val isLetter: Boolean,
    val displayName: String,
    val strokes: List<List<GuidePoint>>,
    val phonemeSoundName: String // E.g., "Aaaa", "Bee", "Uno", "Dos"
)

fun interpolateStroke(points: List<GuidePoint>, segmentsCount: Int = 12): List<GuidePoint> {
    if (points.size < 3) return points

    val result = mutableListOf<GuidePoint>()
    val n = points.size
    val strokeIdx = points[0].strokeIndex

    for (i in 0 until n - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        
        // Catmull-Rom control points
        val p0 = if (i > 0) points[i - 1] else p1
        val p3 = if (i < n - 2) points[i + 2] else p2

        for (j in 0 until segmentsCount) {
            val t = j.toFloat() / segmentsCount
            val t2 = t * t
            val t3 = t2 * t

            val x = 0.5f * (
                (2f * p1.x) +
                (-p0.x + p2.x) * t +
                (2f * p0.x - 5f * p1.x + 4f * p2.x - p3.x) * t2 +
                (-p0.x + 3f * p1.x - 3f * p2.x + p3.x) * t3
            )

            val y = 0.5f * (
                (2f * p1.y) +
                (-p0.y + p2.y) * t +
                (2f * p0.y - 5f * p1.y + 4f * p2.y - p3.y) * t2 +
                (-p0.y + 3f * p1.y - 3f * p2.y + p3.y) * t3
            )

            result.add(GuidePoint(x, y, strokeIdx, isStartOfSegment = (i == 0 && j == 0)))
        }
    }
    // Add the very last point to close the spline precisely
    val last = points.last()
    result.add(GuidePoint(last.x, last.y, strokeIdx, isStartOfSegment = false))
    return result
}

fun smoothCharacter(char: TraceCharacter): TraceCharacter {
    val smoothedStrokes = char.strokes.map { strokePoints ->
        interpolateStroke(strokePoints, segmentsCount = 12)
    }
    return char.copy(strokes = smoothedStrokes)
}

object TracePaths {
    private val rawCharacters: Map<String, TraceCharacter> = mapOf(
        // Numbers
        "1" to TraceCharacter("1", false, "1", listOf(
            listOf(
                GuidePoint(0.35f, 0.25f, 0),
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.50f, 0.85f, 0)
            )
        ), "Uno"),
        
        "2" to TraceCharacter("2", false, "2", listOf(
            listOf(
                GuidePoint(0.30f, 0.30f, 0),
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.70f, 0.30f, 0),
                GuidePoint(0.55f, 0.55f, 0),
                GuidePoint(0.30f, 0.85f, 0),
                GuidePoint(0.70f, 0.85f, 0)
            )
        ), "Dos"),

        "3" to TraceCharacter("3", false, "3", listOf(
            listOf(
                GuidePoint(0.30f, 0.25f, 0),
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.70f, 0.28f, 0),
                GuidePoint(0.50f, 0.48f, 0),
                GuidePoint(0.70f, 0.68f, 0),
                GuidePoint(0.48f, 0.85f, 0),
                GuidePoint(0.30f, 0.75f, 0)
            )
        ), "Tres"),

        "4" to TraceCharacter("4", false, "4", listOf(
            listOf(
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.20f, 0.55f, 0),
                GuidePoint(0.75f, 0.55f, 0)
            ),
            listOf(
                GuidePoint(0.55f, 0.15f, 1),
                GuidePoint(0.55f, 0.85f, 1)
            )
        ), "Cuatro"),

        "5" to TraceCharacter("5", false, "5", listOf(
            listOf(
                GuidePoint(0.68f, 0.20f, 0),
                GuidePoint(0.35f, 0.20f, 0),
                GuidePoint(0.35f, 0.48f, 0),
                GuidePoint(0.68f, 0.55f, 0),
                GuidePoint(0.60f, 0.82f, 0),
                GuidePoint(0.32f, 0.78f, 0)
            )
        ), "Cinco"),

        "6" to TraceCharacter("6", false, "6", listOf(
            listOf(
                GuidePoint(0.60f, 0.18f, 0),
                GuidePoint(0.32f, 0.42f, 0),
                GuidePoint(0.32f, 0.72f, 0),
                GuidePoint(0.52f, 0.85f, 0),
                GuidePoint(0.68f, 0.70f, 0),
                GuidePoint(0.50f, 0.52f, 0),
                GuidePoint(0.32f, 0.62f, 0)
            )
        ), "Seis"),

        "7" to TraceCharacter("7", false, "7", listOf(
            listOf(
                GuidePoint(0.28f, 0.20f, 0),
                GuidePoint(0.72f, 0.20f, 0),
                GuidePoint(0.42f, 0.85f, 0)
            )
        ), "Siete"),

        "8" to TraceCharacter("8", false, "8", listOf(
            listOf(
                GuidePoint(0.50f, 0.48f, 0),
                GuidePoint(0.32f, 0.32f, 0),
                GuidePoint(0.50f, 0.16f, 0),
                GuidePoint(0.68f, 0.32f, 0),
                GuidePoint(0.50f, 0.48f, 0),
                GuidePoint(0.32f, 0.68f, 0),
                GuidePoint(0.50f, 0.84f, 0),
                GuidePoint(0.68f, 0.68f, 0),
                GuidePoint(0.50f, 0.48f, 0)
            )
        ), "Ocho"),

        "9" to TraceCharacter("9", false, "9", listOf(
            listOf(
                GuidePoint(0.50f, 0.48f, 0),
                GuidePoint(0.32f, 0.32f, 0),
                GuidePoint(0.50f, 0.16f, 0),
                GuidePoint(0.68f, 0.32f, 0),
                GuidePoint(0.50f, 0.48f, 0),
                GuidePoint(0.50f, 0.85f, 0)
            )
        ), "Nueve"),

        "0" to TraceCharacter("0", false, "0", listOf(
            listOf(
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.28f, 0.50f, 0),
                GuidePoint(0.50f, 0.85f, 0),
                GuidePoint(0.72f, 0.50f, 0),
                GuidePoint(0.50f, 0.15f, 0)
            )
        ), "Cero"),

        // Letters
        "A" to TraceCharacter("A", true, "A", listOf(
            listOf(
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.25f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.50f, 0.15f, 1),
                GuidePoint(0.75f, 0.85f, 1)
            ),
            listOf(
                GuidePoint(0.35f, 0.55f, 2),
                GuidePoint(0.65f, 0.55f, 2)
            )
        ), "Aaa"),

        "B" to TraceCharacter("B", true, "B", listOf(
            listOf(
                GuidePoint(0.30f, 0.15f, 0),
                GuidePoint(0.30f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.30f, 0.15f, 1),
                GuidePoint(0.60f, 0.25f, 1),
                GuidePoint(0.50f, 0.48f, 1),
                GuidePoint(0.30f, 0.48f, 1)
            ),
            listOf(
                GuidePoint(0.30f, 0.48f, 2),
                GuidePoint(0.65f, 0.60f, 2),
                GuidePoint(0.55f, 0.85f, 2),
                GuidePoint(0.30f, 0.85f, 2)
            )
        ), "Bee"),

        "C" to TraceCharacter("C", true, "C", listOf(
            listOf(
                GuidePoint(0.68f, 0.28f, 0),
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.30f, 0.50f, 0),
                GuidePoint(0.50f, 0.85f, 0),
                GuidePoint(0.68f, 0.72f, 0)
            )
        ), "Cee"),

        "D" to TraceCharacter("D", true, "D", listOf(
            listOf(
                GuidePoint(0.30f, 0.15f, 0),
                GuidePoint(0.30f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.30f, 0.15f, 1),
                GuidePoint(0.68f, 0.30f, 1),
                GuidePoint(0.68f, 0.70f, 1),
                GuidePoint(0.30f, 0.85f, 1)
            )
        ), "Dee"),

        "E" to TraceCharacter("E", true, "E", listOf(
            listOf(
                GuidePoint(0.30f, 0.15f, 0),
                GuidePoint(0.30f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.30f, 0.15f, 1),
                GuidePoint(0.65f, 0.15f, 1)
            ),
            listOf(
                GuidePoint(0.30f, 0.50f, 2),
                GuidePoint(0.55f, 0.50f, 2)
            ),
            listOf(
                GuidePoint(0.30f, 0.85f, 3),
                GuidePoint(0.65f, 0.85f, 3)
            )
        ), "Eee"),

        "F" to TraceCharacter("F", true, "F", listOf(
            listOf(
                GuidePoint(0.30f, 0.15f, 0),
                GuidePoint(0.30f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.30f, 0.15f, 1),
                GuidePoint(0.65f, 0.15f, 1)
            ),
            listOf(
                GuidePoint(0.30f, 0.50f, 2),
                GuidePoint(0.55f, 0.50f, 2)
            )
        ), "Efe"),

        "G" to TraceCharacter("G", true, "G", listOf(
            listOf(
                GuidePoint(0.68f, 0.28f, 0),
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.30f, 0.50f, 0),
                GuidePoint(0.50f, 0.85f, 0),
                GuidePoint(0.68f, 0.70f, 0),
                GuidePoint(0.68f, 0.52f, 0),
                GuidePoint(0.55f, 0.52f, 0)
            )
        ), "Gee"),

        "H" to TraceCharacter("H", true, "H", listOf(
            listOf(
                GuidePoint(0.30f, 0.15f, 0),
                GuidePoint(0.30f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.70f, 0.15f, 1),
                GuidePoint(0.70f, 0.85f, 1)
            ),
            listOf(
                GuidePoint(0.30f, 0.50f, 2),
                GuidePoint(0.70f, 0.50f, 2)
            )
        ), "Hache"),

        "I" to TraceCharacter("I", true, "I", listOf(
            listOf(
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.50f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.35f, 0.15f, 1),
                GuidePoint(0.65f, 0.15f, 1)
            ),
            listOf(
                GuidePoint(0.35f, 0.85f, 2),
                GuidePoint(0.65f, 0.85f, 2)
            )
        ), "Iii"),

        "J" to TraceCharacter("J", true, "J", listOf(
            listOf(
                GuidePoint(0.35f, 0.15f, 0),
                GuidePoint(0.65f, 0.15f, 0)
            ),
            listOf(
                GuidePoint(0.55f, 0.15f, 1),
                GuidePoint(0.55f, 0.70f, 1),
                GuidePoint(0.45f, 0.85f, 1),
                GuidePoint(0.30f, 0.75f, 1)
            )
        ), "Jota"),

        "K" to TraceCharacter("K", true, "K", listOf(
            listOf(
                GuidePoint(0.30f, 0.15f, 0),
                GuidePoint(0.30f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.65f, 0.20f, 1),
                GuidePoint(0.32f, 0.50f, 1)
            ),
            listOf(
                GuidePoint(0.32f, 0.50f, 2),
                GuidePoint(0.68f, 0.82f, 2)
            )
        ), "Ka"),

        "L" to TraceCharacter("L", true, "L", listOf(
            listOf(
                GuidePoint(0.35f, 0.15f, 0),
                GuidePoint(0.35f, 0.82f, 0),
                GuidePoint(0.65f, 0.82f, 0)
            )
        ), "Ele"),

        "M" to TraceCharacter("M", true, "M", listOf(
            listOf(
                GuidePoint(0.25f, 0.85f, 0),
                GuidePoint(0.25f, 0.15f, 0),
                GuidePoint(0.50f, 0.50f, 0),
                GuidePoint(0.75f, 0.15f, 0),
                GuidePoint(0.75f, 0.85f, 0)
            )
        ), "Eme"),

        "N" to TraceCharacter("N", true, "N", listOf(
            listOf(
                GuidePoint(0.28f, 0.85f, 0),
                GuidePoint(0.28f, 0.15f, 0),
                GuidePoint(0.72f, 0.85f, 0),
                GuidePoint(0.72f, 0.15f, 0)
            )
        ), "Ene"),

        "O" to TraceCharacter("O", true, "O", listOf(
            listOf(
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.25f, 0.50f, 0),
                GuidePoint(0.50f, 0.85f, 0),
                GuidePoint(0.75f, 0.50f, 0),
                GuidePoint(0.50f, 0.15f, 0)
            )
        ), "Ooo"),

        "P" to TraceCharacter("P", true, "P", listOf(
            listOf(
                GuidePoint(0.30f, 0.15f, 0),
                GuidePoint(0.30f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.30f, 0.15f, 1),
                GuidePoint(0.65f, 0.28f, 1),
                GuidePoint(0.50f, 0.50f, 1),
                GuidePoint(0.30f, 0.50f, 1)
            )
        ), "Pee"),

        "Q" to TraceCharacter("Q", true, "Q", listOf(
            listOf(
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.25f, 0.48f, 0),
                GuidePoint(0.50f, 0.80f, 0),
                GuidePoint(0.75f, 0.48f, 0),
                GuidePoint(0.50f, 0.15f, 0)
            ),
            listOf(
                GuidePoint(0.55f, 0.65f, 1),
                GuidePoint(0.72f, 0.82f, 1)
            )
        ), "Cuu"),

        "R" to TraceCharacter("R", true, "R", listOf(
            listOf(
                GuidePoint(0.30f, 0.15f, 0),
                GuidePoint(0.30f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.30f, 0.15f, 1),
                GuidePoint(0.65f, 0.28f, 1),
                GuidePoint(0.50f, 0.50f, 1),
                GuidePoint(0.30f, 0.50f, 1)
            ),
            listOf(
                GuidePoint(0.42f, 0.50f, 2),
                GuidePoint(0.68f, 0.85f, 2)
            )
        ), "Erre"),

        "S" to TraceCharacter("S", true, "S", listOf(
            listOf(
                GuidePoint(0.68f, 0.28f, 0),
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.30f, 0.32f, 0),
                GuidePoint(0.50f, 0.50f, 0),
                GuidePoint(0.68f, 0.68f, 0),
                GuidePoint(0.50f, 0.85f, 0),
                GuidePoint(0.30f, 0.72f, 0)
            )
        ), "Ese"),

        "T" to TraceCharacter("T", true, "T", listOf(
            listOf(
                GuidePoint(0.50f, 0.15f, 0),
                GuidePoint(0.50f, 0.85f, 0)
            ),
            listOf(
                GuidePoint(0.25f, 0.15f, 1),
                GuidePoint(0.75f, 0.15f, 1)
            )
        ), "Tee"),

        "U" to TraceCharacter("U", true, "U", listOf(
            listOf(
                GuidePoint(0.30f, 0.15f, 0),
                GuidePoint(0.30f, 0.65f, 0),
                GuidePoint(0.50f, 0.85f, 0),
                GuidePoint(0.70f, 0.65f, 0),
                GuidePoint(0.70f, 0.15f, 0)
            )
        ), "Uuu"),

        "V" to TraceCharacter("V", true, "V", listOf(
            listOf(
                GuidePoint(0.25f, 0.15f, 0),
                GuidePoint(0.50f, 0.85f, 0),
                GuidePoint(0.75f, 0.15f, 0)
            )
        ), "Uve"),

        "W" to TraceCharacter("W", true, "W", listOf(
            listOf(
                GuidePoint(0.22f, 0.15f, 0),
                GuidePoint(0.38f, 0.85f, 0),
                GuidePoint(0.50f, 0.45f, 0),
                GuidePoint(0.62f, 0.85f, 0),
                GuidePoint(0.78f, 0.15f, 0)
            )
        ), "Uve Doble"),

        "X" to TraceCharacter("X", true, "X", listOf(
            listOf(
                GuidePoint(0.25f, 0.18f, 0),
                GuidePoint(0.75f, 0.82f, 0)
            ),
            listOf(
                GuidePoint(0.75f, 0.18f, 1),
                GuidePoint(0.25f, 0.82f, 1)
            )
        ), "Equis"),

        "Y" to TraceCharacter("Y", true, "Y", listOf(
            listOf(
                GuidePoint(0.25f, 0.15f, 0),
                GuidePoint(0.50f, 0.50f, 0)
            ),
            listOf(
                GuidePoint(0.75f, 0.15f, 1),
                GuidePoint(0.50f, 0.50f, 1)
            ),
            listOf(
                GuidePoint(0.50f, 0.50f, 2),
                GuidePoint(0.50f, 0.85f, 2)
            )
        ), "Iega"),

        "Z" to TraceCharacter("Z", true, "Z", listOf(
            listOf(
                GuidePoint(0.28f, 0.20f, 0),
                GuidePoint(0.72f, 0.20f, 0),
                GuidePoint(0.28f, 0.80f, 0),
                GuidePoint(0.72f, 0.80f, 0)
            )
        ), "Zeta")
    )

    val characters: Map<String, TraceCharacter> = rawCharacters.mapValues { (_, char) ->
        smoothCharacter(char)
    }

    fun getAlphabet(): List<TraceCharacter> = characters.values.filter { it.isLetter }.sortedBy { it.id }
    fun getNumbers(): List<TraceCharacter> = characters.values.filter { !it.isLetter }.sortedBy { if (it.id == "0") 10 else it.id.toInt() }
}
