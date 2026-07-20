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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Smoke over the T&E(3) subset: the full solve (which today falls through to
 * brute force — these puzzles are far beyond the implemented techniques) must
 * terminate and every step must be sound. CI runs a deterministic sample
 * (every 10th puzzle) to keep the build fast; the full 200 were validated once
 * at milestone 0.3 close (see docs/log.md) and will be exercised for real by
 * the Tridagon fixtures of milestone 1.6.
 */
public class Te3SmokeTest {

	@TestFactory
	public Stream<DynamicTest> te3SampleSolvesTerminateAndAreSound() {
		List<String> puzzles = Fixtures.lines("/fixtures/te3-mith-200.txt");
		assertEquals(200, puzzles.size(), "T&E(3) subset must hold exactly 200 puzzles");
		return IntStream.range(0, puzzles.size()).filter(i -> i % 10 == 0)
				.mapToObj(puzzles::get)
				.map(puzzle -> dynamicTest("te3 smoke " + puzzle, () -> {
					PuzzleResult result = assertTimeoutPreemptively(Duration.ofMinutes(3),
							() -> HarnessRunner.analyze(puzzle));
					assertEquals(1, result.solutions, "must have a unique solution: " + puzzle);
					assertTrue(result.solved, "solve must terminate in a solved grid: " + puzzle);
					List<String> violations = SoundnessValidator.validate(result);
					assertTrue(violations.isEmpty(), () -> "soundness violations:\n" + String.join("\n", violations));
				}));
	}
}
