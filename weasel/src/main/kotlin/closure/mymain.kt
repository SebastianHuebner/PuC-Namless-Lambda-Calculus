package closure

import kotlinx.collections.immutable.persistentMapOf


/*
* let x = let z = 10 in z in
* x
* //expected 10
* */

fun <T> T.print(){
    println(this)
}

fun <T> test(expr: T): T? {
    return when (expr) {
        is NExpr -> try {
            eval(persistentMapOf(), expr) as T
        } catch (e: Exception) {
            println(e.message); null
        }
        is NExprWithNames -> try {
            eval(persistentMapOf(), expr) as T
        } catch (e: Exception) {
            println(e.message); null
        }
        else -> throw Exception()
    }
}

fun main() {
    val proc = """
        let x = (\x => \z => z - x) in
        let y = (\x => \y => y - x) in
        1 == 1
    """.trimIndent()
    val expr = Parser(Lexer(proc)).parseExpr()
    println(expr)
    testEval(proc)
    val nExpr = translate(persistentMapOf(), expr)
    println(nExpr)
    test(nExpr)?.print()
    val nExprWithName = translateToNExprWithNames(persistentMapOf(), expr)
    println(nExprWithName)
    test(nExprWithName)?.print()
}