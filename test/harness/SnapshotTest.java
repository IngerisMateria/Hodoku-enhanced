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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Regression detection: the JSON solve path of every corpus puzzle must be
 * byte-identical to the committed snapshot. Any diff is red; intentional
 * updates go through <code>gradlew updateSnapshots</code> (see
 * docs/harness.md) and get reviewed in the commit.
 */
public class SnapshotTest {

	private static final String UPDATE_HINT = "if the change is intentional, regenerate with 'gradlew updateSnapshots' and review the diff";

	@Test
	public void snapshotCountMatchesCorpus() {
		List<String> corpus = Fixtures.lines("/fixtures/corpus.txt");
		List<String> snapshots = Fixtures.lines("/fixtures/snapshots.jsonl");
		assertEquals(corpus.size(), snapshots.size(),
				"snapshot line count does not match corpus; " + UPDATE_HINT);
	}

	@TestFactory
	public Stream<org.junit.jupiter.api.DynamicTest> solvePathsMatchSnapshots() {
		List<String> corpus = Fixtures.lines("/fixtures/corpus.txt");
		List<String> snapshots = Fixtures.lines("/fixtures/snapshots.jsonl");
		int n = Math.min(corpus.size(), snapshots.size());
		return IntStream.range(0, n).mapToObj(i -> dynamicTest("snapshot " + corpus.get(i), () -> {
			String actual = HarnessRunner.analyze(corpus.get(i)).toJsonLine();
			assertEquals(snapshots.get(i), actual,
					"solve path changed for puzzle " + corpus.get(i) + "; " + UPDATE_HINT);
		}));
	}
}
