package hso.autonomy.tools.util.swing.wizard;

import hso.autonomy.tools.util.imageBuffer.ImageFile;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

/**
 * @author Stefan Glaser
 */
public class Wizard implements PropertyChangeListener
{
	public static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";

	public static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";

	public static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand";

	private final JFrame owner;

	private JDialog dialog;

	private WizardModel model;

	private WizardController controller;

	private JLabel titleLabel;

	private JLabel descriptionLabel;

	private JLabel iconLabel;

	private JPanel cardPanel;

	private CardLayout cardLayout;

	private JButton backBtn;

	private JButton nextBtn;

	private JButton cancelBtn;

	private JButton finishBtn;

	protected String rootWizardPanelID;

	private boolean blocked;

	public Wizard(JFrame owner)
	{
		this.owner = owner;
		dialog = new JDialog(owner);
		model = new WizardModel();
		controller = new WizardController(this);

		initializeComponents();

		model.addPropertyChangeListener(this);
	}

	public Wizard(JFrame owner, IWizardDescriptor wizardDescriptor)
	{
		this(owner);

		setDialogTitle(wizardDescriptor.getTitle());
		setDialogIconImage(((ImageIcon) wizardDescriptor.getIcon()).getImage());
		setTitle(wizardDescriptor.getTitle());
		setDescription(wizardDescriptor.getDescription());
		setIcon(wizardDescriptor.getIcon());

		// Register wizard panels
		IWizardPanelDescriptor[] wizardPanels = wizardDescriptor.createWizardPanels();
		for (IWizardPanelDescriptor wizardPanel : wizardPanels) {
			registerWizardPanel(wizardPanel);
		}

		// Set root wizard panel
		rootWizardPanelID = wizardDescriptor.getRootWizardPanelID();
	}

	private void initializeComponents()
	{
		dialog.setSize(500, 540);
		dialog.setLocationRelativeTo(owner);
		dialog.getContentPane().setLayout(new BorderLayout());
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			if (!blocked && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				close(false);
			}
			return false;
		});

		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(Color.WHITE);
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {1, 1};
		layout.columnWeights = new double[] {0.1, 0.0};
		layout.rowHeights = new int[] {1, 1, 1};
		layout.rowWeights = new double[] {0.0, 0.1, 0.0};
		titlePanel.setLayout(layout);

		titleLabel = new JLabel("Wizard");
		titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
		descriptionLabel = new JLabel("A Wizard dialog");
		iconLabel = new JLabel(ImageFile.BROKEN_IMAGE.getIcon());

		GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 15, 5, 10), 0, 0);
		titlePanel.add(titleLabel, gbc);

		gbc.gridx = 1;
		gbc.gridheight = 2;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		titlePanel.add(iconLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(5, 20, 10, 10);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		titlePanel.add(descriptionLabel, gbc);

		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		titlePanel.add(new JSeparator(), gbc);

		cardPanel = new JPanel();
		cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		backBtn = new JButton("< Back");
		backBtn.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
		backBtn.addActionListener(controller);

		nextBtn = new JButton("Next >");
		nextBtn.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
		nextBtn.addActionListener(controller);

		cancelBtn = new JButton("Cancel");
		cancelBtn.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);
		cancelBtn.addActionListener(controller);

		finishBtn = new JButton("Finish");

		JPanel buttonPanel = new JPanel();
		JSeparator separator = new JSeparator();
		Box buttonBox = new Box(BoxLayout.LINE_AXIS);

		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(separator, BorderLayout.NORTH);

		buttonBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonBox.add(backBtn);
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(nextBtn);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(cancelBtn);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(finishBtn);

		buttonPanel.add(buttonBox, BorderLayout.EAST);

		dialog.getContentPane().add(titlePanel, BorderLayout.NORTH);
		dialog.getContentPane().add(cardPanel, BorderLayout.CENTER);
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}

	public void setDialogTitle(String title)
	{
		dialog.setTitle(title);
	}

	public void setDialogIconImage(Image icon)
	{
		dialog.setIconImage(icon);
	}

	public void setTitle(String title)
	{
		titleLabel.setText(title);
	}

	public void setDescription(String description)
	{
		descriptionLabel.setText(description);
	}

	public void setIcon(Icon icon)
	{
		iconLabel.setIcon(icon);
	}

	public WizardModel getModel()
	{
		return model;
	}

	public void showDialog()
	{
		onDisplay();
		dialog.setVisible(true);
	}

	public void showModalDialog()
	{
		onDisplay();
		dialog.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		dialog.setVisible(true);
	}

	public ModalityType getModalityType()
	{
		return dialog.getModalityType();
	}

	public void setModalityType(ModalityType type)
	{
		dialog.setModalityType(type);
	}

	/**
	 * Close the wizard.
	 *
	 * @param success Success indicator. True if the close request follows from
	 *        an successful completion of this wizard, false otherwise
	 */
	public void close(boolean success)
	{
		dialog.setVisible(false);
		onClose(success);
	}

	protected void onDisplay()
	{
		if (rootWizardPanelID != null) {
			setCurrentPanel(rootWizardPanelID);
		}
	}

	protected void onClose(boolean success)
	{
	}

	public void registerWizardPanel(IWizardPanelDescriptor panel)
	{
		cardPanel.add(panel.getPanelComponent(), panel.getID());

		model.registerPanelDescriptor(panel);

		// Set a callback to the current wizard.
		panel.setWizard(this);

		if (model.getCurrentDescriptor() == null) {
			setCurrentPanel(panel.getID());
		}
	}

	public void setCurrentPanel(String id)
	{
		model.setCurrentDescriptor(id);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName() == WizardModel.PROPERTY_CURRENT_DESCRIPTOR) {
			if (evt.getOldValue() != null) {
				((IWizardPanelDescriptor) evt.getOldValue()).onHidePanel();
			}

			IWizardPanelDescriptor currentDescriptor = model.getCurrentDescriptor();

			currentDescriptor.onDisplayPanel();
			cardLayout.show(cardPanel, currentDescriptor.getID());
			finishBtn.setAction(currentDescriptor.getFinishAction());
			finishBtn.addActionListener(controller);
			finishBtn.setText("Finish");

			if (currentDescriptor.getFinishAction() == null) {
				finishBtn.setEnabled(false);
			}
		} else if (evt.getPropertyName() == WizardModel.PROPERTY_BACK_BUTTON_ENABLED) {
			backBtn.setEnabled(model.isBackButtonEnabled());
		} else if (evt.getPropertyName() == WizardModel.PROPERTY_NEXT_BUTTON_ENABLED) {
			boolean enabled = model.isNextButtonEnabled();
			nextBtn.setEnabled(enabled);
			dialog.getRootPane().setDefaultButton(enabled ? nextBtn : finishBtn);
		}
	}

	public void setSize(int width, int height)
	{
		dialog.setSize(width, height);
	}

	public void block()
	{
		blocked = true;
		dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		finishBtn.setEnabled(false);
		cancelBtn.setEnabled(false);
	}

	public void unblock()
	{
		blocked = false;
		dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		finishBtn.setEnabled(true);
		cancelBtn.setEnabled(true);
	}
}
