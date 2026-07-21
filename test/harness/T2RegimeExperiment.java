/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.4).
 *
 * HoDoKu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoDoKu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoDoKu. If not, see <http://www.gnu.org/licenses/>.
 */
package harness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import solver.SudokuStepFinder;
import sudoku.Candidate;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.Sudoku2;

/**
 * T2 regime experiment (milestone 1.4, docs/estrategia-taxonomia.md §2):
 * harvests every canonical WXYZ-Wing (Type 1 and Type 2) over the default
 * solve-path states of corpus + te2 + te3 and verifies per state that
 * <ol>
 * <li>the pattern has exactly one non-restricted candidate (Z) — regime R1,
 * never the R0 locked regime;</li>
 * <li>the legacy AlsSolver produces a SIMPLE (single-RCC) ALS-XZ whose
 * eliminations cover the wing's;</li>
 * <li>no doubly linked (locked) structure is needed for the cover.</li>
 * </ol>
 * This settles the "WXYZ Type 2 = Sue de Coq" dispute (§6-1 of the relation
 * map) with our own data. Results: docs/experimentos/t2-regimen.md; P-001.
 *
 * Console tool, not a test: run once per experiment via the test runtime
 * classpath with -Djava.util.Arrays.useLegacyMergeSort=true (known
 * SolutionStep.compareTo quirk, docs/build.md).
 */
public final class T2RegimeExperiment {

	private static final String[] SOURCES = { "/fixtures/corpus.txt", "/fixtures/te2-eleven-100.txt",
			"/fixtures/te3-mith-200.txt" };
	private static final String[] SOURCE_NAMES = { "corpus", "te2", "te3" };

	// harvest counters (per occurrence: one canonical step at one state)
	private static int states = 0;
	private static int statesWithCanonical = 0;
	private static int total = 0;
	private static int t1 = 0;
	private static int t2 = 0;
	// (a) regime
	private static int regimeR1 = 0;
	private static final List<String> regimeFailures = new ArrayList<>();
	// (b)/(c) ALS-XZ cover
	private static int coveredBySimple = 0;
	private static int coveredBySimpleT2 = 0;
	private static int coveredOnlyByDoubly = 0;
	private static int notCovered = 0;
	private static final List<String> coverFailures = new ArrayList<>();
	// distinct patterns (cells + Z + eliminations)
	private static final Set<String> distinct = new HashSet<>();
	private static final Set<String> distinctT2 = new HashSet<>();

	private T2RegimeExperiment() {
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		HarnessRunner.initialize();
		SudokuStepFinder finder = new SudokuStepFinder();
		for (int s = 0; s < SOURCES.length; s++) {
			List<String> puzzles = Fixtures.lines(SOURCES[s]);
			for (int p = 0; p < puzzles.size(); p++) {
				runPuzzle(finder, SOURCE_NAMES[s] + "#" + (p + 1), puzzles.get(p));
			}
			System.out.println("== " + SOURCE_NAMES[s] + " done (" + puzzles.size() + " puzzles, " + states
					+ " states so far, " + total + " canonical steps)");
		}
		report(System.currentTimeMillis() - start);
		// non-daemon solver threads keep the JVM alive (docs/build.md)
		System.exit(0);
	}

	/** Replays the default solve path and examines every intermediate state. */
	private static void runPuzzle(SudokuStepFinder finder, String id, String line) {
		PuzzleResult result = HarnessRunner.analyze(line);
		if (result.solution == null) {
			System.out.println("SKIP (not unique/invalid): " + id);
			return;
		}
		Sudoku2 work = new Sudoku2();
		work.setSudoku(result.puzzle);
		for (int i = 0; i < result.steps.size(); i++) {
			examineState(finder, id, i + 1, work);
			apply(work, result.steps.get(i));
		}
	}

	private static void apply(Sudoku2 work, StepRecord step) {
		for (CellValue placement : step.placements) {
			work.setCell(placement.index, placement.value);
		}
		for (CellValue elimination : step.eliminations) {
			work.delCandidate(elimination.index, elimination.value);
		}
	}

	/** Harvests the canonical wings of one state and runs the three checks. */
	private static void examineState(SudokuStepFinder finder, String id, int stateNo, Sudoku2 work) {
		states++;
		List<SolutionStep> wings = finder.getAllWings(work);
		List<SolutionStep> canonical = new ArrayList<>();
		for (SolutionStep step : wings) {
			if (step.getType() == SolutionType.WXYZ_WING) {
				canonical.add(step);
			}
		}
		if (canonical.isEmpty()) {
			return;
		}
		statesWithCanonical++;
		// the ALS-XZ steps of this state (solver defaults: overlap off), only
		// computed for states that actually hold canonical wings
		List<SolutionStep> alsXz = finder.getAllAlses(work, true, false, false);
		for (SolutionStep step : canonical) {
			examineStep(id, stateNo, work, step, alsXz);
		}
	}

