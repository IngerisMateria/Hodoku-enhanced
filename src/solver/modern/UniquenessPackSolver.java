/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.8).
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
package solver.modern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import solver.AbstractSolver;
import solver.SudokuStepFinder;
import sudoku.Candidate;
import sudoku.Chain;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.Sudoku2;
import sudoku.SudokuSet;

/**
 * The Uniqueness Pack (milestone 1.8, Parte B): Unique Loop, Extended Unique
 * Rectangle, BUG-Lite, Reverse BUG and MUG (catalog v1). Spec:
 * docs/specs/uniqueness-pack.md; sources: Sudopedia mirror (Deadly Pattern,
 * BUG Lite, Reverse BUG), SudokuWiki (Extended Unique Rectangles), forum
 * t3210 (MUGs from BUG-Lite composites).
 *
 * <h3>Common frame (spec &sect;0)</h3>
 * All techniques assume a unique solution. A pattern is a set of cells with a
 * <i>nominal</i> candidate set per cell; stripped to the nominal content the
 * pattern would admit more than one completion (deadly), so the <i>guardians</i>
 * — the candidates beyond the nominal content — cannot all be false. The
 * guardian ladder (third use of {@link Guardians}, after the oddagons and the
 * Tridagon):
 * <ul>
 * <li>no guardians: contradictory state, log and skip (1.6 contract);</li>
 * <li>all guardians in ONE cell: the cell's solution is none of its nominal
 * digits — eliminate the nominal digits from that cell (&equiv; UR Type
 * 1);</li>
 * <li>several guardian cells, all guardians of one digit g: some g guardian is
 * true — eliminate g from every external cell that sees all guardian cells
 * (&equiv; UR Type 2);</li>
 * <li>mixed digits over several cells: out of scope v1 (Type 3-6
 * analogues).</li>
 * </ul>
 *
 * <h3>Mechanical mortality verifier (spec &sect;3/&sect;5)</h3>
 * {@link #isDeadly(int, int[], short[])} proves a stripped pattern deadly
 * before any step is emitted: it enumerates every internally valid assignment
 * (one nominal digit per cell, no digit twice among pattern cells of any
 * house) and groups them by the per-house digit multisets; the pattern is
 * deadly iff at least one assignment exists and EVERY group has &ge;2 members
 * — then whatever the true solution assigns to the pattern, a second
 * assignment with identical house multisets (hence indistinguishable to the
 * rest of the grid) would give a second solution. This is the ground truth
 * for UL, ExtUR, BUG-Lite and MUG; Reverse BUG uses the Sudopedia n/n/n
 * theorem on solved cells instead.
 *
 * <h3>Per technique</h3>
 * <ul>
 * <li><b>Unique Loop</b>: closed loop of L &isin; {6,8,10,12} cells (14+ out
 * of scope v1; L=4 is the legacy UR, excluded), all containing the pair
 * {a,b}, consecutive cells sharing a house, every house holding 0 or 2 loop
 * cells. The Sudopedia mirror page was unreachable at implementation time
 * (connection refused), so instead of trusting the textual condition alone
 * every candidate loop must pass the mortality verifier (which also enforces
 * the loop-parity subtlety: two same-house cells must take opposite colors in
 * the a&harr;b exchange). Enumeration mirrors {@link OddagonSolver}'s
 * canonical DFS (own loop here: even lengths and the 0-or-2 house count
 * replace link purity — too little shared structure to generalize the
 * oddagon enumerator without touching 1.6 code).</li>
 * <li><b>Extended UR</b>: the searchable 2&times;3 form of the 3&times;3
 * deadly pattern (SudokuWiki): six cells of a triple {a,b,c} on two parallel
 * lines of the same chute, crossing exactly three boxes (one per cell pair).
 * Nominal content = candidates &cap; triple (&ge;2 per cell); subtypes in the
 * hint (Type 1 = one guardian cell, Type 2 = uniform digit).</li>
 * <li><b>BUG-Lite</b>: connected sets of 6, 8 or 9 cells (Sudopedia counts
 * D(6)=4, D(8)=9, D(9)=3; 4 = UR, 10+ out of scope) with a nominal PAIR per
 * cell obeying "2 or nothing": every digit appears exactly 0 or 2 times in
 * every house of the pattern. Cells with &gt;2 candidates participate via
 * guardians (UR style, nominal chosen by the search); patterns whose cells
 * all share one pair are excluded (those are URs/Unique Loops — dedup to the
 * more specific technique). Requirement-driven DFS: each unmatched
 * (house,digit) occurrence must be paired, which keeps the pattern connected
 * (= minimal: a disconnected closed pattern splits into closed
 * sub-patterns).</li>
 * <li><b>Reverse BUG</b>: Sudopedia theorem — 2n cells of two digits form an
 * unavoidable set iff they occupy exactly n rows, n columns and n boxes
 * (n &lt; 9). If placing candidate d &isin; {a,b} would leave the solved a/b
 * cells as such a set, the (unsolved, hence given-free) complement of the
 * final solution's a/b cells would be unavoidable too — second solution —
 * so d is eliminated. Hard condition (spec &sect;0/&sect;4, mirroring
 * AvoidableRectangle): no solved a/b cell may be a given. The mirror's
 * "without the presence of hidden singles" clause is definitional flavor of
 * the impossible state, not a soundness condition of the elimination: the
 * complement argument holds regardless (documented in docs/log.md).</li>
 * <li><b>MUG</b> (catalog v1): the t3210 ab/abc block forms — two parallel
 * lines &times; three columns with nominal sets {x,s} | {x,s,y} | {s,y}
 * (s = shared digit) and their isomorphs (digit roles, orientation). Every
 * instance is validated by the mortality verifier before use (&sect;5), which
 * implicitly enforces the sound geometries (lines in one chute). Instances
 * whose cells would also form a valid Extended UR (nominal = full triple) are
 * skipped — the registry parents ExtUR under MUG and the more specific
 * technique owns the pattern.</li>
 * </ul>
 *
 * <h3>Step data layout</h3> (base fields only, see {@link ModernStep}):
 * {@code values} = the pattern digits ascending; {@code indices} = the
 * pattern cells (UL: loop order; Reverse BUG: the solved cells of the pair,
 * then the target cell last); {@code fins} = the guardians (Reverse BUG:
 * none); UL adds one display chain tracing the closed loop.
 */
