# Log de milestones

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
