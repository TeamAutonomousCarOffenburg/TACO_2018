package hso.autonomy.tools.util.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SearchableList<T> extends JPanel
{
	private final SelectAction<T> selectAction;

	private final ItemDescriptor<T> itemDescriptor;

	private List<T> items;

	private JList<T> itemList;

	private JScrollPane itemScrollPane;

	private DefaultListModel<T> itemListModel;

	private JTextField filterTextField;

	public SearchableList(Collection<T> items, SelectAction<T> selectAction, ItemDescriptor<T> itemDescriptor)
	{
		super();
		this.selectAction = selectAction;
		this.itemDescriptor = itemDescriptor;

		initializeComponents();
		setItems(items);
	}

	public T getSelectedItem()
	{
		return itemList.getSelectedValue();
	}

	public void setFilterText(String filter)
	{
		filterTextField.setText(filter);
		refreshList(filter);
	}

	public void requestFilterTextFieldFocus()
	{
		filterTextField.requestFocus();
		refreshList(filterTextField.getText());
	}

	public void setItems(Collection<T> items)
	{
		this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<T>();
		sortItems();
		refreshList();
		setEnabled(isEnabled());
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		filterTextField.setEnabled(enabled);
		itemScrollPane.setEnabled(enabled);
		itemList.setEnabled(enabled);
	}

	private void sortItems()
	{
		Collections.sort(
				items, (item1, item2) -> itemDescriptor.getName(item1).compareTo(itemDescriptor.getName(item2)));
	}

	private void initializeComponents()
	{
		createList();
		createFilterTextField();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, filterTextField.getPreferredSize().height));
		add(filterTextField);

		add(Box.createVerticalGlue());
		add(Box.createVerticalStrut(5));

		itemScrollPane = new JScrollPane(itemList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		itemScrollPane.setMinimumSize(new Dimension(0, 300));
		itemScrollPane.setPreferredSize(new Dimension(0, 300));
		add(itemScrollPane);
	}

	private void createList()
	{
		itemListModel = new DefaultListModel<>();
		itemList = new JList<>(itemListModel);
		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemList.setLayoutOrientation(JList.VERTICAL);
		itemList.setVisibleRowCount(-1);
		itemList.setCellRenderer(new SearchableListCellRenderer());
		itemList.setFocusable(false);
		itemList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() > 1) {
					selectAction.select(getSelectedItem());
				}
			}
		});
	}

	private void createFilterTextField()
	{
		filterTextField = new JTextField();
		filterTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
					itemList.dispatchEvent(e);
				}
			}
		});

		filterTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				refreshList(filterTextField.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				refreshList(filterTextField.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				refreshList(filterTextField.getText());
			}
		});

		filterTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					selectAction.select(getSelectedItem());
				}
			}
		});
	}

	private void refreshList()
	{
		refreshList(null);
	}

	private void refreshList(String filter)
	{
		itemListModel.removeAllElements();
		List<T> filteredItems = items.stream().filter(item -> matches(item, filter)).collect(Collectors.toList());

		Collections.sort(filteredItems, new MatchQualityComparator(filterTextField.getText()));
		for (T item : filteredItems) {
			itemListModel.addElement(item);
		}
		itemList.setSelectedIndex(0);
	}

	private boolean matches(T item, String filter)
	{
		return filter == null || containsIgnoreCase(itemDescriptor.getName(item), filter);
	}

	private boolean containsIgnoreCase(String s1, String s2)
	{
		return s1.toLowerCase().contains(s2.toLowerCase());
	}

	@FunctionalInterface
	public interface SelectAction<T> {
		void select(T item);
	}

	public interface ItemDescriptor<T> {
		String getName(T item);

		Icon getIcon(T item);
	}

	private class SearchableListCellRenderer extends JPanel implements ListCellRenderer<T>
	{
		private final JLabel label;

		public SearchableListCellRenderer()
		{
			setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
			setLayout(new BorderLayout());
			label = new JLabel();
			add(label, BorderLayout.CENTER);
		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus)
		{
			label.setText(itemDescriptor.getName(value));
			label.setIcon(itemDescriptor.getIcon(value));

			if (isSelected && list.isEnabled()) {
				label.setForeground(list.getSelectionForeground());
				setBackground(list.getSelectionBackground());
			} else {
				label.setForeground(list.getForeground());
				setBackground(Color.WHITE);
			}

			label.setEnabled(list.isEnabled());
			return this;
		}
	}

	private class MatchQualityComparator implements Comparator<T>
	{
		private String filter;

		public MatchQualityComparator(String filter)
		{
			this.filter = filter.toLowerCase();
		}

		@Override
		public int compare(T item1, T item2)
		{
			return getMatchScore(item2) - getMatchScore(item1);
		}

		private int getMatchScore(T item)
		{
			int score = 0;
			String name = SearchableList.this.itemDescriptor.getName(item).toLowerCase();
			if (name.startsWith(filter)) {
				score++;
				if (name.length() == filter.length()) {
					score++;
				}
			}
			return score;
		}
	}
}
