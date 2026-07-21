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
package solver.modern.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sudoku.Options;
import sudoku.SolutionType;

/**
 * The technique/option metadata registry (milestone 1.4) — single source for
 * the configuration/hint/analysis UX. Techniques come from
 * {@link TechniqueCatalog}, options from {@link OptionCatalog}; completeness
 * and consistency are guarded by the registry tests.
 *
 * Display names: {@link #getDisplayName(SolutionType)} resolves the name to
 * show for a technique — the per-user preferred alias if one is persisted in
 * {@link Options#getTechniqueDisplayNames()}, the default (legacy intl) name
 * otherwise. Enum names, library codes and persistence never change through
 * this indirection. The modern hint formatters ({@code ModernStep}) resolve
 * through here; the legacy hint pipeline is untouched for now (future
 * increment, see docs/milestones/1.4.md item 7).
 */
public final class TechniqueRegistry {

	private static final TechniqueRegistry INSTANCE = new TechniqueRegistry();

	private final Map<SolutionType, TechniqueInfo> techniques;
	private final List<OptionInfo> options;

	/** Cache of the parsed display-name preferences (raw string -> map). */
	private String parsedPreferences = null;
	private Map<String, String> preferenceMap = Map.of();

	private TechniqueRegistry() {
		Map<SolutionType, TechniqueInfo> rows = new EnumMap<>(SolutionType.class);
		for (TechniqueInfo info : TechniqueCatalog.load()) {
			TechniqueInfo previous = rows.put(info.getId(), info);
			if (previous != null) {
				throw new IllegalStateException("duplicate registry row for " + info.getId());
			}
		}
		techniques = Collections.unmodifiableMap(rows);
		options = List.copyOf(OptionCatalog.load());
	}

	public static TechniqueRegistry getInstance() {
		return INSTANCE;
	}

	/** The row of one technique, or null if the type has no registry row. */
	public TechniqueInfo get(SolutionType type) {
		return techniques.get(type);
	}

	/** All technique rows, in enum order. */
	public Collection<TechniqueInfo> all() {
		return techniques.values();
	}

	/** All option rows. */
	public List<OptionInfo> options() {
		return options;
	}

	/**
	 * The options owned by one technique — the configuration-ownership
	 * relation the 1.5 aside will render.
	 */
	public List<OptionInfo> optionsOwnedBy(SolutionType type) {
		List<OptionInfo> owned = new ArrayList<>();
		for (OptionInfo option : options) {
			if (option.getOwners().contains(type)) {
				owned.add(option);
			}
		}
		return owned;
	}

	/**
	 * The display name of a technique: the persisted per-user preference if
	 * present, the default name otherwise. Types without a registry row
	 * (subtypes collapsed into another config entry, pseudo steps) fall back
	 * to their legacy step name.
	 */
	public String getDisplayName(SolutionType type) {
		String preferred = preferences().get(type.name());
		if (preferred != null && !preferred.isEmpty()) {
			return preferred;
		}
		TechniqueInfo info = techniques.get(type);
		return info != null ? info.getDisplayNameDefault() : type.getStepName();
	}

	/**
	 * Persists the preferred display name for one technique in
	 * {@link Options} (no UI in this milestone — 1.5 adds the switch).
	 * Passing null, an empty string or the default name removes the
	 * preference. Names must not contain '=' or ';' (reserved by the
	 * persistence format).
	 *
	 * @param type the technique
	 * @param name the preferred name (normally one of the aliases), or null
	 */
	public void setPreferredDisplayName(SolutionType type, String name) {
		if (name != null && (name.indexOf('=') >= 0 || name.indexOf(';') >= 0)) {
			throw new IllegalArgumentException("display name must not contain '=' or ';': " + name);
		}
		Map<String, String> prefs = new HashMap<>(preferences());
		TechniqueInfo info = techniques.get(type);
		String defaultName = info != null ? info.getDisplayNameDefault() : type.getStepName();
		if (name == null || name.isEmpty() || name.equals(defaultName)) {
			prefs.remove(type.name());
		} else {
			prefs.put(type.name(), name);
		}
		Options.getInstance().setTechniqueDisplayNames(encode(prefs));
	}

	/** Parses (and caches) the persisted preference string. */
	private Map<String, String> preferences() {
		String raw = Options.getInstance().getTechniqueDisplayNames();
		if (raw == null) {
			raw = "";
		}
		if (!raw.equals(parsedPreferences)) {
			Map<String, String> parsed = new HashMap<>();
			for (String entry : raw.split(";")) {
				int eq = entry.indexOf('=');
				if (eq > 0) {
					parsed.put(entry.substring(0, eq).trim(), entry.substring(eq + 1).trim());
				}
			}
			preferenceMap = parsed;
			parsedPreferences = raw;
		}
		return preferenceMap;
	}

	/** Encodes a preference map into the persisted string format. */
	private static String encode(Map<String, String> prefs) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : prefs.entrySet()) {
			if (sb.length() > 0) {
				sb.append(';');
			}
			sb.append(entry.getKey()).append('=').append(entry.getValue());
		}
		return sb.toString();
	}
}
