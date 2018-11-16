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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

import hso.autonomy.tools.developer.bundles.developer.perspective.IPerspectiveManager;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;

public class TabHeaderPanel extends JPanel implements PropertyChangeListener
{
	private final IView view;

	private final IPerspectiveManager perspectiveManager;

	private JLabel titleLabel;

	public TabHeaderPanel(IPerspectiveManager perspectiveManager, IView view)
	{
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));

		this.perspectiveManager = perspectiveManager;
		this.view = view;

		view.addPropertyChangeListener(this);

		initializeComponents();
	}

	private void initializeComponents()
	{
		setOpaque(false);

		titleLabel = new JLabel(view.getName(), view.getDescriptor().getIcon(), SwingConstants.LEFT);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 2));

		TabButton closeBtn = new TabButton();
		closeBtn.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));

		add(titleLabel);
		add(closeBtn);
	}

	public IView getView()
	{
		return view;
	}

	public void closePanel()
	{
		perspectiveManager.closeView(view);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName() == IView.PROPERTY_NAME) {
			String newName = (String) evt.getNewValue();
			titleLabel.setText(newName);
		}
	}

	private class TabButton extends JButton implements ActionListener
	{
		public TabButton()
		{
			int size = 17;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("Close");
			// Make the button look the same for all Laf's
			setUI(new BasicButtonUI());
			// Make it transparent
			setContentAreaFilled(false);
			// No need to be focusable
			setFocusable(false);
			setBorderPainted(false);
			// Making nice rollover effect
			// we use the same listener for all buttons
			setRolloverEnabled(true);
			// Close the proper tab by clicking the button
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			closePanel();
		}

		// we don't want to update UI for this button
		@Override
		public void updateUI()
		{
		}

		// paint the cross
		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.translate(1, 2);
			g2.setStroke(new BasicStroke(1));

			g2.setColor(Color.BLACK);
			if (getModel().isRollover()) {
				g2.setColor(Color.RED);
			}
			int delta = 5;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}
	}
}
