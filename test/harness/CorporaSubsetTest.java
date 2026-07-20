/*
 * This file is part of the HoDoKu fork verification harness (milestone 0.3).
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Contract for the curated corpus subsets ingested in milestone 0.3 (see
 * docs/corpora.md): every puzzle has a unique solution. Uniqueness only — a
 * full logical solve of these puzzles is exercised separately (they are T&E(2)
 * and T&E(3) monsters, far beyond the implemented techniques for now).
 */
public class CorporaSubsetTest {

	@TestFactory
	public Stream<DynamicTest> te3SubsetPuzzlesHaveUniqueSolutions() {
		List<String> puzzles = Fixtures.lines("/fixtures/te3-mith-200.txt");
		assertEquals(200, puzzles.size(), "T&E(3) subset must hold exactly 200 puzzles");
		return uniquenessTests("te3", puzzles);
	}

	@TestFactory
	public Stream<DynamicTest> te2SubsetPuzzlesHaveUniqueSolutions() {
		List<String> puzzles = Fixtures.lines("/fixtures/te2-eleven-100.txt");
		assertEquals(100, puzzles.size(), "T&E(2) subset must hold exactly 100 puzzles");
		return uniquenessTests("te2", puzzles);
	}

	private static Stream<DynamicTest> uniquenessTests(String prefix, List<String> puzzles) {
		return puzzles.stream().map(puzzle -> dynamicTest(prefix + " " + puzzle, () -> {
			int solutions = assertTimeoutPreemptively(Duration.ofSeconds(30),
					() -> HarnessRunner.countSolutions(puzzle));
			assertEquals(1, solutions, "subset puzzle must have a unique solution: " + puzzle);
		}));
	}
}
