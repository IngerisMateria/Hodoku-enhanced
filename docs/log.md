# Log de milestones

## 1.7 — Oddagons II: Tridagon — 2026-07-21

Hecho, en cinco commits lógicos (spec+prompt / detector / integración /
fixtures+custodia / docs). Spec del PM en `docs/specs/tridagon.md` (manda sobre
cualquier interpretación); prompt archivado verbatim en `docs/milestones/1.7.md`.

1. **Detector** — `solver.modern.TridagonSolver` (clase propia: la detección por
   rectángulos de cajas × transversales no comparte nada con la enumeración de
   ciclos de `OddagonSolver`; lo compartido es exactamente `Guardians`, reusado
   SIN cambios de contrato). Enumeración acotada según spec §4: 84 triples ×
   transversales elegibles por caja (≤6 permutaciones locales filas→columnas,
   filtradas por elegibilidad v1: celda sin resolver con los TRES dígitos) × 9
   rectángulos de cajas × producto cartesiano; poda de triples sin 4 cajas
   pobladas. Condición 3 vía signos: producto = −1. **Hallazgo estructural
   aprovechado**: la permutación de monodromía (componer los enlaces
   fila/columna alrededor del rectángulo) tiene paridad = producto de los 4
   signos; es transposición ⟺ paridad impar ⟺ la descomposición en ciclos de
   las 12 celdas es EXACTAMENTE rectángulo de 4 + lazo de 8 (paridad par da
   3×4-ciclos o un 12-ciclo) — el detector la computa y de ahí salen gratis el
   lazo y el rectángulo del rendering. Escalera de guardianes de la spec §3
   completa (|G|=1 elimina el triple de la celda guardiana; uniforme → visión
   común; mixto → skip documentado (tridagon links/ORk, fase 4); cero →
   log+skip, contrato 1.6). Dedup tipo+celdas+deducción (mecanismo 1.2/1.6).
   Ajuste al buffer de `Guardians`: MAX_GUARDIANS 64→128 (12 celdas × hasta 6
   candidatos extra = 72 > 64; cambio de capacidad interna, no de contrato).
2. **Integración** — `TRIDAGON` (library 1402, el código reservado desde 1.6;
   arg `trid`), display "Tridagon", aliases "Trivalue Oddagon" y "Thor's
   Hammer". Score 500 / EXTREME / orden 5695 (documentado en Options: sobre BO
   440 y mutant fish 450, pasada la meseta 470 "computer only", a la par de los
   kraken 500 porque rompe puzzles T&E(3), pero orden justo tras sus hermanos
   oddagon y antes de ALS 5700 — es baratísimo de enumerar y es EL titular de
   la modernización). Categoría clásica MISCELLANEOUS (el cajón de BO). Familia
   ODDAGON en el registro con refs de la spec (t39885 de mith, t39859 de
   Berthier); sin padres de subsunción (las cadenas no re-derivan el argumento
   de paridad — por eso rompe T&E(3)). Hint vía `TridagonFormatter` con la
   justificación corta del §2. Layout del step: values=triple, indices=lazo de
   8 EN ORDEN + rectángulo, fins=guardianes (dígitos fuera del triple) +
   celdas del rectángulo con el triple (solo display). All-steps: `getAllTridagons`
   + gate en FindAllSteps (patrón 1.6). StepRecord: sin caso nuevo (solo
   eliminaciones, cae al default correcto).
3. **Rendering (verificado con captura)** — chain de display del lazo de 8
   (cerrado, primer nodo repetido, links strong) sobre el menor dígito del
   triple; SudokuPanel intacto. En pantalla: lazo con flechas rojas recorriendo
   las 8 celdas alternando fila/columna, candidatos del triple en verde en las
   celdas del lazo, el rectángulo en azul (fins), el guardián en azul y las
   eliminaciones en rojo — lazo y rectángulo se distinguen a simple vista.
4. **Cosecha y métricas** (paths default con validación inline vs brute force):
   te3-200 → **129/200 puzzles con Tridagon en el path default** (129 pasos, 1
   por puzzle); all-steps 805 instancias en 14.316 estados, |G|: G1=742 G2=60
   G3=3; modos: 742 single / 63 uniforme; **0 violaciones de soundness**.
   te2-100 → 0 instancias en 8.813 estados (colección T&E(2), pre-tridagon: el
   negativo teórico esperado, sanity del corpus). corpus-37 → 9 instancias
   all-steps (todas |G|=1) pero 0 en paths default (los 37 snapshots previos no
   cambian). Pasada all-steps: 12 µs promedio por estado (máx 28 ms con el
   warm-up incluido) — enumeración acotada confirmada.
5. **Fixtures** (`libs/tridagon.txt`, registrado en LibraryCaseRunnerTest) — 4
   positivos de te3: te3#0 (|G|=1, próximo paso default, custodiado), te3#5
   (|G|=1, otra geometría de bandas/stacks), te3#7 (multi-guardián uniforme,
   próximo paso default, custodiado), te3#195 (uniforme con eliminación doble
   externa). 3 negativos near-miss de puzzles distintos: **te3#30 = el negativo
   rey, split de paridad PAR (2/2) con todo lo demás válido** (guardianes
   uniformes con visión común incluidos: un detector de siluetas emitiría paso);
   te3#1 (caja sin transversal); te2#72 (4 cajas con transversales y producto
   −1 pero fuera del rectángulo bandas/stacks). **Desvío documentado**: el
   negativo de paridad par NO existe naturalmente en los 3 corpus (donde hay
   silueta completa casi siempre hay también un combo impar que deduce, y los
   estados par-solos nunca tienen guardianes deducibles); se sintetizó desde el
   estado inicial real de te3#30 podando SOLO candidatos no-solución (el estado
   queda sound), método anotado en el propio fixture.
6. **Custodia** — te3#0 y te3#7 sumados al corpus de snapshots (37→39). Diff de
   snapshots: +2 líneas nuevas (cada una con 1 paso TRIDAGON y solved:true),
   0 líneas existentes modificadas.
7. **Fuera de alcance v1** (spec §7, anotado en registro/javadoc): celdas
   2-de-3; patrones de 6 cajas; Patto Patto / Fryer's Ring / Socks / Broken
   Windmill (candidatos a backlog); tridagon links / virtual pairs / ORk
   (fase 4); anti-tridagon.

