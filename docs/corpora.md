# Corpus de puzzles — fuentes, procedencia y reglas (milestone 0.3)

## Reglas

- Los corpus completos viven en `data/corpora/` (**gitignoreado**; los archivos grandes
  NUNCA se commitean).
- Al repo van solo **subsets curados chicos** bajo `test/fixtures/`, cada uno con su
  procedencia en la cabecera del archivo y en este documento.
- Descarga automatizada: `.\gradlew.bat fetchCorpora` (baja las fuentes de GitHub a
  `data/corpora/sudoku-classif/`, pineadas a un commit fijo). La base de champagne
  requiere descarga manual (ver abajo).

## Fuente 1: GitHub denis-berthier/Sudoku-classif (ingerida en 0.3)

- URL: https://github.com/denis-berthier/Sudoku-classif
- Commit pineado: `15c9d217b08d2d3ebf0f147d4f7c0beb5c2fcdb2` (2025-10-31)
- Licencia del repo: **GPLv3** (misma que este fork). El README aclara que los puzzles
  son propiedad intelectual de sus creadores y las clasificaciones de Denis Berthier;
  corresponde citar a ambos.
- Atribución:
  - Clasificaciones T&E: **Denis Berthier** (CSP-Rules).
  - Puzzles T&E(3): **Philip Newman ("mith")** — colección de 158.276 expansiones BRT
    de minimales a T&E-depth 3, portadores del patrón tridagon (era post-2022).
  - Puzzles T&E(2): **"eleven"** — 26.370 minimales pre-tridagon (BxB ≤ 7).

### Subsets commiteados

| Fixture | Origen | Curado |
|---|---|---|
| `test/fixtures/te3-mith-200.txt` (200) | `mith-158276-TE3/Sample1000/puzzles.txt` (muestra aleatoria de 1000 del propio autor) | 1 de cada 5 líneas (1, 6, … 996) |
| `test/fixtures/te2-eleven-100.txt` (100) | `eleven-26370-TE2/puzzles.txt` | 1 de cada 264 líneas (1, 265, … 26137), recortadas a los 81 chars del puzzle (las líneas originales traen ratings SER) |

Destino: los T&E(3) son los futuros fixtures del milestone 1.7 (Tridagon, numeración post-1.2); los T&E(2)
sirven de corpus duro de control sin tridagon. Contrato en tests: unicidad de solución
de los 300 (`CorporaSubsetTest`) y smoke de terminación+soundness sobre una muestra
determinística del T&E(3) (`Te3SmokeTest`; la pasada completa de los 200 se corrió al
cierre del 0.3, ver `docs/log.md`).

Formatos del repo fuente: `puzzles.txt` con un puzzle por línea. En `mith-158276-TE3`
las líneas son `puzzle;índice;n` (expansiones BRT); en los `Sample*` son 81 chars
limpios. En `eleven-26370-TE2` son `puzzle  SER SER SER`. Metadatos por colección:
`nb-clues.txt`, `nb-cands.txt`, `density.txt`, listas de patrones (`has-trid.clp`,
`Trid-OR5W-levels.txt`, etc. — formato CLIPS de CSP-Rules) y `RESULTS.clp` con los
cómputos reproducibles.

Nota de escala: la colección T&E(3) completa de mith ronda 1,07M de puzzles; el repo
de GitHub trae 158.276 y alcanza de sobra para fixtures.

## Fuente 2: base "potential hardest" de champagne (descarga MANUAL pendiente)

- Hilo: "The hardest sudokus (new thread)" — http://forum.enjoysudoku.com/the-hardest-sudokus-new-thread-t6539.html
- El primer post enlaza el Google Drive de champagne:
  https://drive.google.com/drive/folders/0B5lH6mGXxWzXTDFRMnVTbGNlZU0
  (el post, editado por última vez en oct-2019, menciona `ph_1910.zip`; la versión
  ago-2020 es `ph_2010.zip`, ~3,1M puzzles — el "2010" NO es el año, es AAMM).
- **La descarga automatizada falla**: la carpeta usa un ID de Drive pre-2021 sin
  `resourcekey` público, y Google fuerza login para el acceso anónimo (verificado
  2026-07-20, redirect a accounts.google.com tanto por navegador como por HTTP
  directo). Pasos manuales:
  1. Abrir el link de la carpeta logueado en una cuenta Google.
  2. Descargar `ph_2010.zip` (o la versión más nueva que haya).
  3. Dejarlo en `data/corpora/champagne/ph_2010.zip` (gitignoreado), sin extraer
     subsets: eso es trabajo de las fases 2/3 (SK-Loop / Exocet).
- Al ingerirlo (fases 2/3): verificar el conteo (~3,1M), identificar los archivos de
  propiedades del paquete (tags de exocet: `E` / `EE` double — en releases tempranos,
  24.410 puzzles tagueados con exocet) y verificar si hay tags SK-loop; documentar el
  formato acá.

## Fuente 3: JExocet Compendium (spec de fase 3, no es corpus)

- Hilo t32370 de forum.enjoysudoku.com; los .odt de David P. Bird adjuntos ahí
  (01 Compendium … 14 Senior Exocet). Se ingieren como spec en `docs/specs/` cuando
  arranque la fase 3.

## Ya en fixtures desde 0.2

- `test/fixtures/corpus.txt`: 30 puzzles generados con el propio HoDoKu (snapshots).
- `test/fixtures/extremes.txt`: Easter Monster (SK-loop/JE), Golden Nugget,
  Platinum Blonde — smoke de monstruos clásicos.

## Pendiente de ubicar (no bloquea)

- Hilo de Fireworks con ejemplos etiquetados; listas SK-loop dedicadas.
