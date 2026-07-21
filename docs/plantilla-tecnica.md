# Plantilla: cómo entra una técnica nueva (receta real del milestone 1.1, WXYZ-Wing)

Este es el checklist **ejecutado** en 1.1, en orden. Los milestones 1.2+ lo siguen tal
cual y anotan acá cualquier desvío que descubran.

## 0. Alcance de archivos

Legacy modificable, mínimo y aditivo (regla del dueño, milestone 1.1):

1. `src/sudoku/SolutionType.java` — la entrada del enum.
2. `src/sudoku/Options.java` — el `StepConfig` en `DEFAULT_SOLVER_STEPS`.
3. `src/solver/SudokuStepFinder.java` — registro y dispatch del finder.
4. `src/intl/*.properties` — strings (inglés; el bundle `_de` cae al base por herencia
   per-key, no hace falta duplicar).

Todo lo demás va en `src/solver/modern/` (package `solver.modern`). `SudokuPanel` no se
toca: el rendering estándar (indices verde / fins azul / eliminaciones rojo) alcanza
para patrones tipo wing; si una técnica necesitara marcado nuevo, frenar y consultar.

**Hallazgo clave del 1.1 — el hint text**: `SolutionStep.toString(int)` construye el
texto del hint con un switch por tipo y **tira RuntimeException para tipos que no
conoce**. Como `SolutionStep` NO está en la lista modificable, cada técnica nueva trae
su **subclase de `SolutionStep` en `solver.modern`** que overridea `toString(int)`
(ver `WxyzWingStep`). Reglas de la subclase:

- constructor público sin argumentos (XMLEncoder la persiste al guardar partidas);
- **cero campos nuevos**: todo el estado del paso va en los campos base
  (`values`/`indices`/`fins`/…) con una convención documentada en el javadoc
  (en WXYZ: `values` = 3 restringidos ascendentes + Z al final) — así `clone()`
  (via `super.clone()`, preserva la clase runtime) y la serialización funcionan solas;
- los helpers públicos del padre (`getCompactCellPrint`, getters) se reusan; el formato
  de eliminaciones `" => r1c23<>5"` se replica localmente (el helper del padre es
  privado).

## 1. SolutionType

- Constante **al final del enum** (los ordinales existentes deben quedar estables).
- Library code de 4 dígitos: elegir uno libre **verificando con grep contra todos los
  existentes** (`grep -o '"[0-9]\{4\}"' SolutionType.java`), preferir el hueco de la
  familia (WXYZ = `0802`, hueco de los wings 08xx) y documentarlo en el comentario.
- argName corto para CLI (`wxyz`).
- String del nombre en `src/intl/SolutionType.properties` (solo base, inglés).

## 2. Options (StepConfig)

Entrada en `DEFAULT_SOLVER_STEPS` en la posición del orden de solver deseado:
`index`/`indexProgress` eligen el orden (WXYZ = 5650, justo antes de ALS-XZ 5700),
score default acorde a la dificultad relativa (250, entre XYZ-Wing 180 y ALS-XZ 300),
categoría de la familia (`SolutionCategory.WINGS`), `enabled=true, allSteps=true,
enabledProgress=false, training=false` como el resto.

Nota: configs `hodoku.hcfg` guardadas por versiones previas NO traen el StepConfig
nuevo y el merge de `readOptions` no lo agrega — con una config vieja la técnica queda
invisible en GUI. Para probar en GUI usar config fresca (borrar
`%TEMP%\hodoku.hcfg` o correr con `-Djava.io.tmpdir=<dir limpio>`). El harness no lo
sufre (`Options.resetAll()`).

## 3. Finder

`solver.modern.XxxSolver extends AbstractSolver`, espejo de `WingSolver`:

- `getStep(type)`: modo paso-único (retorna el primero).
- `doStep(step)`: ejecuta las eliminaciones (`sudoku.delCandidate`).
- `getAllXxx()`: modo all-steps (lista completa) con el patrón steps/oldSteps de los
  solvers legacy.
