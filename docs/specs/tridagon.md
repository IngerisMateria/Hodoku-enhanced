# Spec — Tridagon (Trivalue Oddagon) — milestone 1.7

Estado: cerrada por el PM, 2026-07-21, desde fuentes primarias (regla de
técnicas con asterisco). Fuentes leídas hoy:
- mith, "Chromatic Patterns", forum.enjoysudoku.com/chromatic-patterns-t39885.html
  (23-mar-2022): condiciones canónicas del patrón, regla de guardianes,
  prueba vía bivalue oddagons, forma "Thor's Hammer".
- D. Berthier, "The tridagon rule", forum.enjoysudoku.com/the-tridagon-rule-t39859.html:
  el nombre ("tridagon" = abreviatura de "trivalue oddagon"), la regla de
  eliminación, y el ecosistema tridagon-links / ORk-forcing (fase 4, NO 1.7).
- Pruebas: "parity flow" de ryokousha (video de rangsk enlazado en t39885);
  outlines de marek stefanik (hilo hardest, p306942); prueba de mith vía
  bivalue oddagons (t39885, post 318956) — reusa literalmente nuestra 1.6.
- Contexto de uso: Loki (SER 11.9) se resuelve en 3 pasos de guardianes
  (billerbop.blogspot.com, ago-2025).

## 1. El patrón (condiciones de mith, las tres obligatorias)

1. **Cuatro cajas en dos bandas × dos stacks** (9 rectángulos de cajas
   posibles: C(3,2) × C(3,2)).
2. **En cada caja, 3 celdas que cubren las 3 filas y las 3 columnas de la
   caja** — una transversal: una permutación local filas→columnas (6
   posibles por caja). NO es una L de 2×2: ese fue un error de memoria del
   PM que las fuentes corrigieron.
3. **Paridad impar**: el producto de los signos de las 4 permutaciones
   locales es −1 — equivalentemente, split 3/1 (o 1/3) entre orientación
   diagonal y antidiagonal; equivalentemente, los enlaces fila/columna
   entre cajas forman EXACTAMENTE un rectángulo de 4 celdas + un lazo de
   8 celdas. Con split par (4/0, 2/2, 0/4) el patrón ES coloreable y NO
   hay deducción (ver fixtures negativos).

Elegibilidad de celda (v1): sin resolver y conteniendo los TRES dígitos
{a,b,c}, más guardianes opcionales. Variantes con celdas de 2-de-3 dígitos
del triple: fuera de alcance v1 (documentar como extensión en el doc de la
técnica; suelen colapsar en lógica más simple).

Guardianes = los candidatos ∉ {a,b,c} dentro de las 12 celdas del patrón.

## 2. Imposibilidad (esquema para el hint)

Si ningún guardián fuera verdadero, las 12 celdas quedarían confinadas a
{a,b,c}. Dentro de cada caja las 3 celdas se ven todas → biyección del
triple. Los enlaces de fila (entre cajas de la misma banda) y de columna
(misma stack) alrededor del rectángulo componen las cuatro biyecciones en
una permutación que, por la paridad impar, no puede cerrar consistentemente
— contradicción. Luego algún guardián es verdadero. (Pruebas completas en
las fuentes; la de mith pasa por bivalue oddagons de 5 celdas inducidos por
el lazo de 8 — la escalera 1.6→1.7 es literal.)

## 3. Deducciones (framework Guardians de 1.6, sin cambios de contrato)

- |G| = 1 → el guardián es VERDADERO: se eliminan de su celda todos los
  candidatos del triple presentes (queda el dígito guardián). Cita de
  mith: "the three digits are eliminated from that cell".
- |G| > 1, todos del mismo dígito g → eliminar g de toda celda externa que
  vea TODAS las celdas guardianas (mismo argumento que BO 1.6).
- |G| > 1 mixto → sin deducción directa en 1.7. Eso es "tridagon links" /
  virtual pairs de mith / ORk de Berthier: territorio de la fase 4 de
  cadenas. Anotar en el doc de la técnica.
- Patrón sin guardianes → estado contradictorio: assert/log y saltar
  (contrato 1.6), jamás "eliminar todo".

## 4. Detección (enumeración acotada, estilo del proyecto)

Para cada triple {a,b,c} (84): máscara; por caja, transversales elegibles
(≤6 permutaciones, filtradas por elegibilidad de sus 3 celdas). Para cada
uno de los 9 rectángulos de cajas cuyos 4 miembros tengan ≥1 transversal:
producto cartesiano de transversales (típicamente diminuto), chequear
producto de signos = −1, recolectar guardianes, aplicar la escalera. Poda:
descartar triples sin 4 cajas pobladas antes del producto. Costo esperado:
enumeración acotada, milisegundos por estado — medir y reportar.

Dedup: clave tipo + 12 celdas ordenadas + deducción (mismo mecanismo 1.2/1.6).

## 5. Fixtures y custodia

- Positivos ≥4, cosechados de te3-200 (ese corpus ES la colección de
  portadores por construcción — la base T&E(3) de mith): incluir al menos
  un |G|=1 (eliminación del triple en la celda guardiana) y un
  multi-guardián uniforme.
- Negativos ≥3, el primero obligatorio: (a) **split de paridad par (2/2)**
  con todo lo demás válido — el negativo rey, prueba que el detector
  entiende la condición 3 y no matchea siluetas; (b) una caja sin
  transversal (dos celdas en la misma fila local); (c) cuatro cajas que no
  forman rectángulo de bandas/stacks.
- Custodia: esperable que aparezca en paths default de te3 — sumar 1-2
  puzzles al corpus de snapshots y reportar el diff; si no apareciera,
  custodia por fixtures (regla vigente).

## 6. Registro, config y rendering

- SolutionType TRIDAGON, display "Tridagon", aliases "Trivalue Oddagon" y
  "Thor's Hammer"; library code de 4 dígitos libre, documentado.
- Registro: familia ODDAGON, mismo padre taxonómico que BW/BO (rama
  contradicción+guardianes del mapa); referencias = las fuentes de arriba.
- Config: enabled=true (ES el titular de la modernización), dificultad
  EXTREME o UNFAIR alto leyendo la escala vecina y documentando (ancla:
  por encima de BO 440; sugerencia ~500, antes de los kraken).
- Rendering: chain de display del lazo de 8 (mecanismo 1.6, sin tocar
  SudokuPanel) + las 4 celdas del rectángulo y los guardianes como fins.
- Hint (formatter): triple, cajas, celda(s) guardiana(s) y la
  justificación corta del §2.

## 7. Fuera de alcance v1 (anotar en el doc de la técnica)

Celdas 2-de-3; patrones de 6 cajas (mith: se cree que colapsan en el de 4);
los demás patrones cromáticos del hilo (Patto Patto, Fryer's Ring, Socks,
Broken Windmill — candidatos a backlog futuro); tridagon links, virtual
pairs y ORk-forcing (fase 4); anti-tridagon (Berthier).
