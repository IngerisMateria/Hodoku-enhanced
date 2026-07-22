# Backlog de pulido — objeciones diferidas y desarrollos futuros

Se procesan cuando el roadmap principal lo permita (varias en el milestone
5.0). Cada entrada registra el porqué completo para que el futuro no tenga
que reconstruirlo.

Convención: un P-XXX por idea. Si una idea se divide, las partes van bajo
el mismo número, cada una encabezada por "Parte k/N". Estados: COMPLETADA
(cerrada del todo, con milestone y fecha) / PENDIENTE / DIFERIDA a 5.0. En
entradas con partes, el estado se da por parte: COMPLETADA (parte k/N) o
PENDIENTE (parte k/N).

## Checklist

- [ ] **P-001** — Presentación de subtipos del WXYZ-Wing canónico — DIFERIDA a 5.0
- [x] **P-002** — Separar Kraken Fish Type 1/2 — COMPLETADA (milestone 1.5)
- [x] **P-003** — Memoria de posición de popups — COMPLETADA (milestone 1.9)
- [ ] **P-004** — Carpetas / presets / resaltado de solapamiento
	  - [x] Parte 1/2 — Infra de carpetas visuales — COMPLETADA (milestone 1.9)
	  - [ ] Parte 2/2 — Presets de estudio + resaltado de solapamiento — PENDIENTE
- [ ] **P-005** — Vocabulario visual y color por estrategia
	  - [ ] Parte 1/4 — Modelo de roles semánticos (Capa 1) — PENDIENTE
	  - [ ] Parte 2/4 — Roles por técnica (Capa 2) — PENDIENTE
	  - [ ] Parte 3/4 — Editor rico por técnica con preview (Capa 3) — PENDIENTE
	  - [ ] Parte 4/4 — Coloreo de celda como lenguaje — PENDIENTE
- [ ] **P-006** — Alineación de la partición T1/T2 del Uniqueness Pack — PENDIENTE (probable Fable)
- [ ] **P-007** — Agrupar find-all-steps analizados en carpetas generales — PENDIENTE (remitida a P-010)
- [ ] **P-008** — Mouse & interacción de config/tablero
	  - [ ] Parte 1/3 — Checkbox vs nombre en la lista de config — PENDIENTE
	  - [ ] Parte 2/3 — Rueda del mouse = undo/redo — PENDIENTE
	  - [ ] Parte 3/3 — Colocación de dígitos/candidatos con el mouse — PENDIENTE
- [ ] **P-009** — Toolbar configurable
	  - [ ] Parte 1/2 — Toolbar personalizable (mostrar/ocultar/reordenar) — EN CURSO (milestone 1.10)
	  - [ ] Parte 2/2 — Visualizadores de grupos con color propio — PENDIENTE (remitida a P-005)
- [ ] **P-010** — Find-pattern update (filtro de all-steps por celdas) — DIFERIDA (candidata Fable)

> Nota P-007: estaba plegada al 1.10 cuando el 1.10 era el filtro de
> all-steps por celdas. Con el 1.10 redefinido (UI: toolbars + estándar
> visual), P-007 vuelve a PENDIENTE y viaja con P-010: se hace en el mismo
> turno que el filtro, que es donde comparte zona de UI y costo.

────────────────────────────────────────────────────────────

## P-001 — Presentación de subtipos del WXYZ-Wing canónico
Estado: DIFERIDA a 5.0.
- Contexto: el hint del WXYZ canónico reporta "Type 1/2" (T1: Z en la
  bisagra; T2: Z ausente de la bisagra).
- Objeción: no hay consenso teórico sobre esa taxonomía; SudokuWiki usa dos
  tipos y en sus comentarios objetan que el Type 2 es (o se solapa con) un
  Sue de Coq. Al dueño le gusta la salida actual; el riesgo es cosmético
  (reclamos de puristas post-release).
