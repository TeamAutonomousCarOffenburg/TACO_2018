package taco.agent.model.worldmodel.street;

public enum CurveType {
	CURVE_SMALL("CURVE_SMALL", 5),
	CURVE_BIG("CURVE_BIG", 8),
	S_CURVE_BOTTOM("S_CURVE_BOTTOM", 8),
	S_CURVE_TOP("S_CURVE_TOP", 8);

	public final String NAME;
	public final int NUM_OF_MIDDLINES;

	private CurveType(String name, int numOfMiddLines)
	{
		NAME = name;
		NUM_OF_MIDDLINES = numOfMiddLines;
	}
}
