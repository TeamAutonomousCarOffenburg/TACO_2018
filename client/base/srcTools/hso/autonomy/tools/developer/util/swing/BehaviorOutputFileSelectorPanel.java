package hso.autonomy.tools.developer.util.swing;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JCheckBox;

public class BehaviorOutputFileSelectorPanel extends OutputFileSelectorPanel
{
	private JCheckBox walkSelected;

	private JCheckBox ballSelected;

	private JCheckBox legsSelected;

	private JCheckBox jointSpeedSelected;

	private JCheckBox walkToPositionSelected;

	public BehaviorOutputFileSelectorPanel()
	{
		initializeComponents();
	}

	private void initializeComponents()
	{
		walkSelected = new JCheckBox("Walk");
		walkSelected.setSelected(false);
		add(walkSelected, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
								  GridBagConstraints.HORIZONTAL, new Insets(8, 8, 5, 3), 0, 0));

		ballSelected = new JCheckBox("Ball");
		ballSelected.setSelected(true);
		add(ballSelected, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
								  GridBagConstraints.HORIZONTAL, new Insets(8, 8, 5, 3), 0, 0));

		legsSelected = new JCheckBox("Legs");
		legsSelected.setSelected(true);
		add(legsSelected, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
								  GridBagConstraints.HORIZONTAL, new Insets(8, 8, 5, 3), 0, 0));

		jointSpeedSelected = new JCheckBox("JointSpeeds");
		jointSpeedSelected.setSelected(true);
		add(jointSpeedSelected, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
										GridBagConstraints.HORIZONTAL, new Insets(8, 8, 5, 3), 0, 0));

		walkToPositionSelected = new JCheckBox("WalkToPosition");
		walkToPositionSelected.setSelected(false);
		add(walkToPositionSelected, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
											GridBagConstraints.HORIZONTAL, new Insets(8, 8, 5, 3), 0, 0));
	}

	public boolean isWalkSelected()
	{
		return walkSelected.isSelected();
	}

	public boolean isBallSelected()
	{
		return ballSelected.isSelected();
	}

	public boolean isLegsSelected()
	{
		return legsSelected.isSelected();
	}

	public boolean isJointSpeedSelected()
	{
		return jointSpeedSelected.isSelected();
	}

	public boolean isWalkToPositionSelected()
	{
		return walkToPositionSelected.isSelected();
	}
}
