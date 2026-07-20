# Log de milestones

## 0.3 — Corpus etiquetado como fixtures — 2026-07-20

Hecho: roadmap de CLAUDE.md reemplazado por la versión del inventario de técnicas
(`docs/inventario-tecnicas.md`, sección D); `data/corpora/` gitignoreado con tarea
`fetchCorpora` (descargas pineadas a commit fijo de denis-berthier/Sudoku-classif);
subsets curados commiteados: 200 T&E(3) de mith (futuros fixtures de Tridagon, 1.6) y
100 T&E(2) de eleven, con procedencia en cabecera y en `docs/corpora.md`; runner de
librerías (`harness.LibraryCaseRunner` + `LibraryCaseRunnerTest`) reutilizando el
formato LIBRARY nativo de HoDoKu como formato v2 de fixtures por técnica, con 4 casos
de ejemplo (W-Wing / Sue de Coq, positivos y negativos); smoke del subset T&E(3).

Suite nueva sobre la del 0.2: 300 tests de unicidad (`CorporaSubsetTest`), 20 de smoke
T&E(3) (`Te3SmokeTest`, muestra determinística 1 de cada 10) y el runner con sus casos.

Desvíos / hallazgos:

- El repo Sudoku-classif trae `Sample1000`/`Sample500` curados por el propio Berthier
  dentro de la colección T&E(3): el subset de 200 sale de ahí (1 de cada 5), no de la
  colección completa — mejor muestra que cualquier curado propio. Licencia GPLv3
  (misma que el fork); commit pineado `15c9d21`.
- Smoke completo (una vez, al cierre): los 200 T&E(3) por `analyze()` + soundness →
  0 violaciones, 65,6 s total, peor puzzle 1,2 s; y por CLI `--batch-json` → 200
  líneas JSON, 0 errores, 200 `solved:true`. Con la config default caen rápido al
  brute force (esperado: no hay técnica que los muerda todavía). En CI queda la
  muestra de 20 para no pagar los 200 en cada push.
- TablingSolver escupe `WARNING: Too many candidates` por logging en varios T&E(3);
  es ruido conocido del legacy, no afecta resultados (soundness verde). Anotado para
  la fase 4/5 (destino del TablingSolver).
- `RegressionTester` legacy no sirve programático (resultados por stdout, contadores
  privados): se reutiliza su **formato**, no su código. El runner nuevo asserta solo
  presencia/ausencia de la técnica; subvariantes `-N` rechazadas explícitamente
  (se agregan si un milestone de fase 1 las necesita). Justificación en
  `docs/harness.md`.
- Drive de champagne (`ph_2010.zip`): descarga automatizada imposible — carpeta con
  ID pre-2021 sin `resourcekey` público, Google fuerza login anónimo (verificado por
  navegador y HTTP directo). Link y pasos manuales en `docs/corpora.md`; **queda en
  manos del dueño** bajarla a `data/corpora/champagne/`.
- El primer post de t6539 (editado oct-2019) menciona `ph_1910.zip`; la versión
  ago-2020 (`ph_2010.zip`) debería estar en la misma carpeta de Drive.

Cierre: pusheado y Actions en verde.

### Resumen para el dueño del proyecto

Trajimos las dos colecciones de sudokus imposibles que sirven de banco de pruebas: 200
"monstruos" del tipo que solo se rinde con la técnica Tridagon (la estrella de la fase
1) y 100 duros de la generación anterior. Verificamos que los 300 tienen solución única
y que el programa los procesa sin colgarse ni cometer errores (hoy los resuelve "por
fuerza bruta", que es lo esperado: la técnica que los destraba se construye más
adelante). Además dejamos armado el molde para los ejercicios de práctica de cada
técnica nueva: archivos de casos que dicen "en esta posición tiene que aparecer tal
jugada — y en esta otra NO", probado con dos técnicas que el programa ya conoce. La
base gigante de champagne (3 millones de puzzles) hay que bajarla a mano porque Google
pide login: los pasos están anotados, te aviso aparte.

## 0.2 — Arnés de verificación — 2026-07-20

Hecho: paquete `harness` nuevo (batch JSON, validador de soundness, extractor de pasos),
source set de tests en `test/` con JUnit 5, corpus de 30 puzzles + 3 extremos como fixtures,
snapshot tests con tarea `updateSnapshots`, y `docs/harness.md` con esquema y uso. Único
toque fuera del paquete: hook de `--batch-json` al inicio de `sudoku.Main`. `gradlew build`
ya arrastraba `test`, así que el workflow de CI no se tocó.

Suite: 71 tests en verde (30 corpus + 3 extremos + 31 snapshots + 4 del validador con pasos
sintéticos + 3 e2e del modo batch).

Desvíos / hallazgos:

- `Options.getInstance()` autolee un `hodoku.hcfg` de `%TEMP%` si existe: la config de
  técnicas (y por lo tanto el solve path) dependería de la máquina. El arnés fuerza
  `Options.resetAll()` antes de resolver; el modo `--batch-json` se saltea todo el parsing
  legacy y la carga de config.
