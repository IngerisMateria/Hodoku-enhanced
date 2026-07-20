/*
 * This file is part of the HoDoKu fork verification harness (milestone 0.2).
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
 * Corpus contract: every corpus puzzle has a unique solution, is solved
 * logically with the default technique configuration, and every step of the
 * solve path is sound (no elimination/placement contradicts the brute-force
 * solution). The extreme smoke puzzles only need uniqueness, soundness and
 * termination.
 */
public class CorpusTest {

	@TestFactory
	public Stream<DynamicTest> corpusPuzzlesAreUniqueSolvedAndSound() {
		List<String> puzzles = Fixtures.lines("/fixtures/corpus.txt");
		assertTrue(puzzles.size() >= 25, "corpus unexpectedly small: " + puzzles.size() + " puzzles");
		return puzzles.stream().map(puzzle -> dynamicTest("corpus " + puzzle, () -> {
			PuzzleResult result = assertTimeoutPreemptively(Duration.ofMinutes(2),
					() -> HarnessRunner.analyze(puzzle));
			assertEquals(1, result.solutions, "corpus puzzle must have a unique solution: " + puzzle);
			assertTrue(result.solved, "corpus puzzle must be solved logically: " + puzzle);
			List<String> violations = SoundnessValidator.validate(result);
			assertTrue(violations.isEmpty(), () -> "soundness violations:\n" + String.join("\n", violations));
		}));
	}

	@TestFactory
	public Stream<DynamicTest> extremePuzzlesAreUniqueSoundAndTerminate() {
		List<String> puzzles = Fixtures.lines("/fixtures/extremes.txt");
		assertTrue(puzzles.size() >= 2, "expected at least 2 extreme smoke puzzles");
		return puzzles.stream().map(puzzle -> dynamicTest("extreme " + puzzle, () -> {
			PuzzleResult result = assertTimeoutPreemptively(Duration.ofMinutes(3),
					() -> HarnessRunner.analyze(puzzle));
			assertEquals(1, result.solutions, "extreme puzzle must have a unique solution: " + puzzle);
			List<String> violations = SoundnessValidator.validate(result);
			assertTrue(violations.isEmpty(), () -> "soundness violations:\n" + String.join("\n", violations));
		}));
	}
}
