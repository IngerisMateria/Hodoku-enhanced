/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.10).
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
package registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sudoku.ui.DialogMinimumSize;

/**
 * Custody of the popup size floor (milestone 1.10, task D). Skipped headless (a
 * dialog cannot be built without a display), so CI reports it as skipped and the
 * rule is checked on a developer machine — same split the 1.9 popup position
 * memory uses.
 */
public class DialogMinimumSizeTest {

	@BeforeEach
	public void requireDisplay() {
		assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
	}

	/** A dialog whose content declares a real minimum. */
	private static JDialog dialogWithContent(int minW, int minH, int prefW, int prefH) {
		JDialog dialog = new JDialog();
		JPanel content = new JPanel(new BorderLayout());
		content.setMinimumSize(new Dimension(minW, minH));
		content.setPreferredSize(new Dimension(prefW, prefH));
		dialog.getContentPane().add(content, BorderLayout.CENTER);
		dialog.pack();
		return dialog;
	}

	@Test
	public void pinsTheFloorToTheContentMinimum() {
		JDialog dialog = dialogWithContent(200, 150, 400, 300);
		assertFalse(dialog.isMinimumSizeSet(), "nothing set before");
		Dimension expected = dialog.getMinimumSize();
		DialogMinimumSize.apply(dialog);
		assertTrue(dialog.isMinimumSizeSet(), "the popup must get a floor");
		assertEquals(expected, dialog.getMinimumSize());
		assertTrue(dialog.getMinimumSize().width >= 200, "the content minimum has to fit");
		dialog.dispose();
	}

	@Test
	public void neverAsksForMoreThanThePackedSize() {
		// a content whose minimum is larger than its preferred size: honouring it
		// would grow the dialog on open, which this milestone must not do
		JDialog dialog = dialogWithContent(500, 400, 200, 150);
		Dimension packed = dialog.getPreferredSize();
		DialogMinimumSize.apply(dialog);
		assertTrue(dialog.getMinimumSize().width <= packed.width, "floor must not exceed the packed width");
		assertTrue(dialog.getMinimumSize().height <= packed.height, "floor must not exceed the packed height");
		dialog.dispose();
	}

	@Test
	public void leavesAlonePopupsThatAlreadyDecided() {
		JDialog dialog = dialogWithContent(200, 150, 400, 300);
		Dimension own = new Dimension(123, 45);
		dialog.setMinimumSize(own);
		DialogMinimumSize.apply(dialog);
		assertEquals(own, dialog.getMinimumSize(), "an explicit minimum wins");
		dialog.dispose();

		JDialog fixed = dialogWithContent(200, 150, 400, 300);
		fixed.setResizable(false);
		DialogMinimumSize.apply(fixed);
		assertFalse(fixed.isMinimumSizeSet(), "a non-resizable popup needs no floor");
		fixed.dispose();
	}
}
