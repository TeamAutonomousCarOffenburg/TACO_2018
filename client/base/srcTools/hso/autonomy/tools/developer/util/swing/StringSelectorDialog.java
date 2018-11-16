package hso.autonomy.tools.developer.util.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;

public class StringSelectorDialog extends JDialog
{
	private IStringSelectorDialogListener listener;

	private DefaultListModel<String> possibleStringsListModel;

	private JList<String> possibleStringsList;

	private DefaultListModel<String> selectedStringsListModel;

	private JList<String> selectedStringsList;

	private JButton selectAllBtn;

	private JButton selectSelectedBtn;

	private JButton deselectSelectedBtn;

	private JButton deselectAllBtn;

	private JButton okBtn;

	private JButton cancelBtn;

	public StringSelectorDialog()
	{
		setBounds(100, 100, 450, 500);

		initializeComponents();
	}

	private void initializeComponents()
	{
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {1, 1, 1};
		layout.columnWeights = new double[] {0.1, 0.0, 0.1};
		layout.rowHeights = new int[] {1, 1, 1, 1, 1, 1, 1};
		layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.0, 0.1, 0.0, 0.0};
		setLayout(layout);

		possibleStringsListModel = new DefaultListModel<>();
		possibleStringsList = new JList<>(possibleStringsListModel);
		possibleStringsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		selectedStringsListModel = new DefaultListModel<>();
		selectedStringsList = new JList<>(selectedStringsListModel);
		selectedStringsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		selectAllBtn = new JButton("=>");
		selectAllBtn.addActionListener(e -> selectAll());

		selectSelectedBtn = new JButton("->");
		selectSelectedBtn.addActionListener(e -> selectSelected());

		deselectSelectedBtn = new JButton("<-");
		deselectSelectedBtn.addActionListener(e -> deselectSelected());

		deselectAllBtn = new JButton("<=");
		deselectAllBtn.addActionListener(e -> deselectAll());

		okBtn = new JButton("OK");
		okBtn.addActionListener(e -> {
			List<String> selectedStrings = new ArrayList<>(selectedStringsListModel.size());

			for (int i = 0; i < selectedStringsListModel.size(); i++) {
				selectedStrings.add(selectedStringsListModel.get(i));
			}

			StringSelectorDialog.this.listener.selectStrings(selectedStrings);
			StringSelectorDialog.this.listener = null;
			StringSelectorDialog.this.setVisible(false);
		});

		cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(e -> StringSelectorDialog.this.setVisible(false));

		JPanel btnPanel = new JPanel();
		btnPanel.add(okBtn);
		btnPanel.add(cancelBtn);

		add(new JLabel("Possible:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
											 GridBagConstraints.HORIZONTAL, new Insets(8, 8, 1, 1), 0, 0));
		add(new JLabel("Selected:"), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
											 GridBagConstraints.HORIZONTAL, new Insets(8, 1, 1, 8), 0, 0));

		add(new JScrollPane(possibleStringsList),
				new GridBagConstraints(0, 1, 1, 4, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
		add(selectAllBtn, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH,
								  GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(new JScrollPane(selectedStringsList),
				new GridBagConstraints(2, 1, 1, 4, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

		add(selectSelectedBtn, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
									   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		add(deselectSelectedBtn, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH,
										 GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		add(deselectAllBtn, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
									GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		add(new JSeparator(JSeparator.HORIZONTAL),
				new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(5, 1, 5, 1), 0, 0));

		add(btnPanel, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
							  new Insets(3, 10, 8, 8), 0, 0));
	}

	public void showDialog(Component parent, String title, List<String> possibleStrings,
			List<String> preSelectedStrings, IStringSelectorDialogListener listener)
	{
		if (listener == null) {
			return;
		}
		this.listener = listener;
		setTitle(title);

		possibleStringsListModel.clear();
		possibleStrings.stream().filter(s -> !preSelectedStrings.contains(s)).forEach(s -> {
			possibleStringsListModel.addElement(s);
		});

		selectedStringsListModel.clear();
		for (String s : preSelectedStrings) {
			selectedStringsListModel.addElement(s);
		}

		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private void updateButtons()
	{
		if (possibleStringsListModel.size() == 0) {
			selectAllBtn.setEnabled(false);
			selectSelectedBtn.setEnabled(false);
		} else {
			selectAllBtn.setEnabled(true);
			selectSelectedBtn.setEnabled(true);
		}

		if (selectedStringsListModel.size() == 0) {
			deselectAllBtn.setEnabled(false);
			deselectSelectedBtn.setEnabled(false);
		} else {
			deselectAllBtn.setEnabled(true);
			deselectSelectedBtn.setEnabled(true);
		}
	}

	private void selectAll()
	{
		for (int i = 0; i < possibleStringsListModel.size(); i++) {
			selectedStringsListModel.addElement(possibleStringsListModel.get(i));
		}
		possibleStringsListModel.clear();
		updateButtons();
	}

	private void selectSelected()
	{
		int[] selectedIndexes = possibleStringsList.getSelectedIndices();

		for (int i = selectedIndexes.length - 1; i >= 0; i--) {
			selectedStringsListModel.addElement(possibleStringsListModel.get(selectedIndexes[i]));
			possibleStringsListModel.remove(selectedIndexes[i]);
		}
		updateButtons();
	}

	private void deselectAll()
	{
		for (int i = 0; i < selectedStringsListModel.size(); i++) {
			possibleStringsListModel.addElement(selectedStringsListModel.get(i));
		}
		selectedStringsListModel.clear();
		updateButtons();
	}

	private void deselectSelected()
	{
		int[] selectedIndexes = selectedStringsList.getSelectedIndices();

		for (int i = selectedIndexes.length - 1; i >= 0; i--) {
			possibleStringsListModel.addElement(selectedStringsListModel.get(selectedIndexes[i]));
			selectedStringsListModel.remove(selectedIndexes[i]);
		}
		updateButtons();
	}
}
