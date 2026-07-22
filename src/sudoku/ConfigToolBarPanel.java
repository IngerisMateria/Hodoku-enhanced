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
package sudoku;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import sudoku.ui.ToolBarItems;
import sudoku.ui.ToolBarLayout;
import sudoku.ui.UiMetrics;

/**
 * Configuration tab of the two configurable toolbars (milestone 1.10, P-009 Part
 * 1/2).
 * <p>
 * The owner's model is one list, read two ways: a pool of <em>available</em>
 * buttons on the left and the <em>shown</em> toolbar on the right, in display
 * order. Adding and removing is therefore the same gesture as hiding and
 * restoring, and reordering is a move inside the right hand list. The tab does
 * not offer any button that the toolbars do not already have - creating new
 * toolbar functions belongs to P-005 / P-009 Part 2/2.
 * <p>
 * Both toolbars are edited in the same tab, picked with the chooser at the top,
 * because they are the same kind of decision and a tab each would be noise.
 * <p>
 * Hand written (not NetBeans generated) but sized from {@link UiMetrics}, so it
 * matches the classic tabs.
 */
public final class ConfigToolBarPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** the toolbars, in the order of the chooser. */
	private static final String[][] BARS = { ToolBarItems.MAIN, ToolBarItems.ALL_STEPS };

	private final ToolBarLayout[] layouts = new ToolBarLayout[BARS.length];

	private JComboBox<String> barComboBox;
	private JList<String> availableList;
	private JList<String> shownList;
	private DefaultListModel<String> availableModel;
	private DefaultListModel<String> shownModel;
	private JButton addButton;
	private JButton removeButton;
	private JButton upButton;
	private JButton downButton;
	private JButton resetButton;

	/** guards the list selection handlers while the models are rebuilt. */
	private boolean updating;

	public ConfigToolBarPanel() {
		initComponents();
		tabEntered();
	}

	private void initComponents() {
		ResourceBundle bundle = ResourceBundle.getBundle("intl/ConfigToolBarPanel");

		barComboBox = new JComboBox<String>(new String[] { bundle.getString("ConfigToolBarPanel.bar.main"),
				bundle.getString("ConfigToolBarPanel.bar.allSteps") });
		barComboBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fillLists(null);
			}
		});

		availableModel = new DefaultListModel<String>();
		shownModel = new DefaultListModel<String>();
		availableList = createList(availableModel);
		shownList = createList(shownModel);

		addButton = new JButton(bundle.getString("ConfigToolBarPanel.addButton.text"));
		addButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String id = selectedId(availableList);
				if (id != null && currentLayout().show(id)) {
					fillLists(id);
					shownList.requestFocusInWindow();
				}
			}
		});
		removeButton = new JButton(bundle.getString("ConfigToolBarPanel.removeButton.text"));
		removeButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String id = selectedId(shownList);
				if (id != null && currentLayout().hide(id)) {
					fillLists(id);
					availableList.requestFocusInWindow();
				}
			}
		});
		upButton = new JButton(bundle.getString("ConfigToolBarPanel.upButton.text"));
		upButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String id = selectedId(shownList);
				if (id != null && currentLayout().moveUp(id)) {
					fillLists(id);
				}
			}
		});
		downButton = new JButton(bundle.getString("ConfigToolBarPanel.downButton.text"));
		downButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String id = selectedId(shownList);
				if (id != null && currentLayout().moveDown(id)) {
					fillLists(id);
				}
			}
		});
		resetButton = new JButton(bundle.getString("ConfigToolBarPanel.resetButton.text"));
		resetButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				currentLayout().reset();
				fillLists(null);
			}
		});

		JPanel availablePanel = listPanel(bundle.getString("ConfigToolBarPanel.availablePanel.border"), availableList);
		JPanel shownPanel = listPanel(bundle.getString("ConfigToolBarPanel.shownPanel.border"), shownList);

		JPanel transferPanel = new JPanel(new GridBagLayout());
		GridBagConstraints tc = new GridBagConstraints();
		tc.gridx = 0;
		tc.fill = GridBagConstraints.HORIZONTAL;
		tc.insets = new Insets(2, 0, 2, 0);
		transferPanel.add(addButton, tc);
		transferPanel.add(removeButton, tc);

		JPanel orderPanel = new JPanel(new GridBagLayout());
		orderPanel.add(upButton, tc);
		orderPanel.add(downButton, tc);

		JLabel barLabel = new JLabel(bundle.getString("ConfigToolBarPanel.barLabel.text"));
		barLabel.setLabelFor(barComboBox);
		JLabel hintLabel = new JLabel(bundle.getString("ConfigToolBarPanel.hintLabel.text"));
		hintLabel.setHorizontalAlignment(SwingConstants.LEADING);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(UiMetrics.BULLET_GAP, UiMetrics.BULLET_INSET, UiMetrics.BULLET_GAP,
				UiMetrics.BULLET_INSET);

		// row 0: the toolbar chooser; the toggle keeps the standard width and is
		// anchored left of the free space, like the classic tabs
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(UiMetrics.BULLET_INSET, UiMetrics.BULLET_INSET, UiMetrics.BULLET_GAP, 0);
		add(barLabel, c);
		c.gridx = 1;
		c.gridwidth = 3;
		c.insets = new Insets(UiMetrics.BULLET_INSET, UiMetrics.BULLET_GAP, UiMetrics.BULLET_GAP,
				UiMetrics.BULLET_INSET);
		barComboBox.setPreferredSize(
				new java.awt.Dimension(UiMetrics.TOGGLE_WIDTH + 60, barComboBox.getPreferredSize().height));
		add(barComboBox, c);

		// row 1: available | transfer | shown | order
		c.gridwidth = 1;
		c.gridy = 1;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, UiMetrics.BULLET_INSET, UiMetrics.BULLET_GAP, 0);
		c.gridx = 0;
		c.weightx = 1.0;
		add(availablePanel, c);
		c.gridx = 1;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, UiMetrics.BULLET_GAP, UiMetrics.BULLET_GAP, UiMetrics.BULLET_GAP);
		add(transferPanel, c);
		c.gridx = 2;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 0, UiMetrics.BULLET_GAP, 0);
		add(shownPanel, c);
		c.gridx = 3;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, UiMetrics.BULLET_GAP, UiMetrics.BULLET_GAP, UiMetrics.BULLET_INSET);
		add(orderPanel, c);

		// row 2: the explanation and the reset button
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, UiMetrics.BULLET_INSET, UiMetrics.BULLET_INSET, UiMetrics.BULLET_GAP);
		add(hintLabel, c);
		c.gridx = 3;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(0, UiMetrics.BULLET_GAP, UiMetrics.BULLET_INSET, UiMetrics.BULLET_INSET);
		add(resetButton, c);
	}

	private JList<String> createList(DefaultListModel<String> model) {
		JList<String> list = new JList<String>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new ItemRenderer());
		list.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			@Override
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (!updating) {
					checkButtons();
				}
			}
		});
		return list;
	}

	private JPanel listPanel(String title, JList<String> list) {
		JPanel panel = new JPanel(new java.awt.BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(title));
		JScrollPane scroll = new JScrollPane(list);
		scroll.setBorder(BorderFactory.createEmptyBorder(UiMetrics.BULLET_GAP, UiMetrics.BULLET_GAP,
				UiMetrics.BULLET_GAP, UiMetrics.BULLET_GAP));
		panel.add(scroll, java.awt.BorderLayout.CENTER);
		return panel;
	}

	/** @return the layout of the toolbar the chooser currently points at */
	private ToolBarLayout currentLayout() {
		return layouts[barComboBox.getSelectedIndex()];
	}

	private String selectedId(JList<String> list) {
		return list.getSelectedValue();
	}

	/**
	 * Rebuilds both lists from the current layout.
	 *
	 * @param keepSelected the id to leave selected afterwards, or null
	 */
	private void fillLists(String keepSelected) {
		updating = true;
		try {
			availableModel.clear();
			shownModel.clear();
			ToolBarLayout layout = currentLayout();
			for (String id : layout.getAvailable()) {
				availableModel.addElement(id);
			}
			for (String id : layout.getShown()) {
				shownModel.addElement(id);
			}
			if (keepSelected != null) {
				shownList.setSelectedValue(keepSelected, true);
				availableList.setSelectedValue(keepSelected, true);
			}
		} finally {
			updating = false;
		}
		checkButtons();
	}

	private void checkButtons() {
		String available = selectedId(availableList);
		String shown = selectedId(shownList);
		addButton.setEnabled(available != null);
		removeButton.setEnabled(shown != null);
		int index = shown == null ? -1 : shownModel.indexOf(shown);
		upButton.setEnabled(index > 0);
		downButton.setEnabled(index >= 0 && index < shownModel.getSize() - 1);
	}

	/** Rebuilds the tab from the stored options (A5 refresh-on-enter). */
	public void tabEntered() {
		layouts[0] = ToolBarLayout.parse(Options.getInstance().getToolBarMainItems(), ToolBarItems.MAIN);
		layouts[1] = ToolBarLayout.parse(Options.getInstance().getToolBarAllStepsItems(), ToolBarItems.ALL_STEPS);
		fillLists(null);
	}

	/** Writes the edited layouts back into the options. */
	public void okPressed() {
		Options.getInstance().setToolBarMainItems(layouts[0].format());
		Options.getInstance().setToolBarAllStepsItems(layouts[1].format());
	}

	/**
	 * Draws an item with the icon it wears in the toolbar plus its name. The
	 * toolbar icons are 32px; in a list that makes rows twice as tall as they need
	 * to be, so they are scaled down once and cached.
	 */
	private static final class ItemRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;
		private static final int ICON_SIZE = 20;

		private final java.util.Map<String, ImageIcon> icons = new java.util.HashMap<String, ImageIcon>();

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			String id = (String) value;
			setText(ToolBarItems.nameOf(id));
			setIcon(iconFor(id));
			return this;
		}

		private ImageIcon iconFor(String id) {
			if (icons.containsKey(id)) {
				return icons.get(id);
			}
			String resource = ToolBarItems.iconOf(id);
			ImageIcon icon = null;
			if (resource != null) {
				icon = new ImageIcon(new ImageIcon(getClass().getResource(resource)).getImage()
						.getScaledInstance(ICON_SIZE, ICON_SIZE, java.awt.Image.SCALE_SMOOTH));
			}
			icons.put(id, icon);
			return icon;
		}
	}
}