**Resumen para el dueño**: el programa ya encuentra el Tridagon, el patrón
estrella de los sudokus más difíciles que se conocen (los que ningún método
clásico rompe). De los 200 puzzles monstruo de la colección de prueba, 129 lo
usan en su camino de resolución — y el solver ahora los explica con una pista
en texto y un dibujo: un lazo de flechas recorriendo 8 celdas más un rectángulo
marcado aparte, con la celda "salvavidas" resaltada. Dos de esos puzzles quedan
de guardia permanente en las pruebas automáticas: si alguien rompe la técnica
sin querer, las pruebas avisan solas. También quedó una trampa deliberada: un
tablero casi idéntico al patrón real pero matemáticamente inofensivo, para
comprobar que el detector entiende la lógica y no matchea por silueta.

## 1.6 — Oddagons I: Broken Wing + Bivalue Oddagon (framework guardianes) — 2026-07-21

Hecho, en cuatro commits lógicos (framework / broken wing / bivalue oddagon /
custodia+docs). Prompt archivado verbatim en `docs/milestones/1.6.md`.

1. **Framework de guardianes** — `solver.modern.Guardians` (buffer reusable:
   colección de candidatos guardianes + `uniformDigit()` + intersección de
   buddies; 1.7/Tridagon lo reusa tal cual) y `solver.modern.OddagonSolver`
   (motor de ciclos impares L∈{5,7}: DFS canónico — celda mínima como inicio,
   segunda < última para matar la dirección inversa, la asignación de casas es
   parte de la identidad del ciclo). Condición de pureza: cada casa-eslabón
   contiene exactamente sus dos extremos del ciclo — para BW es requisito duro
   de soundness (una tercera celda del ciclo en la casa rompe el argumento de
   enlaces conjugados), para BO es conservador y mantiene la definición
   canónica (documentado en el javadoc). L=3 excluido (colapsa en básicas);
   L≥9 fuera de alcance v1 (documentado). Escalera de deducciones: |G|=0 →
   estado contradictorio, log y skip (jamás "eliminar todo"); |G|=1 → BW
   coloca d en la celda guardiana (paso de colocación), BO elimina {a,b} de la
   celda guardiana; |G|>1 uniforme → eliminar g de toda celda externa que ve
   todas las celdas guardianas; |G|>1 mixto (solo BO) → sin deducción en 1.6
   (territorio de cadenas; anotado en registro y javadoc). Layout del step en
   campos base (`values`=dígito(s), `indices`=ciclo EN ORDEN, `fins`=
   guardianes, colocación marcada por candidatesToDelete vacío con target en
   fins[0]); pasos de colocación exigieron caso propio en
   `harness/StepRecord.from()` (espeja doStep; plantilla actualizada).
2. **Integración** — `BROKEN_WING` (library 1400, arg `bw`) y
   `BIVALUE_ODDAGON` (1401, `bvo`): rango 14xx nuevo para la familia (09xx es
   ALS del upstream; 1402 reservado Tridagon). Scores leyendo la escala
   vecina: BW UNFAIR 240 / orden 5370 (post-coloring, pre-X-Chain; zona
   Sashimi Swordfish 240–SDC 250, bien arriba de la familia turbot HARD
   120-150), BO UNFAIR 440 / orden 5690 (pre-ALS; zona RSTUVWXYZ 440 / mutant
   450, bajo la meseta EXTREME 470). Ambas enabled. Categorías clásicas:
   BW SINGLE_DIGIT_PATTERNS, BO MISCELLANEOUS (no se agregó
   SolutionCategory nueva: legacy intacto; la familia moderna vive en el
   registro). Familia `ODDAGON` en el registro (rama "rank −1 / dark logic"
   §2.d del mapa, raíz taxonómica propia sin padres), aliases "Guardians" /
   "Single-Digit Oddagon" (BW) y "Bi-Value Oddagon" (BO). Hook all-steps:
   `getAllOddagons` público + llamada gateada en el case 19 de FindAllSteps
   (desvío documentado en plantilla: familias no-wing no contaminan
   getAllWings; precedente del toque 1.5).
3. **Rendering (decisión)** — chain de display construido por el finder (ciclo
   cerrado, primer nodo repetido, links strong) y SudokuPanel lo dibuja SIN
   modificaciones: el loop sale con flechas sobre el candidato del ciclo (en
   BO, sobre el menor de los dos). Resaltado estándar: ciclo verde, guardianes
   azul (fins), eliminaciones rojo. Verificado en ambas capturas.
4. **Cosecha y métricas** (26.431 estados de los paths default de
   corpus 35 / te2 100 / te3 200): 7.832 pasos oddagon, **0 violaciones de
   soundness** (validación inline contra brute force). BW 6.303 (L5 3.054 /
   L7 3.249; corpus 606, te2 2.805, te3 2.892), BO 1.529 (L5 678 / L7 851;
   corpus 25, te2 9, te3 1.495 — la técnica es de monstruos, como predice la
   teoría). Distribución |G|: BW G1=966 G2=2.465 G3=1.802 G4=783 G5=200 G6=73
   G7=13 G8=1; BO G1=679 G2=433 G3=269 G4=146 G5=2. Colocaciones: 966 (todos
   los |G|=1 de BW). Pasada all-steps oddagon: 410,9 s totales ≈ 15,5 ms/estado
   (enumeración acotada; el costo real del replay lo pone el tabling legacy).
5. **Fixtures** — `libs/broken-wing.txt` (4 positivos: L5 default-path del
   snapshot, L5 estado inicial, L7, colocación |G|=1; 2 near-miss: ciclo par
   L6, casa-eslabón repetida) y `libs/bivalue-oddagon.txt` (4 positivos: L5
   default te3#112, L7 corpus, |G|=1, L5 default te3#59; 3 near-miss: ciclo
   par, casa repetida, celda sin el par completo). Todos de puzzles distintos
   por categoría; ninguno construido a mano (todo apareció natural).
6. **Custodia** — BW: el puzzle EXTREME línea 52 del corpus ganó 1 paso BW en
   su path default (diff de snapshot: 1 línea, r7c4<>5). BO: 74 estados
   default-step en 43 puzzles, TODOS te3 → se sumaron te3#112 (5 pasos BO,
   solve 642 ms) y te3#59 (1 paso, 272 ms) al corpus de snapshots (+2 líneas).
