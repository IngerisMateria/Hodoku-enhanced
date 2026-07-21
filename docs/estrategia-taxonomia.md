# Estrategia de taxonomía — teoría verificada y arquitectura del motor expandido

Fecha: 2026-07-21. Origen: parada estratégica del dueño + mapa relacional
(`sudoku_mapa_relacional.md`, adjunto al repo como referencia) + verificación
contra fuentes primarias (SudokuWiki, forum.enjoysudoku.com, Sudopedia,
docs de HoDoKu). Este documento arbitra el solapamiento de técnicas y define
cómo lo maneja el motor.

## 1. El clasificador que faltaba: regímenes del conjunto doblado

Para un conjunto de n celdas con exactamente n candidatos en una región de
dos (o más) sectores, un candidato es RESTRINGIDO si todas sus apariciones
dentro del conjunto comparten un sector (se ven entre sí). La variable que
clasifica TODO este territorio es **cuántos candidatos NO restringidos hay**:

| No-restringidos | Régimen | Eliminaciones | Nombres en la literatura |
|---|---|---|---|
| exactamente 1 (Z) | **R1 — Wing** | solo Z, de las celdas que ven todas las Z del conjunto | XY-Wing, XYZ-Wing, WXYZ-Wing (canónico y general/StrmCkr), VWXYZ.. RSTUVWXYZ, "bent naked subset"; en álgebra ALS: ALS-XZ de **un** RCC |
| 0 (todos restringidos) | **R0 — Locked** | CADA dígito, del resto de su sector confinante | Sue de Coq (= Two-Sector Disjoint Subsets), Extended SDC, Distributed/Three-Sector Disjoint Subsets; en álgebra ALS: **doblemente enlazado** (2 RCC); Subset Counting; rank 0 |
| ≥2 | — | ninguna | (el argumento de conteo falla) |

Fuentes: definición general de StrmCkr en SudokuWiki ("exactamente un dígito
común no-restringido") y su corolario en el foro ("3 dígitos restringidos a un
sector cada uno, 1 dígito en 2 sectores"); eleven en el hilo Sue de Coq ("ALS
doblemente enlazados... n celdas, n dígitos, k restringidos a la línea y n−k a
la caja"); hilo Distributed Disjoint Subsets ("N celdas con N dígitos... si
todas las ocurrencias del mismo dígito comparten un sector"); Sudopedia (SDC =
caso especial de Subset Counting; origen TSDS 2005).

**Consecuencias directas:**
- Los dos regímenes son **mutuamente excluyentes sobre las mismas celdas** (o
  hay un no-restringido o no lo hay). Mismo hardware, deducción distinta.
- **VWXYZ-Wing ≠ Extended Sue de Coq.** La identidad verdadera es: *bent-n
  LOCKED (R0) ≈ instancia de SDC/ESDC/DDS*. Lo que shippeamos en 1.1–1.3 es
  íntegramente R1; **el caso R0 fue excluido explícitamente desde la spec del
  1.1** — por eso no hay redundancia alguna entre lo hecho y la futura familia
  SDC.
- El "Type 2 = Sue de Coq" de los comentarios de SudokuWiki es **falso como
  identidad de clase**: un T2 tiene por definición una Z no restringida → R1 →
  ALS-XZ de un solo RCC (así lo replica la propia literatura), no doblemente
  enlazado. La cita de Systematic Sudoku ("todos los ejemplos son también
  ALS-XZ") apoya exactamente eso: ALS-XZ simple, no SDC.
- El futuro milestone de Sue de Coq extendido se **redefine**: es "régimen R0
  del motor bent existente" + las extensiones de hobiwan (candidatos extra,
  ALSs auxiliares multi-celda). Reuso masivo del motor ya construido.

## 2. Veredicto sobre el mapa relacional del dueño

**Queda en pie (~90%),** incluida la estructura de 4 raíces, la irreducibilidad
de Unicidad, el cruce Fish∩Cadena monodígito, los modificadores de pez, la
quimera "Finned Franken Cannibalistic Sue de Coq" (correctamente denunciada),
el conflicto UR Type 5/6, whips/braids como framework paralelo, y la regla de
olfato del §7.

