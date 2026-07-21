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
- Criterio de cierre: decisión explícita del dueño (mantener / configurar
  / omitir) + help actualizado.

## P-002 — Separar Kraken Fish Type 1 y Type 2 en la configuración
- Fecha: 2026-07-21 · Origen: dueño (dolor real del HoDoKu base) ·
  Estado: diferido
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
