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
- [ ] 1.2 VWXYZ-Wing y mayores (mismo motor, parametrizado n≥5) ← PRÓXIMO
- [ ] 1.3 M-Wing / S-Wing / L-Wing / H-Wing
- [ ] 1.4 Broken Wing (guardianes, 1 dígito — establece el framework oddagon)
- [ ] 1.5 Bivalue Oddagon
- [ ] 1.6 Tridagon* (fixtures: subset T&E(3) del 0.3)
- [ ] 1.7 Pack Uniqueness: Unique Loop, Extended UR, BUG-Lite, Reverse BUG, MUG
- [ ] 1.8 Fireworks*
- [ ] 1.9 3D Medusa (+ trazar hallazgo B del inventario)
- [ ] 1.10 GEM* + integración coloring→generador por estrategia
- [ ] 1.11 APE / ATE
- [ ] 1.12 ALS-W-Wing + Extended Sue de Coq + AHS
- [ ] 1.13 Desglose fish: Siamese/endo/cannibal como ítems configurables estilo HoDoKu;
      revisión de defaults (Kraken, mutants, tamaños ≥5)
- [ ] 2.1 SK-Loop* (clásico 16 celdas → general) + rendering
- [ ] 2.2 MSLS* (formulación DPB; 4×4 clásico → general)
- [ ] 2.3 SET / Anillo de Phistomefel* (instancias geométricas fijas)
- [ ] 3.1 Junior Exocet* → 3.2 Double JE* → 3.3 JE+ (tabla completa)* → 3.4 Senior Exocet*
- [ ] 4.1 Motor AIC v2 (módulo nuevo en paralelo a TablingSolver, diff sobre corpus)
- [ ] 4.2 Nodos ALS / AHS / AALS
- [ ] 4.3 Nodos almost-fish (kraken en cadenas) / almost-UR
- [ ] 4.4 Nishio + jerarquía forcing (Dynamic FC)
- [ ] 4.5 Nested FC
- [ ] 4.6 Destino del TablingSolver (retirar u opción legacy)
- [ ] 5.x Recalibración de dificultad, docs, release

Los ítems con * requieren spec previa en `docs/specs/`. **Fuera de alcance** (decisión del
dueño, 2026-07-20): multifish / rank-0 genérico; SK-Loop, MSLS y SET/Phistomefel se
implementan como patrones dirigidos sin el motor general (ver
`docs/inventario-tecnicas.md`, que reemplaza el detalle de este roadmap).

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
- Regla de custodia: toda técnica nueva queda custodiada por ≥1 puzzle del corpus de
  snapshots cuyo solve path default la contenga (además de sus fixtures library).
- Las técnicas de Fase 2 en adelante requieren un spec previo en `docs/specs/` (se discute
  fuera de Claude Code y se vuelca ahí antes de implementar).
- TablingSolver y el generador no se tocan hasta sus fases respectivas.
- Al cerrar un milestone: tildarlo acá y anotar desvíos o hallazgos en `docs/log.md`.
- Todo cierre de milestone incluye, además del log técnico, un resumen de 3-5 líneas en
  lenguaje llano para el dueño del proyecto (sin jerga de build ni de Java).
