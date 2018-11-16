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
package hso.autonomy.tools.util.bundleFramework;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import hso.autonomy.tools.util.bundleFramework.service.ServiceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import hso.autonomy.tools.util.bundleFramework.extension.ExtensionDescriptor;
import hso.autonomy.tools.util.bundleFramework.extension.ExtensionPointDescriptor;

class BundleLoader
{
	private static Validator validator = null;

	public static List<Bundle> getBundles(String bundlesPath)
	{
		init();

		List<Bundle> bundles = new ArrayList<>();

		String[] resourceListing;
		try {
			resourceListing = getResourceListing(BundleLoader.class, bundlesPath);

			for (String dir : resourceListing) {
				// if (dir.isDirectory()) {
				// If the file is a directory, check for the bundle.xml file
				String path = "/" + bundlesPath + dir + "/bundle.xml";
				InputStream stream = BundleLoader.class.getResourceAsStream(path);

				// If the bundle.xml file exists, validate and parse it
				if (stream != null && validateXml(stream, path)) {
					// validateXML is closing the stream, so we have to get one
					// again, ugly
					stream = BundleLoader.class.getResourceAsStream(path);
					bundles.add(parseBundle(stream));
				}
				// }
			}

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}

		// for (Bundle b : bundles) {
		// System.out.println(b);
		// }

		return bundles;
	}

	private static Bundle parseBundle(InputStream xml)
	{
		String bundleID;
		IBundleActivator bundleActivator;
		ArrayList<ServiceDescriptor> services = new ArrayList<>();
		ArrayList<ExtensionPointDescriptor> extensionPoints = new ArrayList<>();
		ArrayList<ExtensionDescriptor> extensions = new ArrayList<>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		String ID;
		String ID2;
		String className;
		List<String> dependencies;

		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(xml);
			document.normalize();

			// Parse name
			NodeList nl = document.getElementsByTagName("bundle");
			bundleID = nl.item(0).getAttributes().getNamedItem("name").getNodeValue();
			bundleActivator =
					(IBundleActivator) Class
							.forName(nl.item(0).getAttributes().getNamedItem("bundleActivator").getNodeValue())
							.newInstance();

			// Parse services
			nl = document.getElementsByTagName("service");
			for (int i = 0; i < nl.getLength(); i++) {
				ID = nl.item(i).getAttributes().getNamedItem("serviceID").getNodeValue();
				className = nl.item(i).getAttributes().getNamedItem("serviceInterface").getNodeValue();
				NodeList depsList = nl.item(i).getChildNodes();

				dependencies = new ArrayList<>();

				for (int j = 0; j < depsList.getLength(); j++) {
					if (depsList.item(j).getNodeName().equals("dependency")) {
						dependencies.add(depsList.item(j).getAttributes().item(0).getNodeValue());
					}
				}

				services.add(new ServiceDescriptor(
						ID, dependencies.toArray(new String[dependencies.size()]), Class.forName(className)));
			}

			// Parse ExtensionPoints
			nl = document.getElementsByTagName("extensionPoint");
			for (int i = 0; i < nl.getLength(); i++) {
				ID = nl.item(i).getAttributes().getNamedItem("extensionPointID").getNodeValue();
				className = nl.item(i).getAttributes().getNamedItem("extensionPointInterface").getNodeValue();

				extensionPoints.add(new ExtensionPointDescriptor(ID, Class.forName(className)));
			}

			// Parse Extensions
			nl = document.getElementsByTagName("extension");
			for (int i = 0; i < nl.getLength(); i++) {
				ID = nl.item(i).getAttributes().getNamedItem("extensionID").getNodeValue();
				ID2 = nl.item(i).getAttributes().getNamedItem("extensionPointID").getNodeValue();
				className = nl.item(i).getAttributes().getNamedItem("extensionPointInterface").getNodeValue();
				NodeList depsList = nl.item(i).getChildNodes();

				dependencies = new ArrayList<>();

				for (int j = 0; j < depsList.getLength(); j++) {
					if (depsList.item(j).getNodeName().equals("dependency")) {
						dependencies.add(depsList.item(j).getAttributes().item(0).getNodeValue());
					}
				}

				extensions.add(new ExtensionDescriptor(
						ID, dependencies.toArray(new String[dependencies.size()]), ID2, Class.forName(className)));
			}

			BundleDescriptor descriptor = new BundleDescriptor(bundleID, services, extensionPoints, extensions);

			return new Bundle(descriptor, bundleActivator);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void init()
	{
		if (validator != null) {
			return;
		}

		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		Schema schema;
		try {
			Source schemaFile = new StreamSource(BundleLoader.class.getResourceAsStream("xsd/bundleSchema.xsd"));
			schema = factory.newSchema(schemaFile);
			validator = schema.newValidator();
		} catch (SAXException e) {
			System.err.println("Loading Bundle schema FAILED!!!");
			e.printStackTrace();
		}
	}

	private static boolean validateXml(InputStream xml, String path)
	{
		try {
			Source source = new StreamSource(xml);
			validator.validate(source);
			return true;

		} catch (SAXException ex) {
			System.err.println("Bundle is not valid: " + path);
			System.err.println(ex.getMessage());
		} catch (IOException e) {
			System.err.println("File could not be found: " + path);
		}
		return false;
	}

	/**
	 * List directory contents for a resource folder. Not recursive. This is
	 * basically a brute-force implementation. Works for regular files and also
	 * JARs.
	 *
	 * @param clazz Any java class that lives in the same place as the resources
	 *        you want.
	 * @param path Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	static String[] getResourceListing(Class<BundleLoader> clazz, String path) throws URISyntaxException, IOException
	{
		URL dirURL = clazz.getClassLoader().getResource(path);
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			/* A file path: easy enough */
			return new File(dirURL.toURI()).list();
		}

		if (dirURL == null) {
			/*
			 * In case of a jar file, we can't actually find a directory. Have to
			 * assume the same jar as clazz.
			 */
			String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}

		if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5,
					dirURL.getPath().indexOf("!")); // strip out only the JAR file
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in
														   // jar
			Set<String> result = new HashSet<>();		   // avoid duplicates in case
														   // it is a subdirectory
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(path)) { // filter according to the path
					String entry = name.substring(path.length());
					int checkSubdir = entry.indexOf("/");
					if (checkSubdir >= 0) {
						// if it is a subdirectory, we just return the directory name
						entry = entry.substring(0, checkSubdir);
					}
					result.add(entry);
				}
			}
			jar.close();
			return result.toArray(new String[result.size()]);
		}

		throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
	}
}
