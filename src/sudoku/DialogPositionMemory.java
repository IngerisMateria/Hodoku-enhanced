/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.9).
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
package sudoku;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;

/**
 * Popup position memory (milestone 1.9, P-003): every dialog reopens at the
 * last on-screen location the user left it at, instead of the generic spot.
 *
 * <p>A single global {@link AWTEventListener}, installed once at startup before
 * any dialog can be shown, covers every {@link Dialog} without touching the
 * individual dialog classes: on show it restores the stored location (if still
 * on a connected screen), on move it stores the new one, persisted per dialog
 * class in {@link Options#getDialogLocations()}.
 *
 * <p>Scope note (the exceptions the prompt asks to list): only
 * {@link java.awt.Dialog} windows are tracked — the top-level frames (the
 * MainFrame and the tool windows KeyboardLayoutFrame / SudokuConsoleFrame /
 * UIQuickBrowse / UIExportLine / UIImportLine) keep their own placement and are
 * deliberately out of scope. Transient modal progress dialogs are tracked by
 * the same mechanism but only ever remember a location the user actually
 * dragged them to, so they cost nothing when left alone.
 */
public final class DialogPositionMemory {

	/** Small margins so a restored title bar always stays grabbable. */
	private static final int MARGIN_RIGHT = 40;
	private static final int MARGIN_BOTTOM = 20;
	private static final int MARGIN_LEFT = 8;

	private static boolean installed = false;

	private DialogPositionMemory() {
	}

	/** Installs the global listener once. Safe to call repeatedly; no-op headless. */
	public static synchronized void install() {
		if (installed || GraphicsEnvironment.isHeadless()) {
			return;
		}
		installed = true;
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				if (!(event instanceof ComponentEvent)) {
					return;
				}
				Component c = ((ComponentEvent) event).getComponent();
				if (!(c instanceof Dialog)) {
					// only dialogs (popups); frames keep their own placement
					return;
				}
				Window w = (Window) c;
				String key = w.getClass().getName();
				switch (event.getID()) {
				case ComponentEvent.COMPONENT_SHOWN:
					Point stored = Options.getInstance().getDialogLocation(key);
					if (stored != null && isReachable(stored)) {
						w.setLocation(stored);
					}
					break;
				case ComponentEvent.COMPONENT_MOVED:
					if (w.isShowing()) {
						Options.getInstance().setDialogLocation(key, w.getLocation());
					}
					break;
				default:
					break;
				}
			}
		}, AWTEvent.COMPONENT_EVENT_MASK);
	}

	/**
	 * True if the point lies on some connected screen device with enough margin
	 * for the dialog's title bar to remain reachable — guards against restoring
	 * onto a monitor that has since been disconnected.
	 */
	private static boolean isReachable(Point p) {
		for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			Rectangle b = gd.getDefaultConfiguration().getBounds();
			if (p.x >= b.x - MARGIN_LEFT && p.y >= b.y && p.x <= b.x + b.width - MARGIN_RIGHT
					&& p.y <= b.y + b.height - MARGIN_BOTTOM) {
				return true;
			}
		}
		return false;
	}
}
