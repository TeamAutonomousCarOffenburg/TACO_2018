package hso.autonomy.tools.developer.util.swing;

import java.util.List;

@FunctionalInterface
public interface IStringSelectorDialogListener {
	void selectStrings(List<String> strings);
}
