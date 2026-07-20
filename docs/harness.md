# Arnés de verificación (milestone 0.2)

Tres piezas que custodian todo el desarrollo posterior: el **modo batch JSON**
(consola), el **validador de soundness** y los **snapshot tests** sobre un corpus
fijo. Todo el código vive en el paquete `harness` (`src/harness/`); los tests en
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

## Correr los tests

```
.\gradlew.bat test
```

`gradlew build` ya arrastra `test` (lifecycle estándar), así que el CI existente
(`.github/workflows/build.yml`, que corre `./gradlew build`) ejecuta soundness y
snapshots en cada push sin cambios en el workflow.
