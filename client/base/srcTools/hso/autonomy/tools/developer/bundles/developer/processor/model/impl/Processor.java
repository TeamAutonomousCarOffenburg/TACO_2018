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
package hso.autonomy.tools.developer.bundles.developer.processor.model.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import hso.autonomy.tools.developer.bundles.developer.processor.model.IProcessorListener;
import hso.autonomy.tools.developer.bundles.developer.processor.model.IProcessor;
import hso.autonomy.tools.developer.bundles.developer.processor.model.JobState;

public class Processor implements IProcessor
{
	private final List<IProcessorListener> listeners;

	private final LinkedList<Job> jobQueue;

	private JobWorker worker;

	private boolean exiting = false;

	public Processor()
	{
		listeners = new ArrayList<>();
		jobQueue = new LinkedList<>();
	}

	@Override
	public boolean isActive()
	{
		if (worker == null) {
			return false;
		}

		return worker.isAlive() && !exiting;
	}

	@Override
	public List<Job> getJobQueue()
	{
		return jobQueue;
	}

	@Override
	public synchronized void addJob(Job newJob)
	{
		if (newJob == null) {
			return;
		}

		newJob.setState(JobState.WAITING);
		jobQueue.add(newJob);
		publishJobAdded(newJob);

		if (worker == null || !worker.isAlive() || exiting) {
			worker = new JobWorker();
			exiting = false;
			worker.start();
			publishStateChanged();
		}
	}

	@Override
	public synchronized void removeJob(Job job)
	{
		if (jobQueue.contains(job) && job.getJobState() != JobState.RUNNING) {
			if (jobQueue.remove(job)) {
				publishJobRemoved(job);
			}
		}
	}

	private synchronized Job fetchNextJob()
	{
		Job nextJob;

		for (int i = 0; i < jobQueue.size(); i++) {
			nextJob = jobQueue.get(i);
			switch (nextJob.getJobState()) {
			case WAITING:
				nextJob.setState(JobState.RUNNING);
				return nextJob;
			case FINISHED:
				jobQueue.remove(i);
				publishJobRemoved(nextJob);
				i--;
				break;
			case ABORTED:
				break;
			case INIT:
				break;
			case RUNNING:
				break;
			default:
				break;
			}
		}

		exiting = true;
		return null;
	}

	private class JobWorker extends Thread
	{
		Job currentJob;

		@Override
		public void run()
		{
			while ((currentJob = fetchNextJob()) != null) {
				publishNextJobStarted(currentJob);

				currentJob.setState(JobState.RUNNING);
				currentJob.run();

				if (currentJob.getJobState() == JobState.FINISHED) {
					jobQueue.remove(currentJob);
					publishJobRemoved(currentJob);
				}
			}
			publishStateChanged();
		}
	}

	@Override
	public boolean addProcessorListener(IProcessorListener listener)
	{
		return listeners.add(listener);
	}

	@Override
	public boolean removeProcessorListener(IProcessorListener listener)
	{
		return listeners.remove(listener);
	}

	private void publishJobAdded(Job newJob)
	{
		for (IProcessorListener l : listeners) {
			l.jobAdded(newJob);
		}
	}

	private void publishJobRemoved(Job job)
	{
		for (IProcessorListener l : listeners) {
			l.jobRemoved(job);
		}
	}

	private void publishStateChanged()
	{
		for (IProcessorListener l : listeners) {
			l.processorStateChanged();
		}
	}

	private void publishNextJobStarted(Job job)
	{
		for (IProcessorListener l : listeners) {
			l.nextJobStarted(job);
		}
	}
}