package hso.autonomy.tools.developer.util.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import hso.autonomy.tools.util.swing.SwingHelper;

public class OutputFileSelectorPanel extends JPanel
{
	private JTextField outputFiletextField;

	private JButton openFileChooserButton;

	private final FileDialogWrapper fileChooser = new FileDialogWrapper();

	private File selectedFile;

	public OutputFileSelectorPanel()
	{
		initializeComponents();
	}

	private void initializeComponents()
	{
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {1, 1, 1};
		layout.columnWeights = new double[] {0.0, 0.1, 0.0};
		layout.rowHeights = new int[] {1, 1};
		layout.rowWeights = new double[] {0.0, 0.1};
		setLayout(layout);

		outputFiletextField = new JTextField();
		outputFiletextField.setEditable(false);

		openFileChooserButton = new JButton("...");
		openFileChooserButton.addActionListener(e -> {
			if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(SwingHelper.getTopLevelWindow(e))) {
				selectedFile = fileChooser.getSelectedFile();
				outputFiletextField.setText(selectedFile.getAbsolutePath());
			}
		});

		add(new JLabel("Output file:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
												GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(outputFiletextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
										 GridBagConstraints.HORIZONTAL, new Insets(8, 8, 5, 3), 0, 0));
		add(openFileChooserButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
										   GridBagConstraints.HORIZONTAL, new Insets(8, 3, 5, 8), 0, 0));
	}

	public File getSelectedFile()
	{
		return selectedFile;
	}
}
