# Experimento: régimen del WXYZ-Wing canónico Type 2 (¿es un Sue de Coq?)

Fecha: 2026-07-21 · Milestone: 1.4 · Herramienta: `test/harness/T2RegimeExperiment.java`

## Pregunta

El mapa relacional del dueño (§6-1) recoge la disputa de SudokuWiki: "el WXYZ-Wing
Type 2 es exactamente lo mismo que Sue de Coq". La estrategia de taxonomía
(`docs/estrategia-taxonomia.md` §1-2) predice lo contrario: un T2 tiene por
definición una Z **no restringida** → régimen R1 → ALS-XZ **simple** (un solo RCC),
no doblemente enlazado (que es el régimen R0 = territorio SDC). El §8-4 del mapa
pedía el umbral empírico: ¿hay eliminaciones de T2 imposibles de replicar sin
estructura locked?

## Método

Sobre los solve paths default de los tres sets de fixtures (corpus 35 + te2-100 +
te3-200), replay estado por estado; en cada estado se cosechan **todos** los
WXYZ-Wing canónicos (all-steps del `WxyzWingSolver`) y por cada paso se verifica
contra el estado:

- **(a) Régimen**: para cada uno de los 4 dígitos del patrón, es RESTRINGIDO si
  todas sus apariciones en las 4 celdas se ven entre sí (`Sudoku2.buddies`). Se
  cuenta cuántos quedan no restringidos y se chequea que sea exactamente 1 y que
  sea Z.
- **(b) Cobertura simple**: `AlsSolver.getAllAlses(xz)` sobre el mismo estado
  (opciones default del solver, overlap off); se busca un ALS-XZ **simple** (un
  solo dígito RC — el AlsSolver registra los RCs como endo-fins, 1 dígito = simple,
  2 = doblemente enlazado) cuyas eliminaciones sean superconjunto de las del wing.
- **(c) Necesidad de locked**: se registra todo wing que solo pudiera cubrirse con
  un ALS-XZ doblemente enlazado, o con ninguno.

Ejecución: JVM con `-Djava.util.Arrays.useLegacyMergeSort=true` (quirk conocido de
`SolutionStep.compareTo`, docs/build.md).

## Resultados

| Métrica | Valor |
|---|---|
| Estados examinados | 26.435 |
| Estados con ≥1 canónico | 93 |
| Pasos canónicos cosechados | **114** (T1 = 33, T2 = 81) |
| Patrones distintos (celdas+Z+eliminaciones) | 37 (25 de ellos T2) |
| (a) Régimen R1 (exactamente 1 no restringido, y es Z) | **114/114 = 100,0 %** |
| (b) Cubiertos por ALS-XZ **simple** | **114/114 = 100,0 %** (T2: 81/81 = 100,0 %) |
| (c) Que necesiten cobertura doblemente enlazada | **0** |
| (c) Sin cobertura ALS-XZ alguna | **0** |
| Runtime | 99 s |

Contraejemplos: ninguno, en ninguna de las tres verificaciones.

## Conclusiones

1. **El "Type 2 = Sue de Coq" queda refutado con datos propios**: los 81 T2
   cosechados son régimen R1 (Z no restringida) y sus eliminaciones se replican
   íntegras con un ALS-XZ de un solo RCC. Ninguno necesitó estructura locked /
   doblemente enlazada. El umbral del §8-4 del mapa se resuelve en la dirección
   prevista por la línea 99 del propio mapa.
2. **T1 tampoco se escapa del álgebra**: 33/33 igual de cubiertos (esperable: la
   bisagra con Z solo agrega un carrier restringido más al mismo esquema).
3. Nota de alcance: el 100 % de cobertura del AlsSolver es sobre los canónicos de
   estos corpus (todos confinados a línea+caja, consistente con el hallazgo del
   1.2 de que ninguna instancia fila+columna+caja apareció); el diagnóstico del
   1.1 sobre bent quads *generales* daba 94,2 % — la diferencia es del motor
   legacy sobre patrones no canónicos, no de la teoría.
4. Este resultado va al help del programa cuando se documente la taxonomía
   (P-001): la etiqueta T1/T2 es presentación; algebraicamente ambos subtipos son
   el mismo ALS-XZ simple.

## Reproducir

```
gradlew compileTestJava
java -Djava.util.Arrays.useLegacyMergeSort=true ^
     -cp "build/classes/java/main;build/classes/java/test;build/resources/main;build/resources/test" ^
     harness.T2RegimeExperiment
```