	private static void examineStep(String id, int stateNo, Sudoku2 work, SolutionStep step,
			List<SolutionStep> alsXz) {
		total++;
		List<Integer> cells = step.getIndices(); // hinge first
		List<Integer> values = step.getValues(); // W/X/Y ascending, Z last
		int z = values.get(values.size() - 1);
		boolean type1 = false;
		for (Candidate fin : step.getFins()) {
			if (fin.getIndex() == cells.get(0)) {
				type1 = true;
				break;
			}
		}
		if (type1) {
			t1++;
		} else {
			t2++;
		}
		String where = id + " state " + stateNo + " " + step.toString(2);
		String key = patternKey(step);
		distinct.add(key);
		if (!type1) {
			distinctT2.add(key);
		}

		// (a) regime: count non-restricted candidates of the pattern
		int nonRestricted = 0;
		boolean zNonRestricted = false;
		for (int digit : values) {
			List<Integer> carriers = new ArrayList<>();
			for (int cell : cells) {
				if ((work.getCell(cell) & Sudoku2.MASKS[digit]) != 0) {
					carriers.add(cell);
				}
			}
			boolean restricted = true;
			for (int i = 0; i < carriers.size() && restricted; i++) {
				for (int j = i + 1; j < carriers.size(); j++) {
					if (!Sudoku2.buddies[carriers.get(i)].contains(carriers.get(j))) {
						restricted = false;
						break;
					}
				}
			}
			if (!restricted) {
				nonRestricted++;
				if (digit == z) {
					zNonRestricted = true;
				}
			}
		}
		if (nonRestricted == 1 && zNonRestricted) {
			regimeR1++;
		} else {
			regimeFailures.add(where + " [nonRestricted=" + nonRestricted + " zNonRestricted=" + zNonRestricted + "]");
		}

		// (b)/(c) ALS-XZ cover: simple (1 RC digit) vs doubly linked (2)
		boolean simpleCover = false;
		boolean doublyCover = false;
		for (SolutionStep xz : alsXz) {
			if (!covers(xz, step)) {
				continue;
			}
			if (rcDigitCount(xz) <= 1) {
				simpleCover = true;
				break; // best case found, no need to keep looking
			} else {
				doublyCover = true;
			}
		}
		if (simpleCover) {
			coveredBySimple++;
			if (!type1) {
				coveredBySimpleT2++;
			}
		} else if (doublyCover) {
			coveredOnlyByDoubly++;
			coverFailures.add(where + " [only doubly-linked cover]");
		} else {
			notCovered++;
			coverFailures.add(where + " [no ALS-XZ cover]");
		}
	}

	/** true if every elimination of <code>wing</code> is one of <code>xz</code>. */
	private static boolean covers(SolutionStep xz, SolutionStep wing) {
		outer: for (Candidate wanted : wing.getCandidatesToDelete()) {
			for (Candidate has : xz.getCandidatesToDelete()) {
				if (has.getIndex() == wanted.getIndex() && has.getValue() == wanted.getValue()) {
					continue outer;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Number of distinct restricted-common digits of an ALS-XZ step. The
	 * AlsSolver stores the RC cells as endo fins (one digit per RC): one
	 * digit = simple ALS-XZ, two = doubly linked.
	 */
	private static int rcDigitCount(SolutionStep xz) {
		Set<Integer> digits = new HashSet<>();
		for (Candidate endoFin : xz.getEndoFins()) {
			digits.add(endoFin.getValue());
		}
		return digits.size();
	}

	private static String patternKey(SolutionStep step) {
		List<Integer> sorted = new ArrayList<>(step.getIndices());
		java.util.Collections.sort(sorted);
		StringBuilder key = new StringBuilder(sorted.toString());
		key.append('Z').append(step.getValues().get(step.getValues().size() - 1));
		List<String> elims = new ArrayList<>();
		for (Candidate cand : step.getCandidatesToDelete()) {
			elims.add(cand.getIndex() + ":" + cand.getValue());
		}
		java.util.Collections.sort(elims);
		key.append(elims);
		return key.toString();
	}

	private static void report(long millis) {
		System.out.println();
		System.out.println("=== T2 regime experiment ===");
		System.out.println("states examined:            " + states);
		System.out.println("states with canonical wing: " + statesWithCanonical);
		System.out.println("canonical steps harvested:  " + total + " (T1=" + t1 + ", T2=" + t2 + ")");
		System.out.println("distinct patterns:          " + distinct.size() + " (T2: " + distinctT2.size() + ")");
		System.out.println();
		System.out.println("(a) regime R1 (exactly one non-restricted candidate, and it is Z):");
		System.out.println("    " + regimeR1 + "/" + total + " = " + pct(regimeR1, total));
		for (String failure : regimeFailures) {
			System.out.println("    COUNTEREXAMPLE: " + failure);
		}
		System.out.println();
		System.out.println("(b) covered by a SIMPLE ALS-XZ (single RCC):");
		System.out.println("    all: " + coveredBySimple + "/" + total + " = " + pct(coveredBySimple, total));
		System.out.println("    T2:  " + coveredBySimpleT2 + "/" + t2 + " = " + pct(coveredBySimpleT2, t2));
		System.out.println();
		System.out.println("(c) needing a doubly linked cover: " + coveredOnlyByDoubly + "; no ALS-XZ cover at all: "
				+ notCovered);
		for (String failure : coverFailures) {
			System.out.println("    COUNTEREXAMPLE: " + failure);
		}
		System.out.println();
		System.out.println("runtime: " + (millis / 1000) + " s");
	}

	private static String pct(int part, int whole) {
		return whole == 0 ? "n/a" : String.format(java.util.Locale.ROOT, "%.1f%%", 100.0 * part / whole);
	}
}