- Dato empírico ya disponible: docs/experimentos/t2-regimen.md — 114
  canónicos, 100% régimen R1, 100% cubiertos por ALS-XZ simple, 0 requieren
  estructura doblemente enlazada. Refuta "T2 = Sue de Coq".
- Cambio candidato: hacer configurable u omitible la etiqueta de subtipo,
  y/o documentar en el help la relación con SDC/ALS-XZ/bent sets.
- Nota técnica: el subtipo NO se almacena, se deriva (T1 sii la bisagra
  aparece en fins). Cambiar la presentación toca solo el formateador de
  ModernStep: costo mínimo, cero migración.
- Criterio de cierre: decisión del dueño (mantener/configurar/omitir) +
  help actualizado.

## P-002 — Separar Kraken Fish Type 1 y Type 2
Estado: COMPLETADA (milestone 1.5, 2026).
- Ejecutada: KRAKEN_FISH_TYPE_1 (8450) y TYPE_2 (8460) con StepConfig
  propio, honradas por solver / find-all-steps / training; el genérico
  KRAKEN_FISH retirado de la config como ancla taxonómica; migración de
  hcfg con roundtrip testeado. Resuelve el cuelgue histórico de all-steps
  con krakens (permite apagar los T2, ~8:1 de ruido computacional).

## P-003 — Memoria de posición de popups
Estado: COMPLETADA (milestone 1.9, 2026).
- Ejecutada: AWTEventListener global cubre todo java.awt.Dialog; los frames
  quedaron como excepción documentada. Los diálogos reaparecen donde el
  usuario los movió por última vez.

## P-004 — Carpetas / presets / resaltado de solapamiento
Estado: dividida (una parte hecha, una pendiente).

Parte 1/2 — Infra de carpetas visuales — COMPLETADA (milestone 1.9, 2026):
Modelo de carpetas por familia como vista sobre el registro, aplicado a
solver / all-steps / progress / training. Carpetas SOLO visuales (el orden
de resolución vive en la lista plana exhaustiva); una técnica puede
repetirse en varias carpetas. Se crearon "Oddagons" y "Uniqueness".

Parte 2/2 — Presets de estudio + resaltado de solapamiento — PENDIENTE:
- Presets de enable para find-all-steps y solver: máscaras guardadas
  ("Wings", "ALS", "Cadenas", "Fish", "Unicidad", "Clásico HoDoKu") +
  presets custom del usuario. Permiten estudiar bajo una nomenclatura sin
  activar el resto.
- Resaltado de solapamiento: marcar en amarillo en la config las técnicas
  con relaciones "interfiere con" del registro; al abrir una, listar con
  cuáles interfiere.
- Selector de vista de carpeta si se decide exponer más de una de las 3
  vistas (por familia / por convención / jerárquica por las 4 raíces).
- Diseño completo en docs/estrategia-taxonomia.md §5.
- Criterio de cierre: presets guardables y aplicables; resaltado visible y
  correcto contra el registro.

## P-005 — Vocabulario visual y color POR ESTRATEGIA
Estado: diseñada, no iniciada. Candidata a Fable (requiere auditoría
teórica de ~100 técnicas + diseño de vocabulario de técnicas aún no
implementadas). ES la funcionalidad que el dueño quería realmente: NO un
vocabulario global expandido, sino que CADA estrategia declare sus propias
partes visuales nombradas, con color editable por parte.

Hallazgo de arquitectura (auditoría 2026, código real):
- No hay hardcodeo de colores en los finders. hobiwan centralizó ~17 roles
  semánticos globales (hint candidate/back, delete/back, fin/back,
  endo-fin, cannibalistic, …), cada uno variable única en Options con
  getter, consumidos por SudokuPanel. ConfigColorPanel YA los edita con
  JColorChooser.
