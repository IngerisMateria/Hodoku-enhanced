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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** End-to-end check of the --batch-json console mode. */
public class BatchJsonModeTest {

	private static final String EASY_PUZZLE = "...1.5...14....67..8...24...63.7..1.9.......3.1..9.52...72...8..26....35...4.9...";

	@TempDir
	Path tempDir;

	@Test
	public void writesOneJsonLinePerPuzzleAndReportsInvalidLines() throws Exception {
		Path in = tempDir.resolve("in.txt");
		Path out = tempDir.resolve("out.jsonl");
		// UTF-8 BOM on purpose: files written on Windows often carry one
		Files.write(in, ("\uFEFF# comment\n" + EASY_PUZZLE + "\n\nnot-a-puzzle\n").getBytes(StandardCharsets.UTF_8));

		int exitCode = BatchJsonMode.run(new String[] { "--batch-json", in.toString(), "--out", out.toString() });
		assertEquals(0, exitCode);

		List<String> lines = Files.readAllLines(out, StandardCharsets.UTF_8);
		assertEquals(2, lines.size(), "comment and blank lines must be skipped");
		assertTrue(lines.get(0).startsWith("{\"puzzle\":\"" + EASY_PUZZLE + "\""), "puzzle echoed in JSON");
		assertTrue(lines.get(0).contains("\"solved\":true"), "easy puzzle must solve");
		assertTrue(lines.get(0).contains("\"steps\":[{\"technique\":"), "solve path present");
		assertTrue(lines.get(1).contains("\"error\":"), "invalid line must produce an error object");
	}

	@Test
	public void unknownOptionFails() {
		assertEquals(1, BatchJsonMode.run(new String[] { "--batch-json", "x.txt", "--frobnicate" }));
	}

	@Test
	public void missingInputFileFails() {
		Path missing = tempDir.resolve("does-not-exist.txt");
		assertEquals(1, BatchJsonMode.run(new String[] { "--batch-json", missing.toString() }));
	}
}
