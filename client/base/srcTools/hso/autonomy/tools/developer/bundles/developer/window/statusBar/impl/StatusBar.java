package hso.autonomy.tools.developer.bundles.developer.window.statusBar.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import hso.autonomy.tools.developer.bundles.developer.window.statusBar.IStatusBar;
import hso.autonomy.tools.developer.bundles.developer.window.statusBar.IStatusObserver;
import hso.autonomy.tools.developer.bundles.developer.window.statusBar.IStatusObserverListener;

public class StatusBar extends JPanel implements IStatusBar
{
	private JLabel statusLabel;

	private List<IStatusObserver> statusObserver;

	public StatusBar(List<IStatusObserver> statusObserver)
	{
		this.statusObserver = statusObserver;

		initializeComponents();
	}

	private void initializeComponents()
	{
		setLayout(new GridBagLayout());

		statusLabel = new JLabel();

		JToolBar toolBar = new JToolBar();
		toolBar.setBorder(null);
		toolBar.setBorderPainted(false);
		toolBar.setFloatable(false);
		for (IStatusObserver observer : statusObserver) {
			toolBar.add(new StatusObserverAction(observer));
		}

		add(new JSeparator(JSeparator.HORIZONTAL),
				new GridBagConstraints(0, 0, 2, 1, 0.1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(0, 1, 1, 1), 0, 0));
		add(statusLabel, new GridBagConstraints(0, 1, 1, 1, 0.1, 0, GridBagConstraints.WEST,
								 GridBagConstraints.HORIZONTAL, new Insets(3, 8, 3, 5), 0, 0));
		add(toolBar, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							 new Insets(0, 0, 0, 0), 0, 0));
	}

	private void showDetailedStatusPanel(IStatusObserver observer)
	{
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		JPopupMenu popupMenu = new JPopupMenu();

		Rectangle rect = new Rectangle(getBounds());

		SwingUtilities.convertPointToScreen(rect.getLocation(), this);

		popupMenu.setPreferredSize(new Dimension(rect.width, 200));
		popupMenu.setLayout(new BorderLayout());
		popupMenu.add(observer.getDetailedStatusPanel(), BorderLayout.CENTER);

		popupMenu.show(this, 0, -200);
		JPopupMenu.setDefaultLightWeightPopupEnabled(true);
	}

	@Override
	public void publishStatusMessage(String statusMsg)
	{
		statusLabel.setText(statusMsg);
	}

	class StatusObserverAction extends AbstractAction implements IStatusObserverListener
	{
		IStatusObserver statusObserver;

		public StatusObserverAction(IStatusObserver observer)
		{
			super(observer.getName(), observer.getIcon());
			setToolTipText(observer.getName());

			this.statusObserver = observer;
			observer.addObserverListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			showDetailedStatusPanel(statusObserver);
		}

		@Override
		public void observerStateChanged(IStatusObserver observer)
		{
			putValue(Action.SMALL_ICON, statusObserver.getIcon());
			showDetailedStatusPanel(statusObserver);
		}

		@Override
		public void publishStatusMessage(String statusMsg)
		{
			statusLabel.setText(statusMsg);
		}
	}
}
