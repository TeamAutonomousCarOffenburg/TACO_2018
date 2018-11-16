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

import javax.swing.JComponent;

import hso.autonomy.tools.developer.bundles.developer.IDeveloperBundle;
import hso.autonomy.tools.developer.bundles.developer.IDeveloperService;
import hso.autonomy.tools.developer.bundles.developer.perspective.IView;
import hso.autonomy.tools.developer.bundles.developer.perspective.ViewBase;
import hso.autonomy.tools.developer.bundles.developer.perspective.ViewDescriptorBase;
import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.imageBuffer.ImageFile;

/**
 * The Processor view, listing active jobs of the processor service.
 *
 * @author Stefan Glaser
 */
public class ProcessorViewDescriptor extends ViewDescriptorBase
{
	public ProcessorViewDescriptor(BundleContext context)
	{
		super(context, IDeveloperBundle.EX_PROCESSOR_VIEW, "Processor", ImageFile.PROCESSOR);
	}

	@Override
	public boolean isManuallyOpenable()
	{
		return false;
	}

	@Override
	public IView createNewView()
	{
		return new ProcessorToolBoxView();
	}

	private class ProcessorToolBoxView extends ViewBase
	{
		public ProcessorToolBoxView()
		{
			super(context, ProcessorViewDescriptor.this, title);
		}

		@Override
		protected JComponent createContentComponent()
		{
			return new ProcessorPanel(IDeveloperService.PROCESSOR.get(context));
		}
	}
}
