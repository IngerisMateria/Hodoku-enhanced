# CLAUDE.md — Fork moderno de HoDoKu (nombre a definir)

## Objetivo
Fork de HoDoKu (solver/generator/trainer de sudoku, Java/Swing, GPLv3) actualizado a la teoría
moderna de resolución: familia Exocet (JE / double JE / JE+ / SE), SK-Loop, MSLS, multifish /
rank-0, fireworks, 3D Medusa, unicidad extendida (BUG-lite, MUG, reverse BUG, unique loops), y
un motor de cadenas v2 con nodos ALS / AHS / almost-fish / almost-UR.
Base: PseudoFish/Hodoku @ c37fe90 (se reporta como v2.3.2 Build 116; continuación mantenida
del upstream 2.2.0 de hobiwan).

## Decisiones tomadas
- Seguimos en Java/Swing como fork genuino. JDK objetivo: 17 o 21 (LTS).
- El modelo (`Sudoku2`, `SudokuSet*`) y la GUI se conservan; solo se tocan en puntos de extensión.
- Todo código nuevo va en paquetes propios (p. ej. `solver.modern.*`), separado del legacy.
- Rank-0: búsquedas dirigidas por familia de patrones, NO búsqueda genérica universal.
- Licencia GPLv3. Se permite portar código del repo MIT kyoyama-kazusa/Sudoku (C#) con
  atribución (header + archivo NOTICE). [Default provisorio; el dueño puede pedir clean-room.]

## Referencias externas
- Specs: compendio JExocet de David P. Bird + threads de forum.enjoysudoku.com
  (SK-Loop, MSLS, fireworks, multifish).
- Implementación abierta de referencia: github.com/kyoyama-kazusa/Sudoku (MIT, C#).
- Oráculo de comportamiento: YZF_Sudoku (binario cerrado; usar para diffs de pasos encontrados).
- Verificación manual de estructuras rank-0: Xsudo (Allan Barker).

## Mapa del código (medido sobre 2.3.0)
- 108 archivos Java, ~65.6k LOC. El repo NO trae build system (ni Ant ni Gradle): hay que crearlo.
- Recursos DENTRO de `src/`: `img/`, `intl/` (ResourceBundles), `help/`, `templates.dat`.
  Deben terminar en el classpath del jar.
- Punto de entrada: `sudoku.Main` (`src/sudoku/Main.java`). Ojo: muchos diálogos Swing tienen
  `main` de prueba propios; el real es ese.
- Modelo: `src/sudoku/Sudoku2.java` (2.8k; bitmasks por celda + tablas precomputadas),
  `SudokuSet` / `SudokuSetBase` (bitsets de 128 bits). No tocar sin necesidad.
- Dispatcher de técnicas: `src/solver/SudokuStepFinder.java` (1.4k).
- Cadenas legacy: `src/solver/TablingSolver.java` (3.3k; forcing chains/nets/AIC por tablas).
  FRÁGIL: no extender ni tocar hasta Fase 5.
- `src/sudoku/Chain.java`: nodos int-empaquetados con solo 3 tipos (NORMAL/GROUP/ALS).
  Por eso el chain engine v2 será un módulo nuevo, no una extensión de este.
- Catálogo de técnicas: `src/sudoku/SolutionType.java` (~96 entradas).
- GUI: `MainFrame.java` (4.8k), `SudokuPanel.java` (4.3k; acá va el rendering nuevo de patrones).

## Plantilla para agregar una técnica (6 puntos de contacto)
1. Entrada en `SolutionType` + score/categoría default (StepConfig).
2. Finder nuevo en paquete propio, registrado en `SudokuStepFinder`.
3. Strings de hint en `intl/` (solo inglés por ahora).
4. Rendering en `SudokuPanel` si el patrón necesita marcado nuevo.
5. Fixtures: puzzles positivos etiquetados (ubicación la define el milestone 0.2).
6. Tests: el paso se encuentra + pasa el validador de soundness.

## Contrato de testing (no negociable)
- Soundness: ninguna eliminación de ningún paso puede borrar un candidato presente en la
  solución brute-force del puzzle. Corre sobre todo el corpus en CI.
- Snapshot tests del solve path sobre un corpus fijo (detección de regresiones).
- Batch CLI con salida JSON para diffear contra oráculos externos.

## Roadmap
- [x] 0.1 Build reproducible (Gradle) con JDK 17/21 + GitHub Actions CI
- [x] 0.2 Arnés: validador de soundness, batch JSON, snapshot tests
- [x] 0.3 Corpus etiquetado como fixtures (colecciones del foro / base de champagne)
- [x] 1.1 WXYZ-Wing (bent naked subset n=4; establece la plantilla de técnica nueva)
- [x] 1.2 Desglose canónico/general del WXYZ: renombre a Bent Quad, WXYZ-Wing canónico
      (bisagra + 3 alas) como entrada separada, motor bent-subset con n paramétrico
- [x] 1.3 Bent subsets n=5..9 (VWXYZ-Wing .. RSTUVWXYZ-Wing, entradas separadas con
      scores escalonados; defaults: 5 on, 6-9 off, estilo fish)
- [x] 1.4 Registro de técnicas y opciones v1 + experimento T2
- [x] 1.5 Aside v2 + buscador en config + split Kraken T1/T2 (P-002)
- [x] 1.6 Oddagons I: Broken Wing + Bivalue Oddagon (framework guardianes)
- [x] 1.7 Oddagons II: Tridagon* (fixtures: subset te3)
- [x] 1.8 Uniqueness Pack: Unique Loop + Extended UR + BUG-Lite + Reverse BUG + MUG
      (+ Parte A: confiabilidad de find-all-steps y config). MUG enabled=false
      (no cosechable, spec §5); UL/Reverse BUG custodiados por fixtures.
- [x] 1.9 Consolidación UI y taxonomía: desglose T1/T2 de TODO el pack de unicidad con
      escalera de guardianes (Unique Loop, Extended UR, BUG-Lite, MUG; Reverse BUG no
      tiene subtipos), modelo de carpetas por familia (Oddagons, Uniqueness…) en las 4
      superficies, buscador en Progress/Training, toggle lista/carpeta en all-steps,
      memoria de posición de popups (P-003), MUG renombrado a "Multivalue Universal
      Grave", título de ventana "Enhanced vX.X". Cero teoría/álgebra nueva; cero cambios
      de solve path (deducciones idénticas, solo cambian etiquetas).
- [ ] 1.10 3D Medusa + GEM (+ hallazgo B: coloring→generador) ← PRÓXIMO
- [ ] 1.11 Fireworks*
- [ ] 2.x Arco multi-sector*: SK-Loop, MSLS, SET
- [ ] 3.x Arco Exocet*: JE → Double → JE+ → SE
- [ ] 4.x Cadenas: motor AIC v2 (acá vuelven M/S/L/H, Nishio, dynamic/nested)
- [ ] 5.0 Pulido (backlog) · 5.x recalibración, docs

Cola de teoría diferida (para Fable — se difirió el SDC/R0 que estaba en 1.9):
- [Fable] SDC extendido/R0 = régimen R0 del motor bent (locked n=4..9) + extensiones hobiwan
- [Fable] M/L/H/S/T-Wings (AICs de forma fija, requieren motor de cadenas v2)
- [Fable] ALS-W-Wing / Ext-SDC / AHS

Contexto (2026-07-21): modo presupuesto-consciente — maximizar novedad teórica por
iteración hasta una release pública financiable. M/S/L/H y el desglose fino de fish
se difieren al arco de cadenas (4.x); carpetas/presets/resaltado van a backlog (P-004).

Los ítems con * requieren spec previa en `docs/specs/`. **Fuera de alcance** (decisión del
dueño, 2026-07-20): multifish / rank-0 genérico; SK-Loop, MSLS y SET/Phistomefel se
implementan como patrones dirigidos sin el motor general (ver
`docs/inventario-tecnicas.md` y `docs/estrategia-taxonomia.md`, que reemplazan el
detalle de este roadmap).

## Reglas de trabajo
- Un milestone por sesión. No se arranca el siguiente sin el criterio de aceptación en verde.
- Al arrancar cada milestone, su spec/prompt completo se archiva verbatim en
  `docs/milestones/NN.md`.
- Cambios mínimos fuera del alcance del milestone; nada de refactors oportunistas.
- Cada técnica nueva entra con sus fixtures y tests en el mismo commit/PR.
- Principio de desglose (estilo HoDoKu): toda generalización que subsuma casos nombrados
  de la literatura expone esos casos como entradas separadas del solver (SolutionType,
  score, enable y trainer propios), ordenadas antes que la generalización. Precedente:
  Skyscraper / 2-String Kite / Empty Rectangle coexisten con Turbot Fish.
- Regla de custodia: toda técnica nueva enabled por default queda custodiada por ≥1
  puzzle del corpus de snapshots cuyo solve path default la contenga (además de sus
  fixtures library). Para técnicas apagadas por default, la custodia son sus fixtures
  de librería (los snapshots no pueden ejercitarlas).
- Objeción de diseño no bloqueante → entrada en docs/pulido.md con motivación completa;
  se procesan en el milestone 5.0.
- Toda opción de configuración tiene pestaña clásica canónica (GENERAL/SOLVER/ALL
  POSSIBLE STEPS/STEPS/COLORS, pensar en cuál a juzgar por lo que contiene cada una)
  y aparece además en el aside de sus técnicas dueñas.
- Las técnicas de Fase 2 en adelante requieren un spec previo en `docs/specs/` (se discute
  fuera de Claude Code y se vuelca ahí antes de implementar).
- TablingSolver y el generador no se tocan hasta sus fases respectivas.
- Al cerrar un milestone: tildarlo acá y anotar desvíos o hallazgos en `docs/log.md`.
- Todo cierre de milestone incluye, además del log técnico, un resumen de 3-5 líneas en
  lenguaje llano para el dueño del proyecto (sin jerga de build ni de Java).
- Versión del título: EN CADA MILESTONE se bumpea `MainFrame.ENHANCED_VERSION` al número
  del milestone (el título de la ventana, el About y el banner de consola muestran
  `Hodoku - v2.3.2 (Enhanced vX.X)`; la base upstream 2.3.2 no cambia). Es parte del
  criterio de cierre.
