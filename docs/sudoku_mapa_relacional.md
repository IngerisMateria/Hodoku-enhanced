# Mapa Conceptual Relacional de las Técnicas de Sudoku

## Cómo leer este mapa

El principio de orden es **la subsunción, no la categoría pedagógica**. Las familias tradicionales (Wings, Fish, ALS, Chains) son justamente las que ocultan las relaciones, así que aquí **no** son el eje. El eje es: *si A es un caso particular de B, A cuelga de B* (la indentación = "es caso particular de").

Solo hay **cuatro lógicas raíz**. Todo lo demás son casos particulares suyos:

1. **AIC** — cadenas de inferencia alternada (la lógica de "cadena").
2. **Lógica de conjuntos base/cover** — la *rank theory* de Allan Barker (la lógica de "patrón estático": peces, subconjuntos/ALS, patrones rank-0 grandes, rank−1).
3. **Unicidad** — asume solución única; **no se reduce** a las otras tres.
4. **Forzado** — la envolvente más general; **AIC ⊂ Forzado**.

Las raíces 1 y 2 **se solapan masivamente**: un patrón monodígito es a la vez pez y X-Chain; un ALS-XZ es a la vez conjunto y cadena. Por eso muchos objetos tienen **doble pertenencia**: figuran una vez en su "casa" y se cruzan con marcadores. Los marcadores son:

- `=` → es literalmente **el mismo objeto** con otro nombre.
- `↔` → **cruce** con otra rama (el objeto vive en dos sitios).
- `⚠` → **zona gris / disputa** (el nombre es convención, no teoría).
- **[+]** → técnica real que **no estaba en tu lista**, añadida.

El corazón del informe está en la **§5 (Cruces y zonas grises)**: ahí está recopilado *todo* lo que "es lo mismo que otra cosa". Si dudás de por qué algo está donde está, esa sección lo arbitra.

*Fuentes generales, con sus defectos: HoDoKu (catálogo técnico más completo, pero descontinuado); SudokuWiki/Andrew Stuart (divulgativo dominante, pero es donde vive el disputado "WXYZ-Wing Type 2"); Sudopedia (solo en espejos); New Sudoku Players' Forum / forum.enjoysudoku.com (origen de casi toda la teoría avanzada); CSP-Rules/Denis Berthier; sudoku.allanbarker.com / sudokuone.com (rank theory); taupierbw, sysudoku, Cracking the Cryptic Wiki.*

---

## RAÍZ 1 — CADENAS DE INFERENCIA ALTERNADA (AIC)

Una AIC alterna enlaces fuertes y débiles entre nodos. Los nodos pueden ser candidatos sueltos, **grupos** o **ALS**. Casi todo lo que llamamos "wing", "turbot", "coloreo" o "cadena" es un caso particular de esto.

- **AIC continua (Nice Loop)** vs **AIC discontinua** (reglas 1 y 2 de SudokuWiki).
- **Nodos de grupo → Grouped AIC** (un Empty Rectangle puede actuar como enlace).
- **Nodos ALS → AIC-con-ALS** ⟶ *esta es la puerta a TODA la Raíz 2b*: un ALS-XZ **es** una AIC con un nodo ALS. Por eso la familia ALS cuelga a la vez de aquí y de la lógica de conjuntos.

### 1.a — Un solo dígito → **X-Chain** (= X-Cycle / X-Loop cuando cierra en bucle)
- **Longitud 3 → Turbot Fish** — 2 enlaces fuertes + 1 débil, un dígito. `⚠ también se describe como pez con aleta` ↔ *ver §5-1*.
  - **Skyscraper** — dos enlaces fuertes paralelos en líneas. `= dos Sashimi X-Wings` (HoDoKu).
  - **2-String Kite** — dos enlaces fuertes perpendiculares (fila + columna) unidos en una caja.
  - **Empty Rectangle (ER)** — el miembro con nodo de grupo en caja.
    - **Dual ER** (HoDoKu) — cuando el candidato eliminado forma par conjugado (doble eliminación).
    - **[+] Grouped Skyscraper / Grouped 2-String Kite** — los hermanos "grouped" de la tríada.
    - **Dual 2-String Kite** (HoDoKu).
- **Remote Pair** — cadena de celdas bivaluadas idénticas (XY). Frontera con el coloreo ↔ *ver 1.d*.

