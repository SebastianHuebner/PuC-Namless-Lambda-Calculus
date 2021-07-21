package closure

import kotlinx.collections.immutable.PersistentMap

sealed class Expr {
    data class Var(val name: String) : Expr()
    data class Lambda(val binder: String, val body: Expr) : Expr()
    data class Application(val func: Expr, val arg: Expr) : Expr()
    data class Number(val n: Int) : Expr()
    data class Boolean(val b: kotlin.Boolean) : Expr()
    data class Binary(val operator: Operator, val x: Expr, val y: Expr) : Expr()
    data class If(val condition: Expr, val thenBranch: Expr, val elseBranch: Expr) : Expr()
    data class Let(val binder: String, val expr: Expr, val body: Expr) : Expr()
}

enum class Operator {
    Equals, Plus, Minus, Multiply
}

sealed class NExpr {
    data class Var(val i: Status) : NExpr()
    data class Lambda(val body: NExpr) : NExpr()
    data class Closure(val scope: PersistentMap<Int, NExpr>, val body: NExpr) : NExpr()
    data class Application(val func: NExpr, val arg: NExpr) : NExpr()
    data class Number(val n: Int) : NExpr()
    data class Boolean(val b: kotlin.Boolean) : NExpr()
    data class Binary(val operator: Operator, val x: NExpr, val y: NExpr) : NExpr()
    data class If(val condition: NExpr, val thenBranch: NExpr, val elseBranch: NExpr) : NExpr()
    data class Let(val expr: NExpr, val body: NExpr) : NExpr()
}

sealed class NExprWithNames {
    data class Var(val i: Status, val name: String) : NExprWithNames()
    data class Lambda(val body: NExprWithNames, val name: String) : NExprWithNames()
    data class Closure(val scope: PersistentMap<Int, NExprWithNames>, val body: NExprWithNames) : NExprWithNames()
    data class Application(val func: NExprWithNames, val arg: NExprWithNames) : NExprWithNames()
    data class Number(val n: Int) : NExprWithNames()
    data class Boolean(val b: kotlin.Boolean) : NExprWithNames()
    data class Binary(val operator: Operator, val x: NExprWithNames, val y: NExprWithNames) : NExprWithNames()
    data class If(val condition: NExprWithNames, val thenBranch: NExprWithNames, val elseBranch: NExprWithNames) : NExprWithNames()
    data class Let(val expr: NExprWithNames, val body: NExprWithNames, val name: String) : NExprWithNames()
}

sealed class Status {
    data class Bound(val i: Int) : Status()
    data class Free(val name: String) : Status()
}