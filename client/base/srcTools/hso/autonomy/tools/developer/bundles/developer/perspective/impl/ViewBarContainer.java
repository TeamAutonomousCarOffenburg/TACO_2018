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
package hso.autonomy.tools.developer.bundles.developer.perspective.impl;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;
import hso.autonomy.tools.developer.bundles.developer.perspective.IViewContainer;
import hso.autonomy.tools.developer.bundles.developer.perspective.dnd.ViewTransferData;
import hso.autonomy.tools.developer.bundles.developer.perspective.dnd.ViewTransferable;

public class ViewBarContainer extends JPanel implements IViewContainer
{
	private static final GhostGlassPane ghostGlassPane = new GhostGlassPane();

	private boolean hasGhost = true;

	private CardLayout toolBoxCardLayout;

	private JPanel toolBoxPanel;

	private ButtonGroup btnGroup;

	private ArrayList<ViewToggleButton> buttons;

	private JToolBar toolBar;

	private IView activeView;

	private final DragGestureListener dgl;

	private final String containerID;

	private final IPerspectiveManager perspectiveManager;

	private final JButton defaultButton = new JButton();

	public ViewBarContainer(String containerID, IPerspectiveManager perspectiveManager)
	{
		this.containerID = containerID;
		this.perspectiveManager = perspectiveManager;

		initializeComponents();

		final DragSourceListener dsl = new DragSourceListener() {
			@Override
			public void dragEnter(DragSourceDragEvent e)
			{
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			@Override
			public void dragExit(DragSourceEvent e)
			{
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}

			@Override
			public void dragOver(DragSourceDragEvent e)
			{
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			@Override
			public void dragDropEnd(DragSourceDropEvent e)
			{
				// Drag-drop-action finished
				if (hasGhost()) {
					ghostGlassPane.setVisible(false);
				}
			}

			@Override
			public void dropActionChanged(DragSourceDragEvent e)
			{
			}
		};

		dgl = e ->
		{
			int dragBtnIndex = buttons.indexOf(e.getComponent());
			if (dragBtnIndex < 0) {
				return;
			}

			try {
				IView view = buttons.get(dragBtnIndex).getView();
				e.startDrag(DragSource.DefaultMoveDrop, new ViewTransferable(ViewBarContainer.this, view, dragBtnIndex),
						dsl);
			} catch (InvalidDnDOperationException idoe) {
				idoe.printStackTrace();
			}
		};

		new DropTarget(toolBar, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
	}

	private void initializeComponents()
	{
		// Create ToolBar
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {10, 1};
		layout.columnWeights = new double[] {0.0, 0.1};
		layout.rowHeights = new int[] {1};
		layout.rowWeights = new double[] {0.1};
		setLayout(layout);

		toolBar = new JToolBar(SwingConstants.VERTICAL);
		toolBar.setFloatable(false);
		toolBar.setBorderPainted(false);

		// Create ToolBox
		toolBoxCardLayout = new CardLayout();

		toolBoxPanel = new JPanel(toolBoxCardLayout);
		toolBoxPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		toolBoxPanel.setVisible(false);
		toolBoxPanel.setPreferredSize(new Dimension(200, 300));

		// Create buttons
		btnGroup = new ButtonGroup();
		btnGroup.add(defaultButton);

		buttons = new ArrayList<>();

		add(toolBar, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							 new Insets(0, 0, 0, 0), 0, 0));
		add(toolBoxPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
								  GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}

	@Override
	public String getContainerID()
	{
		return containerID;
	}

	@Override
	public void setParent(ViewSplitContainer parent)
	{
		// Not implemented for view bar
	}

	@Override
	public void addView(IView view)
	{
		addView(view, buttons.size());
	}

	public void addView(final IView view, int targetIndex)
	{
		ViewToggleButton btn;
		if (view.getDescriptor().getIcon() != null) {
			btn = new ViewToggleButton(view);
		} else {
			btn = new VerticalButton(view, view.getName(), false);
		}

		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isMiddleMouseButton(e) && perspectiveManager.closeView(view)) {
					removeView(view);
				}
			}
		});
		btn.setHideActionText(true);
		btn.setToolTipText(view.getName());
		new DragSource().createDefaultDragGestureRecognizer(btn, DnDConstants.ACTION_COPY_OR_MOVE, dgl);

