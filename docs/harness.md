# Arnés de verificación (milestones 0.2 y 0.3)

Piezas que custodian todo el desarrollo posterior: el **modo batch JSON**
(consola), el **validador de soundness**, los **snapshot tests** sobre un corpus
fijo (0.2) y el **runner de librerías** para fixtures por técnica (0.3). Todo el
código vive en el paquete `harness` (`src/harness/`); los tests en
`test/harness/` con fixtures en `test/fixtures/`.

## Modo batch JSON (`--batch-json`)

Nuevo modo de consola con sintaxis de doble guión (los flags legacy `/xy` se
manglan en Git Bash/MSYS, ver `docs/log.md` del 0.1). Se saltea por completo el
parsing legacy y la carga de configuración: usa **siempre** la configuración
default de técnicas (`Options.resetAll()`), para que la salida sea comparable
entre máquinas.

```
java -jar hodoku.jar --batch-json <archivo> [--out <archivo|stdout>]
```

Ejemplos (PowerShell/cmd):

```powershell
# compilar y correr desde el repo
.\gradlew.bat jar
java -jar build\libs\hodoku-2.3.2.jar --batch-json puzzles.txt --out resultado.jsonl

# o directo contra las clases compiladas
java -cp "build\classes\java\main;build\resources\main" sudoku.Main --batch-json puzzles.txt
```

**Entrada**: un puzzle de 81 caracteres por línea (`.` o `0` = celda vacía).
Líneas en blanco y líneas que empiezan con `#` se ignoran. Se tolera BOM UTF-8.

**Salida**: JSON Lines — un objeto por puzzle, en el orden de entrada, a stdout
salvo que se pase `--out`. Exit code 0 si se procesó el archivo (los puzzles
inválidos se reportan como objetos con `error`, no cortan el batch); 1 ante
error de uso o de I/O.

### Esquema

Puzzle procesado:

```json
{
  "puzzle":   "<81 chars como en la entrada>",
  "solutions": 1,
  "solution": "<81 dígitos, solución única por brute force>",
  "solved":   true,
  "level":    "HARD",
  "steps":    [ { "technique": "HIDDEN_SINGLE",
                  "placements":   [ {"cell": "r1c2", "value": 3} ],
                  "eliminations": [ {"cell": "r4c5", "value": 7} ] } ]
}
```

- `solutions`: 0, 1 o 2 (2 significa "2 o más"). Si no es 1, el objeto lleva
  `error` y no tiene `solution`/`steps`.
- `solved`: `true` si el solve lógico llegó a la solución completa (con la
  config default el brute force está habilitado como última técnica, así que
  en la práctica siempre termina; `false` indicaría GIVE_UP).
- `level`: nombre del enum `DifficultyType` (`EASY`…`EXTREME`), independiente
  del locale (los nombres "visibles" de los niveles salen de ResourceBundles y
  cambian con el idioma; acá no sirven).
- `steps`: la secuencia completa del solve lógico, en orden. `technique` es el
  nombre del enum `SolutionType` (estable, p. ej. `NAKED_SINGLE`, `X_WING`).
  `placements` = valores colocados, `eliminations` = candidatos eliminados,
  ambos en notación `r#c#` 1-based. La extracción refleja exactamente lo que
  ejecuta cada `doStep()` (solo SimpleSolver, BruteForceSolver, TemplateSolver
  y TablingSolver colocan valores; el resto solo elimina candidatos).

Línea inválida:

```json
{"puzzle": "not-a-puzzle", "error": "invalid puzzle line (expected 81 chars of [0-9.])"}
```

Puzzle sin solución única:

```json
{"puzzle": "<81 chars>", "solutions": 2, "error": "puzzle has more than one solution"}
```

## Validador de soundness

`harness.SoundnessValidator` chequea cada paso contra la solución brute-force:

- ningún paso puede **eliminar** un candidato que coincide con el dígito de la
  solución en esa celda;
- ningún paso puede **colocar** un valor distinto al de la solución.

Una violación identifica puzzle, número de paso, técnica y la celda ofensora.
Corre sobre el corpus completo en `CorpusTest` (y por lo tanto en CI en cada
push). `SoundnessValidatorTest` inyecta pasos sintéticos inválidos y verifica
que el detector suena — un detector que nunca sonó no está probado.

## Corpus (`test/fixtures/`)

- `corpus.txt`: 30 puzzles generados **una sola vez** con el generador del
  propio HoDoKu (2026-07-20), cubriendo EASY(7) / MEDIUM(7) / HARD(7) /
  UNFAIR(5) / EXTREME(4). No se regenera en tiempo de test: el determinismo de
  los snapshots depende de que sea fijo.
