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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Loads fixture files from the test classpath (test/fixtures/). */
final class Fixtures {

	private Fixtures() {
	}

	/**
	 * Returns the non-blank, non-comment lines of a classpath resource, e.g.
	 * <code>lines("/fixtures/corpus.txt")</code>.
	 */
	static List<String> lines(String resource) {
		InputStream in = Fixtures.class.getResourceAsStream(resource);
		if (in == null) {
			throw new IllegalStateException("fixture not found on test classpath: " + resource);
		}
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
					lines.add(trimmed);
				}
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return lines;
	}

	/** Same as {@link #lines(String)} but keeps every line verbatim. */
	static List<String> rawLines(String resource) {
		InputStream in = Fixtures.class.getResourceAsStream(resource);
		if (in == null) {
			throw new IllegalStateException("fixture not found on test classpath: " + resource);
		}
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return lines;
	}
}
