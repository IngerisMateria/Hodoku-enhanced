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

/**
 * The canonical classic tabs of the configuration dialog. Per the project
 * rule, every configuration option has exactly one canonical classic tab and
 * additionally appears in the aside of its owning techniques (the aside is
 * milestone 1.5).
 *
 * The mapping to legacy panels: GENERAL = ConfigGeneralPanel, SOLVER =
 * ConfigSolverPanel (technique order/enable), ALL_POSSIBLE_STEPS =
 * ConfigFindAllStepsPanel, STEPS = ConfigStepPanel (solver technique
 * options), COLORS = ConfigColorPanel.
 */
public enum ConfigTab {
	GENERAL,
	SOLVER,
	ALL_POSSIBLE_STEPS,
	STEPS,
	COLORS
}