- `extremes.txt`: Easter Monster, Golden Nugget y Platinum Blonde. Smoke test:
  se asserta unicidad, soundness de los pasos encontrados y que el solve
  termina (timeout); no forman parte de los snapshots.

El arnés asserta que cada puzzle de ambos archivos tiene solución única.

## Snapshot tests

`SnapshotTest` compara el JSON del solve path de cada puzzle del corpus contra
`test/fixtures/snapshots.jsonl` (misma serialización que el modo batch).
Cualquier diferencia = test rojo.

**Actualización intencional** (p. ej. al integrar una técnica nueva que cambia
legítimamente los solve paths):

```
.\gradlew.bat updateSnapshots
```

regenera `snapshots.jsonl` desde el corpus; el diff se revisa y commitea junto
con el cambio que lo causó. Ese es el único camino válido: nunca editar el
archivo a mano ni regenerarlo "porque el test está rojo" sin entender el diff.

## Runner de librerías — formato v2 de fixtures por técnica (milestone 0.3)

**Decisión: se reutiliza el formato de librería nativo de HoDoKu** (el de
`sudoku.RegressionTester` / `ClipboardMode.LIBRARY`) en vez de inventar uno:

```
:codigo[-x]:cands:grid81:candidatos-ya-borrados:eliminaciones-del-paso:comentario
```

- `codigo`: código de técnica de `SolutionType.getLibraryType()` (p. ej. `0803` =
  W-Wing, `1101` = Sue de Coq). Sufijo `-x` = **caso negativo**: ningún paso de esa
  técnica debe existir en ese estado (fixtures anti-falsos-positivos).
- `grid81`: los 81 caracteres del estado; `+` prefija celdas resueltas no-given.
- `candidatos-ya-borrados`: tripletas `crc` (candidato, fila, columna) que definen,
  junto con el grid, el estado exacto de candidatos.

Razones para reutilizarlo: (a) `Sudoku2.setSudoku()` ya lo parsea nativamente, con lo
que un fixture define un **estado intermedio exacto** (grid + candidatos) en una sola
línea de texto; (b) soporta casos negativos, que la fase 1 necesita; (c) cualquier
estado alcanzado en la GUI o en un solve se exporta a este formato con
`getSudoku(ClipboardMode.LIBRARY, step)`, así que generar fixtures nuevos es barato;
(d) los archivos lib del ecosistema HoDoKu existente son reusables tal cual.

Piezas:

- `harness.LibraryCaseRunner`: parsea una línea, reconstruye el estado y asserta
  presencia/ausencia de la técnica vía `SudokuStepFinder.getStep(type)` con la config
  default del arnés. **Alcance deliberado**: solo presencia/ausencia de la técnica, no
  igualdad exacta de eliminaciones; las subvariantes numéricas `-N` de
  RegressionTester (que togglean opciones del solver por caso) se rechazan con error.
  Si un milestone de fase 1 las necesita, se agregan en ese momento.
- `test/fixtures/libs/*.txt`: un archivo lib por grupo de casos; líneas `#` son
  comentarios. Los milestones de técnica de fase 1 agregan acá sus fixtures
  (positivos etiquetados + negativos) y registran el archivo en la lista `LIB_FILES`
  de `LibraryCaseRunnerTest` (receta completa en `docs/plantilla-tecnica.md`).
- `LibraryCaseRunnerTest`: corre todos los casos de los archivos registrados
  (`phase1-examples.txt`: 4 casos de ejemplo con W-Wing y Sue de Coq del 0.3;
  `wxyz-wing.txt`: 4 positivos + 3 negativos near-miss del 1.1).

`sudoku.RegressionTester` (el tester legacy) queda intacto y sin uso directo: sus
resultados van a stdout y sus contadores son privados, así que no sirve programático
desde JUnit; lo que se reutiliza es su **formato**, no su código.

## Subsets de corpus (milestone 0.3)

- `CorporaSubsetTest`: unicidad de solución (brute force vía
  `HarnessRunner.countSolutions`) de los 300 puzzles de
  `te3-mith-200.txt` + `te2-eleven-100.txt`. Procedencia: `docs/corpora.md`.
- `Te3SmokeTest`: sobre una muestra determinística del subset T&E(3) (1 de cada 10),
  el solve completo con config default debe terminar resuelto y sound (hoy cae al
  brute force: son monstruos sin técnica implementada que los muerda). La pasada
  completa de los 200 se validó al cierre del 0.3 (ver `docs/log.md`).

## Correr los tests

```
.\gradlew.bat test
```

`gradlew build` ya arrastra `test` (lifecycle estándar), así que el CI existente
(`.github/workflows/build.yml`, que corre `./gradlew build`) ejecuta soundness y
snapshots en cada push sin cambios en el workflow.