- Por eso "todo lo nuevo se ve verde/rojo/azul": los steps modernos
  reutilizan el vocabulario viejo (celdas de patrón→hint back verde,
  eliminaciones→delete back rojo, guardianes→fin azul). El diccionario fue
  diseñado para fish/cadenas; metemos patrones nuevos en casillas
  semánticas ajenas. Ej.: el guardián de un oddagon NO es una aleta de pez.
- Deuda relacionada: hay técnicas LEGACY con vocabulario propio no modelado
  (bent subsets → pivote y pinzas; UR → roof y floor). La auditoría de
  vocabulario debe cubrir legacy, no solo lo nuevo.

Parte 1/4 — Modelo de roles semánticos (Capa 1) — PENDIENTE:
Registro de roles; migrar los ~17 colores legacy a roles compartidos base
SIN cambiar comportamiento; conectar al ConfigColorPanel existente. Refactor
estructural, bajo riesgo.

Parte 2/4 — Roles por técnica (Capa 2) — PENDIENTE:
Cada SolutionType declara sus partes ("pivote", "pinza", "base", "target",
"S-cell", "guardián", "lazo", "roof", "floor", …), cada una con default
distinguible; los formatters (modernos y, donde aplique, legacy) usan sus
roles propios en vez de pedir prestado. Requiere la auditoría teórica de
qué partes tiene cada técnica — idealmente con la teoría fresca al
implementar cada familia (exocet nace con su vocabulario).

Parte 3/4 — Editor rico por técnica con preview (Capa 3) — PENDIENTE:
UI donde ves cada técnica, sus partes y editás color por parte viendo un
preview en vivo. Swing intrincado; mayor riesgo; refinamiento de UX, no
fundación.

Parte 4/4 — Coloreo de celda como lenguaje — PENDIENTE:
Colorear la CELDA entera, no solo candidatos, donde la celda comunica
(exocet: bases/targets/S-cells; patrones de conjunto). Lenguaje visual de
celda para señalar múltiples conceptos. Diseñar junto con la Parte 2 de las
familias que lo necesiten. Absorbe P-009 Parte 2/2 (visualizadores de
grupos).

