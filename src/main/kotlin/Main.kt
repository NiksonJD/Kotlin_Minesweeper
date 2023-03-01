package minesweeper

fun input(prompt: String) = println(prompt).run { readln().trim() }
fun im(prompt: String) = input(prompt).split(" ").run { Triple(this[0].toInt().dec(), this[1].toInt().dec(), this[2]) }
val mineField = MutableList(9) { MutableList(9) { "." } }
val revealedField = Array(9) { Array(9) { false } }
val ms = mutableSetOf<Pair<Int, Int>>()
val items = mutableSetOf<Pair<Int, Int>>()

fun MutableList<MutableList<String>>.printField(flag: Int) {
    println(" │123456789│").also { println("—│—————————│") }
    mineField.forEachIndexed { y, row ->
        print("${y + 1}|")
        when (flag) {
            1 -> row.forEachIndexed { x, s -> print(if (ms.contains(Pair(x, y))) "*" else s.replace("X", ".")) }
            2 -> row.forEach { print(it) }
            else -> row.forEach { _ -> print(".") }
        }
        println("|")
    }
    println("—│—————————│")
}

fun MutableList<MutableList<String>>.drawField() {
    for (y in mineField.indices) {
        for (x in mineField[y].indices) {
            if (mineField[y][x] == ".") {
                val mineCount = listOf(
                    y - 1 to x - 1, y - 1 to x, y - 1 to x + 1, y to x - 1, y to x + 1,
                    y + 1 to x - 1, y + 1 to x, y + 1 to x + 1
                )
                    .count { (x, y) -> x in mineField.indices && y in mineField[x].indices && mineField[x][y] == "X" }
                mineField[y][x] = if (mineCount > 0) mineCount.toString() else "."
            } else if (mineField[y][x] == "X") items.add(Pair(x, y))
        }
    }
}

fun MutableList<MutableList<String>>.markField(x: Int, y: Int) {
    val markObject = ms.find { it == Pair(x, y) }
    if (markObject == null) {
        if (mineField[y][x] == "X" || mineField[y][x] == "." || mineField[y][x] == "/") ms.add(Pair(x, y))
    } else {
        ms.remove(markObject).also { if (mineField[y][x] != "X") mineField[y][x] = "." }
    }
}

fun MutableList<MutableList<String>>.freeMove(x: Int, y: Int): Int {
    when (mineField[y][x]) {
        "X" -> return 2
        "." -> revealEmptyCells(y, x, mineField, revealedField).also { afterReveal() }
    }
    return 1
}

fun revealEmptyCells(y: Int, x: Int, mField: MutableList<MutableList<String>>, rField: Array<Array<Boolean>>) {
    if (y < 0 || x < 0 || y >= mField.size || x >= mField[0].size || rField[y][x] || mField[y][x] == "X") return
    rField[y][x] = true.also { ms.find { it == Pair(x, y) }?.let { ms.remove(it) } }
    if (mField[y][x] == ".") {
        revealEmptyCells(y - 1, x, mField, rField); revealEmptyCells(y + 1, x, mField, rField)
        revealEmptyCells(y - 1, x - 1, mField, rField); revealEmptyCells(y - 1, x + 1, mField, rField)
        revealEmptyCells(y, x + 1, mField, rField); revealEmptyCells(y, x - 1, mField, rField)
        revealEmptyCells(y + 1, x - 1, mField, rField); revealEmptyCells(y + 1, x + 1, mField, rField)
    }
}

fun afterReveal() {
    revealedField.forEachIndexed { t, row ->
        row.forEachIndexed { r, isR -> if (isR) if (mineField[t][r] == ".") mineField[t][r] = "/" }
    }
    mineField.printField(1)
}

fun makePlayerHappy(x: Int, y: Int, mc: Int) {
    val indexes = (-1..1).flatMap { ol -> (-1..1).map { il -> (y + ol) * 9 + (x + il) } }
        .filter { it in 0..80 }.toSet()
    mineField.flatten().indices.minus(indexes).shuffled().take(mc).forEach { i -> mineField[i / 9][i % 9] = "X" }
    mineField.drawField()
    revealEmptyCells(y, x, mineField, revealedField).also { afterReveal() }
}

fun startGame() {
    val mc = input("How many mines do you want on the field?").toInt().also { mineField.printField(0) }
    while (true) {
        val (x, y, z) = im("Set/unset mines marks or claim a cell as free:")
        when (z) {
            "mine" -> mineField.markField(x, y).also { mineField.printField(1) }
            "free" -> if (items.isEmpty()) makePlayerHappy(x, y, mc) else mineField.printField(mineField.freeMove(x, y))
            else -> continue
        }
        if (ms == items) {
            println("Congratulations! You found all the mines!")
            break
        } else if (z == "free" && items.contains(Pair(x, y))) {
            println("You stepped on a mine and failed!")
            break
        }
    }
}

fun main() = startGame()