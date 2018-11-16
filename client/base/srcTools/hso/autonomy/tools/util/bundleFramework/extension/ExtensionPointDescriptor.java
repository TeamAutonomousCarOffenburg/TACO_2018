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
package hso.autonomy.tools.util.bundleFramework.extension;

public class ExtensionPointDescriptor implements IExtensionPointDescriptor
{
	private final String extensionPointID;

	private final Class<?> extensionPointInterface;

	public ExtensionPointDescriptor(String extensionPointID, Class<?> extensionPointInterface)
	{
		this.extensionPointID = extensionPointID;
		this.extensionPointInterface = extensionPointInterface;
	}

	@Override
	public String getExtensionPointID()
	{
		return extensionPointID;
	}

	@Override
	public Class<?> getExtensionPointInterface()
	{
		return extensionPointInterface;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ExtensionPoint: \"")
				.append(extensionPointID)
				.append("\" -> \"")
				.append(extensionPointInterface)
				.append("\"\n");

		return sb.toString();
	}
}
