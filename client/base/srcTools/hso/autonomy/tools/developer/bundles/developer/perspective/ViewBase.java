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
package hso.autonomy.tools.developer.bundles.developer.perspective;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.plaf.LayerUI;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.swing.command.ICommandList;

/**
 * @author Stefan Glaser
 */
public abstract class ViewBase implements IView
{
	protected final IViewDescriptor viewDescriptor;

	private String name;

	protected int index;

	protected Action saveAction;

	protected Action saveAsAction;

	private JComponent contentPanel;

	private JComponent innerPanel;

	private IPerspectiveManager perspectiveManager;

	protected ViewBase(BundleContext context, IViewDescriptor viewDescriptor, String name)
	{
		this.viewDescriptor = viewDescriptor;
		this.name = name;
		perspectiveManager = IDeveloperService.PERSPECTIVE_MANAGER.get(context);
	}

	@Override
	public IViewDescriptor getDescriptor()
	{
		return viewDescriptor;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public JComponent getContentComponent()
	{
		if (contentPanel == null) {
			innerPanel = createContentComponent();
			contentPanel = createMouseFocusWrapper(innerPanel);
		}
		return contentPanel;
	}

	private JLayer<JComponent> createMouseFocusWrapper(JComponent panel)
	{
		LayerUI<JComponent> ui = new LayerUI<JComponent>() {
			@Override
			protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> l)
			{
				if (e.getID() == MouseEvent.MOUSE_RELEASED) {
					perspectiveManager.setActiveView(ViewBase.this);
				}
			}

			@Override
			protected void processKeyEvent(KeyEvent e, JLayer<? extends JComponent> l)
			{
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_W) {
					perspectiveManager.closeView(ViewBase.this);
				}
			}
		};
		JLayer<JComponent> layer = new JLayer<>(innerPanel, ui);
		layer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
		layer.setLayerEventMask(AWTEvent.KEY_EVENT_MASK);
		return layer;
	}

	@Override
	public JComponent getInnerContentComponent()
	{
		if (innerPanel == null) {
			getContentComponent();
		}
		return innerPanel;
	}

	@Override
	public boolean wasModified()
	{
		return false;
	}

	@Override
	public Action getSaveAction()
	{
		return saveAction;
	}

	@Override
	public Action getSaveAsAction()
	{
		return saveAsAction;
	}

	@Override
	public void discardChanges()
	{
	}

	@Override
	public ICommandList getCommandList()
	{
		return null;
	}

	@Override
	public boolean acceptsObject(Object o)
	{
		return viewDescriptor.acceptsObject(o);
	}

	@Override
	public void selectObject(Object o)
	{
	}

	@Override
	public Object getSelectedObject()
	{
		return null;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
	}

	@Override
	public boolean isClosable()
	{
		return true;
	}

	@Override
	public void onClose()
	{
	}

	protected abstract JComponent createContentComponent();
}
