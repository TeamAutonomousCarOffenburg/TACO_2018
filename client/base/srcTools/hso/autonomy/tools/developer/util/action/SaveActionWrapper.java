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
package hso.autonomy.tools.developer.util.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManagerListener;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;
import hso.autonomy.tools.util.imageBuffer.ImageFile;

public class SaveActionWrapper extends AbstractAction implements PropertyChangeListener, IPerspectiveManagerListener
{
	private final IPerspectiveManager model;

	private Action saveAction;

	public SaveActionWrapper(IPerspectiveManager model)
	{
		super("Save", ImageFile.SAVE.getIcon());
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));

		this.model = model;

		setEnabled(false);

		this.model.addPerspectiveManagerListener(this);
	}

	void setSaveAction(Action action)
	{
		if (saveAction != null) {
			saveAction.removePropertyChangeListener(this);
		}

		saveAction = action;

		if (saveAction != null) {
			setEnabled(saveAction.isEnabled());
			saveAction.addPropertyChangeListener(this);
		} else {
			setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (saveAction != null) {
			saveAction.actionPerformed(e);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals("enabled")) {
			this.setEnabled((Boolean) evt.getNewValue());
		}
	}

	@Override
	public void activeViewChanged()
	{
		if (model.getActiveView() != null) {
			setSaveAction(model.getActiveView().getSaveAction());
		} else {
			setSaveAction(null);
		}
	}

	@Override
	public void viewOpened(IView newView)
	{
	}

	@Override
	public void viewClosed(IView view)
	{
	}
}
