# Backlog de pulido y desarrollos futuros — detalle completo

Estado al 2026-07-21. Convención: un P-XXX por idea; si una idea se divide,
las partes van juntas bajo el mismo número, cada una encabezada por
"Parte k/N". Las entradas cerradas se conservan por trazabilidad.

Nota de estado de las entradas previas (verificado contra el código):
- P-001 — diferida (se procesa en 5.0). Sigue abierta.
- P-002 — CERRADA: ejecutada en el milestone 1.5 (split Kraken T1/T2).
- P-003 — CERRADA: ejecutada en el milestone 1.9 (memoria de popups).
- P-004 — PARCIAL: la infra de carpetas visuales se hizo en 1.9; quedan
  pendientes los presets de estudio (máscaras de enable guardadas) y el
  resaltado de solapamiento. Ver P-004 abajo, reescrita a lo que resta.
- P-005 en adelante: nuevas, definidas abajo.

────────────────────────────────────────────────────────────

## P-001 — Presentación de subtipos del WXYZ-Wing canónico
Estado: diferida a 5.0. (Sin cambios; ver detalle histórico ya en el
archivo.) Resumen: la etiqueta "Type 1/2" del WXYZ canónico no tiene
consenso teórico; decisión de mantener/omitir/configurar queda para la
pasada de pulido, con el dato del experimento t2-regimen.md ya adjunto.

## P-004 — Presets de estudio + resaltado de solapamiento (resto)
Estado: parcial (infra de carpetas hecha en 1.9). Lo que queda:
- Presets de enable para find-all-steps y solver: máscaras guardadas
  ("Wings", "ALS", "Cadenas", "Fish", "Unicidad", "Clásico HoDoKu") +
  presets custom del usuario. Permiten estudiar bajo una nomenclatura sin
  activar el resto.
- Resaltado de solapamiento: en la config, marcar en amarillo las técnicas
  con relaciones "interfiere con" del registro; al abrir una, listar con
  cuáles interfiere.
- Las 3 vistas de carpeta (por familia / por convención / jerárquica por
  las 4 raíces del mapa) ya tienen la infra; falta exponer el selector de
  vista si se decide más de una.
Diseño completo en docs/estrategia-taxonomia.md §5.
Criterio de cierre: presets guardables y aplicables en las superficies;
resaltado visible y correcto contra el registro.

────────────────────────────────────────────────────────────

## P-005 — Vocabulario visual y color POR ESTRATEGIA (Strat Vocab & Color)
Estado: diseñada, no iniciada. Candidata a Fable (requiere auditoría
teórica de ~100 técnicas + diseño de vocabulario de técnicas aún no
implementadas). ESTA es la funcionalidad que el dueño quería realmente:
NO un vocabulario global expandido, sino que CADA estrategia declare sus
propias partes visuales nombradas, con color editable por parte.

Hallazgo de arquitectura (auditoría 2026-07-21, código real):
- No hay hardcodeo de colores en los finders. hobiwan centralizó ~17
  "roles semánticos" globales (hint candidate/back, delete/back, fin/back,
  endo-fin, cannibalistic, …), cada uno variable única en Options con
  getter, consumidos por SudokuPanel. ConfigColorPanel YA los edita con
  JColorChooser.
- Por eso "todo lo nuevo se ve verde/rojo/azul": los steps modernos
  reutilizan el vocabulario viejo (celdas de patrón→hint back verde,
  eliminaciones→delete back rojo, guardianes→fin azul). El diccionario de
  roles fue diseñado para fish/cadenas; metemos patrones nuevos en casillas
  semánticas ajenas. Ej. concreto: el guardián de un oddagon NO es una
  aleta de pez, pero le toca el color de aleta por ser lo más parecido.
- Deuda relacionada: hay técnicas LEGACY con vocabulario propio no
  modelado (los bent subsets tienen pivote y pinzas; los UR tienen roof y
  floor). La auditoría de vocabulario debe cubrir legacy, no solo lo nuevo.

Arquitectura objetivo (roadmap por capas):
- Capa 1 — Modelo de roles semánticos: registro de roles; migrar los ~17
  colores legacy a roles compartidos base SIN cambiar comportamiento;
  conectar al ConfigColorPanel existente. Refactor estructural, bajo
  riesgo.
- Capa 2 — Roles POR TÉCNICA: cada SolutionType declara sus partes
  ("pivote", "pinza", "base", "target", "S-cell", "guardián", "lazo",
  "roof", "floor", …), cada una con default distinguible; los formatters
  (modernos y, donde aplique, legacy) usan sus roles propios en vez de
  pedir prestado. Requiere la auditoría teórica de qué partes tiene cada
  técnica — trabajo fino, idealmente con la teoría fresca al implementar
  cada familia.
