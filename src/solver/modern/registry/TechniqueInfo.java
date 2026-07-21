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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import sudoku.SolutionType;

/**
 * Metadata of one solver technique — one row of the {@link TechniqueRegistry}
 * (single source for all the configuration/hint/analysis UX; the UI on top of
 * it is milestone 1.5).
 *
 * Two DISTINCT relations meet here and must not be mixed:
 * <ul>
 * <li><b>Taxonomy</b> ("is a case of"): {@link #getSubsumedBy()}, a
 * multi-parent DAG seeded from the owner's relation map
 * (docs/sudoku_mapa_relacional.md). Folder views and alias grouping follow
 * this relation.</li>
 * <li><b>Configuration ownership</b> ("my behavior depends on these
 * options"): the inverse of {@link OptionInfo#getOwners()}. The aside
 * inheritance of milestone 1.5 follows this relation, never the
 * taxonomy.</li>
 * </ul>
 *
 * Hard invariant (docs/estrategia-taxonomia.md §3): enum names, library codes
 * and persistence NEVER change because of aliasing — the display-name switch
 * is pure presentation indirection resolved through
 * {@link TechniqueRegistry#getDisplayName(SolutionType)}.
 */
public final class TechniqueInfo {

	private final SolutionType id;
	private final String displayNameDefault;
	private final List<String> aliases;
	private final String description;
	private final Family family;
	private final String engine;
	private final Set<SolutionType> subsumedBy;
	private final Set<Regime> regimes;
	private final List<String> references;

	TechniqueInfo(SolutionType id, String displayNameDefault, List<String> aliases, String description, Family family,
			String engine, Set<SolutionType> subsumedBy, Set<Regime> regimes, List<String> references) {
		this.id = id;
		this.displayNameDefault = displayNameDefault;
		this.aliases = List.copyOf(aliases);
		this.description = description;
		this.family = family;
		this.engine = engine;
		this.subsumedBy = subsumedBy.isEmpty() ? Set.of() : Set.copyOf(subsumedBy);
		this.regimes = regimes.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(regimes));
		this.references = List.copyOf(references);
	}

	/** The stable identity of the technique (enum name never changes). */
	public SolutionType getId() {
		return id;
	}

	/**
	 * The default display name — always the legacy intl bundle name of the
	 * {@link SolutionType}, so registry and hints cannot drift apart.
	 */
	public String getDisplayNameDefault() {
		return displayNameDefault;
	}

	/**
	 * Alternative names from the literature (searchable; candidates for the
	 * per-user display-name switch). Must not contain '=' or ';' (reserved by
	 * the preference persistence format, see
	 * {@link sudoku.Options#getTechniqueDisplayNames()}).
	 */
	public List<String> getAliases() {
		return aliases;
	}

	/** Short plain-language description (1-3 lines). */
	public String getDescription() {
		return description;
	}

	/** Coarse literature family (single bucket; not the taxonomy). */
	public Family getFamily() {
		return family;
	}

	/** Simple class name of the finder/engine that produces the steps. */
	public String getEngine() {
		return engine;
	}

	/**
	 * Taxonomy parents: the registered techniques this one is a special case
	 * of ("es caso de"). Multi-parent; the whole relation must stay an
	 * acyclic DAG (guarded by tests). Parents must themselves be registry
	 * rows.
	 */
	public Set<SolutionType> getSubsumedBy() {
		return subsumedBy;
	}

	/**
	 * Bent-set regimes this technique operates in, if the classification
	 * applies (empty otherwise). Note ALS-XZ spans both: one RCC is R1, the
	 * doubly linked case is R0.
	 */
	public Set<Regime> getRegimes() {
		return regimes;
	}

	/** Optional references (internal docs, primary sources). */
	public List<String> getReferences() {
		return references;
	}

	@Override
	public String toString() {
		return "TechniqueInfo[" + id.name() + " \"" + displayNameDefault + "\" " + family + "/" + engine + "]";
	}
}
