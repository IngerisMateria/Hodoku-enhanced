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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Console mode <code>--batch-json &lt;file&gt; [--out &lt;file&gt;]</code>:
 * reads one 81 character puzzle per line and writes the complete logical solve
 * path of each puzzle as JSON Lines (one object per puzzle). Uses double-dash
 * syntax on purpose: MSYS/Git Bash mangles the legacy '/xy' options into
 * Windows paths (documented in milestone 0.1). Schema: docs/harness.md.
 */
public final class BatchJsonMode {

	private BatchJsonMode() {
	}

	/** Entry point called from sudoku.Main. Returns the process exit code. */
	public static int run(String[] args) {
		String inFile = null;
		String outFile = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--batch-json")) {
				if (i + 1 >= args.length) {
					return usage("--batch-json requires an input file");
				}
				inFile = args[++i];
			} else if (args[i].equals("--out")) {
				if (i + 1 >= args.length) {
					return usage("--out requires an output file");
				}
				outFile = args[++i];
			} else {
				return usage("unknown option: " + args[i]);
			}
		}
		if (inFile == null) {
			return usage("no input file given");
		}

		List<String> lines = new ArrayList<String>();
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8))) {
			String line;
			boolean first = true;
			while ((line = in.readLine()) != null) {
				if (first) {
					first = false;
					// files written on Windows often carry a UTF-8 BOM
					if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
						line = line.substring(1);
					}
				}
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}
				lines.add(trimmed);
			}
		} catch (IOException ex) {
			System.err.println("Cannot read input file '" + inFile + "': " + ex.getMessage());
			return 1;
		}

		PrintWriter out;
		boolean toStdout = outFile == null || outFile.equals("stdout");
		try {
			if (toStdout) {
				out = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
			} else {
				out = new PrintWriter(
						new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8));
			}
		} catch (IOException ex) {
			System.err.println("Cannot open output file '" + outFile + "': " + ex.getMessage());
			return 1;
		}

		try {
			for (String line : lines) {
				out.print(HarnessRunner.analyze(line).toJsonLine());
				out.print('\n');
			}
			out.flush();
		} finally {
			if (!toStdout) {
				out.close();
			}
		}
		return 0;
	}

	private static int usage(String message) {
		System.err.println(message);
		System.err.println("Usage: --batch-json <file> [--out <file|stdout>]");
		System.err.println("  <file>: one 81 character puzzle per line ('.' or '0' for empty cells;");
		System.err.println("          blank lines and lines starting with '#' are skipped)");
		System.err.println("  Output: JSON Lines, one object per puzzle (schema: docs/harness.md);");
		System.err.println("          written to stdout unless --out is given");
		return 1;
	}
}
