# Build y ejecución

## Requisitos

- JDK 17 o 21 (LTS). El build usa [Gradle toolchains](https://docs.gradle.org/current/userguide/toolchains.html)
  apuntando a Java 21; si no hay un JDK 21 instalado, el plugin `foojay-resolver-convention`
  lo descarga automáticamente.
- No hace falta instalar Gradle: el repo incluye el wrapper (`gradlew` / `gradlew.bat`),
  que descarga Gradle 8.14.3 en el primer uso.

## Build

```
./gradlew build        # Linux/macOS
gradlew.bat build      # Windows
```

El jar ejecutable queda en `build/libs/hodoku-<version>.jar`. Incluye los recursos que
viven dentro de `src/` (`img/`, `intl/`, `help/`, `templates.dat`) — el `sourceSet` de
resources es `src/` excluyendo `**/*.java`.

Solo compilar y empaquetar (sin checks):

```
./gradlew jar
```

## Ejecución

GUI (punto de entrada real: `sudoku.Main`; ignorar los `main` de prueba de los diálogos Swing):

```
java -jar build/libs/hodoku-2.3.2.jar
```

o directo desde Gradle:

```
./gradlew run
```

### Lanzador sin terminal (Windows)

Doble clic en **`hodoku.bat`** (raíz del repo): builda el jar si hace falta (`gradlew -q jar`)
y abre la GUI con `javaw` (sin dejar consola abierta). Si el build falla, la ventana queda
abierta mostrando el error. Requiere Java en el PATH (el mismo requisito de `gradlew.bat`).

Modo consola (ejemplo: batch solve de un archivo con un puzzle por línea):

```
java -jar build/libs/hodoku-2.3.2.jar /bs puzzles.txt
```

Nota Windows: no invocar las opciones `/xx` desde Git Bash — la conversión de paths de
MSYS las rompe. Usar PowerShell o cmd.

## Notas de compatibilidad JDK moderno

Cambios mínimos aplicados para compilar/correr con JDK 17/21 (sin tocar lógica de solving):

- `DefaultMutableTreeNode.children()` devuelve `Enumeration<TreeNode>` desde JDK 9; los
  casts directos a `Enumeration<CheckNode>` / `Enumeration<DefaultMutableTreeNode>` ya no
  compilan. Se insertó un doble cast vía `Enumeration<?>` en las 9 ocurrencias
  (`AllStepsPanel`, `CheckNode`, `ConfigSolverPanel`, `ConfigFindAllStepsPanel`,
  `ConfigProgressPanel`, `ConfigTrainigPanel`).
- `SudokuUtil.setLookAndFeel` instanciaba el LaF por reflexión (`Class.newInstance`); el
  sistema de módulos bloquea el acceso a `com.sun.java.swing.plaf.windows.*` desde código
  externo y la GUI caía al LaF por defecto. Ahora se instala vía
  `UIManager.setLookAndFeel(String)`.

Warnings pendientes (no bloqueantes, se dejan para más adelante): usos de
`new Integer(...)` / `new Float(...)` / `new Boolean(...)` deprecados y marcados
para remoción, y APIs Swing deprecadas.

## Quirk: threads no-daemon del solver (la JVM no termina sola)

`SudokuSolverFactory` y `SudokuGeneratorFactory` arrancan threads de mantenimiento
**no-daemon** al primer uso (y `BackgroundGeneratorThread` hace lo propio si se toca la
GUI/opciones). Consecuencia: cualquier `main` que use el solver o el generador y termine
"cayéndose por el final" deja la JVM viva — el proceso no muere nunca aunque el trabajo
haya terminado.

Regla práctica para todo código de consola/herramientas (el harness batch ya lo hace):
**terminar siempre con `System.exit(0)` explícito**. Vale también para tools ad-hoc de
un milestone (harvesters de fixtures, probes de GUI, etc.). Los tests JUnit no lo
sufren porque el launcher de Gradle mata la JVM del worker.

Quirk relacionado al ordenar pasos: `SolutionStep.compareTo` viola el contrato de
`Comparable` (lo detecta TimSort con `IllegalArgumentException: Comparison method
violates its general contract!`, p. ej. al ordenar listas grandes de pasos ALS en
`AlsSolver.getAllAlses`). Es legacy conocido; para herramientas ad-hoc que disparen ese
sort, correr la JVM con `-Djava.util.Arrays.useLegacyMergeSort=true`. No afecta el uso
normal de GUI/tests.

## Quirk: cancel de find-all-steps (milestone 1.8, A4)

El cancel del diálogo "Searching..." es **cooperativo**: interrumpe el thread de
`FindAllSteps` y los finders (fish/kraken, ALS, oddagons, rating de progress)
pollean `Thread.currentThread().isInterrupted()` y abortan devolviendo lo
encontrado hasta ahí. El botón ya NO hace `join()` en el EDT (eso congelaba la
UI y dejaba threads zombie si el worker estaba dentro de una fase larga); el
worker esconde el diálogo solo al terminar. Excepción documentada:
`TablingSolver` (chains/nets) no tiene checks internos — regla "no tocar hasta
Fase 5" — así que un cancel durante una búsqueda de forcing chains/nets puede
demorar lo que dure esa llamada individual. Al agregar un finder nuevo con
bucles largos, poner el check en su bucle exterior (patrón en `FishSolver`,
`AlsSolver`, `OddagonSolver`).

## CI

`.github/workflows/build.yml` compila con JDK 21 en cada push/PR (`./gradlew build`) y
sube el jar como artifact.
