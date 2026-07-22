# Spec — Uniqueness Pack — milestone 1.8 (Parte B)

Estado: cerrada por el PM, 2026-07-21, desde fuentes. Fuentes leídas hoy:
- SudokuWiki, "Extended Unique Rectangles" (sudokuwiki.org/Extended_Unique_Rectangles):
  patrón mortal 3×3 y forma buscable 2×3.
- Sudopedia (mirror sudopedia.enjoysudoku.com): "Deadly Pattern" (definición
  general, conteos D(n), BUG/BUG-Lite como jerarquía), "BUG Lite",
  "Reverse BUG" (teorema n/n/n), "Reverse BUG Lite".
- Forum: "Forming MUGs from BUG-Lite composites" (t3210): MUGs como
  composites de BUG-Lites, catálogo semilla.

## 0. Marco común (obligatorio)

- Todas estas técnicas ASUMEN unicidad de solución (Raíz 3 del mapa;
  categoría UNIQUENESS; mismas convenciones que el UniquenessSolver legacy).
- Estructura unificada: patrón mortal (deadly = daría ≥2 soluciones si
  quedara "pelado") + GUARDIANES = los candidatos extra que lo impiden.
  La escalera del framework Guardians (1.6) aplica textual — tercer uso:
  |G|=1 en una celda → eliminar de esa celda los candidatos del patrón
  (≡ UR Type 1); |G|>1 uniforme en dígito g → eliminar g de celdas
  externas que vean todas las celdas guardianas (≡ UR Type 2); mixto →
  fuera de alcance v1 (equivalentes de Type 3-6: extensión futura).
- Soundness: las eliminaciones por unicidad jamás borran el dígito de LA
  solución → compatibles con el validador del harness sin cambios.
- Reverse-*: el patrón usa celdas RESUELTAS y exige que NINGUNA sea given
  (un given "ancla" el conjunto inevitable). Espejar el tracking de
  givens del AvoidableRectangle legacy. [Verificar el matiz "without the
  presence of hidden singles" contra la página completa del mirror al
  implementar.]

## 1. Unique Loop (UNIQUE_LOOP)

- Def: lazo CERRADO de L celdas, L PAR ∈ {6, 8, 10, 12} (14+ fuera de
  alcance v1, documentar), todas conteniendo el par {a,b} (+ guardianes),
  celdas consecutivas comparten una casa, y **toda casa del tablero
  contiene 0 o 2 celdas del lazo** (filas, columnas Y cajas — la
  condición que lo hace mortal: pelado, a↔b se intercambian y dan segunda
  solución). El UR es el caso L=4, ya cubierto por el legacy: excluirlo
  del output (dedup a más-específico del registro).
- Guardianes: candidatos ∉ {a,b} en las celdas del lazo. Escalera §0.
- Confianza alta del PM; verificar contra la página "Unique Loop" del
  mirror de Sudopedia al implementar y citarla en el doc de la técnica.
- Detección: emparentada con la enumeración de ciclos del OddagonSolver
  (longitudes pares + condición 0-o-2 en TODAS las casas en vez de pureza
  solo en eslabones). Evaluá reusar/generalizar ese enumerador vs clase
  propia; documentá la decisión.
- Negativo rey: lazo con una casa conteniendo 1 o 3 celdas del lazo.

## 2. Extended Unique Rectangle (EXTENDED_UR)

- Def (SudokuWiki): el patrón mortal completo es 3×3 — nueve celdas del
  triple {a,b,c} ocupando EXACTAMENTE tres filas, tres columnas y tres
  cajas. La forma buscable es la 2×3 (o 3×2): seis celdas del triple en
  dos líneas paralelas de tres, cruzando exactamente TRES cajas (una por
  par). Pelado = múltiples soluciones; guardianes = extras.
- Subtipos en el hint estilo WXYZ (Type 1 = guardianes en una celda;
  Type 2 = dígito uniforme), un solo SolutionType.
- Negativo rey: seis celdas análogas pero en DOS cajas (no es el patrón).
- Nota taxonómica para el registro: ExtUR es un MUG con nombre propio
  (subsumedBy MUG).

## 3. BUG-Lite (BUG_LITE)

- Def (Sudopedia): cualquier patrón mortal cuyas celdas tienen
  EXACTAMENTE 2 candidatos; UR y BUG son casos particulares. Regla
  operativa ("2 o nada", forum): en el patrón pelado, cada dígito aparece
  exactamente 0 o 2 veces en cada fila, columna y caja del patrón.
