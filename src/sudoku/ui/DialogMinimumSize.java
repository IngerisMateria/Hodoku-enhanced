/*
 * Copyright (C) 2026  HoDoKu modern fork
 *
 * This file is part of HoDoKu.
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
package sudoku.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;

/**
 * Floor for the size of the popups (milestone 1.10, task D).
 * <p>
 * A Swing top-level window does not enforce the minimum size of what it
 * contains: AWT happily lets the user drag a dialog down to a sliver, and the
 * inner UI - which does have a fixed minimum - gets clipped instead of
 * reflowing. The owner's decision is explicitly <em>not</em> to make the dialogs
 * responsive (on a screen that small the content would not be usable anyway) but
 * to stop the shrinking at the point where the content stops fitting.
 * <p>
 * That point is the layout's own minimum: {@code Window#getMinimumSize()}
 * returns it as long as nobody has set one, which for these dialogs is a real,
 * content-derived size (the ConfigDialog for instance reports 556x643 against a
 * packed 582x643 - it has almost no slack, which is exactly why it was the one
 * that looked broken when squeezed).
 * <p>
 * Applied globally from {@code sudoku.DialogPositionMemory}, which already owns
 * the one AWT listener that sees every {@link Dialog} of the app, so this covers
 * every popup without editing them one by one.
 */
public final class DialogMinimumSize {

	private DialogMinimumSize() {
		// static helper
	}

	/**
	 * Pins the window's minimum size to the minimum of its content, once.
	 * <p>
	 * Never asks for more than the packed size: if a layout reported a minimum
	 * larger than its preferred size, honouring it would grow the dialog on open,
	 * and a milestone about tidying the UI has no business resizing windows the
	 * user did not touch.
	 *
	 * @param window the window; non-resizable ones and windows that already
	 *               declare a minimum are left alone
	 */
	public static void apply(Window window) {
		if (window == null || window.isMinimumSizeSet()) {
			return;
		}
		if (window instanceof Dialog && !((Dialog) window).isResizable()) {
			// cannot be shrunk in the first place
			return;
		}
		Dimension min = window.getMinimumSize();
		Dimension preferred = window.getPreferredSize();
		if (min == null || preferred == null) {
			return;
		}
		Dimension floor = new Dimension(Math.min(min.width, preferred.width),
				Math.min(min.height, preferred.height));
		if (floor.width <= 0 || floor.height <= 0) {
			return;
		}
		window.setMinimumSize(floor);
	}
}
