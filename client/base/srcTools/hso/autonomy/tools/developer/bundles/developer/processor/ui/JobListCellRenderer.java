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
package hso.autonomy.tools.developer.bundles.developer.processor.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import hso.autonomy.tools.developer.bundles.developer.processor.model.impl.Job;

public class JobListCellRenderer extends JPanel implements ListCellRenderer<Job>
{
	private JLabel titleLabel;

	private JLabel descriptionLabel;

	private JLabel noLabel;

	private Color borderColor;

	public JobListCellRenderer()
	{
		createComponents();
	}

	private void createComponents()
	{
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {1, 1, 1};
		layout.columnWeights = new double[] {0.0, 0.1, 0.0};
		layout.rowHeights = new int[] {1, 1};
		layout.rowWeights = new double[] {0.0, 0.0};
		setLayout(layout);

		titleLabel = new JLabel();

		descriptionLabel = new JLabel();
		descriptionLabel.setFont(
				new Font(descriptionLabel.getFont().getFontName(), descriptionLabel.getFont().getStyle(), 10));

		noLabel = new JLabel();
		noLabel.setFont(new Font(noLabel.getFont().getFontName(), Font.BOLD, 16));

		add(titleLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
								new Insets(3, 3, 1, 3), 0, 0));

		add(noLabel, new GridBagConstraints(2, 0, 1, 2, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
							 new Insets(6, 6, 6, 6), 0, 0));

		add(descriptionLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
									  GridBagConstraints.BOTH, new Insets(1, 3, 3, 3), 0, 0));
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends Job> list, Job job, int index, boolean isSelected, boolean cellHasFocus)
	{
		// Set AgentRunner Information
		titleLabel.setText(job.getJobName());
		descriptionLabel.setText(job.getJobDescription());
		noLabel.setText("" + job.getJobID());

		switch (job.getJobState()) {
		case RUNNING:
			borderColor = Color.GREEN.brighter();
			break;
		case FINISHED:
			borderColor = Color.GREEN;
			break;
		case WAITING:
			borderColor = Color.LIGHT_GRAY;
			break;
		case ABORTED:
			borderColor = Color.RED;
			break;
		case INIT:
		default:
			borderColor = Color.LIGHT_GRAY;
			break;
		}

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.WHITE),
				BorderFactory.createLineBorder(borderColor, 1)));

		// Set Background dependend on selection
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			titleLabel.setForeground(list.getSelectionForeground());
			descriptionLabel.setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			titleLabel.setForeground(list.getForeground());
			descriptionLabel.setForeground(list.getForeground());
		}

		return this;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(borderColor);
		g2d.fillPolygon(new int[] {getWidth() - (getHeight() + 10), getWidth(), getWidth(), getWidth() - 20},
				new int[] {0, 0, getHeight(), getHeight()}, 4);
	}
}