- Capa 3 — Editor rico por técnica con preview: UI donde ves cada técnica,
  sus partes y editás color por parte viendo un preview en vivo. Swing
  intrincado; mayor riesgo; refinamiento de UX, no fundación.
- Extensión (idea del dueño): COLOREO DE CELDAS COMO LENGUAJE, no solo de
  candidatos. Para exocet (bases/targets/S-cells), patrones de conjunto y
  similares, la celda entera comunica. Introduce un lenguaje visual de
  celda que sirve para señalar múltiples conceptos; abre posibilidades
  amplias. Diseñar junto con la Capa 2 de las familias que lo necesiten.

Secuenciación recomendada: Capas 1+2 dan TODO el valor estructural (el
"framework desde el cual pensar el diseño"); Capa 3 es lujo posterior.
Idealmente los roles de cada familia se declaran cuando la familia se
implementa (exocet nace con su vocabulario), no todo por adelantado.
Criterio de cierre: cada técnica declara sus partes; cada parte tiene
color editable persistido; rendering usa los roles por técnica; coloreo de
celda disponible donde la familia lo requiera.

## P-006 — Alineación de la partición T1/T2 del Uniqueness Pack
Estado: diferida (probable Fable: discriminar tipos canónicos es álgebra
fina). Contexto: en 1.9 se desglosó el pack (Unique Loop, Extended UR,
BUG-Lite, MUG) en Type 1/Type 2 por CARDINALIDAD DE GUARDIANES (T1 = un
guardián en una celda; T2 = dígito uniforme) — es sound, funciona y está
bien etiquetado internamente, pero NO coincide con la taxonomía de tipos de
la literatura: los "tipos" canónicos de UR/UL son estructuralmente
distintos (T1 = una celda con extras; T2 = mismo extra en dos celdas; T3 =
extras formando subset; T4 = strong link; etc.), y BUG-Lite/MUG no tienen
tipos numerados asentados en la comunidad. Reverse BUG no se tocó (no pasa
por la escalera de guardianes; sin subtipos, correcto).
Cambio candidato: alinear las etiquetas con los tipos canónicos donde
existan (UR/UL), o documentar explícitamente en el help que nuestra
partición es por cardinalidad de guardianes y por qué. Requiere
discriminar los tipos canónicos reales al detectar — álgebra fina.
Criterio de cierre: decisión del dueño (alinear / documentar) + coherencia
entre etiquetas mostradas y literatura, o nota de ayuda que lo aclare.

## P-007 — Agrupar find-all-steps analizados en carpetas generales
Estado: PLEGADA al milestone 1.10.1 (barata; se hace junto al modo
restringido del Pattern Finder). Contexto: hoy, tras un análisis, la vista
de all-steps agrupa por estrategia individual; el dueño quiere esas
carpetas-de-estrategia dentro de las carpetas GENERALES por familia (las
mismas del 1.9: Oddagons, Uniqueness, Wings, …). La infra de carpetas por
familia y el toggle lista/carpeta ya existen del 1.9; esto conecta ese
agrupamiento al resultado de un análisis. Bajo riesgo.
Criterio de cierre: el resultado de un análisis se puede ver agrupado por
las carpetas generales de familia, consistente con las pestañas de config.

## P-008 — Mouse & interacción de config/tablero
Estado: dividida; partes 1-2 candidatas a un turno de UI simple, parte 3
delicada (modelo de interacción del tablero).

Parte 1/3 — Checkbox vs nombre en la lista de config (contenido):
Hoy tocar una técnica en la lista siente que activás el checkbox y abrís la
config a la vez. Separar: tocar el checkbox (enable/disable) NO abre la
config; tocar el NOMBRE abre la config (aside) pero NO togglea el enable.
El renderer con checkbox ya existe (CheckNode/CheckRenderer/CheckBoxRenderer);
es reestructurar la interacción de hit-area. Bajo riesgo.

Parte 2/3 — Rueda del mouse = undo/redo (contenido, con matiz de diseño):
No hay ningún MouseWheel listener hoy. Propuesta del dueño: rueda arriba =
deshacer, rueda abajo = avanzar. MATIZ a resolver en el diseño: la rueda es
también scroll; definir dónde aplica (¿solo sobre el tablero? ¿con
modificador? ¿cuando no hay scroll disponible?) para no romper el
desplazamiento. Agregar el listener es contenido; la decisión de alcance es
de diseño.