### 1.b — Celdas bivaluadas → **XY-Chain**
- **Longitud 3 → XY-Wing (Y-Wing)** — pivote + 2 pinzas. `= Bent Triple = ALS-XZ mínimo` ↔ *ver §5-2 y Raíz 2b*.
  - **XYZ-Wing** — XY-Wing con pivote **trivaluado**.
- **W-Wing** — dos bivalores remotos (XY)…(XY) unidos por enlace fuerte en un dígito ("par desnudo semirremoto"). Patrón VLV.

### 1.c — AIC cortas con nombre (mnemónicos; 2–3 dígitos, 3–4 enlaces)
**Todas son AIC.** Sus nombres son etiquetas de reconocimiento visual, no un sistema de nomenclatura (StrmCkr: *"w, m, s, l, h wings no existirían por sí solos: son todos nice loops/AIC"*). Origen: hilos del foro (m-wings, Split-Wing, Local-Wing, Hybrid-Wings).
- **M-Wing** (M = Medusa) — 1 bivalor + 2 enlaces fuertes conjugados sobre 2 dígitos (VLL). **Conjunto minimal de 14 tipos**, etiquetados **1A, 1B, 2A, 2B, 3A, 3B, 4A, 4B, 5A, 5B, 6A, 6B, 7A, 7B** (más el **Type 2C**, excepción confirmada por Leren). Cita marco (ronk): *"un conjunto minimal de catorce M-Wings y cuatro M-Rings; cualquier M-Wing válido coincide con uno solo de los ejemplares."*
  - **M-Ring** (versión bucle continuo) — **4 ejemplares: Type A, B, C, D** (cada uno con un pivote extra). Añadidos posteriores de StrmCkr: **Type B1** y **Type AB**.
- **S-Wing** (S = Split) — estructura LVL (fuerte–bivalor–fuerte). Creado por StrmCkr (2010). Muchos subtipos (1, 1a–1d, 2…6). Solo discontinuos; los continuos "caen bajo M-Rings".
- **L-Wing** (L = Local) — 3 enlaces fuertes conjugados sobre 3 dígitos, sin bivalor (L2/L3-Wing).
- **H-Wing** (H = Hybrid) — mezcla arbitraria (H1/H2/H3/H4-Wing).
- **W-Wing** — ya arriba (1.b); su versión **[+] W-Ring** (bucle de dos bivalores + dos bilocales) elimina hasta 28 candidatos.