Secuenciación: Partes 1+2 dan TODO el valor estructural ("el framework
desde el cual pensar el diseño"); Parte 3 es lujo posterior. Los roles de
cada familia se declaran idealmente cuando la familia se implementa.
Criterio de cierre: cada técnica declara sus partes; cada parte tiene color
editable persistido; rendering usa los roles por técnica; coloreo de celda
disponible donde la familia lo requiera.

## P-006 — Alineación de la partición T1/T2 del Uniqueness Pack
Estado: PENDIENTE (probable Fable: discriminar tipos canónicos es álgebra
fina).
- Contexto: en 1.9 se desglosó el pack (Unique Loop, Extended UR, BUG-Lite,
  MUG) en Type 1/Type 2 por CARDINALIDAD DE GUARDIANES (T1 = un guardián en
  una celda; T2 = dígito uniforme). Es sound, funciona y está bien
  etiquetado internamente, pero NO coincide con la taxonomía de la
  literatura: los tipos canónicos de UR/UL son estructuralmente distintos
  (T1 = una celda con extras; T2 = mismo extra en dos celdas; T3 = extras
  formando subset; T4 = strong link; etc.), y BUG-Lite/MUG no tienen tipos
  numerados asentados. Reverse BUG no se tocó (no pasa por la escalera de
  guardianes; sin subtipos, correcto).
- Cambio candidato: alinear las etiquetas con los tipos canónicos donde
  existan (UR/UL), o documentar en el help que nuestra partición es por
  cardinalidad de guardianes y por qué. Requiere discriminar los tipos
  canónicos reales al detectar — álgebra fina.
- Criterio de cierre: decisión del dueño (alinear/documentar) + coherencia
  etiquetas↔literatura, o nota de ayuda que lo aclare.

## P-007 — Agrupar find-all-steps analizados en carpetas generales
Estado: PENDIENTE — remitida a P-010 (viajaba plegada al filtro de
all-steps, y el filtro se difirió al redefinirse el 1.10).
- Contexto: tras un análisis, la vista de all-steps agrupaba por estrategia
  individual; el dueño quiere esas carpetas-de-estrategia dentro de las
  carpetas GENERALES por familia (las del 1.9: Oddagons, Uniqueness,
  Wings, …). La infra de carpetas por familia y el toggle lista/carpeta ya
  existen del 1.9; esto conecta ese agrupamiento al resultado de un
  análisis. Bajo riesgo.
- Por qué viaja con P-010: misma zona de UI (el árbol de resultados del
  panel de all-steps) y mismo mecanismo de vista; hacerla suelta obliga a
  tocar dos veces el mismo código.
- Criterio de cierre: el resultado de un análisis se puede ver agrupado por
  las carpetas generales de familia, consistente con las pestañas de config.

## P-008 — Mouse & interacción de config/tablero
Estado: dividida; partes 1-2 candidatas a un turno de UI simple, parte 3
delicada (modelo de interacción del tablero).

Parte 1/3 — Checkbox vs nombre en la lista de config — PENDIENTE (contenido):
Hoy tocar una técnica en la lista siente que activás el checkbox y abrís la
config a la vez. Separar: tocar el checkbox (enable/disable) NO abre la
config; tocar el NOMBRE abre la config (aside) pero NO togglea el enable. El
renderer con checkbox ya existe (CheckNode/CheckRenderer/CheckBoxRenderer);
es reestructurar el hit-area. Bajo riesgo.

Parte 2/3 — Rueda del mouse = undo/redo — PENDIENTE (contenido, con matiz):
No hay ningún MouseWheel listener hoy. Propuesta: rueda arriba = deshacer,
rueda abajo = avanzar. MATIZ a resolver: la rueda es también scroll;
definir dónde aplica (¿solo sobre el tablero? ¿con modificador? ¿cuando no
hay scroll?) para no romper el desplazamiento. Agregar el listener es
contenido; el alcance es decisión de diseño.

Parte 3/3 — Colocación de dígitos/candidatos con el mouse — PENDIENTE
(delicada): Mejor "feeling" al colocar valores y candidatos con el mouse.
Toca el modelo de interacción del tablero (SudokuPanel handleColoring/
placement), sensible y fácil de romper. Requiere diseño cuidado y,
preferentemente, un modelo capaz. No mezclar con lo contenido.
Criterio de cierre (global): las tres partes con su interacción esperada,
sin regresiones en selección/colocación existentes.

## P-009 — Toolbar configurable
Estado: dividida; parte 1 EN CURSO (milestone 1.10), parte 2 remitida a
P-005.

Parte 1/2 — Toolbar personalizable — EN CURSO (milestone 1.10):
Permitir mostrar/ocultar qué aparece en la toolbar y reordenarlo. El dueño
la EXTENDIÓ a las DOS toolbars de la app y fijó el modelo:

> "conjunto de botones disponibles; el usuario elige cuáles se muestran y
> en qué orden"

Un solo modelo cubre reordenar Y ocultar/reponer (2-en-1: un botón quitado
vuelve a la lista de disponibles). NO se agregan botones-función nuevos —
eso es P-005 / P-009 Parte 2/2; acá solo se gestiona lo que ya existe.

- B1 — Toolbar SUPERIOR de `MainFrame`: el bloque de filtros (dígitos 1-9,
  XY y el toggle rojo/verde de celdas inválidas), hoy un array
  `JToggleButton[10]` bien delimitado. Reordenable + ocultable, persistido.
- B2 — Toolbar de ALL-STEPS (`jToolBar1` de `AllStepsPanel`): botón de
  configuración + los cinco `*SortToggleButton`. Verificado como `JToolBar`
  con `.add()` secuencial y toggles bien nombrados → estructura limpia,
  mismo tratamiento que B1.
- B3 — UI de configuración dentro de `edit → preferences…`, como el resto de
  las opciones, para reordenar y mostrar/ocultar los botones de AMBAS
  toolbars. Persistencia y migración en `Options`.

Parte 2/2 — Visualizadores de grupos con color propio — PENDIENTE (remitida
a P-005): "Visualizar tríos/cuartetos/…/grupos de 9 con color de highlight
editable por cada uno" es exactamente el vocabulario de color por concepto
de P-005 (ver P-005 Parte 4/4). NO implementar por separado: se haría dos
veces. Aquí queda solo el enganche de toolbar que los dispara, una vez que
P-005 provea los roles.
Criterio de cierre: parte 1 con las dos toolbars configurables y
persistidas; parte 2 ejecutada dentro de P-005.

## P-010 — Find-pattern update (filtro de all-steps por celdas)
Estado: DIFERIDA (candidata Fable). Era el contenido del 1.10 antes de que
el dueño lo redefiniera a reestructuración de UI.

Qué es: un filtro EN VIVO **posterior** a un find-all-steps. El usuario
ilumina celdas del tablero y la lista de steps del panel de all-steps se
reduce a las técnicas cuyo **PATRÓN** incluye celdas iluminadas. "Tengo la
intuición de que por acá hay algo pero no lo veo o no conozco la teoría" →
ilumino la celda → veo qué estructuras se apoyan ahí.

Naturaleza técnica: NO es búsqueda nueva ni recomputo. Es un predicado de
filtro más sobre la lista de steps YA calculada, montado en el mismo
mecanismo de filtrado de vista que introdujo el buscador del 1.5/1.9. Cero
motor nuevo, cero álgebra, cero recomputo del análisis.

Decisiones ya cerradas por el dueño:
- Qué cuenta como "toca": SOLO celdas del PATRÓN (la estructura de la
  técnica). Las eliminaciones NO cuentan — filtrar por eliminación ya es
  posible en la UI existente; duplicarlo sería ruido.
- Se compone con el buscador de texto del 1.9 (texto AND celdas).
- Estado vacío (ninguna técnica toca las celdas iluminadas) legible.

**Dos diseños ALTERNATIVOS.** El dueño fue explícito: NO son fases de una
misma cosa, son modelos de interacción distintos y hay que elegir uno.

- **Alternativa A — switch global de 3 estados** (apagado / restringido /
  abierto) en la toolbar superior; Ctrl+click ilumina celdas.
  *Restringido* = solo técnicas contenidas en la selección; *abierto* =
  técnicas que tocan alguna celda de la selección. Simple de implementar y
  de explicar; su límite es que no distingue celda por celda (todas las
  iluminadas tienen el mismo rol).
- **Alternativa B — dos capas de selección sobre el tablero**: la selección
  amarilla existente más un segundo Ctrl+click sobre una celda ya
  seleccionada, que la marca como "abierta" (otro color) en vez de
  "restringida". El filtro pasa una técnica si contiene TODAS las
  restringidas y toca alguna abierta. Mucho más expresivo (p. ej.: x-wing
  restringido + aletas dudosas abiertas). Riesgo: se mete con el modelo de
  selección del tablero, que es sensible. Control: botón on/off simple.

Pendiente de decisión: A vs B, y la ubicación del control (depende de la
toolbar configurable de P-009 Parte 1/2, ya hecha en el 1.10).

Absorbe **P-007** (agrupar el resultado del análisis por las carpetas
generales de familia): misma zona de UI, se hace en el mismo turno.

Criterio de cierre: iluminar celdas filtra la lista en vivo por celdas del
patrón, con el modelo de interacción elegido; el resultado se puede agrupar
por carpetas generales (P-007); se compone con el buscador de texto; cero
recomputo, cero álgebra, cero cambios de solve paths.
