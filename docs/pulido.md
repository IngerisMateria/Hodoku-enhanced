# Backlog de pulido — objeciones de diseño diferidas

Se procesan en el milestone 5.0, cuando no quede nada más en el roadmap.
Cada entrada registra el porqué completo, para que el futuro no tenga que
reconstruirlo.

## P-001 — Presentación de subtipos del WXYZ-Wing canónico
- Fecha: 2026-07-21 · Origen: dueño · Estado: diferido (se queda como está)
- Contexto: el hint reporta "Type 1/2" (T1: Z en la bisagra; T2: Z ausente
  de la bisagra).
- Objeción: no hay consenso teórico sobre esta taxonomía; SudokuWiki usa
  dos tipos y en sus comentarios objetan que el Tipo 2 es (o se solapa
  con) un Sue de Coq. Al dueño le gusta la salida actual; el riesgo es
  cosmético: reclamos de puristas post-release.
- Cambio candidato al pulir: hacer configurable u omitible la etiqueta de
  subtipo, y/o documentar en el help nuestra taxonomía y su relación con
  SDC / ALS-XZ / bent sets (proyecciones de la misma álgebra de dos ALS
  con comunes restringidos).
- Nota técnica: el subtipo NO se almacena — se deriva (T1 sii la bisagra,
  primer índice del paso, aparece en fins). Cambiar la presentación es
  tocar solo el formateador en ModernStep: costo mínimo, cero migración.
- Datos (experimento del 1.4, docs/experimentos/t2-regimen.md): la objeción
  "Type 2 = Sue de Coq" quedó refutada empíricamente — los 114 canónicos
  cosechados sobre corpus+te2+te3 (33 T1, 81 T2) son 100 % régimen R1
  (exactamente una Z no restringida) y el 100 % se cubre con un ALS-XZ
  simple de un solo RCC; cero necesitaron estructura doblemente enlazada.
  La respuesta documentada para el help ya existe; lo que queda de esta
  entrada es solo la decisión de presentación (mantener/configurar/omitir
  la etiqueta) + volcar la explicación al help.
- Criterio de cierre: decisión explícita del dueño (mantener / configurar
  / omitir) + help actualizado.

## P-002 — Separar Kraken Fish Type 1 y Type 2 en la configuración
- Fecha: 2026-07-21 · Origen: dueño (dolor real del HoDoKu base) ·
  Estado: plegada al milestone 1.5 (el des-colapso es acotado: el enum ya
  tiene TYPE_1/0371 y TYPE_2/0372; solo la config los colapsa —
  `SolutionType.getStepConfigType()` mapea ambos a KRAKEN_FISH)
- Contexto verificado en la base: una sola StepConfig KRAKEN_FISH
  (default off) gobierna todo kraken; los tipos 1/2 que se ven en los
  listados no tienen enable, score ni orden propios. Consecuencia:
  imposible habilitar T1 sin T2 en find-all-steps, en el solver y en la
  generación por técnica.
- Motivación del dueño (sus palabras): "hodoku siempre se cuelga
  analizando all possible steps porque hay cientos de kraken fish type 2
  y ninguno te sirve como deducción porque igual nunca los encontrarías;
  es una estrategia de computadora; en cambio los type 1 sí es posible".
- Cambio deseado: StepConfigs separadas para T1 y T2 (enable, score y
  orden propios), respetando el principio de desglose, visibles y
  filtrables en las tres superficies: find-all-steps, solver y
  training/generador.
- Alcance técnico estimado: SolutionType/Options/StepConfig, paneles de
  config, el camino kraken de FishSolver; confirmar al implementar cómo
  se representa hoy el subtipo (enum vs texto del paso); migración de
  hcfg viejos (una sola entrada). Snapshots no deberían cambiar (kraken
  default off).
- Nota de planificación: encaja naturalmente en el milestone de desglose
  de fish (hoy 1.14) si el dueño decide adelantarlo; por defecto, 5.0.
- Criterio de cierre: en la GUI se puede habilitar T1 sin T2 en las tres
  superficies, y un all-steps sobre un puzzle kraken-pesado termina en
  tiempo razonable con T2 apagado.

## P-003 — Memoria de posición de popups
- Fecha: 2026-07-21 · Origen: dueño (queja de uso) · Estado: diferido
- Contexto: los diálogos/popups de la GUI (config, find-all-steps,
  training, etc.) se abren siempre en su posición default; el usuario
  los mueve y la posición se pierde al cerrar.
- Cambio deseado: todos los popups recuerdan dónde los movió el usuario.
  Si una implementación global es eficiente (interceptar en un ancestro
  común / helper compartido que persista bounds por clase de diálogo en
  Options), preferirla a tocar diálogo por diálogo.
- Nota: QoL puro, sin relación con el registro de técnicas
  (estrategia-taxonomia.md §5, "nueva queja 1").
- Criterio de cierre: mover un diálogo, cerrarlo, reabrirlo (incluso tras
  reiniciar la app) → reaparece donde quedó.

## P-004 — Carpetas/presets/resaltado de solapamiento (UX de config avanzada)
- Fecha: 2026-07-21 · Origen: dueño · Estado: diferido post-release por
  presupuesto (decisión 2026-07-21, modo presupuesto-consciente)
- Contexto: el registro de metadatos (milestone 1.4) deja lista la fuente
  de datos; la UX rica encima se difiere. Diseño completo en
  estrategia-taxonomia.md §5 y en el prompt archivado de 1.4
  (docs/milestones/1.4.md).
- Alcance diferido:
  - Carpetas del solver: 3 vistas (por familia, por convención de
    nomenclatura, jerárquica por las 4 raíces del mapa del dueño) +
    vistas custom del usuario. Las vistas son SOLO visuales: el orden de
    ejecución vive únicamente en la lista plana exhaustiva, y una técnica
    puede repetirse en varias carpetas.
  - Presets de enable para find-all-steps (máscaras de enable guardadas;
    propuesta inicial: "Wings", "ALS", "Cadenas", "Fish", "Unicidad",
    "Clásico HoDoKu").
  - Resaltado de solapamiento: técnicas con relaciones "interfiere con"
    marcadas en la lista; al abrirlas, lista de interferencias (datos ya
    presentes en el registro como taxonomía/overlaps).
- Criterio de cierre: decisión del dueño post-release sobre qué subconjunto
  entra, con el registro como única fuente de datos.
