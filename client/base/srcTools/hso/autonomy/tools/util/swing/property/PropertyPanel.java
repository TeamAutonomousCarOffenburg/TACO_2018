package hso.autonomy.tools.util.swing.property;

import hso.autonomy.util.logging.PropertyMap;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class PropertyPanel extends JPanel
{
	private PropertyPanelTableModel tableModel;

	private TableColumnAdjuster adjuster;

	private PropertyMap properties;

	private boolean frozen = false;

	private boolean initialized = false;

	public void refresh()
	{
		SwingUtilities.invokeLater(this ::refreshTable);
	}

	private void refreshTable()
	{
		if (!initialized) {
			return;
		}

		while (tableModel.getRowCount() > 0) {
			tableModel.removeRow(0);
		}

		tableModel.refresh();

		Map<String, PropertyMap.Property> map = properties.getMap();
		for (String name : map.keySet()) {
			PropertyMap.Property property = map.get(name);
			tableModel.addRow(new Object[] {name, property.value});
			tableModel.rowsWithChangedValues.add(property.valueChanged);
			tableModel.rowsUpdatedLastCycle.add(property.updatedLastCycle);
		}

		adjuster.adjustColumns();
	}

	public void freeze()
	{
		frozen = true;
		refresh();
	}

	public void setProperties(final PropertyMap properties)
	{
		if (properties == this.properties) {
			return;
		}

		this.properties = properties;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		frozen = false;

		removeAll();

		tableModel = new PropertyPanelTableModel(new Object[] {"Name", "Value"}, 0);

		JTable table = new JTable(tableModel) {
			private Color unmodifiedEven;

			private Color unmodifiedOdd;

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component component = super.prepareRenderer(renderer, row, column);

				if (component instanceof JComponent) {
					boolean isEven = row % 2 == 0;
					JComponent jc = (JComponent) component;
					cacheColors(isEven, jc);

					jc.setToolTipText(getValueAt(row, column).toString());
					if (unmodifiedEven != null && unmodifiedOdd != null) {
						jc.setBackground(getBackground(
								isEven, tableModel.wasRowValueChanged(row), tableModel.wasRowUpdatedLastCycle(row)));
					}
				}
				return component;
			}

			private void cacheColors(boolean isEven, JComponent jc)
			{
				if (isEven && unmodifiedEven == null) {
					unmodifiedEven = jc.getBackground();
				} else if (!isEven && unmodifiedOdd == null) {
					unmodifiedOdd = jc.getBackground();
				}
			}

			private Color getBackground(boolean isEven, boolean valueChanged, boolean updatedLastCycle)
			{
				if (frozen) {
					return Color.LIGHT_GRAY;
				}
				if (valueChanged) {
					return Color.GREEN;
				}
				if (updatedLastCycle) {
					return Color.YELLOW;
				}
				return isEven ? unmodifiedEven : unmodifiedOdd;
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(false);
		table.setFocusable(false);
		add(table);

		adjuster = new TableColumnAdjuster(table);

		initialized = true;
		refresh();
	}

	public void clear()
	{
		removeAll();
		revalidate();
		repaint();

		frozen = false;
		initialized = false;
		tableModel = null;
		adjuster = null;
		properties = null;
	}

	private class PropertyPanelTableModel extends DefaultTableModel
	{
		public final List<Boolean> rowsWithChangedValues = new ArrayList<>();

		public final List<Boolean> rowsUpdatedLastCycle = new ArrayList<>();

		public PropertyPanelTableModel(Object[] columnNames, int rowCount)
		{
			super(columnNames, rowCount);
		}

		@Override
		public boolean isCellEditable(int row, int column)
		{
			return false;
		}

		public boolean wasRowValueChanged(int row)
		{
			if (row >= rowsWithChangedValues.size()) {
				return false;
			}
			return rowsWithChangedValues.get(row);
		}

		public boolean wasRowUpdatedLastCycle(int row)
		{
			if (row >= rowsUpdatedLastCycle.size()) {
				return false;
			}
			return rowsUpdatedLastCycle.get(row);
		}

		public void refresh()
		{
			rowsWithChangedValues.clear();
			rowsUpdatedLastCycle.clear();
		}
	}
}
