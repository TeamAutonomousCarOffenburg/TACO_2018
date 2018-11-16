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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;
import hso.autonomy.tools.developer.bundles.developer.perspective.IViewContainer;
import hso.autonomy.tools.developer.bundles.developer.perspective.dnd.ViewTransferData;
import hso.autonomy.tools.developer.bundles.developer.perspective.dnd.ViewTransferable;

public class ViewTabContainer extends JTabbedPane implements IViewContainer
{
	private static final GhostGlassPane ghostGlassPane = new GhostGlassPane();

	private boolean hasGhost = true;

	private IPerspectiveManager perspectiveManager;

	private ViewSplitContainer parent;

	private final String containerID;

	private enum DropAction { NONE, INSERT, SPLIT_NORTH, SPLIT_EAST, SPLIT_SOUTH, SPLIT_WEST }

	public ViewTabContainer(IPerspectiveManager perspectiveManager, String containerID, ViewSplitContainer parent)
	{
		super();

		this.containerID = containerID;
		this.parent = parent;
		this.perspectiveManager = perspectiveManager;

		final DragSourceListener dsl = new DragSourceListener() {
			@Override
			public void dragEnter(DragSourceDragEvent e)
			{
				// Drag entered panel,
				// drop-target active
				// drop-target accepts drop
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			@Override
			public void dragExit(DragSourceEvent e)
			{
				// Drag exited panel
				// OR: drop-target got inactive
				// OR: drop-target rejected drop
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}

			@Override
			public void dragOver(DragSourceDragEvent e)
			{
				// Drag moved within panel,
				// drop-target still active
				// drop-target still accepts drop
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

		final DragGestureListener dgl = e ->
		{
			Point tabPt = e.getDragOrigin();
			int dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
			if (dragTabIndex < 0) {
				return;
			}

			try {
				IView view = ((TabHeaderPanel) getTabComponentAt(dragTabIndex)).getView();
				e.startDrag(DragSource.DefaultMoveDrop, new ViewTransferable(ViewTabContainer.this, view, dragTabIndex),
						dsl);
			} catch (InvalidDnDOperationException idoe) {
				idoe.printStackTrace();
			}
		};

		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);

		addChangeListener(e -> selectActiveView());

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e)
			{
				selectActiveView();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isMiddleMouseButton(e)) {
					ViewTabContainer tabContainer = ViewTabContainer.this;
					try {
						Component component = tabContainer.getTabComponentAt(getSelectedIndex());
						((TabHeaderPanel) component).closePanel();
					} catch (Exception ex) {
					}
				}
			}
		});
	}

	@Override
	public void setParent(ViewSplitContainer parent)
	{
		this.parent = parent;
	}

	@Override
	public String getContainerID()
	{
		return containerID;
	}

	@Override
	public void addView(IView view)
	{
		JComponent content = view.getContentComponent();

		addTab(view.getName(), content);
		int index = indexOfComponent(content);
		setTabComponentAt(index, new TabHeaderPanel(perspectiveManager, view));
		setSelectedIndex(index);
	}

	private void selectActiveView()
	{
		IView view = null;

		try {
			view = ((TabHeaderPanel) getTabComponentAt(getSelectedIndex())).getView();
		} catch (Exception ex) {
		}

		if (view != null) {
			ViewTabContainer.this.perspectiveManager.setActiveView(view);
		}
	}

	@Override
	public boolean removeView(IView view)
	{
		boolean containsView = false;

		for (int i = 0; i < getTabCount(); i++) {
			if (((TabHeaderPanel) getTabComponentAt(i)).getView().getName().equals(view.getName())) {
				containsView = true;
				remove(view.getContentComponent());
				break;
			}
		}

		if (getTabCount() < 1) {
			parent.removeViewContainer(this);
		}

		return containsView;
	}

	@Override
	public List<IView> getOpenViews()
	{
		List<IView> openViews = new ArrayList<>();

		for (int i = 0; i < getTabCount(); i++) {
			openViews.add(((TabHeaderPanel) getTabComponentAt(i)).getView());
		}

		return openViews;
	}

	private ViewTransferData getTabTransferData(DropTargetDropEvent event)
	{
		try {
			return (ViewTransferData) event.getTransferable().getTransferData(ViewTransferable.DATA_FLAVOR);
		} catch (Exception e) {
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private List<File> getFileTransferData(DropTargetDropEvent event)
	{
		try {
			return (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		} catch (Exception e) {
		}

		return null;
	}

	private ViewTransferData getTabTransferData(DropTargetDragEvent event)
	{
		try {
			return (ViewTransferData) event.getTransferable().getTransferData(ViewTransferable.DATA_FLAVOR);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private List<File> getFileTransferData(DropTargetDragEvent event)
	{
		try {
			return (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		} catch (Exception e) {
		}

		return null;
	}

	class CDropTargetListener implements DropTargetListener
	{
		private DropAction currentDropAction = DropAction.NONE;

		private int currentInsertIndex = -1;

		@Override
		public void dragEnter(DropTargetDragEvent e)
		{
			// Drag entered panel
			if (isDragAcceptable(e)) {
				dragOver(e);
				e.acceptDrag(e.getDropAction());
			} else {
				e.rejectDrag();
			}
		}

		@Override
		public void dragExit(DropTargetEvent e)
		{
			// Drag exited panel
			currentInsertIndex = -1;
			currentDropAction = DropAction.NONE;
			ghostGlassPane.setVisible(false);
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent e)
		{
		}

		@Override
		public void dragOver(final DropTargetDragEvent e)
		{
			if (!hasGhost()) {
				return;
			}

			// Drag active and moved within the panel
			DropAction newAction;
			List<File> files = getFileTransferData(e);
			if (files != null) {
				// files can only be inserted
				newAction = DropAction.INSERT;
			} else {
				newAction = determineDropAction(e.getLocation());
			}

			if (currentDropAction != newAction) {
				currentDropAction = newAction;

				// Drop-Action changed to insert, so calculate new insert point
				if (currentDropAction == DropAction.INSERT) {
					currentInsertIndex = determineTargetTabIndex(e.getLocation());
				}

				initGlassPane(currentInsertIndex, currentDropAction);
				ghostGlassPane.repaint();
			} else if (currentDropAction == DropAction.INSERT) {
				int newTargetIndex = determineTargetTabIndex(e.getLocation());
				if (newTargetIndex != currentInsertIndex) {
					currentInsertIndex = newTargetIndex;

					initGlassPane(currentInsertIndex, currentDropAction);
					ghostGlassPane.repaint();
				}
			}
		}

		@Override
		public void drop(DropTargetDropEvent e)
		{
			if (isDropAcceptable(e)) {
				List<File> files = getFileTransferData(e);
				ViewTransferData viewData = getTabTransferData(e);

				if (files != null) {
					fileDropAction(files);
				} else if (viewData != null) {
					// Check if we drag a view within a TabContainer with just one
					// tab -
					// in this case, no drop is supported
					if (viewData.getSourceContainer() == ViewTabContainer.this &&
							ViewTabContainer.this.getTabCount() == 1) {
						e.dropComplete(false);
						return;
					}
					viewDataDropAction(e, viewData);
				}

				e.dropComplete(true);
			} else {
				e.dropComplete(false);
			}

			currentDropAction = DropAction.NONE;
			currentInsertIndex = -1;
			ghostGlassPane.setVisible(false);
			repaint();
		}

		private void fileDropAction(List<File> files)
		{
			for (File file : files) {
				perspectiveManager.openView(file, ViewTabContainer.this);
			}
		}

		private void viewDataDropAction(DropTargetDropEvent e, ViewTransferData transferData)
		{
			switch (currentDropAction) {
			case SPLIT_NORTH:
				transferAndSplit(transferData, ViewSplitContainer.Placement.NORTH);
				break;
			case SPLIT_EAST:
				transferAndSplit(transferData, ViewSplitContainer.Placement.EAST);
				break;
			case SPLIT_SOUTH:
				transferAndSplit(transferData, ViewSplitContainer.Placement.SOUTH);
				break;
			case SPLIT_WEST:
				transferAndSplit(transferData, ViewSplitContainer.Placement.WEST);
				break;
			case INSERT:
			default:
				transferTab(transferData, determineTargetTabIndex(e.getLocation()));
				break;
			}
		}

		public boolean isDragAcceptable(DropTargetDragEvent e)
		{
			Transferable t = e.getTransferable();
			if (t == null) {
				return false;
			}

			DataFlavor[] flavor = e.getCurrentDataFlavors();
			if (!t.isDataFlavorSupported(flavor[0])) {
				return false;
			}

			List<File> files = getFileTransferData(e);
			if (files != null) {
				return true;
			}

			ViewTransferData data = getTabTransferData(e);
			if (data == null) {
				return false;
			}

			if (ViewTabContainer.this == data.getSourceContainer() && data.getIndex() >= 0) {
				return true;
			}

			if (ViewTabContainer.this != data.getSourceContainer()) {
				return true;
			}

			return false;
		}

		public boolean isDropAcceptable(DropTargetDropEvent e)
		{
			Transferable t = e.getTransferable();
			if (t == null) {
				return false;
			}

			DataFlavor[] flavor = e.getCurrentDataFlavors();
			if (!t.isDataFlavorSupported(flavor[0])) {
				return false;
			}

			e.acceptDrop(DnDConstants.ACTION_COPY);

			List<File> files = getFileTransferData(e);
			if (files != null) {
				return true;
			}

			ViewTransferData data = getTabTransferData(e);
			if (data == null) {
				return false;
			}

			if (ViewTabContainer.this == data.getSourceContainer() && data.getIndex() >= 0) {
				return true;
			}

			if (ViewTabContainer.this != data.getSourceContainer()) {
				return true;
			}

			return false;
		}

		private DropAction determineDropAction(Point dropPoint)
		{
			int dropX = (int) dropPoint.getX();
			int dropY = (int) dropPoint.getY();

			int panelWidth = getWidth();
			int panelHeight = getHeight();

			int thresholdX = panelWidth / 3;
			int thresholdY = panelHeight / 3;

			// Check for parent Split
			switch (getTabPlacement()) {
			case RIGHT:
				if (dropX > panelWidth - 5) {
					return DropAction.SPLIT_EAST;
				} else if (dropX > panelWidth - thresholdX) {
					return DropAction.INSERT;
				} else if (dropX < thresholdX) {
					return DropAction.SPLIT_WEST;
				} else if (dropY < thresholdY) {
					return DropAction.SPLIT_NORTH;
				} else if (dropY > panelHeight - thresholdY) {
					return DropAction.SPLIT_SOUTH;
				}
				break;
			case LEFT:
				if (dropX < 5) {
					return DropAction.SPLIT_WEST;
				} else if (dropX < thresholdX) {
					return DropAction.INSERT;
				} else if (dropX > panelWidth - thresholdX) {
					return DropAction.SPLIT_EAST;
				} else if (dropY < thresholdY) {
					return DropAction.SPLIT_NORTH;
				} else if (dropY > panelHeight - thresholdY) {
					return DropAction.SPLIT_SOUTH;
				}
				break;
			case BOTTOM:
				if (dropY > panelHeight - 5) {
					return DropAction.SPLIT_SOUTH;
				} else if (dropY > panelHeight - thresholdY) {
					return DropAction.INSERT;
				} else if (dropY < thresholdY) {
					return DropAction.SPLIT_NORTH;
				} else if (dropX < thresholdX) {
					return DropAction.SPLIT_WEST;
				} else if (dropX > panelWidth - thresholdX) {
					return DropAction.SPLIT_EAST;
				}
				break;
			case TOP:
			default:
				if (dropY < 5) {
					return DropAction.SPLIT_NORTH;
				} else if (dropY < thresholdY) {
					return DropAction.INSERT;
				} else if (dropY > panelHeight - thresholdY) {
					return DropAction.SPLIT_SOUTH;
				} else if (dropX < thresholdX) {
					return DropAction.SPLIT_WEST;
				} else if (dropX > panelWidth - thresholdX) {
					return DropAction.SPLIT_EAST;
				}
				break;
			}

			return DropAction.INSERT;
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

	/**
	 * returns potential index for drop.
	 * @param location point given in the drop site component's coordinate
	 * @return returns potential index for drop.
	 */
	private int determineTargetTabIndex(Point location)
	{
		int targetIndex = indexAtLocation(location.x, location.y);

		if (targetIndex < 0) {
			return getTabCount();
		}

		return targetIndex;
	}

	private void transferTab(ViewTransferData data, int targetIndex)
	{
		IViewContainer source = data.getSourceContainer();
		IView view = data.getView();
		int sourceIndex = data.getIndex();

		if (sourceIndex < 0) {
			return;
		}

		Component cmp = view.getContentComponent();
		Component tabHead;
		String str = view.getName();

		if (this != source) {
			tabHead = new TabHeaderPanel(perspectiveManager, view);
			source.removeView(view);

			if (targetIndex == getTabCount()) {
				addTab(str, cmp);
				setTabComponentAt(getTabCount() - 1, tabHead);
			} else {
				if (targetIndex < 0) {
					targetIndex = 0;
				}

				insertTab(str, null, cmp, null, targetIndex);
				setTabComponentAt(targetIndex, tabHead);
			}

			setSelectedComponent(cmp);
		} else if (targetIndex >= 0 && sourceIndex != targetIndex) {
			// It's a drag and drop within this container
			tabHead = this.getTabComponentAt(sourceIndex);

			if (targetIndex == getTabCount()) {
				source.removeView(view);
				addTab(str, cmp);
				setTabComponentAt(getTabCount() - 1, tabHead);
				setSelectedIndex(getTabCount() - 1);
			} else {
				source.removeView(view);
				insertTab(str, null, cmp, null, targetIndex);
				setTabComponentAt(targetIndex, tabHead);
				setSelectedIndex(targetIndex);
			}
		}
	}

	private void transferAndSplit(ViewTransferData data, ViewSplitContainer.Placement placement)
	{
		IViewContainer source = data.getSourceContainer();
		IView view = data.getView();

		if (source == null) {
			return;
		}

		source.removeView(view);
		parent.splitContainer(this, view, placement);
	}

	private void initGlassPane(int targetIndex, DropAction currentDropAction)
	{
		int panelWith = getWidth();
		int panelHeight = getHeight();

		if (hasGhost()) {
			getRootPane().setGlassPane(ghostGlassPane);

			switch (currentDropAction) {
			case SPLIT_NORTH:
				ghostGlassPane.setWidth(panelWith - 2);
				ghostGlassPane.setHeight(panelHeight / 2);
				ghostGlassPane.setGhostLocation(
						SwingUtilities.convertPoint(ViewTabContainer.this, new Point(1, 1), ghostGlassPane));
				break;
			case SPLIT_EAST:
				ghostGlassPane.setWidth(panelWith / 2);
				ghostGlassPane.setHeight(panelHeight - 2);
				ghostGlassPane.setGhostLocation(SwingUtilities.convertPoint(
						ViewTabContainer.this, new Point(panelWith / 2, 1), ghostGlassPane));
				break;
			case SPLIT_SOUTH:
				ghostGlassPane.setWidth(panelWith - 2);
				ghostGlassPane.setHeight(panelHeight / 2);
				ghostGlassPane.setGhostLocation(SwingUtilities.convertPoint(
						ViewTabContainer.this, new Point(1, panelHeight / 2), ghostGlassPane));
				break;
			case SPLIT_WEST:
				ghostGlassPane.setWidth(panelWith / 2);
				ghostGlassPane.setHeight(panelHeight - 2);
				ghostGlassPane.setGhostLocation(
						SwingUtilities.convertPoint(ViewTabContainer.this, new Point(1, 1), ghostGlassPane));
				break;

			case INSERT:
			default:
				// If the target index is invalid, set it to the end of the tab list
				if (targetIndex < 0) {
					targetIndex = getTabCount();
				}

				Rectangle rect;
				if (targetIndex == getTabCount()) {
					if (getTabCount() > 0) {
						// This container contains tabs and we wanna add one to the
						// end
						rect = new Rectangle(getBoundsAt(targetIndex - 1));

						// Since we wanna add a tab in the end, we have to calculate
						// the resulting location based on the currently last tab
						if (tabPlacement == LEFT || tabPlacement == RIGHT) {
							rect.y += rect.getHeight();
						} else {
							rect.x += rect.getWidth();
						}
					} else {
						// This container doesn't contain any tabs, so the location is
						// default
						rect = new Rectangle(1, 1, panelWith - 2, panelHeight - 2);
					}
				} else {
					// This container contains tabs and we wanna insert somewhere on
					// an existing tab
					rect = getBoundsAt(targetIndex);
				}

				ghostGlassPane.setWidth(rect.width);
				ghostGlassPane.setHeight(rect.height);
				ghostGlassPane.setGhostLocation(SwingUtilities.convertPoint(this, rect.getLocation(), ghostGlassPane));
				break;
			}
		}

		ghostGlassPane.setVisible(true);
	}
}
