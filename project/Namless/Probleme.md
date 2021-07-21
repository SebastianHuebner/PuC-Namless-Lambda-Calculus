# Probleme
# Translate to namless expression
```kotlin
private val lambdaMap = mutableMapOf<String, Int>()
private val letMap = mutableMapOf<String, Int>()

private fun _translate(expr: Expr): NExpr =
    when (expr) {
        is Expr.Application -> NExpr.Application(_translate(expr.func), _translate(expr.arg))
        is Expr.Binary -> NExpr.Binary(expr.operator, _translate(expr.x), _translate(expr.y))
        is Expr.Boolean -> NExpr.Boolean(expr.b)
        is Expr.If -> NExpr.If(
            _translate(expr.condition),
            _translate(expr.thenBranch),
            _translate(expr.elseBranch)
        )
        is Expr.Lambda -> {
            lambdaMap.filter { it.key != expr.binder }.keys.forEach { lambdaMap[it] = lambdaMap[it]!! + 1 }
            lambdaMap[expr.binder] = 0
            NExpr.Lambda(_translate(expr.body))
        }
        is Expr.Let -> {
            letMap[expr.binder] = 0
            letMap.filter { it.key != expr.binder }.keys.forEach { letMap[it] = letMap[it]!! + 1 }
            val translatedExpr = _translate(expr.expr)
            lambdaMap.clear()
            lambdaMap.putAll(letMap)
            val translatedBody = _translate(expr.body)
            lambdaMap.putAll(letMap)
            NExpr.Let(translatedExpr, translatedBody)
        }
        is Expr.Number -> NExpr.Number(expr.n)
        is Expr.Var -> NExpr.Var(lambdaMap[expr.name] ?: ((lambdaMap.values.maxOrNull() ?: 0) + 1))
    }

```

```
//For evaluation  
private var currentDepth = 0  
private val argStack = mutableListOf<Value>()  
  
private fun _eval(expr: NExpr): Value =  
    when (expr) {  
 is NExpr.Application -> {  
 val evaledFunc = _eval(expr.func)  
 val evaledArg = _eval(expr.arg)  
 if (evaledFunc is Value.NamelessClosure) {  
 currentDepth += 1  
 argStack.add(evaledArg)  
 _eval(evaledFunc.body)  
 } else throw Exception("$evaledFunc is not a function")  
 } is NExpr.Binary -> when (expr.operator) {  
 Operator.Equals -> equalsValue(_eval(expr.x), _eval(expr.y))  
 Operator.Plus -> evalBinaryNumber(_eval(expr.x), _eval(expr.y)) { x, y -> x + y }  
 Operator.Minus -> evalBinaryNumber(_eval(expr.x), _eval(expr.y)) { x, y -> x - y }  
 Operator.Multiply -> evalBinaryNumber(_eval(expr.x), _eval(expr.y)) { x, y -> x * y }  
 } is NExpr.Boolean -> Value.Boolean(expr.b)  
 is NExpr.If -> {  
 val cond = _eval(expr.condition) as? Value.Boolean ?: throw Exception("Not a boolean")  
 if (cond.b) _eval(expr.thenBranch)  
 else _eval(expr.elseBranch)  
 } is NExpr.Lambda -> {  
 Value.NamelessClosure(expr.body)  
 } is NExpr.Let -> {  
 currentDepth += 1  
 val evaledExpr = _eval(expr.expr)  
 argStack.add(evaledExpr)  
 _eval(expr.body)  
 } is NExpr.Number -> Value.Number(expr.n)  
 is NExpr.Var -> {  
 val i = currentDepth - expr.i - 1  
 if (i in argStack.indices) argStack[i]  
 else throw Exception("Variable not defined")  
 } }****
```