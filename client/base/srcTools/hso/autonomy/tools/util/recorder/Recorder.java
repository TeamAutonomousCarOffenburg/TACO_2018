package hso.autonomy.tools.util.recorder;

import java.util.ArrayList;
import java.util.List;

public class Recorder<T extends ICopyable<T>>
{
	protected int currentIndex = 0;

	protected List<T> history = new ArrayList<>();

	private List<Runnable> historyChangedListeners = new ArrayList<>();

	private List<Runnable> indexChangedListeners = new ArrayList<>();

	public Recorder(T initialState)
	{
		record(initialState);
	}

	public void record(T state)
	{
		while (history.size() - 1 > currentIndex) {
			history.remove(history.size() - 1);
		}

		history.add(state.copy());
		currentIndex = history.size() - 1;
		historyChangedListeners.forEach(Runnable::run);
	}

	public T getCurrentRecording()
	{
		return getRecording(currentIndex);
	}

	public T getRecording(int index)
	{
		if (history.isEmpty()) {
			return null;
		}

		// history entries have to be immutable, so make a copy before passing
		// it to the outside...
		return history.get(index).copy();
	}

	public int getSize()
	{
		return history.size();
	}

	public int getCurrentIndex()
	{
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex, boolean triggerUpdate)
	{
		this.currentIndex = currentIndex;

		if (triggerUpdate) {
			indexChangedListeners.forEach(Runnable::run);
		}
	}

	public void clear()
	{
		currentIndex = 0;
		history = new ArrayList<>();
		historyChangedListeners.forEach(Runnable::run);
	}

	public void addHistoryChangedListener(Runnable listener)
	{
		historyChangedListeners.add(listener);
	}

	public void removeHistoryChangedListener(Runnable listener)
	{
		historyChangedListeners.remove(listener);
	}

	public void addIndexChangedListener(Runnable listener)
	{
		indexChangedListeners.add(listener);
	}

	public void removeIndexChangedListener(Runnable listener)
	{
		indexChangedListeners.remove(listener);
	}
}