Parte 3/3 — Colocación de dígitos/candidatos con el mouse (delicada):
Mejor "feeling" al colocar valores y candidatos con el mouse. Toca el
modelo de interacción del tablero (SudokuPanel handleColoring/placement),
que es sensible y fácil de romper. Requiere diseño de interacción cuidado y,
preferentemente, un modelo capaz. Mayor riesgo; no mezclar con lo contenido.
Criterio de cierre (global): las tres partes con su interacción esperada,
sin regresiones en selección/colocación existentes.

## P-009 — Toolbar configurable
Estado: dividida; parte 1 contenida, parte 2 remitida a P-005.

Parte 1/2 — Toolbar personalizable (contenido):
Permitir mostrar/ocultar qué aparece en la toolbar (inclusión/exclusión,
primary/secondary color, dígitos 1-9, XY) y reordenar. Hoy es un array
JToggleButton[10] bien delimitado en MainFrame; hacerlo configurable +
persistir la preferencia es contenido de alcance acotado.

Parte 2/2 — Visualizadores de grupos con color propio (REMITIDA a P-005):
"Visualizar tríos/cuartetos/…/grupos de 9 con color de highlight editable
por cada uno" es exactamente el vocabulario de color por concepto de P-005.
NO implementar por separado: se haría dos veces. Se ejecuta como parte de
P-005 (roles por concepto + coloreo de celda). Aquí queda solo el enganche
de toolbar para dispararlos, una vez que P-005 provea los roles.
Criterio de cierre: parte 1 con toolbar configurable y persistida; parte 2
diferida a P-005.

────────────────────────────────────────────────────────────

## Milestone en curso: 1.10 — Filtro de all-steps por celdas del patrón

Funcionalidad nueva, pedagógica: DESPUÉS de un find-all-steps normal, el
usuario "ilumina" celdas del tablero y la lista de steps del panel se
filtra EN VIVO, mostrando solo las técnicas cuyo PATRÓN incluye alguna
celda iluminada. "Tengo la intuición de que por acá hay algo pero no lo veo
o no conozco la teoría" → ilumino la celda → veo qué estructuras se apoyan
ahí. La selección crece con cada celda (unión: se muestra toda técnica que
toque AL MENOS UNA iluminada).

Naturaleza técnica: NO es búsqueda nueva ni recomputo. Es un predicado de
filtro más sobre la lista de steps YA calculada, montado en el mismo
mecanismo de filtrado del panel que introdujo el buscador del 1.9 (filtra
la vista sin tocar los datos). El árbol de all-steps ya sabe agruparse y
filtrarse. Cero motor nuevo, cero álgebra, cero recomputo del análisis.

Decisiones del dueño (cerradas 2026-07-21):
- Qué cuenta como "toca": SOLO celdas del PATRÓN (estructura de la técnica).
  Las eliminaciones NO cuentan — filtrar por eliminación ya es posible en
  la UI existente; duplicarlo sería ruido.
- Semántica de acumulación: unión creciente (cada celda iluminada agranda
  el conjunto de steps visibles).
- Dos accesos, ambos: (a) trigger por tecla Ctrl — al presionar Ctrl se
  aplica el filtro instantáneamente y, con Ctrl mantenido, cada Ctrl+click
  ilumina otra celda; (b) botón toggle on/off en el panel que activa el
  modo "iluminar", para descubribilidad (los atajos ocultos no se
  descubren solos; el botón enseña que la función existe).
- Todo focalizado en el panel de all-steps; sin ida y vuelta con el resto
  de la UI.

Incluye P-007 (plegada): con el filtro activo o sin él, el resultado del
análisis debe poder verse agrupado por las carpetas GENERALES por familia
(Oddagons, Uniqueness, Wings, …), no solo por estrategia individual —
usando la infra de carpetas y el toggle lista/carpeta del 1.9. Misma zona
de UI, barato.

Detalles a definir en la spec (todos de UI, ninguno de álgebra):
- Feedback visual de las celdas iluminadas en el tablero (resaltado propio,
  distinto del de un hint activo).
- Interacción con el toggle lista/carpeta y con el buscador de texto del
  1.9 (deberían componerse: texto AND celdas).
- Cómo se limpia la selección (soltar Ctrl no debería perderla si se usó el
  botón; definir el modelo).
- Estado vacío (ninguna técnica toca las celdas iluminadas) legible.

Criterio de cierre: iluminar celdas filtra la lista en vivo por celdas del
patrón, con ambos accesos (Ctrl y botón); el resultado se puede agrupar por
carpetas generales (P-007); se compone con el buscador de texto; cero
recomputo, cero álgebra, cero cambios de solve paths. Como todo milestone:
bump de Enhanced version (a 1.10), tests donde aplique (el predicado de
filtro es testeable headless sobre una lista de steps fija), docs + resumen
llano. P-007 marcada como ejecutada en su entrada.
