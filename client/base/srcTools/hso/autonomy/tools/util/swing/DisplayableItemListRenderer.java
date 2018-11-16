package hso.autonomy.tools.util.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import hso.autonomy.tools.util.imageBuffer.ImageFile;
import hso.autonomy.tools.util.swing.model.IDisplayableItem;

/**
 * @author Stefan Glaser
 */
public class DisplayableItemListRenderer extends DefaultListCellRenderer
{
	public DisplayableItemListRenderer()
	{
		setIcon(ImageFile.BROKEN_IMAGE.getIcon());
		setText("Title");
	}

	@Override
	public Component getListCellRendererComponent(
			JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (value instanceof IDisplayableItem) {
			IDisplayableItem item = (IDisplayableItem) value;

			label.setIcon(item.getIcon());
			label.setText(item.getTitle());
			label.setToolTipText(item.getToolTip());
		} else {
			label.setIcon(ImageFile.BROKEN_IMAGE.getIcon());
			label.setText("Title");
			label.setToolTipText("ToolTip");
		}

		return label;
	}
}