public class UniquenessPackSolver extends AbstractSolver {

	private static final Logger LOGGER = Logger.getLogger(UniquenessPackSolver.class.getName());

	/** Loop lengths of the Unique Loop search (v1; 14+ documented out of scope). */
	private static final int MAX_LOOP = 12;
	private static final int MIN_LOOP = 6;

	/** Maximum BUG-Lite size (v1; 10+ documented out of scope). */
	private static final int MAX_BUG_LITE = 9;

	/** One global step for creation (cloned when a pattern is found). */
	private SolutionStep globalStep = new ModernStep();
	/** A list for all steps found in one search. */
	private List<SolutionStep> steps = new ArrayList<SolutionStep>();
	/** Dedup keys (type + cells + eliminations) of one all-steps search. */
	private Set<String> stepKeys = new HashSet<String>();
	/** The guardians of the current pattern. */
	private Guardians guardians = new Guardians();
	/** The pattern cells as a set (for elimination exclusion). */
	private SudokuSet patternSet = new SudokuSet();
	/** A set for elimination checks. */
	private SudokuSet elimSet = new SudokuSet();

	/** Single-step mode: stop at the first step found. */
	private boolean onlyOne;
	/** The first step found in single-step mode. */
	private SolutionStep result;

	// --- guardian-ladder feasibility prune (shared by the UL/BUG-Lite DFS) ---
	// The ladder only ever deduces when all guardians sit in one cell (Type 1)
	// or share one digit (Type 2). As soon as a partial pattern has >=2
	// guardian cells with >=2 distinct guardian digits, no completion can
	// yield a step: prune the whole branch (this is what keeps the searches
	// bounded on open states).
	/** Distinct guardian cells of the current partial pattern. */
	private int pruneGuardianCells;
	/** Bitmask of guardian digits of the current partial pattern. */
	private int pruneGuardianDigits;

	/** Is a deduction still possible with these partial guardians? */
	private boolean ladderStillPossible() {
		return pruneGuardianCells <= 1 || Integer.bitCount(pruneGuardianDigits) <= 1;
	}

	/** The guardian digits bitmask a cell adds beyond its nominal mask. */
	private int guardianDigitsOf(int cell, short nominal) {
		int mask = 0;
		int[] digits = Sudoku2.POSSIBLE_VALUES[(short) (sudoku.getCell(cell) & ~nominal)];
		for (int i = 0; i < digits.length; i++) {
			mask |= 1 << digits[i];
		}
		return mask;
	}

	// --- Unique Loop search state ---
	/** The current pair (a < b). */
	private int digitA, digitB;
	/** The pair mask. */
	private short pairMask;
	/** The loop cells in loop order. */
	private int[] loopCells = new int[MAX_LOOP];
	/** Cell membership of the current path. */
	private boolean[] inLoop = new boolean[Sudoku2.LENGTH];
	/** Loop cells per house (0..26) of the current path. */
	private int[] houseCount = new int[27];

	// --- BUG-Lite search state ---
	/** The member cells of the current pattern. */
	private int[] bugCells = new int[MAX_BUG_LITE];
	/** The nominal pair mask per member. */
	private short[] bugNominals = new short[MAX_BUG_LITE];
	/** The number of members. */
	private int bugSize;
	/** Membership flag per cell. */
	private boolean[] inBug = new boolean[Sudoku2.LENGTH];
	/** Per (house 0..26, digit 1..9): nominal occurrences in the pattern. */
	private int[][] bugCounts = new int[27][10];
	/** Guardian-prune state saved per member (restored on backtrack). */
	private int[] bugSavedGuardianCells = new int[MAX_BUG_LITE];
	private int[] bugSavedGuardianDigits = new int[MAX_BUG_LITE];

	// --- shared verifier state (sized for the largest client: 12-cell loops) ---
	private int[] verifierCells = new int[MAX_LOOP];
	private short[] verifierNominals = new short[MAX_LOOP];
	private int[] verifierAssignment = new int[MAX_LOOP];
	private short[] verifierUsed = new short[27];
	private Map<String, Integer> signatureCounts = new HashMap<String, Integer>();

	/**
	 * Creates a new instance of UniquenessPackSolver
	 *
	 * @param finder
	 */
	public UniquenessPackSolver(SudokuStepFinder finder) {
		super(finder);
	}

	@Override
	protected SolutionStep getStep(SolutionType type) {
		sudoku = finder.getSudoku();
		switch (type) {
		case UNIQUE_LOOP:
			return findUniqueLoops(true);
		case EXTENDED_UR:
			return findExtendedUrs(true);
		case BUG_LITE:
			return findBugLites(true);
		case REVERSE_BUG:
			return findReverseBugs(true);
		case MUG:
			return findMugs(true);
		default:
			return null;
		}
	}