		// Add ToolBox ToggleButton to the ButtonMap, the ToolBar and the
		// ButtonGroup
		if (targetIndex < 0 || targetIndex > buttons.size()) {
			targetIndex = buttons.size();
		}
		buttons.add(targetIndex, btn);
		toolBar.add(btn, targetIndex);
		btnGroup.add(btn);
		btnGroup.setSelected(btn.getModel(), true);

		// Add ToolBox-Panel
		toolBoxPanel.add(view.getContentComponent(), view.getName());
		toggleView(view);
	}

	@Override
	public boolean removeView(IView view)
	{
		ViewToggleButton btn;
		for (int i = 0; i < buttons.size(); i++) {
			btn = buttons.get(i);
			if (btn.getView().getName().equals(view.getName())) {
				// Remove ToolBox ToggleButton from the ButtonMap, the ToolBar and
				// the ButtonGroup
				buttons.remove(i);
				toolBar.remove(btn);
				btnGroup.remove(btn);
				validate();

				// Remove ToolBox-Panel
				toolBoxPanel.remove(view.getContentComponent());

				if (btn.getView() == activeView) {
					toggleView(activeView);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public List<IView> getOpenViews()
	{
		List<IView> openViews = new ArrayList<>();

		for (ViewToggleButton button : buttons) {
			openViews.add(button.getView());
		}

		return openViews;
	}

	private void toggleView(IView view)
	{
		if (activeView == view) {
			toolBoxPanel.setVisible(false);
			activeView = null;
			btnGroup.setSelected(defaultButton.getModel(), true);
		} else {
			toolBoxCardLayout.show(toolBoxPanel, view.getName());
			toolBoxPanel.setVisible(true);
			activeView = view;
		}
	}

	public void collapse()
	{
		toggleView(activeView);
	}

	public int getActiveView()
	{
		return getOpenViews().indexOf(activeView);
	}

	public void setActiveView(int index)
	{
		List<IView> openViews = getOpenViews();
		if (openViews.isEmpty()) {
			return;
		}
		IView view = openViews.get(index);
		if (activeView != view) {
			toggleView(view);
		}
	}

	private int indexAtLocation(Point location)
	{
		Component comp = toolBar.getComponentAt(location);

		if (comp != null && comp instanceof ViewToggleButton) {
			IView view = ((ViewToggleButton) comp).getView();

			for (int i = 0; i < buttons.size(); i++) {
				if (buttons.get(i).getView() == view) {
					return i;
				}
			}
		}

		return -1;
	}

	private int determineTargetIndex(Point location)
	{
		int index = indexAtLocation(location);

		if (index < 0) {
			return buttons.size();
		} else {
			return index;
		}
	}

	public void setPaintGhost(boolean flag)
	{
		hasGhost = flag;
	}

	public boolean hasGhost()
	{
		return hasGhost;
	}

	private class ViewToggleButton extends JToggleButton
	{
		private final IView view;

		public ViewToggleButton(IView view)
		{
			super(new ToggleViewAction(view));
			this.view = view;
		}

		public IView getView()
		{
			return view;
		}
	}

	private class ToggleViewAction extends AbstractAction
	{
		private final IView view;

		public ToggleViewAction(IView view)
		{
			super(view.getName(), view.getDescriptor().getIcon());
			this.view = view;
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			toggleView(view);
		}
	}

	private class VerticalButton extends ViewToggleButton
	{
		public VerticalButton(IView view, String caption, boolean clockwise)
		{
			super(view);

			Font f = getFont();
			FontMetrics fm = getFontMetrics(f);
			int captionHeight = fm.getHeight();
			int captionWidth = fm.stringWidth(caption);
			BufferedImage bi = new BufferedImage(captionHeight + 4, captionWidth, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) bi.getGraphics();

			g.setColor(new Color(0, 0, 0, 0)); // transparent
			g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

			g.setColor(getForeground());
			g.setFont(f);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			if (clockwise) {
				g.rotate(Math.PI / 2);
			} else {
				g.rotate(-Math.PI / 2);
				g.translate(-bi.getHeight(), bi.getWidth());
			}
			g.drawString(caption, 5, -5);

			Icon icon = new ImageIcon(bi);
			setIcon(icon);

			setMargin(new Insets(2, 2, 2, 2));
			setActionCommand(caption);
		}
	}

	class CDropTargetListener implements DropTargetListener
	{
		int currentTargetIndex = -1;

		@Override
		public void dragEnter(DropTargetDragEvent e)
		{
			if (isDragDropAcceptable(e.getTransferable(), e.getCurrentDataFlavors())) {
				currentTargetIndex = -1;
				e.acceptDrag(e.getDropAction());
			} else {
				e.rejectDrag();
			}
		}

		@Override
		public void dragExit(DropTargetEvent e)
		{
			currentTargetIndex = -1;
			ghostGlassPane.setVisible(false);
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent e)
		{
		}

		@Override
		public void dragOver(final DropTargetDragEvent e)
		{
			int newTargetIndex = determineTargetIndex(e.getLocation());
			if (newTargetIndex != currentTargetIndex) {
				currentTargetIndex = newTargetIndex;
				initGlassPane(currentTargetIndex);
				ghostGlassPane.repaint();
			}
		}

		@Override
		public void drop(DropTargetDropEvent e)
		{
			Transferable transferable = e.getTransferable();
			if (isDragDropAcceptable(transferable, e.getCurrentDataFlavors())) {
				convertView(getTabTransferData(transferable), determineTargetIndex(e.getLocation()));
				e.dropComplete(true);
			} else {
				e.dropComplete(false);
			}

			currentTargetIndex = -1;
			ghostGlassPane.setVisible(false);
			repaint();
		}

		public boolean isDragDropAcceptable(Transferable transferable, DataFlavor[] flavor)
		{
			return !(transferable == null || getTabTransferData(transferable) == null) &&
					transferable.isDataFlavorSupported(flavor[0]);
		}
	}

	private ViewTransferData getTabTransferData(Transferable transferable)
	{
		try {
			return (ViewTransferData) transferable.getTransferData(ViewTransferable.DATA_FLAVOR);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void convertView(ViewTransferData data, int targetIndex)
	{
		IViewContainer source = data.getSourceContainer();
		IView view = data.getView();
		int sourceIndex = data.getIndex();

		if (source == null || view == null) {
			return;
		}

		if (this != source) {
			source.removeView(view);
			addView(view, targetIndex);
		} else if (targetIndex >= 0 && sourceIndex != targetIndex) {
			// It's a drag and drop within this container
			source.removeView(view);
			addView(view, targetIndex);
		}
	}

	private void initGlassPane(int targetIndex)
	{
		if (hasGhost()) {
			getRootPane().setGlassPane(ghostGlassPane);

			// If the target index is invalid, set it to the end of the tab list
			if (targetIndex < 0 || targetIndex > buttons.size()) {
				targetIndex = buttons.size();
			}

			Rectangle rect;
			if (targetIndex == buttons.size()) {
				if (buttons.size() > 0) {
					// This container contains buttons and we wanna add one to the
					// end
					rect = new Rectangle(buttons.get(targetIndex - 1).getBounds());

					// Since we wanna add a button in the end, we have to calculate
					// the resulting location based on the currently last button
					rect.y += rect.getHeight();
				} else {
					// This container doesn't contain any buttons, so the location is
					// default
					rect = new Rectangle(1, 1, 16, 16);
				}
			} else {
				// This container contains buttons and we wanna insert somewhere on
				// an existing button
				rect = buttons.get(targetIndex).getBounds();
			}

			ghostGlassPane.setWidth(rect.width);
			ghostGlassPane.setHeight(rect.height);
			ghostGlassPane.setGhostLocation(SwingUtilities.convertPoint(toolBar, rect.getLocation(), ghostGlassPane));
		}

		ghostGlassPane.setVisible(true);
	}
}
