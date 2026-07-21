# Inventario de técnicas — lista del dueño vs código real

Fecha: 2026-07-20. Base auditada: PseudoFish/Hodoku @ c37fe90 (la base del fork).
Método: catálogo extraído de `Options.DEFAULT_SOLVER_STEPS` (90 StepConfigs con flags
solver/allSteps/training) + greps dirigidos en `FishSolver`, `AlsSolver`, `TablingSolver`,
`SolutionType`, `RegressionTester`.

## A. Ítems de la lista que YA están implementados (con evidencia)

| Ítem de la lista | Evidencia en código | Nota |
|---|---|---|
| Sue de Coq (básico) | `SolutionType.SUE_DE_COQ`, habilitado por default | El **Extended** SDC NO está → gap |
| Doubly-Linked ALS-XZ | `AlsSolver.java:362` ("If two ALS are linked by 2 RCs") + `checkDoublyLinkedAls()` | Completo |
| Endo-Finned Fish | `FishSolver`: soporte extenso (`endoFins*` por todo el motor) | Integrado en la búsqueda de fish, no es ítem separado |
| Cannibalistic Fish | `FishSolver` (`cannibalistic*`) + colores propios en `Options` | Ídem |
| Siamese Fish | `FishSolver` + flag `Siamese` en `Options` | Es opción global, no `SolutionType` → desglose pendiente (1.13) |
| Kraken Fish | `SolutionType.KRAKEN_FISH` | **Deshabilitado por default** — por eso "no se ve" |
| Forcing Net | `SolutionType.FORCING_NET` (TablingSolver) | Presente |
| Multiple Forcing Chains | Variantes contradiction/verity de FC/FN en TablingSolver | Nomenclatura ≠ SE; verificar equivalencia exacta en fase 4 |
| Matriz fish completa | 42 StepConfigs: basic/finned/sashimi/franken/mutant × X-Wing..Leviathan | Muchos con default off (mutants: todos off) |

Patrón importante: varios de estos están **apagados por default** (Kraken, mutants, fish ≥5).
Parte del "no lo tiene" observado es en realidad "lo tiene apagado". El desglose de defaults
y exposición es trabajo de config/UI (milestone 1.13), no de lógica nueva.

## B. El caso coloring (hallazgo abierto)

Los defaults dicen `SIMPLE_COLORS` y `MULTI_COLORS`: solver=**true**, allSteps=**true**.
La columna `enabledTraining` es false, pero es false para TODAS las técnicas, así que no
discrimina. La observación del dueño ("coloring no participa del generador de juegos por
estrategia") apunta entonces a la capa de training/generación (filtrado en
`ConfigTrainigPanel` / lógica de generación por técnica) y NO a los StepConfigs.
**Ítem de verificación obligatorio en el milestone 1.10-1.11** (numeración post-1.2): trazar ese filtrado y
documentar la causa real. GEM y 3D Medusa: ausentes, confirmado.

## C. Gap real confirmado (~26 ítems), organizado por familia de implementación

- **Uniqueness** (extender `UniquenessSolver`): Extended UR, Unique Loop, BUG-Lite,
  Reverse BUG, MUG.
- **Oddagon** (familia nueva, lógica de guardianes sobre ciclos imposibles — escalera
  arquitectónica): Broken Wing (1 dígito) → Bivalue Oddagon (2 dígitos) → Tridagon
  (3 dígitos / 4 cajas; la técnica clave de los monstruos post-2022, era Loki/mith).
- **Wings de cadena corta** (finders baratos, AICs de forma fija): M-Wing, S-Wing,
  L-Wing, H-Wing.
- **ALS / bent sets**: WXYZ-Wing, VWXYZ-Wing y mayores (motor parametrizado por n),
  ALS-W-Wing, Extended Sue de Coq, APE, ATE, AHS como paso nombrado. AALS: como paso
  aporta poco; su valor pleno es como nodo de cadenas → fase 4.
- **Coloring**: 3D Medusa, GEM, + integración coloring→generador (resolver hallazgo B).
- **Multi-sector**: Fireworks*, SK-Loop*, MSLS*, SET / Anillo de Phistomefel*.
- **Exocet**: JE*, Double JE*, JE+ (reglas extendidas)*, Senior Exocet*.
- **Cadenas** (fase 4): Nishio, Dynamic FC, Nested FC, y nodos ALS/AHS/AALS/
  almost-fish/almost-UR en AICs. (Nishio podría adelantarse como finder standalone
  si el dueño lo pide: no toca TablingSolver.)