	@Override
	protected boolean doStep(SolutionStep step) {
		sudoku = finder.getSudoku();
		switch (step.getType()) {
		case UNIQUE_LOOP:
		case EXTENDED_UR:
		case BUG_LITE:
		case REVERSE_BUG:
		case MUG:
			for (Candidate cand : step.getCandidatesToDelete()) {
				sudoku.delCandidate(cand.getIndex(), cand.getValue());
			}
			return true;
		default:
			return false;
		}
	}

	/**
	 * Finds all steps of the five pack techniques (all-steps mode; the caller
	 * filters by enabled step types).
	 *
	 * @return all steps found
	 */
	public List<SolutionStep> getAllUniquenessPack() {
		sudoku = finder.getSudoku();
		List<SolutionStep> newSteps = new ArrayList<SolutionStep>();
		List<SolutionStep> oldSteps = steps;
		steps = newSteps;
		stepKeys.clear();
		findUniqueLoops(false);
		findExtendedUrs(false);
		findReverseBugs(false);
		findBugLites(false);
		findMugs(false);
		steps = oldSteps;
		return newSteps;
	}

	// ==================================================================
	// mortality verifier (spec §3/§5)
	// ==================================================================

	/**
	 * Proves the stripped pattern deadly: enumerates every internally valid
	 * assignment of one nominal digit per cell and requires every per-house
	 * digit-multiset signature class to contain at least two assignments (and
	 * at least one assignment to exist). See the class javadoc for why this
	 * implies a second solution whenever all guardians are false.
	 *
	 * @param n the number of cells
	 * @param cells the pattern cells
	 * @param nominals the nominal candidate mask per cell
	 * @return true if the pattern is provably deadly
	 */
	private boolean isDeadly(int n, int[] cells, short[] nominals) {
		System.arraycopy(cells, 0, verifierCells, 0, n);
		System.arraycopy(nominals, 0, verifierNominals, 0, n);
		signatureCounts.clear();
		for (int i = 0; i < 27; i++) {
			verifierUsed[i] = 0;
		}
		enumerateAssignments(0, n);
		if (signatureCounts.isEmpty()) {
			return false;
		}
		for (int count : signatureCounts.values()) {
			if (count < 2) {
				return false;
			}
		}
		return true;
	}

	/** Recursive assignment enumeration for {@link #isDeadly}. */
	private void enumerateAssignments(int depth, int n) {
		if (depth == n) {
			String signature = assignmentSignature(n);
			Integer old = signatureCounts.get(signature);
			signatureCounts.put(signature, old == null ? 1 : old + 1);
			return;
		}
		int cell = verifierCells[depth];
		int[] digits = Sudoku2.POSSIBLE_VALUES[verifierNominals[depth]];
		for (int i = 0; i < digits.length; i++) {
			int digit = digits[i];
			short mask = Sudoku2.MASKS[digit];
			int[] houses = Sudoku2.CONSTRAINTS[cell];
			if ((verifierUsed[houses[0]] & mask) != 0 || (verifierUsed[houses[1]] & mask) != 0
					|| (verifierUsed[houses[2]] & mask) != 0) {
				continue;
			}
			verifierAssignment[depth] = digit;
			verifierUsed[houses[0]] |= mask;
			verifierUsed[houses[1]] |= mask;
			verifierUsed[houses[2]] |= mask;
			enumerateAssignments(depth + 1, n);
			verifierUsed[houses[0]] &= ~mask;
			verifierUsed[houses[1]] &= ~mask;
			verifierUsed[houses[2]] &= ~mask;
		}
	}

	/**
	 * The per-house digit multiset signature of the current assignment: for
	 * every house, the sorted digits the pattern places there.
	 */
	private String assignmentSignature(int n) {
		// house -> sorted digit list; cells are processed in fixed order, so
		// per house the digits appear in cell order — sort per house
		StringBuilder sb = new StringBuilder(64);
		for (int h = 0; h < 27; h++) {
			int digitsMask = 0;
			int digitsList = 0;
			for (int i = 0; i < n; i++) {
				int[] houses = Sudoku2.CONSTRAINTS[verifierCells[i]];
				if (houses[0] == h || houses[1] == h || houses[2] == h) {
					// valid assignments never repeat a digit in a house, so a
					// bitmask is a faithful multiset here
					digitsMask |= 1 << verifierAssignment[i];
					digitsList++;
				}
			}
			if (digitsList > 0) {
				sb.append(h).append(':').append(digitsMask).append(';');
			}
		}
		return sb.toString();
	}

	// ==================================================================
	// guardian ladder (spec §0)
	// ==================================================================

	/**
	 * Collects the guardians of the current pattern: every candidate of a
	 * pattern cell beyond its nominal mask.
	 */
	private void collectGuardians(int n, int[] cells, short[] nominals) {
		guardians.clear();
		patternSet.clear();
		for (int i = 0; i < n; i++) {
			patternSet.add(cells[i]);
			short extras = (short) (sudoku.getCell(cells[i]) & ~nominals[i]);
			int[] digits = Sudoku2.POSSIBLE_VALUES[extras];
			for (int j = 0; j < digits.length; j++) {
				guardians.add(cells[i], digits[j]);
			}
		}
	}