- Un `globalStep` (instancia de la subclase de step) + `clone()` al emitir.
- Registro en `SudokuStepFinder`: campo + `new` en `initialize()` + entrada en el array
  `solvers` + colgarlo del `getAllXxx` agregado (los wings van dentro de
  `getAllWings()`, que `FindAllSteps` ya llama incondicionalmente y filtra por técnica
  habilitada — así el panel "find all steps" de la GUI lo lista sin tocar
  `FindAllSteps`).

## 4. Fixtures (formato library, `test/fixtures/libs/`)

1. Cosechar con el propio finder: tool ad-hoc (scratch, no se commitea) que reproduce
   el solve path default de `corpus.txt` + `te2-eleven-100.txt` con
   `solver.getHint(work,false)` / `solver.doStep(work,hint)` y en cada estado corre el
   finder nuevo; exportar cada hallazgo con
   `work.getSudoku(ClipboardMode.LIBRARY, step)` — eso ES la línea del fixture.
2. Negativos near-miss: el tool relaja una condición del patrón por vez (en WXYZ: unión
   de 5; dos no-restringidos; conjunto íntegro en una casa) y elige estados donde la
   estructura relajada existe pero `finder.getStep(tipo) == null`. Línea:
   `getSudoku(LIBRARY, null)` y reemplazar el prefijo `:0000:x:` por `:CODE-x:x:`.
   Elegir estados/puzzles DISTINTOS por categoría (dos near-miss en el mismo estado =
   fixtures duplicados).
3. Validación fuerte antes de commitear: revalidar TODOS los estados cosechados contra
   la solución brute-force (ninguna eliminación de ningún paso hallado puede matar el
   dígito de la solución) y que los `-x` de verdad no tengan pasos (en 1.1: 15.282
   estados, 85.552 pasos, 0 violaciones).
4. Commitear `test/fixtures/libs/<tecnica>.txt` (positivos diversos: formas distintas
   del patrón, fila y columna, con/sin celdas de intersección, eliminación simple y
   múltiple) y **registrar el archivo en `LIB_FILES` de `LibraryCaseRunnerTest`**.

## 5. Verificación diagnóstica (si aplica)

Si la técnica tiene una formulación equivalente en otra familia (WXYZ ≡ ALS-XZ con las
partes línea/caja como ALSs), correr el diff en el tool de cosecha y reportar cobertura
en el log. Es diagnóstico, NO un assert (en 1.1: 94,2% cubierto; el resto son ALS-XZ
que el AlsSolver legacy no emite en ese estado).

## 6. GUI

Ver el paso en la GUI real con config fresca: buscar un estado donde la técnica sea el
**próximo paso del path default** (si no, el hint muestra el paso más barato), cargar
la línea library con `SudokuPanel.setSudoku(String)`, pedir hint (F12 =
`getNextStep(false)` + `setSolutionStep`), confirmar texto y resaltado. Con un probe
programático: capturar el frame con `printAll` a PNG y mirarlo.

## 7. Snapshots y métricas

```
gradlew.bat updateSnapshots
```

Revisar el diff (puede ser vacío legítimamente: en 1.1 el corpus de 30 no cambió — el
orden 5650 hace que cadenas más baratas ganen siempre en esos puzzles). Reportar en el
log: cuántos solve paths cambiaron y cuántos pasos de la técnica aparecen; si el corpus
no la ejercita, medir también sobre `te2-eleven-100.txt` con `--batch-json` (en 1.1:
22 pasos en 18/100 puzzles).

**Regla de custodia (milestone 1.2, matizada en 1.3)**: toda técnica nueva **enabled
por default** queda custodiada por ≥1 puzzle del corpus de snapshots cuyo solve path
default la contenga. Si ningún puzzle del corpus la ejercita, sumar al corpus 1-3
puzzles (p. ej. de te2) cuyos paths default la contengan, regenerar snapshots como
tarea documentada y reportar el diff en el log. Para técnicas **apagadas por default**
la custodia son sus fixtures de librería: los snapshots corren con la config default y
no pueden ejercitarlas (el runner de librerías usa `getStep(type)` directo, que no
mira el enable).

## 8. Cierre

- `gradlew test` verde local; commit(s) lógicos (técnica+fixtures+tests juntos); push;
  CI verde.
- Tick en el roadmap de CLAUDE.md + mover el marcador `← PRÓXIMO`.
- Entrada en `docs/log.md` con métricas + resumen llano de 3-5 líneas.
