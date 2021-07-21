package closure

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap


fun translate(scope: PersistentMap<String, Int>, expr: Expr): NExpr =
    when (expr) {
        is Expr.Application -> {
            val translatedFunc = translate(scope, expr.func)
            val translatedArg = translate(scope, expr.arg)
            NExpr.Application(translatedFunc, translatedArg)
        }
        is Expr.Binary -> {
            val translatedX = translate(scope, expr.x)
            val translatedY = translate(scope, expr.y)
            NExpr.Binary(expr.operator, translatedX, translatedY)
        }
        is Expr.Boolean -> NExpr.Boolean(expr.b)
        is Expr.If -> NExpr.If(
            translate(scope, expr.condition),
            translate(scope, expr.thenBranch),
            translate(scope, expr.elseBranch)
        )
        is Expr.Lambda -> {
            val newScope = scope.mapValues { it.value + 1 }.toPersistentMap().put(expr.binder, 0)
            val translatedBody = translate(newScope, expr.body)
            NExpr.Lambda(translatedBody)
        }
        is Expr.Let -> {
            val newScope = scope.mapValues { it.value + 1 }.toPersistentMap().put(expr.binder, 0)
            val translatedExpr = translate(scope, expr.expr)
            val translatedBody = translate(newScope, expr.body)
            NExpr.Let(translatedExpr, translatedBody)
        }
        is Expr.Number -> NExpr.Number(expr.n)
        is Expr.Var -> NExpr.Var(scope[expr.name]?.let { Status.Bound(it) } ?: Status.Free(expr.name))
    }

fun eval(scope: PersistentMap<Int, NExpr>, expr: NExpr): NExpr =
    when (expr) {
        is NExpr.Application -> {
            val evaledArg = eval(scope, expr.arg)
            val evaledFun = eval(scope, expr.func)
            if (evaledFun is NExpr.Closure)
                eval(evaledFun.scope.mapKeys { it.key + 1 }.toPersistentMap().put(0, evaledArg), evaledFun.body)
            else throw Exception("$evaledFun is not a function")
        }
        is NExpr.Binary -> when (expr.operator) {
            Operator.Equals -> equals(eval(scope, expr.x), eval(scope, expr.y))
            Operator.Plus -> evalBinaryExpr(eval(scope, expr.x), eval(scope, expr.y)) { a, b -> a + b }
            Operator.Minus -> evalBinaryExpr(eval(scope, expr.x), eval(scope, expr.y)) { a, b -> a - b }
            Operator.Multiply -> evalBinaryExpr(eval(scope, expr.x), eval(scope, expr.y)) { a, b -> a * b }
        }
        is NExpr.Boolean -> expr
        is NExpr.If -> {
            val evaledCond = eval(scope, expr.condition) as? NExpr.Boolean ?: throw Exception("")
            if (evaledCond.b) eval(scope, expr.thenBranch)
            else eval(scope, expr.elseBranch)
        }
        is NExpr.Lambda -> NExpr.Closure(scope, expr.body)
        is NExpr.Closure -> eval(expr.scope, expr.body)
        is NExpr.Let -> {
            val evaledExpr = eval(scope, expr.expr)
            val newScope = scope.mapKeys { it.key + 1 }.toPersistentMap().put(0, evaledExpr)
            eval(newScope, expr.body)
        }
        is NExpr.Number -> expr
        is NExpr.Var -> {
            val i = when (expr.i) {
                is Status.Bound -> expr.i.i
                is Status.Free -> throw Exception("Variable ${expr.i.name} not defined")
            }
            //TODO: Find a case where this throws a exception
            scope[i] ?: throw Exception("Variable not defined")
        }
    }

private fun equals(x: NExpr, y: NExpr): NExpr {
    val a = x as? NExpr.Number ?: throw Exception("Can't compare $x, it's not a number")
    val b = x as? NExpr.Number ?: throw Exception("Can't compare $y, it's not a number")
    return NExpr.Boolean(a.n == b.n)
}

private fun evalBinaryExpr(x: NExpr, y: NExpr, z: (Int, Int) -> Int): NExpr {
    val a = x as? NExpr.Number ?: throw Exception("Can't use a binary operation on $x, it's not a number")
    val b = y as? NExpr.Number ?: throw Exception("Can't use a binary operation on $y, it's not a number")
    return NExpr.Number(z(a.n, b.n))
}