### 1.d — La misma lógica de cadena, vista por **coloreo**
El coloreo no es una familia aparte: es notación de colores para las mismas cadenas.
- **Simple Colouring (Single's Chains)** `= X-Chain monodígito` en dos colores.
- **Multi-Colouring** — varios clusters de un dígito.
- **3D Medusa** — coloreo multidígito `= AIC sobre un cluster`.
- **GEM (Graded Equivalence Marks)** — Medusa "graduada" con 6 marcas; superconjunto de Equivalence Marks (Sudopedia).

### 1.e — **[+] Framework alternativo de cadenas: whips / braids (Denis Berthier, CSP-Rules)**
*No estaba en tu lista y es una ausencia importante del canon HoDoKu/SudokuWiki.* Berthier ("The Hidden Logic of Sudoku") define una jerarquía de cadenas con disciplina distinta (basada en "candidates" y z-candidates) **y con rating medible de complejidad**:
- **bivalue-chains → whips → g-whips (whips con grupos) → braids → g-braids**, con **whips ⊂ braids**.
- Coronada por la escala **T&E(1) / T&E(2)** (trial-and-error de profundidad acotada). Reexpresa buena parte de AIC y forcing bajo una métrica de dificultad ordenada.

---

## RAÍZ 2 — LÓGICA DE CONJUNTOS BASE/COVER (rank theory — Allan Barker)

Patrones estáticos de "N conjuntos base cubiertos por N cover" (rank 0) o rank −1. Los *truths* de Barker = las CSP-variables de Berthier. Esta raíz contiene los peces, los subconjuntos/ALS y los patrones rank-0 grandes.

### 2.a — **PECES** (un dígito; rank-0 monodígito)
- Cadena básica por número de conjuntos: **X-Wing (2) → Swordfish (3) → Jellyfish (4) → [+] Squirmbag (5) → [+] Leviathan (6–7)** *(los dos últimos casi nunca útiles; nombres poco estándar).*
- `⚠⚠ CRUCE MAYOR con Raíz 1.a` — **los peces monodígito y las X-Chains describen los MISMOS patrones.** Finned X-Wing `= X-Wing + Skyscraper`; un par de Sashimi X-Wing `= Skyscraper`. HoDoKu: *"un Skyscraper es un Turbot Fish especial y son dos Sashimi X-Wings."* → El monodígito es literalmente **Fish ∩ Cadena** ↔ *§5-1, §5-11*.
- **Modificadores** (ortogonales, se combinan entre sí):
  - **Finned** (aleta exo) · **Sashimi** (finned que sin la aleta degeneraría).
  - **Franken** (bases/covers mezclan líneas y cajas) · **Mutant** (mezcla libre de filas, columnas y cajas).
  - **Endo-Finned** (aleta interna; solo existe en Franken/Mutant).
  - **Cannibalistic** (un candidato base en dos covers se elimina a sí mismo).
  - **Siamese** (dos finned en las mismas celdas que difieren en un cover; el caso mínimo Siamese-Sashimi = Skyscraper).
  - **Degenerate** (se reduce a algo menor).
- **Kraken Fish** — pez que necesita soporte de una cadena. `= Pez + AIC` ↔ *§5-5, Raíz 4*.
- *Ejemplo de combinación coherente (HoDoKu): "Cannibalistic Finned Franken Swordfish with Endo Fins" — extremo pero legítimo, porque todos son modificadores de PEZ.*

### 2.b — **SUBCONJUNTOS Y SUBCONJUNTOS DOBLADOS → toda la familia Wings/ALS** (rank-0 multidígito)
Aquí está el núcleo de las zonas grises que te interesan.
- **Subconjuntos rectos:** Naked/Hidden **Pair · Triple · Quad** (complementarios: Naked N ↔ Hidden 9−N); **Locked Pair/Triple** (con caja compartida); **Locked Candidates** (Pointing/Claiming) `= pez degenerado`.
- **Cuando un subconjunto desnudo se "dobla" en una intersección caja-línea → Bent Naked Subset.** Aquí nacen TODOS los wings multidígito y Sue de Coq, y **todos son ALS-XZ.** (StrmCkr: *"ALS-XZ es la técnica padre de todos los xy, xyz, wxyz"*; unificados bajo el nombre **[+] BARN / BNS** = *Bent Almost Restricted Naked set*).
  - **ALS-XZ** (regla base: 2 ALS + 1 RCC) — **nodo raíz de todo este sub-árbol.**
    - Bent Triple `=` **XY-Wing** `↔ mismo objeto que en 1.b`.
    - Bent Triple + pivote `=` **XYZ-Wing**.
    - Bent Quad `=` **WXYZ-Wing**.
    - Bent Quint `=` **VWXYZ-Wing** → **UVWXYZ-Wing** y mayores.
    - **Doblemente enlazado (2 RCC) → Sue de Coq** (Two-Sector Disjoint Subsets) — todos los candidatos son *restringidos* (por eso elimina mucho más que un WXYZ-Wing, que tiene un común **no** restringido).
      - **Extended Sue de Coq**.
      - **[+] Three-Sector / Distributed Disjoint Subsets** — generalización a tres sectores.
      - `⚠⚠ ZONA GRIS CENTRAL — respuesta a tu pregunta:` **VWXYZ-Wing ≡ Extended Sue de Coq.** El mismo patrón rank-0 se escribe como bent quint **o** como Sue de Coq (foro: *"se podría escribir también este Sue de Coq… mismo grado de libertad, mismas eliminaciones"*). El argumento de Sue de Coq **no** se restringe a caja+línea, así que **Extended Sue de Coq y los bent subsets grandes son la misma familia con dos vocabularios.** El nombre es pedagógico, no teórico ↔ *§5-3*.
  - **Cadenas de ALS** (longitud creciente): **ALS-XZ (long. 2) → ALS-XY-Wing (long. 3) → ALS-Chain (long. n)**.
  - **Death Blossom** — celda "tallo" + un ALS "pétalo" por cada candidato del tallo. Con tallo de 2 candidatos `= ALS-XY-Wing` (HoDoKu). Nació extendiendo APE ↔ *§5-10*.
  - **ALS-W-Wing** — W-Wing con nodos ALS.
  - **Formulación dual/oculta** (el reverso, con conjuntos ocultos): **Almost Hidden Set (AHS) → AHS-XZ**; **AALS** (N celdas, N+2 candidatos) → **[+] AAALS**.
  - **Almost Locked Candidates (ALC)** — versión "almost" de Locked Candidates.
- `⚠ CRUCE con Raíz 1` — **toda esta familia = AIC con nodos ALS.** Por eso XY-Wing y compañía viven a la vez aquí y en 1.b ↔ *§5-4*.

### 2.c — **PATRONES rank-0 GRANDES** (multidígito; misma lógica que los peces, a gran escala)
- **MSLS (Multi-Sector Locked Sets)** — familia amplia y **"difusamente definida"** (su creador David P Bird incluía hasta los Sue de Coq). Contiene:
  - **SK Loop** (Stephen Kurzhal's Loop) — patrón rank-0 bien definido, subconjunto de MSLS.
  - **SET / Anillo de Phistomefel** (Set Equivalence Theory) — los bloques 2×2 de esquina contienen los mismos dígitos que el borde de la caja central. `⊂ familia SK Loop ⊂ MSLS` (foro: *"Phistomefel es otra forma de ver los SK Loops; ambos son subconjuntos de MSLS"*). Popularizado por "Phistomefel" (abril 2020) ↔ *§5-9*.
  - **Domino Loop** — bucle de pares de celdas; generaliza el SK Loop (David P Bird).
- **Exocet** — 2 celdas base + 2 target que duplican su contenido. Origen: David P Bird, champagne.
  - **Junior Exocet (JE)** — base y target en la misma banda. Subtipos JE2 / JE3 / JE4 (double) / JE+.
  - **Senior Exocet (SE)** — target **fuera** de la banda. `⚠ "SE" ≠ escala de dificultad Sudoku Explainer`; el propio Bird eligió "Senior" porque el método de búsqueda es distinto.
  - **Double Exocet** · **[+] Generic/Generalized Exocet**.
- **Multifish / Rank-0** — peces donde bases = covers.
- **Fireworks** — descubierto por **shye** (foro, **nov. 2021**): un candidato limitado a la misma caja en una fila y columna que se cruzan debe ir en la celda de cruce. Subtipos: **[+] Firework Triple, Firework Quadruple, Dual Firework (= W-Wing), Firework Exocet, Firework W-Wing, Firework MSHS**. Muy raros (~1/1000–1700 según YZF_Sudoku).

### 2.d — **rank −1 / "dark logic"** (Barker) — patrones cromáticos / oddagons
Rama distinta: patrones que producen lógica rank −1 (un sudoku válido nunca la produce, de ahí la eliminación).
- **Broken Wing / Guardians** `= oddagon de un dígito` (bucle de longitud impar de un dígito + guardianes).
- **Bivalue Oddagon** — 5 celdas bivaluadas en bucle + guardianes.
- **Tridagon / Trivalue Oddagon** ("Thor's Hammer") — patrón que exige ≥4 dígitos (grafo no 3-coloreable). Hallado por **mith (Philip Newman), marzo 2022** (puzzle "Loki", SER 11.9); el nombre corto "tridagon" lo acuñó **Denis Berthier**.

---

## RAÍZ 3 — UNICIDAD (spine independiente)

**No se reduce a las otras raíces:** depende del axioma "el puzzle tiene solución única". Raíz conceptual: el *deadly pattern* (patrón mortal que daría solución múltiple). *(Categoría paraguas que tu lista no nombraba explícitamente.)*

### 3.a — Rectángulos
- **Unique Rectangle (UR)** — **Type 1 · 2 · 3 · 4 (+4B) · 5 · 6**.
  - `⚠ Conflicto de numeración documentado:` el **Type 6 de HoDoKu = Type 5 de SudokuWiki** (Stuart lo confirma: *"el Type 6 de Hodoku es mi Type 5"*).
  - **Type 3** `↔ solapa con Subsets (2.b)` · **Type 4/6** `↔ solapa con monodígito/pez (2.a)` — *ver §5-12*.
- **Hidden (Unique) Rectangle** — variante oculta.
- **Avoidable Rectangle** — **Type 1 · 2** (UR sobre pistas ya resueltas).
- **Extended Unique Rectangle (Extended Rectangle)** — patrón 2×3.
- **Unique Loop (UL)** — generaliza el UR a bucles mayores (con sus propios Type 1–4, análogos a los del UR).

### 3.b — BUG y parientes
- **BUG (Bivalue Universal Grave)** → **BUG+1** → **BUG-Lite** → **Reverse BUG**.
- **MUG (Multi-digit Unavoidable Group)** — generaliza BUG-Lite a más dígitos.

---

## RAÍZ 4 — FORZADO (la envolvente más general)

"Si asumo X y llego a contradicción/convergencia, concluyo." **AIC (Raíz 1) es el subconjunto disciplinado y bidireccional del forzado.** De más simple a más general:

- **Forcing Chain simple** (Cell / Digit FC).
- **Nishio (Nishio Forcing Chain)** — una sola rama busca contradicción (nombrado por Tetsuya Nishio).
- **Multiple Forcing Chains** — Cell FC · Unit/Region FC · Digit FC.
- **Forcing Net** — ramificación no lineal.
- **Dynamic Forcing Chains → Nested Forcing Chains** (Dynamic + FC / + Multiple / + Dynamic).
- **Kraken Fish** `= Pez + cadena` `↔ CRUCE con 2.a` — *§5-5*.
- **[+] Métodos de plantilla / fuerza bruta** (tu lista no los traía): **Pattern Overlay Method (POM) / Templates**; **Bowman Bingo**; **T&E** puro (que en el framework de Berthier se ordena como T&E(1)/T&E(2), *ver 1.e*).

---

## §5 — CRUCES Y ZONAS GRISES (el corazón relacional)

Aquí está recopilado **todo lo que "es lo mismo que otra cosa"**. Cada línea es un objeto con doble (o triple) pertenencia. Esto es lo que un mapa por categorías no puede mostrar.

1. **Monodígito (Fish ∩ Cadena):** Skyscraper `=` dos Sashimi X-Wing `=` Turbot len-3 `=` X-Chain len-3. *(El mismo patrón es simultáneamente pez con aleta y cadena.)*
2. **Bent triple (Subset ∩ ALS ∩ Cadena):** XY-Wing `=` Bent Triple `=` ALS-XZ mínimo `=` XY-Chain len-3.
3. **Bent quint (respuesta a tu pregunta):** VWXYZ-Wing `=` Extended Sue de Coq `=` ALS-XZ doble grande. *(Y sí, el patrón se extiende hacia arriba: UVWXYZ-Wing tiene su contraparte Sue de Coq igual.)*
4. **ALS-XZ (Conjunto ∩ Cadena):** un ALS-XZ `=` una AIC con un nodo ALS. Este es el gozne que hace que toda la familia ALS sea también cadena.
5. **Kraken (Pez ∩ Forzado):** Kraken Fish `=` pez + AIC/forcing.
6. **Coloreo `=` AIC:** el coloreo es notación de colores de la misma lógica de cadena.
7. **Coloreo, en detalle:** Simple Colouring `=` X-Chain monodígito; 3D Medusa `=` AIC multidígito sobre un cluster.
8. **`⚠` WXYZ-Wing "Type 2" de SudokuWiki `=` Sue de Coq** — *disputa; ver §6-1.*
9. **Anidamiento rank-0 grande:** Phistomefel/SET `⊂` familia SK Loop `⊂` MSLS.
10. **Death Blossom (tallo de 2) `=` ALS-XY-Wing.**
11. **Finned X-Wing `=` X-Wing + Skyscraper.**
12. **Unicidad que se solapa:** UR Type 3 `=` Unicidad ∩ Subset; UR Type 4/6 `=` Unicidad ∩ monodígito.

---

## §6 — CONFLICTOS DE NOMENCLATURA (postura crítica)

1. **"WXYZ-Wing Type 2" = Sue de Coq (el conflicto central).** En la página WXYZ-Wing de SudokuWiki un comentario afirma: *"El Type 2 parece ser exactamente lo mismo que la estrategia Sue de Coq."* El blog Systematic Sudoku va más allá: *"todos los ejemplos de WXYZ de sudokuwiki son también ALS-XZ."* **Veredicto: la crítica es válida.** El "Type 2" no es un WXYZ genuino sino un ALS doblemente enlazado (= Sue de Coq); es un artefacto de implementación, no una técnica distinta.
2. **Turbot Fish como nombre específico vs. genérico.** Usar "Turbot Fish" para el tercer miembro sin nombre de la tríada es mala elección: es el nombre **genérico** de los tres patrones (Skyscraper, Kite, ER) más el X-Wing. **"Turbot Crane" es un alias decorativo sin fundamento teórico independiente.**
3. **M/S/L/H-Wing: nombres proliferados sin sistema.** ronk: los números de tipo *"se incluyen solo para facilitar la discusión, no propongo que se usen como sistema de nombres"*. Son mnemónicos de AIC cortas; su valor es de reconocimiento visual, no teórico.
4. **Sashimi vs Finned.** Debate no resuelto sobre si "Sashimi" sustituye a "Finned" o es un atributo adicional; HoDoKu lo zanja usando "Finned" para todo pez no básico.
5. **MSLS "difusamente definido".** Al no tener definición formal consensuada, no se puede decir con precisión qué patrones "son" MSLS (SpAce). Cualquier afirmación al respecto es aproximada.
6. **Conflicto de numeración UR Type 5/6** entre HoDoKu y SudokuWiki — ya documentado en §3.a; obliga a citar siempre la fuente al hablar de "Type 5" o "Type 6".

---

## §7 — NOMBRES DUDOSOS, QUIMÉRICOS O MAL FUNDADOS

*(Agrupados aquí al final, como pediste.)*

- **"Finned Franken Cannibalistic Sue de Coq"** — **quimera incoherente.** *Finned / Franken / Cannibalistic* son modificadores de **PEZ** (monodígito, base/cover); Sue de Coq es una técnica **ALS multidígito**. Son categorías disjuntas: **no combinan.** El nombre carece de sentido teórico. *(Contrastar con "Cannibalistic Finned Franken Swordfish with Endo Fins" de HoDoKu, que SÍ es coherente porque todos son modificadores de pez.)* → **Regla general de olfato: cualquier nombre que cruce un modificador de pez (finned/franken/mutant/sashimi/cannibalistic/kraken/endo) con una técnica ALS (Sue de Coq, WXYZ-Wing, Death Blossom) es casi seguro inventado.**
- **"Turbot Crane"** — alias decorativo del Turbot Fish, sin estatus teórico propio.
- **M-Wing Types 8A/8B** (rjamil, 2023) — eliminaciones parcialmente redundantes con un claiming (refutadas por Leren); fuera del conjunto minimal de 14.
- **"L1-Wing"** — etiqueta lógicamente posible pero nunca usada, porque *"es obviamente una X-Chain"*.

---

## §8 — CÓMO NAVEGAR LAS ZONAS GRISES (guía práctica)

1. **Bajá siempre al árbol:** patrón con nombre → AIC / conjuntos base-cover / Unicidad → (para todo salvo Unicidad y forcing nets) lógica rank-0 de Barker. Casi todo colapsa a rank-0. Si dudás de la "autenticidad" de un nombre, fijate a qué nivel del árbol pertenece.
2. **Ante WXYZ / Sue de Coq / VWXYZ / Extended Sue de Coq, usá la notación de conjuntos como árbitro:** el patrón es único (ALS doblemente enlazado / rank-0), el nombre es convención. El bent quint y el Extended Sue de Coq son el mismo objeto.
3. **Tratá M/S/L/H-Wing como mnemónicos de AIC cortas**, no como técnicas fundamentales.
4. **Umbral que cambiaría el veredicto del §6-1:** si apareciera una fuente primaria que mostrara eliminaciones exclusivas de un "Type 2" WXYZ **imposibles** de replicar como ALS-XZ doble, habría que reconsiderarlo. Hasta hoy ninguna fuente lo ha mostrado.

---

## §9 — CAVEATS (limitaciones de este informe)

- **HoDoKu está descontinuado** y **Sudopedia solo vive en espejos**; los enlaces pueden degradarse aunque el contenido técnico siga siendo la referencia.
- Los **type-labels de M/S/L/H-Wing** provienen de hilos donde el autor original (ronk) llegó a borrar sus posts; el conjunto de 14 sobrevive como **restauración de una cita** por StrmCkr, no como post vivo. Atribución con esa cautela.
- El **framework whip/braid de Berthier (1.e)** es real y bien documentado en CSP-Rules, pero su vocabulario **casi no se cruza** con el del canon HoDoKu/SudokuWiki; encajarlo en el mismo árbol es una síntesis mía, no un mapeo que las fuentes hagan explícito.
- **MSLS carece de definición formal consensuada**; toda afirmación sobre su extensión es aproximada.
- El campo evoluciona: **Fireworks (2021)** y **Tridagon (2022)** son recientes y su nomenclatura aún no está estabilizada.
- **Squirmbag/Leviathan** (peces 5–7) son nombres poco estándar y de utilidad marginal; los incluyo por completitud, no porque tengan uso práctico.
