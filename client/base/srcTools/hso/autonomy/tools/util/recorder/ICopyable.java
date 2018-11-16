package hso.autonomy.tools.util.recorder;

public interface ICopyable<T extends ICopyable<T>> {
	T copy();
}