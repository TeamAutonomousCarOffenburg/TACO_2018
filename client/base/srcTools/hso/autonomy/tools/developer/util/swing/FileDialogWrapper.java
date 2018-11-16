package hso.autonomy.tools.developer.util.swing;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import net.tomahawk.XFileDialog;

/**
 * Wraps XFileDialog (native windows file dialogs), making it interchangeable
 * with JFileChooser by providing the same methods.
 */
public class FileDialogWrapper
{
	private XFileDialog dialog;

	private JFileChooser fileChooser;

	private String selectedFile;

	private String[] selectedFiles;

	private boolean multiSelectionEnabled;

	private File currentDirectory;

	private int fileSelectionMode;

	public FileDialogWrapper()
	{
		if (isWindows()) {
			disableTraces();
		} else {
			fileChooser = new JFileChooser();
		}
	}

	public FileDialogWrapper(String currentDirectoryPath)
	{
		this(new File(currentDirectoryPath));
	}

	public FileDialogWrapper(File currentDirectory)
	{
		this();
		setCurrentDirectory(currentDirectory);
	}

	private void disableTraces()
	{
		// disabling traces leads to a trace, disabling sout as a workaround
		PrintStream original = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b)
			{ /* do nothing */
			}
		}));
		XFileDialog.setTraceLevel(0);
		System.setOut(original);
	}

	private boolean isWindows()
	{
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	private XFileDialog createXFileDialog(Component parent)
	{
		if (parent instanceof Frame) {
			dialog = new XFileDialog((Frame) parent);
		} else {
			// dialog = new XFileDialog(MainFrame.WINDOW_TITLE);
		}

		if (currentDirectory != null) {
			dialog.setDirectory(currentDirectory.getAbsolutePath());
		}
		return dialog;
	}

	public void setCurrentDirectory(File dir)
	{
		if (isWindows()) {
			currentDirectory = dir;
		} else {
			fileChooser.setCurrentDirectory(dir);
		}
	}

	public File getCurrentDirectory()
	{
		if (isWindows()) {
			return new File(dialog.getDirectory());
		} else {
			return fileChooser.getCurrentDirectory();
		}
	}

	public void setSelectedFile(File file)
	{
		if (isWindows()) {
			// not supported
		} else {
			fileChooser.setSelectedFile(file);
		}
	}

	public void setFileSelectionMode(int mode)
	{
		if (isWindows()) {
			fileSelectionMode = mode;
		} else {
			fileChooser.setFileSelectionMode(mode);
		}
	}

	public int showOpenDialog(Component parent)
	{
		if (isWindows()) {
			return showOpenDialogWindows(parent);
		} else {
			return fileChooser.showOpenDialog(parent);
		}
	}

	private int showOpenDialogWindows(Component parent)
	{
		if (multiSelectionEnabled) {
			if (fileSelectionMode == JFileChooser.DIRECTORIES_ONLY) {
				selectedFiles = createXFileDialog(parent).getFolders();
			} else {
				selectedFiles = createXFileDialog(parent).getFiles();
			}
			if (selectedFiles != null) {
				return JFileChooser.APPROVE_OPTION;
			}
		} else {
			if (fileSelectionMode == JFileChooser.DIRECTORIES_ONLY) {
				selectedFile = createXFileDialog(parent).getFolder();
			} else {
				selectedFile = createXFileDialog(parent).getFile();
			}
			if (selectedFile != null) {
				return JFileChooser.APPROVE_OPTION;
			}
		}
		return JFileChooser.CANCEL_OPTION;
	}

	public int showSaveDialog(Component parent)
	{
		if (isWindows()) {
			return showSaveDialogWindows(parent);
		} else {
			return fileChooser.showSaveDialog(parent);
		}
	}

	private int showSaveDialogWindows(Component parent)
	{
		selectedFile = createXFileDialog(parent).getSaveFile();
		if (selectedFile != null) {
			return JFileChooser.APPROVE_OPTION;
		}
		return JFileChooser.CANCEL_OPTION;
	}

	public File getSelectedFile()
	{
		if (isWindows()) {
			return fileNameToFile(selectedFile);
		} else {
			return fileChooser.getSelectedFile();
		}
	}

	public File[] getSelectedFiles()
	{
		if (isWindows()) {
			return getSelectedFilesWindows();
		} else {
			return fileChooser.getSelectedFiles();
		}
	}

	private File[] getSelectedFilesWindows()
	{
		if (selectedFiles == null) {
			return new File[0];
		} else {
			List<File> files = new ArrayList<>();
			for (String file : selectedFiles) {
				files.add(fileNameToFile(file));
			}
			return files.toArray(new File[0]);
		}
	}

	private File fileNameToFile(String fileName)
	{
		Path currentDir = Paths.get(getCurrentDirectory().getAbsolutePath());
		return new File(currentDir.resolve(fileName).toString());
	}

	public void setMultiSelectionEnabled(boolean b)
	{
		if (isWindows()) {
			multiSelectionEnabled = b;
		} else {
			fileChooser.setMultiSelectionEnabled(b);
		}
	}
}