	/**
	 * Applies the guardian ladder to the current pattern and emits a step if a
	 * deduction exists.
	 *
	 * @param type the technique
	 * @param n number of pattern cells
	 * @param cells the pattern cells
	 * @param nominals the nominal mask per cell
	 * @param withChain add a display chain tracing the cells as a closed loop
	 */
	private void applyGuardianLadder(SolutionType type, int n, int[] cells, short[] nominals, boolean withChain) {
		if (guardians.size() == 0) {
			// contradictory state: never "eliminate everything" (1.6 contract)
			LOGGER.log(Level.WARNING, "{0} without guardians: contradictory state, pattern skipped", type);
			return;
		}
		if (guardians.getCellSet().size() == 1) {
			// all guardians in one cell: its solution is none of the nominal
			// digits — strip them (≡ UR Type 1)
			int cell = guardians.getCell(0);
			int nominalMask = 0;
			for (int i = 0; i < n; i++) {
				if (cells[i] == cell) {
					nominalMask = nominals[i];
					break;
				}
			}
			int[] digits = Sudoku2.POSSIBLE_VALUES[(short) (sudoku.getCell(cell) & nominalMask)];
			if (digits.length == 0) {
				return;
			}
			initStep(type, n, cells, withChain);
			for (int i = 0; i < digits.length; i++) {
				globalStep.addCandidateToDelete(cell, digits[i]);
			}
			emitStep();
			return;
		}
		int digit = guardians.uniformDigit();
		if (digit == -1) {
			// mixed guardian digits over several cells: Type 3-6 analogues,
			// out of scope v1 (spec §0)
			return;
		}
		// some guardian of the uniform digit is true: eliminate the digit from
		// every external cell that sees ALL guardian cells (≡ UR Type 2)
		guardians.collectBuddies(elimSet);
		elimSet.and(finder.getCandidates()[digit]);
		elimSet.andNot(guardians.getCellSet());
		elimSet.andNot(patternSet);
		if (elimSet.isEmpty()) {
			return;
		}
		initStep(type, n, cells, withChain);
		for (int i = 0; i < elimSet.size(); i++) {
			globalStep.addCandidateToDelete(elimSet.get(i), digit);
		}
		emitStep();
	}

	/** Fills {@link #globalStep} with type, digits, cells, guardians. */
	private void initStep(SolutionType type, int n, int[] cells, boolean withChain) {
		globalStep.reset();
		globalStep.setType(type);
		// values: the distinct pattern digits ascending (set by each finder)
		for (int d = 1; d <= 9; d++) {
			if ((currentDigitMask & (1 << d)) != 0) {
				globalStep.addValue(d);
			}
		}
		for (int i = 0; i < n; i++) {
			globalStep.addIndex(cells[i]);
		}
		for (int i = 0; i < guardians.size(); i++) {
			globalStep.addFin(guardians.getCell(i), guardians.getDigit(i));
		}
		if (withChain) {
			// display chain: the closed loop (first cell repeated), lets
			// SudokuPanel paint it without changes (Tridagon precedent)
			int[] chain = new int[n + 1];
			for (int i = 0; i < n; i++) {
				chain[i] = Chain.makeSEntry(cells[i], digitA, true);
			}
			chain[n] = chain[0];
			globalStep.addChain(0, n, chain);
		}
	}

	/** The distinct digits (bit d = digit d) of the current pattern, set by each finder. */
	private int currentDigitMask;

	/**
	 * Emits {@link #globalStep}: single-step mode sets {@link #result},
	 * all-steps mode deduplicates by type + cells + eliminations.
	 */
	private void emitStep() {
		if (onlyOne) {
			if (result == null) {
				result = (SolutionStep) globalStep.clone();
			}
			return;
		}
		if (stepKeys.add(stepKey(globalStep))) {
			steps.add((SolutionStep) globalStep.clone());
		}
	}

	/** Equality key of a step: type + cells (order-insensitive) + eliminations. */
	private static String stepKey(SolutionStep step) {
		StringBuilder key = new StringBuilder();
		key.append(step.getType().ordinal());
		List<Integer> cells = new ArrayList<Integer>(step.getIndices());
		java.util.Collections.sort(cells);
		key.append('|').append(cells);
		key.append('|');
		for (Candidate cand : step.getCandidatesToDelete()) {
			key.append(cand.getIndex()).append(':').append(cand.getValue()).append(',');
		}
		return key.toString();
	}

	// ==================================================================
	// Unique Loop
	// ==================================================================