- Los nombres visibles de niveles y técnicas salen de ResourceBundles (cambian con el
  idioma). El JSON usa los nombres de enum (`DifficultyType`, `SolutionType`), que son
  estables entre máquinas — clave para que los snapshots pasen igual acá y en CI.
- Semántica de placements relevada de los `doStep()`: solo SimpleSolver (singles),
  BruteForceSolver, TemplateSolver y TablingSolver (forcing chains/nets con verity)
  colocan valores; todo el resto solo elimina candidatos. `StepRecord.from` replica eso.
- Archivos escritos por PowerShell 5.1 llevan BOM UTF-8 y rompían la primera línea del
  batch; el reader lo tolera (con test).
- Easter Monster verificado: solución única (igual que Golden Nugget y Platinum Blonde,
  los otros dos extremos elegidos). Los tres terminan en ~1s con la config default
  (brute force habilitado como última técnica).
- El corpus salió en 87 intentos de generación (la distribución del generador default
  cubre UNFAIR/EXTREME más seguido de lo esperado). Generado una vez y commiteado;
  regenerarlo invalidaría los snapshots.

Cierre: pusheado y Actions en verde (mismo workflow del 0.1; `build` ejecuta la suite).

### Resumen para el dueño del proyecto

Le pusimos al proyecto su red de seguridad. Desde ahora, cada vez que subamos un cambio,
un robot resuelve una colección fija de 33 sudokus (de fáciles a monstruos famosos) y
verifica dos cosas: que ningún paso del programa contradiga jamás la solución real del
puzzle, y que el camino de resolución no haya cambiado sin que nos demos cuenta. Además
el programa ahora puede escupir, por consola, la resolución completa de cualquier lista
de puzzles en un formato que otras herramientas pueden leer, para comparar contra
programas de referencia. Probamos la alarma a propósito con pasos falsos: suena.

## 0.1 — Build reproducible (Gradle) — 2026-07-19

Hecho: wrapper Gradle 8.14.3, `build.gradle` con toolchain Java 21 (foojay resolver para
auto-provisión), sourceSets sobre el layout legacy (`src/` con recursos adentro), jar
ejecutable con `Main-Class: sudoku.Main`, `docs/build.md`, workflow de CI
(`.github/workflows/build.yml`).

Verificado localmente: `gradlew build` en verde con JDK 21.0.5; el jar (~1.3 MB) contiene
`img/` (71), `intl/` (83), `help/`, `templates.dat`; la GUI abre con LaF nativo Windows y
genera/carga un puzzle (Ctrl+N); batch solve por consola resuelve un puzzle válido
lógicamente.

Desvíos / hallazgos:

- El directorio de trabajo estaba vacío: se clonó PseudoFish/Hodoku (HEAD `c37fe90`,
  se identifica como v2.3.2 Build 116, no 2.3.0). `version` del build quedó en 2.3.2;
  el esquema de versiones propio del fork se define más adelante.
- 9 errores de compilación con JDK 21, todos el mismo patrón: `children()` de
  `DefaultMutableTreeNode` devuelve `Enumeration<TreeNode>` desde JDK 9. Fix mínimo:
  doble cast vía `Enumeration<?>` (7 archivos GUI, cero solving).
- `SudokuUtil.setLookAndFeel` instanciaba el LaF con `Class.newInstance` por reflexión;
  el sistema de módulos lo bloquea (la GUI quedaba sin LaF nativo). Fix mínimo:
  `UIManager.setLookAndFeel(className)`.
- Quedan ~40 warnings de deprecación (`new Integer(...)`, `new Float(...)`,
  `new Boolean(...)` marcados for-removal, APIs Swing viejas). No bloquean; encararlos
  recién si una versión de JDK los remueve.
- `Options` de este fork no tiene `setAlternativeMouseMode`; si existe un
  `hodoku.hcfg` viejo escrito por otro build (p. ej. en `%TEMP%`), el XMLDecoder
  loguea `NoSuchMethodException` y sigue. Inofensivo.
- En Windows, las opciones de consola (`/bs`, etc.) no se pueden pasar desde Git Bash:
  la conversión de paths de MSYS las mangla. Usar PowerShell/cmd.
- El bit ejecutable de `gradlew` se fijó en el índice git
  (`git update-index --chmod=+x gradlew`) para que el workflow corra en Linux.

Cierre: pusheado a IngerisMateria/Hodoku-enhanced (`origin`; el upstream PseudoFish quedó
como remote `upstream`) y workflow de Actions en verde.

### Resumen para el dueño del proyecto

El programa ya se puede fabricar desde cero con un solo comando, en cualquier máquina y
sin instalar nada a mano: eso antes no existía y era el paso previo a todo lo demás.
Comprobamos que el resultado funciona: la aplicación abre, genera un sudoku y lo resuelve.
Además, cada vez que subamos un cambio, un robot de GitHub rehace la fabricación y avisa
si algo se rompió. Hubo que hacer dos arreglos chicos porque el código era de una época
anterior de Java; nada del "cerebro" que resuelve sudokus se tocó.
