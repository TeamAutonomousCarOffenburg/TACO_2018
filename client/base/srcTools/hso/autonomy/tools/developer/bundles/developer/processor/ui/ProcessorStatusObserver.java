package hso.autonomy.tools.developer.bundles.developer.processor.ui;

import javax.swing.JComponent;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.developer.bundles.developer.processor.model.IProcessorListener;
import hso.autonomy.tools.developer.bundles.developer.processor.model.impl.Job;
import hso.autonomy.tools.developer.bundles.developer.processor.model.IProcessor;
import hso.autonomy.tools.developer.bundles.developer.processor.model.JobState;
import hso.autonomy.tools.developer.bundles.developer.window.statusBar.StatusObserverBase;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.imageBuffer.ImageFile;

public class ProcessorStatusObserver extends StatusObserverBase implements IProcessorListener
{
	private IProcessor processor;

	public ProcessorStatusObserver(BundleContext context)
	{
		super(context, "Processor Status", ImageFile.PROCESSOR.getIcon());
		IDeveloperService.PROCESSOR.get(context).addProcessorListener(this);
		processor = IDeveloperService.PROCESSOR.get(context);
	}

	@Override
	protected JComponent createDetailedStatusPanel()
	{
		return new ProcessorPanel(processor);
	}

	@Override
	public void jobAdded(Job newJob)
	{
	}

	@Override
	public void jobRemoved(Job job)
	{
		if (job.getJobState() == JobState.FINISHED) {
			publishMessage(job.getJobName() + " ... done!");
		} else {
			publishMessage(job.getJobName() + " ... removed!");
		}
	}

	@Override
	public void nextJobStarted(Job job)
	{
		publishMessage(job.getJobName() + " ...");
	}

	@Override
	public void processorStateChanged()
	{
	}
}
