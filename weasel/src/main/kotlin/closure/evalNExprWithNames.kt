package closure

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap

fun translateToNExprWithNames(scope: PersistentMap<String, Int>, expr: Expr): NExprWithNames =
    when (expr) {
        is Expr.Application -> {
            val translatedFunc = translateToNExprWithNames(scope, expr.func)
            val translatedArg = translateToNExprWithNames(scope, expr.arg)
            NExprWithNames.Application(translatedFunc, translatedArg)
        }
        is Expr.Binary -> {
            val translatedX = translateToNExprWithNames(scope, expr.x)
            val translatedY = translateToNExprWithNames(scope, expr.y)
            NExprWithNames.Binary(expr.operator, translatedX, translatedY)
        }
        is Expr.Boolean -> NExprWithNames.Boolean(expr.b)
        is Expr.If -> NExprWithNames.If(
            translateToNExprWithNames(scope, expr.condition),
            translateToNExprWithNames(scope, expr.thenBranch),
            translateToNExprWithNames(scope, expr.elseBranch)
        )
        is Expr.Lambda -> {
            val newScope = scope.mapValues { it.value + 1 }.toPersistentMap().put(expr.binder, 0)
            val translatedBody = translateToNExprWithNames(newScope, expr.body)
            NExprWithNames.Lambda(translatedBody, expr.binder)
        }
        is Expr.Let -> {
            val newScope = scope.mapValues { it.value + 1 }.toPersistentMap().put(expr.binder, 0)
            val translatedExpr = translateToNExprWithNames(scope, expr.expr)
            val translatedBody = translateToNExprWithNames(newScope, expr.body)
            NExprWithNames.Let(translatedExpr, translatedBody, expr.binder)
        }
        is Expr.Number -> NExprWithNames.Number(expr.n)
        is Expr.Var -> NExprWithNames.Var(scope[expr.name]?.let { Status.Bound(it) } ?: Status.Free(expr.name),
            expr.name)
    }


fun eval(scope: PersistentMap<Int, NExprWithNames>, expr: NExprWithNames): NExprWithNames =
    when (expr) {
        is NExprWithNames.Application -> {
            val evaledArg = eval(scope, expr.arg)
            val evaledFun = eval(scope, expr.func)
            if (evaledFun is NExprWithNames.Closure)
                eval(evaledFun.scope.mapKeys { it.key + 1 }.toPersistentMap().put(0, evaledArg), evaledFun.body)
            else throw Exception("$evaledFun is not a function")
        }
        is NExprWithNames.Binary -> when (expr.operator) {
            Operator.Equals -> equals(eval(scope, expr.x), eval(scope, expr.y))
            Operator.Plus -> evalBinaryExpr(eval(scope, expr.x), eval(scope, expr.y)) { a, b -> a + b }
            Operator.Minus -> evalBinaryExpr(eval(scope, expr.x), eval(scope, expr.y)) { a, b -> a - b }
            Operator.Multiply -> evalBinaryExpr(eval(scope, expr.x), eval(scope, expr.y)) { a, b -> a * b }
        }
        is NExprWithNames.Boolean -> expr
        is NExprWithNames.If -> {
            val evaledCond = eval(scope, expr.condition) as? NExprWithNames.Boolean ?: throw Exception("")
            if (evaledCond.b) eval(scope, expr.thenBranch)
            else eval(scope, expr.elseBranch)
        }
        is NExprWithNames.Lambda -> NExprWithNames.Closure(scope, expr.body)
        is NExprWithNames.Closure -> eval(expr.scope, expr.body)
        is NExprWithNames.Let -> {
            val evaledExpr = eval(scope, expr.expr)
            val newScope = scope.mapKeys { it.key + 1 }.toPersistentMap().put(0, evaledExpr)
            eval(newScope, expr.body)
        }
        is NExprWithNames.Number -> expr
        is NExprWithNames.Var -> {
            val i = when (expr.i) {
                is Status.Bound -> expr.i.i
                is Status.Free -> throw Exception("Variable ${expr.i.name} not defined")
            }
            scope[i] ?: throw Exception("Variable ${expr.name} not defined")
        }
    }

private fun equals(x: NExprWithNames, y: NExprWithNames): NExprWithNames {
    val a = x as? NExprWithNames.Number ?: throw Exception("Can't compare $x, it's not a number")
    val b = x as? NExprWithNames.Number ?: throw Exception("Can't compare $y, it's not a number")
    return NExprWithNames.Boolean(a.n == b.n)
}

private fun evalBinaryExpr(x: NExprWithNames, y: NExprWithNames, z: (Int, Int) -> Int): NExprWithNames {
    val a = x as? NExprWithNames.Number ?: throw Exception("Can't use a binary operation on $x, it's not a number")
    val b = y as? NExprWithNames.Number ?: throw Exception("Can't use a binary operation on $y, it's not a number")
    return NExprWithNames.Number(z(a.n, b.n))
}