**Corrección central (contradicción interna):** la línea 99 del mapa enuncia
el clasificador correcto ("Sue de Coq: todos los candidatos restringidos, por
eso elimina mucho más que un WXYZ que tiene un común NO restringido") y las
líneas 102, §5-3 y §6-1 lo contradicen al identificar VWXYZ≡ESDC y T2≡SDC.
Resolución: **gana la línea 99.** El §8-4 del mapa pedía un umbral ("una
fuente con eliminaciones de T2 imposibles de replicar como ALS-XZ doble"); el
umbral se resuelve al revés: las eliminaciones de T2 se replican como ALS-XZ
**simple** y en general NO como doble.

**Marcas menores:** Phistomefel ⊂ SK-loop ⊂ MSLS — dirección correcta, pero
con MSLS "difusamente definido" (§6-5 del propio mapa) toda inclusión es
aproximada; atribución de Fireworks (shye, nov-2021) y subtipos — a verificar
en la spec del milestone Fireworks; "rank −1 de Barker" para oddagons —
etiqueta de síntesis, la familia es correcta como rama propia
(contradicción+guardianes); lore de tipos M/S/L/H — frágil por posts borrados,
como el §9 ya confiesa; se fija en su spec con las fuentes que sobrevivan.

**Experimento empírico propuesto (zanjar §6-1 con datos propios):** clasificar
por régimen cada canónico T1/T2 cosechado en los corpus y verificar que sus
eliminaciones se replican como ALS-XZ simple y no requieren R0. Con el harness
existente es barato; el resultado va al help como respuesta documentada.

## 3. Arquitectura del motor expandido: tres capas

**Capa 1 — Motores (pocos, por familia lógica).** bent-subset (existe; R1 hoy,
R0 se agrega para la familia SDC), canonical-wings (existe), fish (legacy,
completo), ALS-pair/chain (legacy, se extiende), single-digit chains (legacy
turbot+familia; unificable a futuro), short-AIC de forma fija (M/S/L/H, nuevo),
oddagons (nuevo, escalera 1/2/3), multi-sector dirigidos (SK/MSLS/SET, exocet,
fireworks — nuevos), y los motores de derivación (tabling legacy; AIC v2 en su
fase). Un motor produce instancias crudas con metadatos estructurales.

**Capa 2 — Etiquetas (SolutionType + StepConfig).** La moneda HoDoKu: enable,
score y orden POR NOMBRE de la literatura. Muchas etiquetas pueden compartir
un motor (ya pasa: 6 etiquetas sobre el motor bent). Un clasificador asigna
cada instancia a su etiqueta habilitada MÁS ESPECÍFICA según el DAG de
subsunción (el mapa del dueño es la semilla del DAG). Si solo hay etiquetas
específicas habilitadas, el motor restringe la búsqueda (pushdown de filtros).

**Capa 3 — Registro de metadatos (fuente única para toda la UX).** Por
técnica: id estable, nombre default, aliases buscables, descripción corta,
familia/motor, padres de subsunción ("es caso de"), relaciones de solapamiento
("interfiere con"), régimen si aplica, referencias. **Invariante duro:** nombres
de enum, library codes y persistencia NUNCA cambian por aliasing — el switch de
nombre es indirección de presentación pura (config, hints y análisis resuelven
el display-name a través del registro).

**Reporte y dedup:** en all-steps, cada instancia se reporta una vez bajo su
etiqueta más específica habilitada (precedente ya operativo: canónico antes
que Bent Quad; Skyscraper antes que Turbot). En el path del solver, el orden
decide, como siempre.

## 4. Costos: enumeración vs derivación (la respuesta de performance)

Regla de oro: **el costo vive en los motores, no en las etiquetas.**
- *Motores de enumeración acotada* (subsets, bent-n, fish sin kraken, wings de
  forma fija, oddagons, uniqueness): combinatoria chica por región con
  chequeos O(1) de bits. Medido: la pasada all-steps con n=4..9 sobre 26.243
  estados tomó ~100 s incluyendo replay y brute force. Agregar tamaños o
  etiquetas acá es ~gratis. Bent n=9 cuesta lo mismo que n=6 (C(15,9)=C(15,6)).
- *Motores de derivación* (kraken = pez × cadena por aleta; forcing/tabling;
  AIC dinámicas): el costo se multiplica por profundidad y ramificación —
  cada eliminación exige PROBAR una cadena, y eso es búsqueda, no chequeo.
  Estos motores llevan throttles configurables (longitud, nodos, tipos de
  nodo, aletas). P-002 (separar Kraken T1/T2) es el caso testigo del patrón.

## 5. UX del dueño, mapeada al registro

- **Buscador en toda tab que liste técnicas (3c):** consulta sobre nombre +
  aliases + descripción del registro.
- **Panel de info por técnica (3c):** descripción + "otros nombres /
  solapamiento" desde el registro (aliases + relaciones).
- **Switch de nombre (3d):** preferencia de display-name por técnica; se aplica
  en config, hints y análisis; el alias desplazado pasa a la lista de alias.
- **Resaltado de solapamiento (3a):** técnicas con relaciones "interfiere con"
  marcadas; al abrirlas, lista de interferencias.
- **Carpetas/presets del solver (3b) y presets de find-all-steps (2):**
  agrupamientos = vistas nombradas sobre el registro (por familia, por
  convención de nomenclatura, custom del usuario); presets de estudio =
  máscaras de enable guardadas (propuesta inicial: "Wings", "ALS", "Cadenas",
  "Fish", "Unicidad", "Clásico HoDoKu").
- **Memoria de posición de popups (nueva queja 1):** QoL puro, sin relación
  con el registro → entrada P-003 del backlog de pulido.

## 6. Impacto propuesto en el roadmap

- **1.4 (nuevo) — Registro de técnicas v1:** modelo de datos + carga de las
  ~96 técnicas existentes y las 8 modernas + indirección de display-name +
  buscador y panel de info en las tabs de config (núcleo de 3c/3d).
- **1.5 (nuevo) — UX de configuración:** carpetas/presets del solver, presets
  de find-all-steps, resaltado de solapamiento (2/3a/3b), preferencias de
  alias persistidas.
- **M/S/L/H pasa a 1.6** y el resto se corre. Motivo del orden: quedan ~20
  técnicas por fabricar; cada una que entre sin metadatos es retrofit futuro.
  Con el registro, fabricar una técnica = finder + una fila del registro.
- **Milestone ESDC/SDC-extendido se redefine** como "régimen R0 del motor
  bent + extensiones hobiwan" (sección 1).
- **P-001 se actualiza** con la resolución del régimen y el experimento
  empírico; **P-003 (popups)** se agrega al backlog.

## 7. Decisiones pendientes del dueño

1. ¿Bendecís insertar 1.4/1.5 antes de M/S/L/H?
2. Nombres default: propongo nomenclatura wings/HoDoKu como default y
   SDC/foro como aliases (tu 3d permite invertirlo por usuario). ¿OK?
3. El experimento empírico de §2: ¿lo corremos dentro de 1.4 o queda anotado
   en P-001 para 5.0?
4. Presets de estudio iniciales: ¿bendecís la lista propuesta en §5 como
   punto de partida para 1.5?
