package closure

import kotlinx.collections.immutable.persistentMapOf


/*
* let x = let z = 10 in z in
* x
* //expected 10
* */

fun <T> T.print() {
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
        let plus10 = \x => x + 10 in
        let minus5 = \x => x - 5 in
        let value =  plus10 1 in
        let complexValue = 
            let temp = 5 in
            plus10 (minus5 value) - temp
        in
        complexValue
    """.trimIndent()
    println("Der eingegebene Code lautet:\n$proc")
    val expr = Parser(Lexer(proc)).parseExpr()
    println("-----------------\nVorlesung:")
    println("Die Expr vom Parser: $expr")
    print("Ergebnis: ")
    testEval(proc)
    println("-----------------\nLocally Nameless:")
    val nExpr = translate(persistentMapOf(), expr)
    println("Die übersetzte Expr: $nExpr")
    print("Ergebnis: ")
    test(nExpr)?.print()
    /*val nExprWithName = translateToNExprWithNames(persistentMapOf(), expr)
    println(nExprWithName)
    test(nExprWithName)?.print()*/
}