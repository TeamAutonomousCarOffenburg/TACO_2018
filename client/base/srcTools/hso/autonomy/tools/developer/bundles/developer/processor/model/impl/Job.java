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
import java.util.List;

import hso.autonomy.tools.developer.bundles.developer.processor.model.IJobListener;
import hso.autonomy.tools.developer.bundles.developer.processor.model.JobState;

public abstract class Job implements Runnable
{
	private static int jobCount = 0;

	protected final List<IJobListener> listeners;

	protected final int jobID;

	protected final String jobName;

	protected final String jobDescription;

	protected JobState jobState;

	public Job(String nameAndDescription)
	{
		this(nameAndDescription, nameAndDescription);
	}

	public Job(String name, String description)
	{
		this.jobID = ++jobCount;
		this.jobName = name;
		this.jobDescription = description;
		this.jobState = JobState.INIT;

		this.listeners = new ArrayList<>();
	}

	public void setState(JobState newState)
	{
		if (jobState != newState) {
			jobState = newState;
			publishStateChanged();
		}
	}

	@Override
	public void run()
	{
		setState(JobState.RUNNING);

		try {
			runJob();
		} catch (Throwable t) {
			t.printStackTrace();
			setState(JobState.ABORTED);
			return;
		}

		setState(JobState.FINISHED);
	}

	protected abstract void runJob() throws Throwable;

	public String getJobName()
	{
		return jobName;
	}

	public String getJobDescription()
	{
		return jobDescription;
	}

	public int getJobID()
	{
		return jobID;
	}

	public JobState getJobState()
	{
		return jobState;
	}

	public boolean addJobListener(IJobListener listener)
	{
		return listeners.add(listener);
	}

	public boolean removeJobListener(IJobListener listener)
	{
		return listeners.remove(listener);
	}

	protected void publishStateChanged()
	{
		listeners.forEach(IJobListener::jobStateChanged);
	}
}
