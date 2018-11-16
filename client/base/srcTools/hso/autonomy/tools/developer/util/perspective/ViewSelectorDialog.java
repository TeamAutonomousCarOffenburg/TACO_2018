/*******************************************************************************
 * Copyright 2008, 2012 Hochschule Offenburg
 * Klaus Dorer, Mathias Ehret, Stefan Glaser, Thomas Huber, Fabian Korak,
 * Simon Raffeiner, Srinivasa Ragavan, Thomas Rinklin,
 * Joachim Schilling, Ingo Schindler, Rajit Shahi, Bjoern Weiler
 *
 * This file is part of magmaOffenburg.
 *
 * magmaOffenburg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * magmaOffenburg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with magmaOffenburg. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package hso.autonomy.tools.developer.util.perspective;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IViewDescriptor;
import hso.autonomy.tools.developer.bundles.developer.window.IWindowManager;
import hso.autonomy.tools.util.imageBuffer.ImageFile;
import hso.autonomy.tools.util.swing.SearchableList;

public class ViewSelectorDialog extends JDialog
{
	private final IPerspectiveManager perspectiveManager;

	private SearchableList<IViewDescriptor> searchableList;

	public ViewSelectorDialog(IPerspectiveManager perspectiveManager, IWindowManager windowManager)
	{
		this.perspectiveManager = perspectiveManager;

		setTitle("Open View");
		setIconImage(windowManager.getMainWindow().getIconImage());

		initializeComponents();
		setModal(true);
		reset();
	}

	private void initializeComponents()
	{
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {1};
		layout.columnWeights = new double[] {0.1};
		layout.rowHeights = new int[] {1, 1};
		layout.rowWeights = new double[] {1, 0.0};
		setLayout(layout);

		createSearchableList();
		add(searchableList, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));

		add(createButtonPanel(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHEAST,
										 GridBagConstraints.NONE, new Insets(1, 5, 5, 5), 0, 0));

		// dialog-global key events
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			if (e.getID() == KeyEvent.KEY_RELEASED && isVisible() && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				ViewSelectorDialog.this.setVisible(false);
			}
			return false;
		});
	}

	private void createSearchableList()
	{
		searchableList = new SearchableList<>(perspectiveManager.getAvailableViews(), this ::openView,
				new SearchableList.ItemDescriptor<IViewDescriptor>() {
					@Override
					public String getName(IViewDescriptor item)
					{
						return item.getTitle();
					}

					@Override
					public Icon getIcon(IViewDescriptor item)
					{
						return item.getIcon();
					}
				});
	}

	private JPanel createButtonPanel()
	{
		JPanel buttonPanel = new JPanel();
		JButton openButton = new JButton("Open");
		openButton.setFocusable(false);
		getRootPane().setDefaultButton(openButton);
		openButton.addActionListener(e -> openView(searchableList.getSelectedItem()));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setFocusable(false);
		cancelButton.addActionListener(e -> ViewSelectorDialog.this.setVisible(false));
		buttonPanel.add(cancelButton);
		buttonPanel.add(openButton);
		return buttonPanel;
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if (visible) {
			reset();
		}
	}

	private void reset()
	{
		searchableList.requestFilterTextFieldFocus();
		searchableList.setFilterText("");
		setSize(300, 400);
	}

	private void openView(IViewDescriptor view)
	{
		if (view != null) {
			ViewSelectorDialog.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			perspectiveManager.openView(view);
			ViewSelectorDialog.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			ViewSelectorDialog.this.setVisible(false);
		}
	}
}