7. **GUI** — 2 capturas revisadas con config fresca en estados default-step:
   BW corpus#35 (`Broken Wing: 5 in r4c3,r4c9,r8c9,r8c1,r5c1 (guardians:
   r7c9,r8c5,r7c1) => r7c4<>5`, loop con flechas) y BO te3#112 (`Bivalue
   Oddagon: 3/7 in r2c3,r3c2,r3c6,r9c6,r9c3 (guardians: 8 in ...) =>
   r13c3<>8`).

Desvíos / hallazgos: (i) el tool de cosecha scratch quedó "colgado" 70 min
tras terminar en 17 — las factories legacy arrancan un thread housekeeping
no-daemon y la JVM no muere sin `System.exit()`; diagnóstico por jstack (main
ya era DestroyJavaVM). Regla para tools scratch: `System.exit(0)` siempre.
(ii) El |G|>1 de BW es siempre uniforme (un dígito); el mixto solo existe en
BO y quedó sin deducción directa, como pide la spec. (iii) Los dos StepConfigs
nuevos no migran a hcfg viejos (limitación conocida de la plantilla, misma
que 1.1-1.3).

**Resumen llano:** el programa aprendió dos técnicas nuevas de la familia
"oddagon": anillos impares de celdas que serían imposibles si no fuera por
unos pocos candidatos "guardianes" que los desarman — y de ahí se deduce qué
eliminar o incluso qué número colocar. La maquinaria de guardianes quedó
armada como pieza reutilizable: la próxima técnica de la escalera (Tridagon,
la de los sudokus monstruo modernos) la va a enchufar directo. Las dos
técnicas quedaron activas por defecto, con sus casos de prueba, sus puzzles
custodios y verificación visual en pantalla.

## 1.5 — Aside v2 + buscador en config + split Kraken T1/T2 (P-002) — 2026-07-21

Hecho, en cuatro commits lógicos (kraken / aside / buscador / cierre). Prompt
archivado verbatim en `docs/milestones/1.5.md`.

1. **Split Kraken T1/T2 (P-002 cerrada, detalle en docs/pulido.md)** —
   `KRAKEN_FISH_TYPE_1` (índice 8450, hereda el slot del genérico) y `TYPE_2`
   (8460) con StepConfig propia, ambas derivadas del genérico (EXTREME /
   LAST_RESORT / score 500 / off en solver, all-steps y training). Decisión
   sobre la entrada genérica: **retirada de la lista de config** (la GUI
   muestra solo Type 1/Type 2); la fila KRAKEN_FISH del registro queda como
   ancla taxonómica sin StepConfig (excepción `CONFIGLESS_ROWS` en el test de
   completitud, que ahora exige además las dos filas nuevas). El colapso de
   `SolutionType.getStepConfig()` se eliminó; `FishSolver` gana
   `krakenTypeFilter` (pedir un tipo nunca devuelve el otro; el request
   genérico KRAKEN_FISH sigue buscando ambos por compatibilidad);
   `FindAllSteps` corre kraken si cualquiera de los dos está on y filtra el
   resultado por tipo; `setTestType` normaliza el arg batch `kf` → kf1+kf2
   (quirk preexistente: el check de `/bt` en Main filtraba los pasos kraken
   incluso antes del split; `kf1`/`kf2` ahora funcionan de punta a punta).
   Migración de hcfg pre-split en `Options.migrateKrakenFishStepConfig()`
   (idempotente, preserva enabled/score/level/category/flags; roundtrip en
   `KrakenConfigMigrationTest`). Fixture `test/fixtures/kraken-split.txt`:
   estado te3 (puzzle #2 del subset, básicos jugados) con 22 T1 + 179 T2 bajo
   los parámetros del test; `KrakenSplitFixtureTest` asierta que con T2 off el
   all-steps devuelve solo T1, que el estado contiene ambos tipos, y que el
   finder honra el tipo pedido. Snapshots: cero cambios (kraken off por
   default) — suite completa verde antes y después.
2. **Aside v2** (`ConfigSolverPanel`) — al seleccionar técnica muestra:
   descripción del registro, "Other names:" (aliases), selector "Show as:"
   (persistencia ENUM=alias del 1.4; bufferizado en el diálogo y aplicado en
   okPressed → semántica OK/Cancel intacta), y las opciones cuya DUEÑA es la
   técnica, editables in situ. Sincronización con la pestaña clásica por
   **modelos Swing compartidos** (ButtonModel/ComboBoxModel/Document de los
   controles de ConfigStepPanel, expuestos vía `getOptionEditor(key)`): ambas
   vistas coinciden siempre y el valor se escribe en Options una sola vez, en
   el okPressed clásico — el aside es espejo puro. Multi-dueño: etiqueta
   "also affects: X, Y, +N more" (testigos: allowDualsAndSiamese con +41,
   opciones kraken con "also affects: Kraken Fish Type 2"). El caso
   afectada-no-dueña (grisado + click navega al dueño) quedó implementado
   estructuralmente (`addOptionRow(..., owned=false)` +
   `affectedNotOwnedOptions()`, hoy vacío — sin instancias, como se esperaba).
   Los nombres preferidos se renderizan ahora en TODAS las listas de config
   vía `StepConfig.toString()` → registro (lista y árbol del solver, árbol de
   find-all-steps, combos de training/progress); hints legacy sin tocar
   (limitación ya anotada en el log del 1.4).
3. **Buscador** — campo de filtro sobre nombre + aliases + descripción
   (predicado `TechniqueRegistry.matches`, case-insensitive). En el solver:
   en la toolbar lista/árbol, filtra ambas vistas; mientras hay filtro activo
   se deshabilita el reordenamiento (Up/Down y drag&drop — los índices
   filtrados no mapean al orden de resolución). Reuso en Find-All-Steps: SÍ
   resultó barato — mismo predicado, campo montado como columnHeader del
   scrollpane del árbol (cero cirugía del layout generado).
4. **Verificación GUI** — 4 capturas revisadas: (a) aside ALS-XZ con
   descripción + "Allow overlap" editable y su alcance; (b) aside Skyscraper
   con "Allow Duals/Siamese" y "also affects: … +41 more"; (c) lista del
   solver con Kraken Fish Type 1 y Type 2 separados (aside de T1 con sus 6
   opciones fish/kraken); (d) "disjoint" filtra a Sue de Coq (matchea
   descripción "Two-Sector Disjoint Subsets" y alias TSDS).

Desvíos/hallazgos: ninguno de alcance; dos notas. (i) El probe headless de
javac se congela si `src/` está en el classpath de compilación (sourcepath
implícito) — compilar tools scratch solo contra `build/classes/java/main`.
(ii) `FindAllSteps.run()` termina con `Thread.interrupt()` sobre su propio
thread: los tests que lo invocan inline deben limpiar el flag
(`Thread.interrupted()`), ya contemplado en `KrakenSplitFixtureTest`.

**Resumen para el dueño (en llano):** El panel del solver ahora te cuenta qué
es cada técnica: al tocar una, al costado aparece su explicación, sus otros
nombres, y podés elegir con cuál nombre querés verla en todo el programa.
Las opciones que gobiernan esa técnica se editan ahí mismo (son las mismas de
la pestaña Steps, no un duplicado), y si una opción también afecta a otras
técnicas, te lo avisa. Arriba de la lista hay un buscador: escribís
"disjoint" y aparece Sue de Coq aunque el nombre no se parezca. Y el pedido
viejo del Kraken quedó resuelto: Type 1 y Type 2 ahora se prenden y apagan
por separado — podés buscar solo los Type 1 (los humanamente encontrables)
sin que la computadora te inunde con cientos de Type 2. Las configuraciones
guardadas se convierten solas al formato nuevo.

### Follow-up 1.5 (2026-07-21): nombre preferido en TODO el pipeline legacy

Reporte del dueño tras el cierre: el nombre preferido no llegaba a la pestaña
Training ni al tablero (hints vago/concreto de la pestaña Puzzle, área de
texto del next hint, y las vistas Summary / All possible steps / Solution
path). Causa: esas superficies resuelven nombres por
`SolutionStep.getStepName()` (instancia + 2 estáticos) o por
`SolutionType.getStepName()` directo, no por `StepConfig.toString()`.

Fix central: los tres `SolutionStep.getStepName` rutean ahora por
`TechniqueRegistry.getDisplayName` — con eso todo `toString(0/1/2)` (diálogos
de hint vago/concreto, área de hint, solution path, print) sale con el nombre
preferido. Call sites directos restantes ruteados: árbol de Training
(ConfigTrainigPanel), árbol y comparadores de AllStepsPanel (ordena por
nombre mostrado), SummaryPanel, árbol/diálogo de ConfigProgressPanel y
`Options.getTrainingStepsString` (label de training). La limitación "hints
legacy sin display names" anotada en 1.4/1.5 queda LEVANTADA.

Seguridad: el arnés batch/snapshots es inmune (StepRecord persiste
`getType().name()`, los códigos library no se tocan); custodia nueva en
`DisplayNamePropagationTest` (preferencia → getStepName×3, toString(0),
StepConfig.toString; identidades enum/library invariantes; quitar la
preferencia restaura el default). Verificado además con probe GUI: pestaña
Training mostrando "Y-Wing" (XY-Wing renombrado) y los textos exactos de
toString(0/1/2) con un nombre custom.

## 1.4 — Registro de técnicas y opciones v1 + experimento T2 — 2026-07-21

Hecho, en cinco commits lógicos (docs del dueño / arranque / registro / experimento
/ cierre). Contexto nuevo del milestone: modo presupuesto-consciente — roadmap
pendiente reescrito (M/S/L/H y desglose fino de fish diferidos al arco de cadenas
4.x; carpetas/presets/resaltado → P-004), regla nueva de pestaña canónica + aside,
P-002 plegada al 1.5, P-003 (posición de popups) y P-004 al backlog.

1. **Docs del dueño commiteados** — `docs/sudoku_mapa_relacional.md` (mapa por
   subsunción, 4 raíces) y `docs/estrategia-taxonomia.md` (regímenes R1/R0,
   arquitectura de 3 capas). Son la semilla del DAG del registro.
2. **Registro** (`solver.modern.registry`, SIN UI): `TechniqueInfo` por cada
   StepConfig — 97 filas (90 legacy + 7 modernas; INCOMPLETE/GIVE_UP excluidos como
   pseudo-pasos) — con display default = nombre intl (no pueden divergir), aliases
   del mapa (SDC/foro incluidos), descripción 1-3 líneas, familia, motor, DAG de
   subsunción multi-padre y régimen R1/R0. Las dos relaciones pedidas quedaron
   separadas: taxonomía en `TechniqueInfo.subsumedBy`, propiedad de configuración
   en `OptionInfo.owners` (la herencia del aside 1.5 va por la segunda).
   `OptionInfo`: TODO ConfigStepPanel inventariado (21 opciones, pestaña STEPS),
   descripciones derivadas del código consumidor. Hallazgos de semántica real:
   `allowAlsOverlap` NO gobierna el doubly-linked (getAlsXZInt chequea el segundo
   RC siempre; la opción solo excluye pares de ALS solapados de la colección de
   RCs, exige pinzas disjuntas en el XY-Wing y pétalos disjuntos en Death
   Blossom); `onlyOneAlsPerStep`, pese al nombre, solo dedupea ALS-XY-Chain y
   Death Blossom por conjunto de eliminaciones (prefiere menos celdas ALS) y no
   toca XZ ni XY-Wing; `maxFins` no aplica al kraken (tiene `maxKrakenFins`);
   endo-fins solo existen en franken/mutant (dueños de `maxEndoFins`). Testigo de
   multi-dueño: `allowDualsAndSiamese` → Skyscraper + 2-String Kite + ER + los 42
   fish. `useZeroInsteadOfDot` quedó sin dueños (excepción documentada: es formato
   de clipboard, no opción de técnica — desvío justificado de la regla "≥1").
3. **Display-name v1** — preferencia por técnica persistida en `Options` como
   string `ENUM=alias;...` (bean property nueva `techniqueDisplayNames`, XMLEncoder
   la arrastra gratis; claves = nombres de enum, estables por el invariante duro
   del §3 de la estrategia). API en `TechniqueRegistry.getDisplayName/
   setPreferredDisplayName`, consumida por los DOS formateadores de `ModernStep`
   (bent + canónico). Hints legacy intactos (incremento futuro anotado).
4. **Tests de custodia** (`test/registry/TechniqueRegistryTest`): completitud en
   ambas direcciones (StepConfig sin fila rompe el build — obliga a registrar toda
   técnica futura — y fila sin StepConfig también), DAG acíclico (DFS tricolor),
   aliases sin colisiones exactas (case-insensitive, incluye nombres default),
   claves de opciones verificadas contra Options por reflexión, dueños
   registrados, inventario ConfigStepPanel completo por lista espejo de
   okPressed(), y roundtrip de la preferencia de display-name.
5. **Experimento T2** (`test/harness/T2RegimeExperiment`, reporte en
   `docs/experimentos/t2-regimen.md`): sobre 26.435 estados de los solve paths
   default (corpus 35 + te2-100 + te3-200), 114 canónicos cosechados (33 T1 /
   81 T2, 37 patrones distintos): **100 % régimen R1** (exactamente una Z no
   restringida), **100 % cubiertos por un ALS-XZ simple de un solo RCC**, **cero**
   necesitaron estructura doblemente enlazada. "Type 2 = Sue de Coq" refutado con
   datos propios; el umbral del §8-4 del mapa se resuelve a favor de la línea 99.
   P-001 actualizada con los datos. Runtime 99 s. (Clasificación simple/doble vía
   endo-fins del paso: el AlsSolver registra ahí los dígitos RC — 1 = simple.)

Desvíos / hallazgos:

- Suite verde SIN regenerar snapshots: la indirección de display-name con
  preferencia vacía devuelve exactamente los nombres de siempre, así que ningún
  hint ni solve path cambió. Cero UI nueva, legacy tocado solo en Options
  (property nueva) — dentro de la lista blanca.
- La cuenta real es 90 legacy + 7 modernas = 97 técnicas configuradas (el prompt
  estimaba ~96+8): WXYZ canónico + Bent Quad + 5 bent grandes son 7, no 8.
- El experimento reusó `HarnessRunner.analyze` + replay manual de StepRecords;
  el 100 % de cobertura ALS-XZ sobre canónicos contrasta con el 94,2 % del
  diagnóstico 1.1 sobre bent quads generales — la diferencia es del motor legacy
  sobre patrones no canónicos, no de la teoría (anotado en el reporte).
- `T2RegimeExperiment` corre con `-Djava.util.Arrays.useLegacyMergeSort=true`
  (quirk TimSort de SolutionStep.compareTo ya documentado en build.md) y termina
  con `System.exit(0)` (threads no-daemon).

Cierre: pusheado y Actions en verde.

### Resumen para el dueño del proyecto

Este milestone no agregó jugadas nuevas: construyó el fichero maestro donde cada
una de las 97 técnicas del programa tiene su ficha — nombre, otros nombres con que
la conoce la literatura, explicación corta, de qué técnica mayor es caso especial,
y qué opciones de configuración la afectan (con la explicación real de cada opción,
leída del código, incluidas dos que engañaban por el nombre). Sobre ese fichero se
montará la configuración nueva del milestone que viene. Además el programa ya
puede recordar tu nombre preferido para cada técnica. Y zanjamos con datos la
polémica que traías del foro: revisamos las 114 apariciones del WXYZ-Wing clásico
en nuestras colecciones y ninguna — cero — es un Sue de Coq disfrazado; tu
intuición del mapa era correcta y quedó documentada para el futuro help.

## 1.3 — Bent subsets n=5..9 + backlog de pulido — 2026-07-21

Hecho, en cuatro commits lógicos:

1. **Backlog de pulido** (mecanismo nuevo) — `docs/pulido.md` con P-001 (subtipos del
   WXYZ canónico) y P-002 (separar Kraken T1/T2), regla nueva en CLAUDE.md y el ítem
   5.0 en el roadmap. Prompt del milestone archivado en `docs/milestones/1.3.md`.
2. **Las cinco técnicas** — `VWXYZ_WING`(0805)/`UVWXYZ_WING`(0806)/`TUVWXYZ_WING`
   (0807)/`STUVWXYZ_WING`(0808)/`RSTUVWXYZ_WING`(0809) (codes = huecos siguientes de
   los wings 08xx, verificados libres); registro en `TYPE_BY_SIZE` del motor y UN
   formateador bent compartido en `ModernStep` (misma instancia para n=4..9).
   Options: índices 5655/5660/5665/5670/5675 (entre Bent Quad 5650 y ALS-XZ 5700),
   scores 280/320/360/400/440 — +40 por celda extra desde 280 (n=5 apenas debajo de
   ALS-XZ 300: se divisa como un ALS-XZ fácil), intercalando con la familia ALS
   (320/340/360) y con techo 440 < 470, el score "solo computadora" de los fish
   grandes. Defaults estilo fish: n=5 enabled; n=6..9 off con `allSteps=true`
   (existen, desglosadas, apagadas — find-all-steps las lista si se habilitan).
   El código de producción nuevo son ~30 líneas: el motor paramétrico del 1.2 hizo
   todo el trabajo.
3. **Fixtures** — 5 archivos nuevos en `test/fixtures/libs/` (23 casos): vwxyz-wing
   4 positivos (todos pasos de PATH DEFAULT de te2/te3, diversos: fila/columna, celda
   de intersección, eliminación simple/múltiple) + 3 negativos near-miss de puzzles
   distintos (unión 6 / dos no-restringidos / naked quint); u/t/s/rstuvwxyz-wing 4
   positivos cada uno (corpus + te2 + te3, estados tempranos y profundos). Registrados
   en `LIB_FILES`.
4. **Custodia + cierre** — te2#23 (1 VWXYZ default) y te2#52 (2 VWXYZ default) sumados
   al corpus de snapshots (33→35).

Métricas (all-steps sobre los estados de los solve paths default, metodología 1.1/1.2):

- corpus (33 puzzles, 2.046 estados, 3,2 s): n5=18.188 n6=12.297 n7=4.945 n8=1.283
  n9=221. te2-100 (8.802 estados, 39,5 s): n5=57.624 n6=34.799 n7=13.036 n8=2.824
  n9=153. te3-200 (15.395 estados, 56,9 s): n5=71.493 n6=39.094 n7=14.794 n8=3.468
  n9=538. Sanity de performance OK: la pasada completa (incluye replay + brute force
  por puzzle) tarda ~100 s; los tamaños grandes no explotan (regiones de ≤15 celdas
  acotan la combinatoria).
- Revalidación soundness de TODO lo cosechado: **444.432 pasos bent (n=4..9) contra
  brute force, 0 violaciones**.
- Paths default: VWXYZ aparece en 7 puzzles te2 (8 pasos: #23, #32, #35, #52×2, #53,
  #69, #90) y 7 puzzles te3; n=6..9 en ninguno (apagadas, como corresponde). En el
  corpus de 33 previo: 0 → por eso la custodia nueva.
- Custodia: snapshot diff = exactamente 2 líneas nuevas (te2#23: 1×VWXYZ_WING;
  te2#52: 2×VWXYZ_WING), 0 líneas modificadas. Antes de agregarlas se verificó que
  habilitar VWXYZ NO cambió ningún path de los 33 (suite verde sin regenerar).
- GUI verificada sobre la ventana real (config fresca, te2#32 estado 8 donde VWXYZ es
  el próximo paso default): hint `VWXYZ-Wing: 1/4/6/8/9 in r1c89,r3c89,r4c8 (Z=9) =>
  r2c8<>9`, candidatos del set en verde, Z=9 del patrón en azul, eliminación en rojo;
  captura revisada.

Desvíos / hallazgos:

- **La rareza esperada de n=6..9 no existe en all-steps**: hasta 538 RSTUVWXYZ en
  te3 y cientos en el corpus fácil (estados tempranos con máscaras gordas). NO hubo
  que construir ningún positivo a mano (el prompt lo preveía por tamaño sin caso
  natural). La rareza real es otra: con sus scores/orden casi nunca ganan un path
  default, y n≥6 son de valor humano dudoso — de ahí los defaults apagados.
- Matiz de la regla de custodia (CLAUDE.md + plantilla): custodia por snapshots solo
  para técnicas enabled por default; para las apagadas, la custodia son sus fixtures
  de librería (el runner usa `getStep(tipo)` directo, que no mira el enable — por eso
  los fixtures de n=6..9 corren igual).

Cierre: pusheado y Actions en verde.

### Resumen para el dueño del proyecto

La familia del WXYZ quedó completa: el programa ahora conoce las versiones de 5, 6,
7, 8 y 9 celdas como jugadas separadas, cada una con su nombre de la literatura y su
puntaje. La de 5 quedó activa (aparece de verdad en los puzzles duros: la vimos en
pantalla con su explicación y colores); las de 6 a 9 existen pero vienen apagadas,
igual que los peces gigantes, porque son jugadas que solo una computadora encontraría
— el que quiera puede prenderlas en las opciones. Verificamos 444 mil apariciones
contra la solución real sin un solo error, y dos puzzles de guardia nuevos vigilan la
de 5. Además estrenamos el cuaderno de pulido: las objeciones de diseño que no frenan
el trabajo quedan anotadas ahí (arrancó con dos) para resolverlas todas juntas al
final del proyecto.

## 1.2 — Desglose canónico/general del WXYZ + motor paramétrico — 2026-07-21

Hecho, en cuatro commits lógicos:

1. **Renombre** — `SolutionType.WXYZ_WING` del 1.1 pasó a `BENT_QUAD` (display "Bent
   Quad", argName `bq`), conservando el library code `0802`: el fixture (renombrado a
   `bent-quad.txt`) siguió andando sin tocar. Verificado: 0/30 snapshots contenían la
   técnica, ninguno cambió. **El enum viejo muere**: saves `.hsol` de estos días que
   referencien `WXYZ_WING` (o la clase `WxyzWingStep`, ver refactor) quedan huérfanos —
   aceptable pre-release.
2. **Refactor custodiado** — `WxyzWingSolver` → `solver.modern.BentSubsetSolver` con n
   paramétrico vía `TYPE_BY_SIZE` (n=4 = Bent Quad; 1.3 registra 5..9); enumeración de
   subconjuntos recursiva en orden lexicográfico, idéntico al de los loops fijos del
   1.1 (primer paso estable). `WxyzWingStep` → `ModernStep`, subclase ÚNICA de
   `SolutionStep` para todo el fork con registro de formateadores de hint por
   `SolutionType` (cero campos nuevos; los finders registran el suyo en un static).
   Dedup del nit del 1.1: en all-steps, pasos iguales (tipo + celdas + eliminaciones)
   se emiten una sola vez. Criterio duro cumplido: suite verde SIN regenerar snapshots.
3. **WXYZ-Wing canónico** — `SolutionType.WXYZ_WING` nuevo (código `0804`, siguiente
   hueco de los wings 08xx), finder propio `solver.modern.WxyzWingSolver`: bisagra +
   3 alas bivalue {a,Z}, Tipo 1 (Z en la bisagra) / Tipo 2 (Z fuera), naked quad
   excluido; el tipo se reporta en el hint (estilo subtipos de fish) y se DERIVA de los
   campos base (bisagra ∈ fins ⟺ Tipo 1) para respetar el cero-campos-nuevos de
   `ModernStep`. Options: 5640/score 230, inmediatamente antes de Bent Quad (5650/250);
   ambos enabled; solapamiento deliberado sin filtrado (principio de desglose).
   Fixtures `wxyz-wing.txt`: 3 positivos T1 + 2 T2 + 3 negativos near-miss de puzzles
   distintos (el obligatorio: el Bent Quad del screenshot del 1.1 — te2#4, r156c7+r2c8,
   Z=5 — que NO es canónico; más un naked-quad degenerado y una estructura sin
   eliminaciones).
4. **Custodia + docs** — regla de custodia nueva (en CLAUDE.md y la plantilla) aplicada
   retroactivamente: +3 puzzles te2 al corpus de snapshots.

Métricas (corpus + te2-100):

- Paths default: el canónico aparece en te2#47 y te2#48 (ambos Tipo 2); Bent Quad en
  20 pasos repartidos en 16 puzzles te2; corpus: 0 y 0 (igual que en 1.1). Vs. 1.1:
  los 22 pasos "WXYZ" en 18 puzzles ahora son 20 Bent Quad + 2 canónicos — el orden
  5640 < 5650 hace que el canónico gane donde ambos existen, como se buscaba.
- All-steps sobre los 10.557 estados de esos solve paths: 91 pasos canónicos (26 Tipo
  1 — 7 corpus, 19 te2 — y 65 Tipo 2 — 9 y 56) contra 80.980 Bent Quads. Solapamiento:
  exactamente los 91 canónicos tienen un Bent Quad con mismas celdas, Z y eliminaciones
  idénticas (91/80.980 Bent Quads son también canónicos). O sea: en este corpus NO
  apareció ninguna instancia canónica del caso fila+columna+caja que el motor bent no
  cubre — la razón de que sea finder propio sigue siendo teórica, no empírica.
- Revalidación soundness de todo lo cosechado: 81.071 pasos de ambos finders sobre los
  10.557 estados contra brute force, **0 violaciones**.
- Custodia: snapshot diff = exactamente 3 líneas nuevas (te2#8: 3×BENT_QUAD; te2#47 y
  te2#48: 1×WXYZ_WING cada uno), 0 líneas modificadas. Suite verde antes y después.
- GUI verificada sobre la ventana real (config fresca, te2#47 donde el canónico es el
  próximo paso default): hint `WXYZ-Wing Type 2: 3/7/8/6 in r13c6,r2c35 (Z=6) =>
  r2c4<>6`, bisagra r2c5 {3,7,8} y letras en verde, Z=6 de las alas en azul,
  eliminación en rojo; captura revisada.

Desvíos / hallazgos:

- `gradlew run` ya existía desde el 0.1 (plugin `application`); el QoL nuevo es
  `hodoku.bat` (doble clic → `gradlew -q jar` + `javaw`), documentado en
  `docs/build.md`. Quirk Windows: con `NoDefaultCurrentDirectoryInExePath` seteado,
  `cmd` no resuelve `gradlew.bat` del directorio actual — el .bat usa ruta explícita
  `%~dp0gradlew.bat`.
- Renumeración del roadmap (el nuevo 1.3 = bent subsets n=5..9): además de CLAUDE.md
  se corrieron las referencias numéricas en `docs/inventario-tecnicas.md`,
  `docs/corpora.md` y la cabecera de `te3-mith-200.txt` (Tridagon ahora es 1.7).
- El formateador de hints del canónico vive en un static de su finder (se registra al
  cargar la clase); si alguna vez un `ModernStep` se deserializa sin que el finder
  haya cargado, `toString` tiraría — irrelevante hoy (la GUI siempre inicializa los
  solvers antes), anotado por si aparece.

Cierre: pusheado y Actions en verde.

### Resumen para el dueño del proyecto

Ordenamos la familia del WXYZ como pediste: lo que hicimos en el milestone pasado
quedó con su nombre correcto ("Bent Quad", la forma general) y ahora el programa
también conoce el WXYZ-Wing clásico de los libros, como jugada separada que se
muestra primero cuando las dos aplican. El motor quedó preparado para las versiones
más grandes (de 5 a 9 celdas), que son el próximo milestone. Verificamos todo igual
que siempre: 81 mil apariciones contra la solución real sin un solo error, tres
puzzles de guardia nuevos que vigilan que estas jugadas no se rompan en el futuro, y
la jugada nueva vista en pantalla con su explicación. De regalo: ahora hay un archivo
`hodoku.bat` en la carpeta del proyecto — doble clic y se abre el programa, sin
consola ni comandos.

## 1.1 — WXYZ-Wing (primera técnica nueva + plantilla) — 2026-07-20

Hecho: WXYZ-Wing en formulación bent naked subset (n=4) como primera técnica del fork:
`solver.modern.WxyzWingSolver` (paso único + all-steps, espejo de `WingSolver`) +
`solver.modern.WxyzWingStep`; integración legacy limitada EXACTAMENTE a los 4 puntos
permitidos (`SolutionType` — código library `0802`, hueco libre de los wings 08xx —,
`Options` — StepConfig 5650/UNFAIR/WINGS/score 250, antes de ALS-XZ —,
`SudokuStepFinder` — registro + inclusión en `getAllWings()`, con lo que el "find all
steps" de la GUI lo lista sin tocar `FindAllSteps` —, `intl/SolutionType.properties`);
fixtures library `test/fixtures/libs/wxyz-wing.txt` (4 positivos diversos + 3 negativos
near-miss, registrados en `LIB_FILES` del runner); `docs/plantilla-tecnica.md` con la
receta ejecutada; quirks de threads no-daemon y de `SolutionStep.compareTo` en
`docs/build.md`.

Métricas (punto 7 del milestone):

- El corpus de snapshots tiene **30** puzzles (el prompt decía 33; los 3 extremes no
  forman parte de los snapshots): **0/30 cambiaron de solve path y 0 pasos WXYZ**
  aparecen. Es genuino, no un bug de integración: con orden 5650 (después de X-Chain
  5500 / Nice Loop 5600) en esos 30 siempre gana antes una técnica más barata.
- Donde sí muerde: en los paths default del subset T&E(2) (`--batch-json` sobre
  `te2-eleven-100.txt`) aparecen **22 pasos WXYZ-Wing en 18 de 100 puzzles**.
- Cosecha: 5.967 estados positivos distintos sobre los solve paths de corpus+te2;
  revalidación fuerte de TODO lo cosechado contra brute force: **15.282 estados,
  85.552 pasos WXYZ hallados, 0 violaciones de soundness**, 0 inconsistencias
  presencia/ausencia en los negativos.
- Diagnóstico ALS-XZ (no assert): 5.622/5.967 (94,2%) de los positivos tienen un
  ALS-XZ del AlsSolver con eliminaciones que cubren las del WXYZ en el mismo estado;
  los 345 restantes son ALS-XZ que el AlsSolver legacy no emite (el fixture 4 del lib
  documenta uno).
- GUI verificada sobre la ventana real (config fresca, estado te2#4 donde WXYZ es el
  próximo paso default): hint `WXYZ-Wing: 3/4/7/5 in r156c7,r2c8 (Z=5) =>
  r3c7,r45c8<>5`, resaltado estándar correcto (celdas verde, Z azul, eliminaciones
  rojo), captura revisada.

Desvíos / hallazgos:

- **`SolutionStep.toString(int)` tira RuntimeException para tipos desconocidos** y no
  está en la lista de archivos modificables → cada técnica del fork trae subclase de
  `SolutionStep` con su `toString` (cero campos nuevos; clone/XMLEncoder intactos).
  Patrón acuñado en `WxyzWingStep` y documentado en la plantilla. Alternativa
  descartada: agregar el case al switch legacy (5° archivo fuera de la regla).
- Configs `hodoku.hcfg` guardadas por builds previos no traen el StepConfig nuevo y el
  merge de `readOptions` no lo agrega: con config vieja la técnica queda invisible en
  la GUI. Anotado en la plantilla (config fresca para probar).
- El caso "0 no-restringidos" (locked set puro) queda explícitamente fuera del 1.1;
  anotado como extensión futura en el javadoc del finder.
- El corpus de snapshots no ejercita WXYZ; si se quiere que los snapshots custodien la
  técnica habría que sumar 2-3 puzzles T&E(2) al corpus — NO se hizo (el corpus fijo es
  la base del determinismo del 0.2; decisión del dueño si vale la pena).
- `AlsSolver.getAllAlses` dispara `Comparison method violates its general contract!`
  (TimSort) al ordenar listas grandes — legacy conocido, workaround en `docs/build.md`.

Cierre: pusheado y Actions en verde.

### Resumen para el dueño del proyecto

El programa ya conoce su primera jugada nueva: el WXYZ-Wing, en su forma moderna y más
general. La probamos a fondo: la buscamos en miles de posiciones intermedias de los
puzzles de prueba y verificamos contra la solución real que ninguna de las 85 mil
apariciones comete un error. En los 100 puzzles duros de la colección, la jugada
aparece de verdad en 18 y ninguno de los resultados anteriores se rompió. También la
vimos funcionando en la pantalla, con su explicación y su resaltado de colores, como la
vería un usuario. Y lo más importante para lo que viene: quedó escrita la receta paso a
paso que vamos a repetir para las ~25 técnicas que siguen.

## 0.3 — Corpus etiquetado como fixtures — 2026-07-20

Hecho: roadmap de CLAUDE.md reemplazado por la versión del inventario de técnicas
(`docs/inventario-tecnicas.md`, sección D); `data/corpora/` gitignoreado con tarea
`fetchCorpora` (descargas pineadas a commit fijo de denis-berthier/Sudoku-classif);
subsets curados commiteados: 200 T&E(3) de mith (futuros fixtures de Tridagon, 1.6) y
100 T&E(2) de eleven, con procedencia en cabecera y en `docs/corpora.md`; runner de
librerías (`harness.LibraryCaseRunner` + `LibraryCaseRunnerTest`) reutilizando el
formato LIBRARY nativo de HoDoKu como formato v2 de fixtures por técnica, con 4 casos
de ejemplo (W-Wing / Sue de Coq, positivos y negativos); smoke del subset T&E(3).

Suite nueva sobre la del 0.2: 300 tests de unicidad (`CorporaSubsetTest`), 20 de smoke
T&E(3) (`Te3SmokeTest`, muestra determinística 1 de cada 10) y el runner con sus casos.

Desvíos / hallazgos:

- El repo Sudoku-classif trae `Sample1000`/`Sample500` curados por el propio Berthier
  dentro de la colección T&E(3): el subset de 200 sale de ahí (1 de cada 5), no de la
  colección completa — mejor muestra que cualquier curado propio. Licencia GPLv3
  (misma que el fork); commit pineado `15c9d21`.
- Smoke completo (una vez, al cierre): los 200 T&E(3) por `analyze()` + soundness →
  0 violaciones, 65,6 s total, peor puzzle 1,2 s; y por CLI `--batch-json` → 200
  líneas JSON, 0 errores, 200 `solved:true`. Con la config default caen rápido al
  brute force (esperado: no hay técnica que los muerda todavía). En CI queda la
  muestra de 20 para no pagar los 200 en cada push.
- TablingSolver escupe `WARNING: Too many candidates` por logging en varios T&E(3);
  es ruido conocido del legacy, no afecta resultados (soundness verde). Anotado para
  la fase 4/5 (destino del TablingSolver).
- `RegressionTester` legacy no sirve programático (resultados por stdout, contadores
  privados): se reutiliza su **formato**, no su código. El runner nuevo asserta solo
  presencia/ausencia de la técnica; subvariantes `-N` rechazadas explícitamente
  (se agregan si un milestone de fase 1 las necesita). Justificación en
  `docs/harness.md`.
- Drive de champagne (`ph_2010.zip`): descarga automatizada imposible — carpeta con
  ID pre-2021 sin `resourcekey` público, Google fuerza login anónimo (verificado por
  navegador y HTTP directo). Link y pasos manuales en `docs/corpora.md`; **queda en
  manos del dueño** bajarla a `data/corpora/champagne/`.
- El primer post de t6539 (editado oct-2019) menciona `ph_1910.zip`; la versión
  ago-2020 (`ph_2010.zip`) debería estar en la misma carpeta de Drive.

Cierre: pusheado y Actions en verde.

### Resumen para el dueño del proyecto

Trajimos las dos colecciones de sudokus imposibles que sirven de banco de pruebas: 200
"monstruos" del tipo que solo se rinde con la técnica Tridagon (la estrella de la fase
1) y 100 duros de la generación anterior. Verificamos que los 300 tienen solución única
y que el programa los procesa sin colgarse ni cometer errores (hoy los resuelve "por
fuerza bruta", que es lo esperado: la técnica que los destraba se construye más
adelante). Además dejamos armado el molde para los ejercicios de práctica de cada
técnica nueva: archivos de casos que dicen "en esta posición tiene que aparecer tal
jugada — y en esta otra NO", probado con dos técnicas que el programa ya conoce. La
base gigante de champagne (3 millones de puzzles) hay que bajarla a mano porque Google
pide login: los pasos están anotados, te aviso aparte.

## 0.2 — Arnés de verificación — 2026-07-20

Hecho: paquete `harness` nuevo (batch JSON, validador de soundness, extractor de pasos),
source set de tests en `test/` con JUnit 5, corpus de 30 puzzles + 3 extremos como fixtures,
snapshot tests con tarea `updateSnapshots`, y `docs/harness.md` con esquema y uso. Único
toque fuera del paquete: hook de `--batch-json` al inicio de `sudoku.Main`. `gradlew build`
ya arrastraba `test`, así que el workflow de CI no se tocó.

Suite: 71 tests en verde (30 corpus + 3 extremos + 31 snapshots + 4 del validador con pasos
sintéticos + 3 e2e del modo batch).

Desvíos / hallazgos:

- `Options.getInstance()` autolee un `hodoku.hcfg` de `%TEMP%` si existe: la config de
  técnicas (y por lo tanto el solve path) dependería de la máquina. El arnés fuerza
  `Options.resetAll()` antes de resolver; el modo `--batch-json` se saltea todo el parsing
  legacy y la carga de config.
- Los nombres visibles de niveles y técnicas salen de ResourceBundles (cambian con el
  idioma). El JSON usa los nombres de enum (`DifficultyType`, `SolutionType`), que son
  estables entre máquinas — clave para que los snapshots pasen igual acá y en CI.
- Semántica de placements relevada de los `doStep()`: solo SimpleSolver (singles),
  BruteForceSolver, TemplateSolver y TablingSolver (forcing chains/nets con verity)
  colocan valores; todo el resto solo elimina candidatos. `StepRecord.from` replica eso.
- Archivos escritos por PowerShell 5.1 llevan BOM UTF-8 y rompían la primera línea del
  batch; el reader lo tolera (con test).
- Easter Monster verificado: solución única (igual que Golden Nugget y Platinum Blonde,
  los otros dos extremos elegidos). Los tres terminan en ~1s con la config default
  (brute force habilitado como última técnica).
- El corpus salió en 87 intentos de generación (la distribución del generador default
  cubre UNFAIR/EXTREME más seguido de lo esperado). Generado una vez y commiteado;
  regenerarlo invalidaría los snapshots.

Cierre: pusheado y Actions en verde (mismo workflow del 0.1; `build` ejecuta la suite).

### Resumen para el dueño del proyecto

Le pusimos al proyecto su red de seguridad. Desde ahora, cada vez que subamos un cambio,
un robot resuelve una colección fija de 33 sudokus (de fáciles a monstruos famosos) y
verifica dos cosas: que ningún paso del programa contradiga jamás la solución real del
puzzle, y que el camino de resolución no haya cambiado sin que nos demos cuenta. Además
el programa ahora puede escupir, por consola, la resolución completa de cualquier lista
de puzzles en un formato que otras herramientas pueden leer, para comparar contra
programas de referencia. Probamos la alarma a propósito con pasos falsos: suena.

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
