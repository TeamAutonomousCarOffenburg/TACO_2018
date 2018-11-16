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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import hso.autonomy.tools.developer.bundles.developer.processor.model.IProcessorListener;
import hso.autonomy.tools.developer.bundles.developer.processor.model.impl.Job;
import hso.autonomy.tools.developer.bundles.developer.processor.model.IProcessor;
import hso.autonomy.tools.developer.bundles.developer.processor.model.JobState;
import hso.autonomy.tools.developer.util.swing.TitleLabel;
import hso.autonomy.tools.util.imageBuffer.ImageFile;

public class ProcessorPanel extends JPanel implements IProcessorListener
{
	private final IProcessor processor;

	private DefaultListModel<Job> jobListModel;

	private JList<Job> jobList;

	private Action removeJobAction;

	private Action restartJobAction;

	private Job selectedJob;

	private JPopupMenu popupMenu;

	public ProcessorPanel(IProcessor processor)
	{
		this.processor = processor;

		initializeComponents();
		refreshActions();

		this.processor.addProcessorListener(this);
	}

	private void initializeComponents()
	{
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {1};
		layout.columnWeights = new double[] {0.1};
		layout.rowHeights = new int[] {1, 1, 1};
		layout.rowWeights = new double[] {0.0, 0.0, 0.1};
		setLayout(layout);

		JLabel titleLabel = new TitleLabel("Background Job Queue", ImageFile.PROCESSOR.getIcon());

		// Create Job ListModel
		jobListModel = new DefaultListModel<>();
		for (Job job : processor.getJobQueue()) {
			jobListModel.addElement(job);
		}

		// Create Job JList
		jobList = new JList<>(jobListModel);
		jobList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jobList.setLayoutOrientation(JList.VERTICAL);
		jobList.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		jobList.setVisibleRowCount(-1);
		jobList.setCellRenderer(new JobListCellRenderer());
		jobList.addListSelectionListener(e -> setSelectedJob(jobList.getSelectedValue()));
		jobList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e)
			{
				checkPopupTrigger(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				checkPopupTrigger(e);
			}

			private void checkPopupTrigger(MouseEvent e)
			{
				if (e.isPopupTrigger()) {
					int row = jobList.locationToIndex(e.getPoint());
					if (row != -1 && row != jobList.getSelectedIndex()) {
						jobList.setSelectedIndex(row);
					}
					showPopupMenu(e);
				}
			}
		});

		add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
								GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(jobList, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							 new Insets(2, 2, 2, 2), 0, 0));

		// Create default menus
		popupMenu = new JPopupMenu();

		removeJobAction = new RemoveJobAction();
		restartJobAction = new RestartJobAction();

		popupMenu.add(removeJobAction);
		popupMenu.add(restartJobAction);

		JPopupMenu.setDefaultLightWeightPopupEnabled(true);
	}

	private void setSelectedJob(Job job)
	{
		if (selectedJob != job) {
			selectedJob = job;
			refreshActions();
		}
	}

	private void refreshActions()
	{
		if (selectedJob == null) {
			removeJobAction.setEnabled(false);
			restartJobAction.setEnabled(false);
		} else {
			switch (selectedJob.getJobState()) {
			case RUNNING:
				removeJobAction.setEnabled(false);
				restartJobAction.setEnabled(false);
				break;
			case ABORTED:
				removeJobAction.setEnabled(true);
				restartJobAction.setEnabled(true);
				break;
			default:
				removeJobAction.setEnabled(true);
				restartJobAction.setEnabled(false);
				break;
			}
		}
	}

	private void showPopupMenu(MouseEvent e)
	{
		// Show popup menu
		popupMenu.show(ProcessorPanel.this, e.getX(), e.getY());
	}

	@Override
	public void jobAdded(Job newJob)
	{
		jobListModel.addElement(newJob);
	}

	@Override
	public void jobRemoved(Job job)
	{
		int index = jobListModel.indexOf(job);

		jobListModel.removeElement(job);
		if (job == selectedJob) {
			if (jobListModel.isEmpty()) {
				setSelectedJob(null);
			} else {
				if (index == jobListModel.size()) {
					jobList.setSelectedIndex(jobListModel.size() - 1);
				} else {
					jobList.setSelectedIndex(0);
				}
			}
		}
	}

	@Override
	public void processorStateChanged()
	{
		jobList.repaint();
		refreshActions();
	}

	@Override
	public void nextJobStarted(Job job)
	{
		jobList.repaint();
		refreshActions();
	}

	private class RemoveJobAction extends AbstractAction
	{
		public RemoveJobAction()
		{
			super("Remove Job", ImageFile.REMOVE.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (selectedJob != null && selectedJob.getJobState() != JobState.RUNNING) {
				processor.removeJob(selectedJob);
			}
		}
	}

	private class RestartJobAction extends AbstractAction
	{
		public RestartJobAction()
		{
			super("Restart Job", ImageFile.EXECUTE.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (selectedJob != null && selectedJob.getJobState() == JobState.ABORTED) {
				Job jobToRestart = selectedJob;
				processor.removeJob(jobToRestart);
				processor.addJob(jobToRestart);
			}
		}
	}
}