Los ítems marcados con * requieren spec previa en `docs/specs/` antes de implementar
(fuentes primarias citadas, no memoria del modelo).

## D. Roadmap revisado (reemplaza la sección Roadmap de CLAUDE.md desde 1.1)

- [x] 1.1 WXYZ-Wing (bent naked subset n=4; establece la plantilla de técnica nueva)
- [x] 1.2 Desglose canónico/general del WXYZ: Bent Quad + WXYZ-Wing canónico + motor
      bent-subset con n paramétrico (renumeración del roadmap: 2026-07-21)
- [ ] 1.3 Bent subsets n=5..9 (VWXYZ-Wing .. RSTUVWXYZ-Wing, entradas separadas con
      scores escalonados; defaults: 5 on, 6-9 off, estilo fish)
- [ ] 1.4 M-Wing / S-Wing / L-Wing / H-Wing
- [ ] 1.5 Broken Wing (guardianes, 1 dígito — establece el framework oddagon)
- [ ] 1.6 Bivalue Oddagon
- [ ] 1.7 Tridagon* (fixtures: subset T&E(3) del 0.3)
- [ ] 1.8 Pack Uniqueness: Unique Loop, Extended UR, BUG-Lite, Reverse BUG, MUG
- [ ] 1.9 Fireworks*
- [ ] 1.10 3D Medusa (+ trazar hallazgo B)
- [ ] 1.11 GEM* + integración coloring→generador por estrategia
- [ ] 1.12 APE / ATE
- [ ] 1.13 ALS-W-Wing + Extended Sue de Coq + AHS
- [ ] 1.14 Desglose fish: Siamese/endo/cannibal como ítems configurables estilo HoDoKu;
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

**Fuera de alcance** (decisión del dueño, 2026-07-20): multifish / rank-0 genérico.
Nota: SK-Loop, MSLS y SET/Phistomefel son instancias fijas de esa misma teoría de
conjuntos; se implementan como patrones dirigidos sin el motor general, consistente
con la decisión de "búsquedas dirigidas".

## E. Fuentes de corpus verificadas (hoy, con URL viva)

1. **Hilo "The hardest sudokus (new thread)"** — forum.enjoysudoku.com, t6539.
   Base de champagne `ph_2010.zip` (~3,1M puzzles "potential hardest", ago-2020;
   el "2010" NO es el año), enlazada al Google Drive de champagne desde el primer
   post. El paquete incluye archivos de propiedades con tags de exocet (E / EE
   double); en releases tempranos, 24.410 puzzles tagueados con exocet. Verificar
   tags SK-loop al ingerir.
2. **GitHub denis-berthier/Sudoku-classif** — colecciones clasificadas con URLs
   estables (ideal para ingesta automatizada): colección T&E(3) 2023 (~55.088
   minimales, portadores del patrón tridagon) y colección T&E(2). Atribución y
   licencia a documentar en la ingesta. (La base T&E(3) completa de mith ronda
   1,07M puzzles, depth 3; el subset de GitHub alcanza para fixtures.)
3. **JExocet Compendium** — forum.enjoysudoku.com, t32370. Los .odt de David P.
   Bird (01 Compendium, 02 JE Definition v2b, ... 14 = Senior Exocet) adjuntos en
   el hilo. Es la spec de la fase 3.
4. **Ya en fixtures** (0.2): Easter Monster (SK-loop/JE), Golden Nugget,
   Platinum Blonde.
5. **Pendiente de ubicar** (no bloquea 0.3): hilo de Fireworks con ejemplos;
   listas SK-loop dedicadas.

## F. Formato de fixtures por técnica

HoDoKu trae un tester de regresión nativo: `src/sudoku/RegressionTester.java`
(hobiwan / MaNik-e Team), que corre casos por técnica con soporte de subvariantes,
técnicas que colocan valores y **casos de fallo** ("ninguna instancia de la técnica
debe encontrarse") — es decir, fixtures negativos anti-falsos-positivos. Milestone
0.3 evalúa reutilizar su formato de librería como formato v2 de fixtures
"estado → técnica esperada" antes de inventar uno propio.