- Alcance v1 ACOTADO por el conteo de Sudopedia (D(6)=4, D(8)=9, D(9)=3):
  búsqueda de conjuntos de celdas bivalue de tamaño 6, 8 y 9 que cumplan
  la regla 2-o-nada por dígito por casa, con VALIDACIÓN MECÁNICA de
  mortalidad antes de emitir (chequeo de intercambio: el patrón pelado
  admite una segunda asignación válida — implementar el verificador, no
  confiar en la regla sola). Tamaño 4 = UR (excluir del output), lazos
  puros = UL (excluir: dedup a más-específico). 10+ celdas fuera de
  alcance v1.
- Guardianes: extras en celdas del patrón (celdas con >2 candidatos
  participan como "casi-miembro" solo vía guardianes, estilo UR).
- Negativo rey: conjunto bivalue que viola 2-o-nada en una caja.

## 4. Reverse BUG (REVERSE_BUG)

- Def (Sudopedia): para un par de dígitos {a,b}, si TODAS las celdas
  resueltas de a y b formaran un conjunto inevitable, el puzzle no sería
  único → no puede ocurrir → se eliminan los candidatos que lo crearían.
- Teorema implementable (Sudopedia, verbatim adaptado): un conjunto de 2n
  celdas con dos dígitos es inevitable ⟺ ocupa exactamente n filas, n
  columnas y n cajas (n < 9).
- Técnica: para cada par {a,b} y cada celda sin resolver con candidato a
  (o b): si colocarlo dejara las celdas resueltas NO-GIVEN de {a,b}
  formando 2n celdas en exactamente n/n/n → eliminar ese candidato.
- Condición dura: ninguna celda del conjunto puede ser given (§0).
- Negativo rey: mismo conjunto con UN given adentro → NO hay eliminación.
- Reverse BUG-Lite (dos líneas parcialmente llenas formando inevitable):
  documentar como extensión futura, NO implementar en v1.

## 5. MUG (Multivalue Universal Grave) — v1 por catálogo

- Def general (forum): patrones mortales con celdas de >2 candidatos;
  "la mayoría son composites de BUG-Lites". El caso general NO es
  buscable acotadamente → v1 = CATÁLOGO:
  (a) las formas ab/abc de t3210 (el bloque 2×3 con columnas ab|abc|bc y
      sus isomorfos), cada una con su descomposición en BUG-Lites citada;
  (b) NO duplicar el ExtUR (ya es su propia técnica; el registro los
      emparenta).
- Toda forma del catálogo se valida mecánicamente antes de usarse
  (verificador de mortalidad del §3 extendido a celdas multivalor, o la
  descomposición en BUG-Lites verificados).
- Guardianes: extras respecto del contenido nominal de cada celda del
  catálogo. Escalera §0.
- Si el catálogo v1 resulta no cosechable en los corpus, se acepta
  fixtures artesanales validados (documentar), y MUG queda enabled=false
  por default con custodia por fixtures (regla vigente) — decisión
  documentada en el log.

## 6. Integración común

- 5 SolutionTypes nuevos, library codes libres documentados; hints vía
  formatters (subtipos donde aplique); intl.
- Registro: familia UNIQUENESS (raíz 3), aliases de las fuentes,
  subsumedBy: UR→BUG_LITE→(BUG), EXTENDED_UR→MUG, UNIQUE_LOOP→BUG_LITE;
  las dos relaciones bien separadas como siempre.
- Options: categoría UNIQUENESS, enabled=true (salvo MUG según §5),
  scores leyendo la escala de los UR legacy (UR1≈100s… BUG+1) y
  documentando; orden: cerca de la familia UR, específico antes que
  general (UL y ExtUR antes que BUG_LITE antes que MUG).
- enabledProgress=true para las 5 (regla nueva de la Parte A del 1.8).
- Custodia: la de siempre (paths default → corpus; si no, fixtures).

## 7. Fuera de alcance v1 (anotar en docs de técnica)

Tipos 3-6 análogos (subset/strong-link) para UL/ExtUR/BUG-Lite; UL ≥14;
BUG-Lite ≥10 celdas; Reverse BUG-Lite; MUG general/no-catálogo; Hidden
variantes.
