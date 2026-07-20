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

## CI

`.github/workflows/build.yml` compila con JDK 21 en cada push/PR (`./gradlew build`) y
sube el jar como artifact.
