package taco.agent.communication.perception;

import com.google.gson.annotations.SerializedName;

public enum RecognizedObjectType {
	@SerializedName("1")
	PERSON_CROSSING,

	@SerializedName("2")
	PERSON,

	@SerializedName("3")
	CHILD,

	@SerializedName("4")
	STOP_LINE_LEFT,

	@SerializedName("5")
	STOP_LINE_RIGHT,

	@SerializedName("6")
	STOP_LINE_AHEAD,

	@SerializedName("7")
	STOP_LINE_ONCOMING,

	@SerializedName("8")
	CAR,

	@SerializedName("9")
	CROSSWALK,

	@SerializedName("10")
	MIDDLE_LANE,

	@SerializedName("11")
	SIREN_ON,

	@SerializedName("12")
	X_CROSSING,

	@SerializedName("13")
	T_CROSSING_RIGHT,

	@SerializedName("14")
	T_CROSSING_LEFT,

	@SerializedName("15")
	T_CROSSING_BOTH;

	public boolean isPedestrian()
	{
		return this == PERSON || this == PERSON_CROSSING || this == CHILD;
	}

	public boolean isObjectForBackend()
	{
		return isPedestrian() || this == CAR;
	}

	public boolean isEmergencyCar()
	{
		return this == SIREN_ON;
	}

	/**
	 * checks if object has a height or is something on the floor (crossings / lanes)
	 * @return
	 */
	public boolean isObjectWithHeight()
	{
		return isEmergencyCar() || isObjectForBackend();
	}
}