	/**
	 * Searches Unique Loops for every pair {a,b}.
	 *
	 * @param onlyOne return the first step found instead of collecting all
	 * @return the first step found if <code>onlyOne</code> is set, else null
	 */
	private SolutionStep findUniqueLoops(boolean onlyOne) {
		this.onlyOne = onlyOne;
		result = null;
		for (int a = 1; a <= 8; a++) {
			for (int b = a + 1; b <= 9; b++) {
				// milestone 1.8 A4: cooperative cancel for find-all-steps
				if (Thread.currentThread().isInterrupted()) {
					return result;
				}
				digitA = a;
				digitB = b;
				pairMask = (short) (Sudoku2.MASKS[a] | Sudoku2.MASKS[b]);
				currentDigitMask = (1 << a) | (1 << b);
				searchLoopsForPair();
				if (onlyOne && result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/** Is the cell unsolved and does it contain both pair digits? */
	private boolean isLoopEligible(int cell) {
		return sudoku.getValue(cell) == 0 && (sudoku.getCell(cell) & pairMask) == pairMask;
	}

	/** Canonical DFS over the eligible cells of the current pair. */
	private void searchLoopsForPair() {
		for (int i = 0; i < 27; i++) {
			houseCount[i] = 0;
		}
		for (int start = 0; start < Sudoku2.LENGTH; start++) {
			if (!isLoopEligible(start)) {
				continue;
			}
			loopCells[0] = start;
			inLoop[start] = true;
			addHouses(start, 1);
			int gm = guardianDigitsOf(start, pairMask);
			pruneGuardianCells = gm != 0 ? 1 : 0;
			pruneGuardianDigits = gm;
			extendLoop(1);
			addHouses(start, -1);
			inLoop[start] = false;
			if (onlyOne && result != null) {
				return;
			}
		}
	}

	private void addHouses(int cell, int delta) {
		int[] houses = Sudoku2.CONSTRAINTS[cell];
		houseCount[houses[0]] += delta;
		houseCount[houses[1]] += delta;
		houseCount[houses[2]] += delta;
	}

	/** Do the two cells share a house? */
	private static boolean sharesHouse(int c1, int c2) {
		return c1 / 9 == c2 / 9 || c1 % 9 == c2 % 9 || (c1 / 27 == c2 / 27 && (c1 % 9) / 3 == (c2 % 9) / 3);
	}

	/**
	 * Depth-first loop extension. Canonical form (mirrors OddagonSolver): all
	 * cells &gt; start, and at close the second cell must be smaller than the
	 * last (kills the reverse traversal). Houses never exceed 2 loop cells.
	 */
	private void extendLoop(int depth) {
		int last = loopCells[depth - 1];
		// try to close at even lengths >= MIN_LOOP
		if (depth >= MIN_LOOP && depth % 2 == 0 && sharesHouse(last, loopCells[0]) && loopCells[1] < last) {
			checkLoop(depth);
			if (onlyOne && result != null) {
				return;
			}
		}
		if (depth == MAX_LOOP) {
			return;
		}
		for (int next = loopCells[0] + 1; next < Sudoku2.LENGTH; next++) {
			if (inLoop[next] || !isLoopEligible(next) || !sharesHouse(last, next)) {
				continue;
			}
			int[] houses = Sudoku2.CONSTRAINTS[next];
			// a house may never hold more than 2 loop cells (0-or-2 condition)
			if (houseCount[houses[0]] >= 2 || houseCount[houses[1]] >= 2 || houseCount[houses[2]] >= 2) {
				continue;
			}
			int gm = guardianDigitsOf(next, pairMask);
			int savedCells = pruneGuardianCells;
			int savedDigits = pruneGuardianDigits;
			if (gm != 0) {
				pruneGuardianCells++;
				pruneGuardianDigits |= gm;
			}
			if (ladderStillPossible()) {
				loopCells[depth] = next;
				inLoop[next] = true;
				addHouses(next, 1);
				extendLoop(depth + 1);
				addHouses(next, -1);
				inLoop[next] = false;
			}
			pruneGuardianCells = savedCells;
			pruneGuardianDigits = savedDigits;
			if (onlyOne && result != null) {
				return;
			}
		}
	}

	/**
	 * Validates a closed candidate loop: every touched house must hold exactly
	 * 2 loop cells (0-or-2 over the whole board), and the stripped loop must
	 * pass the mortality verifier. Then guardians + ladder.
	 */
	private void checkLoop(int n) {
		for (int h = 0; h < 27; h++) {
			if (houseCount[h] == 1) {
				return;
			}
		}
		short[] nominals = new short[n];
		for (int i = 0; i < n; i++) {
			nominals[i] = pairMask;
		}
		if (!isDeadly(n, loopCells, nominals)) {
			return;
		}
		collectGuardians(n, loopCells, nominals);
		applyGuardianLadder(SolutionType.UNIQUE_LOOP, n, loopCells, nominals, true);
	}

	// ==================================================================
	// Extended Unique Rectangle
	// ==================================================================

	/**
	 * Searches Extended URs: for every triple and both orientations, two
	 * parallel lines of the same chute crossing three boxes.
	 */
	private SolutionStep findExtendedUrs(boolean onlyOne) {
		this.onlyOne = onlyOne;
		result = null;
		int[] cells = new int[6];
		short[] nominals = new short[6];
		for (int a = 1; a <= 7; a++) {
			for (int b = a + 1; b <= 8; b++) {
				for (int c = b + 1; c <= 9; c++) {
					// milestone 1.8 A4: cooperative cancel for find-all-steps
					if (Thread.currentThread().isInterrupted()) {
						return result;
					}
					short tripleMask = (short) (Sudoku2.MASKS[a] | Sudoku2.MASKS[b] | Sudoku2.MASKS[c]);
					currentDigitMask = (1 << a) | (1 << b) | (1 << c);
					// orientation 0: two rows of one band × one column per stack
					// orientation 1: two columns of one stack × one row per band
					for (int orient = 0; orient < 2; orient++) {
						for (int chute = 0; chute < 3; chute++) {
							for (int l1 = 0; l1 < 2; l1++) {
								for (int l2 = l1 + 1; l2 < 3; l2++) {
									int line1 = chute * 3 + l1;
									int line2 = chute * 3 + l2;
									for (int p0 = 0; p0 < 3; p0++) {
										for (int p1 = 0; p1 < 3; p1++) {
											for (int p2 = 0; p2 < 3; p2++) {
												int[] cross = { p0, 3 + p1, 6 + p2 };
												boolean ok = true;
												for (int i = 0; i < 3 && ok; i++) {
													int c1 = cellOf(orient, line1, cross[i]);
													int c2 = cellOf(orient, line2, cross[i]);
													cells[2 * i] = c1;
													cells[2 * i + 1] = c2;
													ok = extUrEligible(c1, tripleMask, nominals, 2 * i)
															&& extUrEligible(c2, tripleMask, nominals, 2 * i + 1);
												}
												if (!ok) {
													continue;
												}
												if (!isDeadly(6, cells, nominals)) {
													continue;
												}
												collectGuardians(6, cells, nominals);
												applyGuardianLadder(SolutionType.EXTENDED_UR, 6, cells, nominals,
														false);
												if (onlyOne && result != null) {
													return result;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	/** The cell at (line, cross) in the given orientation. */
	private static int cellOf(int orient, int line, int cross) {
		return orient == 0 ? line * 9 + cross : cross * 9 + line;
	}

	/** Unsolved with &ge;2 triple candidates; stores the nominal mask. */
	private boolean extUrEligible(int cell, short tripleMask, short[] nominals, int i) {
		if (sudoku.getValue(cell) != 0) {
			return false;
		}
		short nominal = (short) (sudoku.getCell(cell) & tripleMask);
		if (Sudoku2.ANZ_VALUES[nominal] < 2) {
			return false;
		}
		nominals[i] = nominal;
		return true;
	}

	// ==================================================================
	// BUG-Lite
	// ==================================================================

	/**
	 * Searches BUG-Lites: connected closed sets of cells with a nominal pair
	 * each, sizes 6/8/9, "2 or nothing" per digit per house, mechanically
	 * verified deadly.
	 */
	private SolutionStep findBugLites(boolean onlyOne) {
		this.onlyOne = onlyOne;
		result = null;
		for (int start = 0; start < Sudoku2.LENGTH; start++) {
			// milestone 1.8 A4: cooperative cancel for find-all-steps
			if (Thread.currentThread().isInterrupted()) {
				return result;
			}
			if (sudoku.getValue(start) != 0) {
				continue;
			}
			int[] cands = Sudoku2.POSSIBLE_VALUES[sudoku.getCell(start)];
			if (cands.length < 2) {
				continue;
			}
			// try every nominal pair of the start cell
			for (int i = 0; i < cands.length; i++) {
				for (int j = i + 1; j < cands.length; j++) {
					bugSize = 0;
					pruneGuardianCells = 0;
					pruneGuardianDigits = 0;
					for (int h = 0; h < 27; h++) {
						java.util.Arrays.fill(bugCounts[h], 0);
					}
					addBugMember(start, (short) (Sudoku2.MASKS[cands[i]] | Sudoku2.MASKS[cands[j]]));
					extendBugLite();
					removeBugMember();
					if (onlyOne && result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	private void addBugMember(int cell, short nominal) {
		bugCells[bugSize] = cell;
		bugNominals[bugSize] = nominal;
		bugSavedGuardianCells[bugSize] = pruneGuardianCells;
		bugSavedGuardianDigits[bugSize] = pruneGuardianDigits;
		int gm = guardianDigitsOf(cell, nominal);
		if (gm != 0) {
			pruneGuardianCells++;
			pruneGuardianDigits |= gm;
		}
		bugSize++;
		inBug[cell] = true;
		int[] digits = Sudoku2.POSSIBLE_VALUES[nominal];
		int[] houses = Sudoku2.CONSTRAINTS[cell];
		for (int h = 0; h < 3; h++) {
			for (int d = 0; d < digits.length; d++) {
				bugCounts[houses[h]][digits[d]]++;
			}
		}
	}

	private void removeBugMember() {
		bugSize--;
		pruneGuardianCells = bugSavedGuardianCells[bugSize];
		pruneGuardianDigits = bugSavedGuardianDigits[bugSize];
		int cell = bugCells[bugSize];
		short nominal = bugNominals[bugSize];
		inBug[cell] = false;
		int[] digits = Sudoku2.POSSIBLE_VALUES[nominal];
		int[] houses = Sudoku2.CONSTRAINTS[cell];
		for (int h = 0; h < 3; h++) {
			for (int d = 0; d < digits.length; d++) {
				bugCounts[houses[h]][digits[d]]--;
			}
		}
	}

	/**
	 * Requirement-driven DFS: find the first (member, house, digit) whose
	 * count is 1 and try every partner; when no requirement is open the
	 * pattern is closed — check sizes/digits and emit.
	 */
	private void extendBugLite() {
		if (!ladderStillPossible()) {
			// no completion of this partial pattern can yield a deduction
			return;
		}
		// find the first unsatisfied requirement, deterministically
		int reqHouse = -1;
		int reqDigit = -1;
		for (int m = 0; m < bugSize && reqHouse == -1; m++) {
			int[] houses = Sudoku2.CONSTRAINTS[bugCells[m]];
			int[] digits = Sudoku2.POSSIBLE_VALUES[bugNominals[m]];
			for (int h = 0; h < 3 && reqHouse == -1; h++) {
				for (int d = 0; d < digits.length; d++) {
					if (bugCounts[houses[h]][digits[d]] == 1) {
						reqHouse = houses[h];
						reqDigit = digits[d];
						break;
					}
				}
			}
		}
		if (reqHouse == -1) {
			// closed pattern
			checkBugLite();
			return;
		}
		if (bugSize >= MAX_BUG_LITE) {
			return;
		}
		int start = bugCells[0];
		int[] houseCells = Sudoku2.ALL_UNITS[reqHouse];
		for (int i = 0; i < houseCells.length; i++) {
			int cell = houseCells[i];
			// canonical: the start cell is the smallest member
			if (cell <= start || inBug[cell] || sudoku.getValue(cell) != 0) {
				continue;
			}
			short cellMask = sudoku.getCell(cell);
			if ((cellMask & Sudoku2.MASKS[reqDigit]) == 0) {
				continue;
			}
			// nominal = required digit + one partner digit
			int[] cands = Sudoku2.POSSIBLE_VALUES[cellMask];
			for (int j = 0; j < cands.length; j++) {
				if (cands[j] == reqDigit) {
					continue;
				}
				short nominal = (short) (Sudoku2.MASKS[reqDigit] | Sudoku2.MASKS[cands[j]]);
				if (bugCountOverflow(cell, nominal)) {
					continue;
				}
				addBugMember(cell, nominal);
				extendBugLite();
				removeBugMember();
				if (onlyOne && result != null) {
					return;
				}
			}
		}
	}

	/** Would adding the member push any (house,digit) count past 2? */
	private boolean bugCountOverflow(int cell, short nominal) {
		int[] digits = Sudoku2.POSSIBLE_VALUES[nominal];
		int[] houses = Sudoku2.CONSTRAINTS[cell];
		for (int h = 0; h < 3; h++) {
			for (int d = 0; d < digits.length; d++) {
				if (bugCounts[houses[h]][digits[d]] >= 2) {
					return true;
				}
			}
		}
		return false;
	}

	/** Validates a closed pattern: size, digit spread, mortality; then ladder. */
	private void checkBugLite() {
		if (bugSize != 6 && bugSize != 8 && bugSize != 9) {
			// 4 = the legacy UR; 5/7 have no deadly forms (Sudopedia counts);
			// 10+ out of scope v1
			return;
		}
		int digitMask = 0;
		for (int i = 0; i < bugSize; i++) {
			int[] digits = Sudoku2.POSSIBLE_VALUES[bugNominals[i]];
			digitMask |= (1 << digits[0]) | (1 << digits[1]);
		}
		if (Integer.bitCount(digitMask) <= 2) {
			// every cell shares one pair: that is a UR / Unique Loop (or a
			// composite of them) — dedup to the more specific technique
			return;
		}
		if (!isDeadly(bugSize, bugCells, bugNominals)) {
			return;
		}
		currentDigitMask = digitMask;
		collectGuardians(bugSize, bugCells, bugNominals);
		short[] nominals = new short[bugSize];
		System.arraycopy(bugNominals, 0, nominals, 0, bugSize);
		int[] cells = new int[bugSize];
		System.arraycopy(bugCells, 0, cells, 0, bugSize);
		applyGuardianLadder(SolutionType.BUG_LITE, bugSize, cells, nominals, false);
	}

	// ==================================================================
	// Reverse BUG
	// ==================================================================

	/**
	 * Searches Reverse BUGs: for every pair {a,b} whose solved cells are all
	 * non-given, every unsolved cell whose placement of a or b would leave
	 * those solved cells as a 2n / n rows / n columns / n boxes unavoidable
	 * set loses that candidate.
	 */
	private SolutionStep findReverseBugs(boolean onlyOne) {
		this.onlyOne = onlyOne;
		result = null;
		int[] solved = new int[18];
		for (int a = 1; a <= 8; a++) {
			for (int b = a + 1; b <= 9; b++) {
				int count = 0;
				boolean hasGiven = false;
				for (int cell = 0; cell < Sudoku2.LENGTH; cell++) {
					int value = sudoku.getValue(cell);
					if (value == a || value == b) {
						if (sudoku.isFixed(cell)) {
							// hard condition (spec §0/§4): a given anchors the
							// set — no deduction for this pair
							hasGiven = true;
							break;
						}
						solved[count++] = cell;
					}
				}
				if (hasGiven || count == 0 || count % 2 == 0 || count >= 17) {
					// a placement completes the set, so the solved cells must
					// number 2n-1 (odd); 2n < 18 keeps n < 9
					continue;
				}
				currentDigitMask = (1 << a) | (1 << b);
				checkReverseBugPlacements(a, b, solved, count);
				if (onlyOne && result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/** Tries every placement of a or b that would complete an n/n/n set. */
	private void checkReverseBugPlacements(int a, int b, int[] solved, int count) {
		int n = (count + 1) / 2;
		for (int cell = 0; cell < Sudoku2.LENGTH; cell++) {
			if (sudoku.getValue(cell) != 0) {
				continue;
			}
			short mask = sudoku.getCell(cell);
			for (int d = 0; d < 2; d++) {
				int digit = d == 0 ? a : b;
				if ((mask & Sudoku2.MASKS[digit]) == 0) {
					continue;
				}
				if (!isNnnSet(solved, count, cell, n)) {
					continue;
				}
				// placing digit here would create the Reverse BUG state:
				// eliminate the candidate
				globalStep.reset();
				globalStep.setType(SolutionType.REVERSE_BUG);
				globalStep.addValue(a);
				globalStep.addValue(b);
				for (int i = 0; i < count; i++) {
					globalStep.addIndex(solved[i]);
				}
				globalStep.addIndex(cell);
				globalStep.addCandidateToDelete(cell, digit);
				emitStep();
				if (onlyOne && result != null) {
					return;
				}
			}
		}
	}

	/** Do the solved cells plus the extra cell span exactly n rows/cols/boxes? */
	private static boolean isNnnSet(int[] solved, int count, int extra, int n) {
		int rows = 0;
		int cols = 0;
		int boxes = 0;
		for (int i = 0; i <= count; i++) {
			int cell = i == count ? extra : solved[i];
			int row = cell / 9;
			int col = cell % 9;
			int box = (row / 3) * 3 + col / 3;
			rows |= 1 << row;
			cols |= 1 << col;
			boxes |= 1 << box;
		}
		return Integer.bitCount(rows) == n && Integer.bitCount(cols) == n && Integer.bitCount(boxes) == n;
	}

	// ==================================================================
	// MUG (catalog v1)
	// ==================================================================

	/**
	 * Searches the catalog MUG forms (t3210): two parallel lines &times; three
	 * crossings with nominal sets {x,s} | {x,s,y} | {s,y} (s = the shared
	 * digit) in every digit-role assignment and both orientations. Every
	 * instance must pass the mortality verifier; instances that would also
	 * form a valid Extended UR are skipped (the more specific technique owns
	 * them).
	 */
	private SolutionStep findMugs(boolean onlyOne) {
		this.onlyOne = onlyOne;
		result = null;
		int[] cells = new int[6];
		short[] nominals = new short[6];
		for (int a = 1; a <= 7; a++) {
			for (int b = a + 1; b <= 8; b++) {
				for (int c = b + 1; c <= 9; c++) {
					// milestone 1.8 A4: cooperative cancel for find-all-steps
					if (Thread.currentThread().isInterrupted()) {
						return result;
					}
					int[] triple = { a, b, c };
					short tripleMask = (short) (Sudoku2.MASKS[a] | Sudoku2.MASKS[b] | Sudoku2.MASKS[c]);
					currentDigitMask = (1 << a) | (1 << b) | (1 << c);
					for (int role = 0; role < 3; role++) {
						int shared = triple[role];
						int x = triple[role == 0 ? 1 : 0];
						int y = triple[role == 2 ? 1 : 2];
						short maskXS = (short) (Sudoku2.MASKS[x] | Sudoku2.MASKS[shared]);
						short maskSY = (short) (Sudoku2.MASKS[shared] | Sudoku2.MASKS[y]);
						for (int orient = 0; orient < 2; orient++) {
							searchMugGeometry(orient, maskXS, tripleMask, maskSY, cells, nominals);
							if (onlyOne && result != null) {
								return result;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * True if the six cells are exactly the shape the ExtUR finder catches: two
	 * parallel lines in the same chute, three crossings in three distinct
	 * perpendicular chutes, every cell holding all three triple digits. Those
	 * belong to the (more specific) Extended UR technique.
	 */
	private boolean isExtUrShape(int orient, int line1, int line2, int crossXS, int crossXSY, int crossSY, int[] cells,
			short tripleMask) {
		if (line1 / 3 != line2 / 3) {
			return false;
		}
		if (crossXS / 3 == crossXSY / 3 || crossXS / 3 == crossSY / 3 || crossXSY / 3 == crossSY / 3) {
			return false;
		}
		for (int i = 0; i < 6; i++) {
			if ((sudoku.getCell(cells[i]) & tripleMask) != tripleMask) {
				return false;
			}
		}
		return true;
	}

	/** Walks line pairs and crossing triples for one digit-role assignment. */
	private void searchMugGeometry(int orient, short maskXS, short maskXSY, short maskSY, int[] cells,
			short[] nominals) {
		for (int line1 = 0; line1 < 8; line1++) {
			for (int line2 = line1 + 1; line2 < 9; line2++) {
				// per crossing line: eligibility for each of the three roles
				for (int crossXS = 0; crossXS < 9; crossXS++) {
					if (!mugPairEligible(orient, line1, line2, crossXS, maskXS)) {
						continue;
					}
					for (int crossXSY = 0; crossXSY < 9; crossXSY++) {
						if (crossXSY == crossXS || !mugPairEligible(orient, line1, line2, crossXSY, maskXSY)) {
							continue;
						}
						for (int crossSY = 0; crossSY < 9; crossSY++) {
							if (crossSY == crossXS || crossSY == crossXSY
									|| !mugPairEligible(orient, line1, line2, crossSY, maskSY)) {
								continue;
							}
							cells[0] = cellOf9(orient, line1, crossXS);
							cells[1] = cellOf9(orient, line2, crossXS);
							cells[2] = cellOf9(orient, line1, crossXSY);
							cells[3] = cellOf9(orient, line2, crossXSY);
							cells[4] = cellOf9(orient, line1, crossSY);
							cells[5] = cellOf9(orient, line2, crossSY);
							nominals[0] = maskXS;
							nominals[1] = maskXS;
							nominals[2] = maskXSY;
							nominals[3] = maskXSY;
							nominals[4] = maskSY;
							nominals[5] = maskSY;
							if (!isDeadly(6, cells, nominals)) {
								continue;
							}
							// ExtUR dedup (spec 5b): skip exactly the shape the ExtUR
							// finder catches - two parallel lines of the same chute, three
							// crossings in three distinct stacks/bands, every cell holding
							// all three triple digits. Anything else is a genuine
							// (non-ExtUR) catalog MUG.
							if (isExtUrShape(orient, line1, line2, crossXS, crossXSY, crossSY, cells, maskXSY)) {
								continue;
							}
							collectGuardians(6, cells, nominals);
							applyGuardianLadder(SolutionType.MUG, 6, cells, nominals, false);
							if (onlyOne && result != null) {
								return;
							}
						}
					}
				}
			}
		}
	}

	/** The cell at (line 0..8, cross 0..8) in the given orientation. */
	private static int cellOf9(int orient, int line, int cross) {
		return orient == 0 ? line * 9 + cross : cross * 9 + line;
	}

	/** Are both crossing cells unsolved and holding the nominal set? */
	private boolean mugPairEligible(int orient, int line1, int line2, int cross, short nominal) {
		int c1 = cellOf9(orient, line1, cross);
		int c2 = cellOf9(orient, line2, cross);
		return sudoku.getValue(c1) == 0 && sudoku.getValue(c2) == 0
				&& (sudoku.getCell(c1) & nominal) == nominal && (sudoku.getCell(c2) & nominal) == nominal;
	}
}
