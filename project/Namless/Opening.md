# Variable Opening / Opening (β-Reduktion)
## Variable Opening
- Ein fresh atom ist ein unique neu generierter name meistens in Form einer Zahl (hier String)
- Ersetzen des äußersten binders mit einem fresh atom d.h. eine gebundene variable zu einer freien machen

## Opening (β-Reduktion)
- Anstelle des normalen Opening wird hier kein neues atom generiert sondern direkt mit der Expr ersetzt
### Bsp.
ARG ist hier die Expr die als Argument bei der Application gegeben wird
`λ => λ => λ => 2 + 1 + 0` => `λ => λ => ARG + 1 + 0`
Mann sieht, dass erfolgreich um einen λ-Term reduziert wurde.