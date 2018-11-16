package hso.autonomy.tools.util.imageBuffer;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public enum ImageFile {
	MAGMA_LOGO(Directory.LOGO, "magma"),
	TACO_LOGO(Directory.LOGO, "taco2"),

	BROKEN_IMAGE(Directory.GENERAL, "brokenImage"),
	INFO(Directory.GENERAL, "info"),
	REFRESH(Directory.GENERAL, "refresh"),
	REFRESH_RED(Directory.GENERAL, "refreshRed"),
	OPEN_FILE(Directory.GENERAL, "openFile"),
	SAVE(Directory.GENERAL, "save"),
	SAVE_AS(Directory.GENERAL, "saveAs"),
	ADD(Directory.GENERAL, "add"),
	REMOVE(Directory.GENERAL, "remove"),
	EXECUTE(Directory.GENERAL, "execute"),
	CONNECT(Directory.GENERAL, "connect"),
	DISCONNECT(Directory.GENERAL, "disconnect"),
	UNDO(Directory.GENERAL, "undo"),
	REDO(Directory.GENERAL, "redo"),
	DOWN(Directory.GENERAL, "down"),
	UP(Directory.GENERAL, "up"),
	LED_OFF(Directory.GENERAL, "led_off"),
	LED_GREEN(Directory.GENERAL, "led_green"),
	LED_RED(Directory.GENERAL, "led_red"),
	GEAR(Directory.GENERAL, "gear"),
	CHEVRON_LEFT(Directory.GENERAL, "chevron-left"),
	CHEVRON_RIGHT(Directory.GENERAL, "chevron-right"),
	WRENCH(Directory.GENERAL, "wrench"),
	BRUSH(Directory.GENERAL, "brush"),
	HASHTAG(Directory.GENERAL, "hashtag"),
	LINK(Directory.GENERAL, "link"),
	INFO_CIRCLE(Directory.GENERAL, "info-circle"),
	MAP_MARKER(Directory.GENERAL, "map-marker"),
	TRASH(Directory.GENERAL, "trash"),
	PLUG(Directory.GENERAL, "plug"),
	BUG(Directory.GENERAL, "bug"),
	HISTORY(Directory.GENERAL, "history"),

	NAO(Directory.AGENT, "nao"),
	NAO_RED(Directory.AGENT, "naoRed"),
	NAO_GRAY(Directory.AGENT, "naoGray"),
	NAO_RECORDING(Directory.AGENT, "naoRecording"),
	NAO_RECORDING_RED(Directory.AGENT, "naoRecordingRed"),
	NAO_LOG_PLAYER(Directory.AGENT, "naoLogPlayer"),
	NAO_LOG_PLAYER_PAUSING(Directory.AGENT, "naoLogPlayerPausing"),
	NAO_LOG_PLAYER_PLAYING(Directory.AGENT, "naoLogPlayerPlaying"),
	NAO_SIMULATED(Directory.AGENT, "naoSimulated"),

	BEGIN(Directory.MEDIA_PLAYER, "begin"),
	FAST_FORWARD(Directory.MEDIA_PLAYER, "fastForward"),
	NEXT(Directory.MEDIA_PLAYER, "next"),
	PAUSE(Directory.MEDIA_PLAYER, "pause"),
	PLAY(Directory.MEDIA_PLAYER, "play"),
	PREVIOUS(Directory.MEDIA_PLAYER, "previous"),
	RECORD(Directory.MEDIA_PLAYER, "record"),
	REWIND(Directory.MEDIA_PLAYER, "rewind"),
	STOP(Directory.MEDIA_PLAYER, "stop"),
	LOOP(Directory.MEDIA_PLAYER, "loop"),

	BALL(Directory.RENDERERS, "ball.gif"),
	VISIBLE_AREA(Directory.RENDERERS, "visibleArea"),
	DESIRED_POSITION(Directory.RENDERERS, "desiredPosition"),
	BALL_FUTURE_POSITIONS(Directory.RENDERERS, "ballFuturePositions"),
	OWN_PLAYER(Directory.RENDERERS, "ownPlayer"),
	OPPONENT_PLAYER(Directory.RENDERERS, "opponentPlayer"),
	SOCCER_PLAYER_3D(Directory.RENDERERS, "soccerPlayer3D"),
	KICK_OPTION_PROFILER(Directory.RENDERERS, "kickOptionsProfiler"),
	GRID(Directory.RENDERERS, "grid"),
	STABILITY_HULL(Directory.RENDERERS, "stabilityHull"),
	BEHAVIOR_TRAJECTORY(Directory.RENDERERS, "behaviorTrajectory"),
	BODY_TRAJECTORY(Directory.RENDERERS, "bodyTrajectory"),
	ROLE(Directory.RENDERERS, "role"),
	PLAYER_NUMBER(Directory.RENDERERS, "playerNumber"),
	BEHIND_BALL_PENALTY(Directory.RENDERERS, "behindBallPenalty"),
	FOUL(Directory.RENDERERS, "foul"),
	ROAD(Directory.RENDERERS, "road"),
	GRID_BLACK(Directory.RENDERERS, "gridBlack"),
	CAR(Directory.RENDERERS, "car"),
	EYE(Directory.RENDERERS, "eye"),
	BARS(Directory.RENDERERS, "bars"),
	ARROW_RIGHT(Directory.RENDERERS, "arrow-right"),
	RED_CIRCLE(Directory.RENDERERS, "redCircle"),

	BEAM_TO_POSITION(Directory.EDITORS, "beamToPosition"),

	RC_SERVER(Directory.SERVER, "rcServer"),
	RC_SERVER_CONNECTED(Directory.SERVER, "rcServerConnected"),

	TWO_D_FIELD(Directory.VIEWS, "2DField"),
	THREE_D_FIELD(Directory.VIEWS, "3DField"),
	WATCH(Directory.VIEWS, "watch"),
	AGENT_CONTROLLER(Directory.VIEWS, "agentController"),
	EXPLORER(Directory.VIEWS, "explorer"),
	SWEATY(Directory.VIEWS, "sweaty"),
	CHART(Directory.VIEWS, "chart"),
	PROCESSOR(Directory.VIEWS, "processor"),
	MICROCHIP(Directory.VIEWS, "microchip"),
	LAPTOP(Directory.VIEWS, "laptop");

	private final String path;

	ImageFile(Directory directory, String path)
	{
		String ending = "";
		if (!path.contains(".")) {
			ending = ".png";
		}
		this.path = directory.path + "/" + path + ending;
	}

	public Icon getIcon()
	{
		return ImageLoader.getIcon(path);
	}

	public Image getImage()
	{
		return ImageLoader.getImage(path);
	}

	public ImageIcon getImageIcon()
	{
		return new ImageIcon(getImage());
	}

	private enum Directory {
		LOGO("logo"),
		GENERAL("general"),
		AGENT("agent"),
		MEDIA_PLAYER("mediaPlayer"),
		RENDERERS("renderers"),
		EDITORS("editors"),
		SERVER("server"),
		VIEWS("views");

		public final String path;

		Directory(String path)
		{
			this.path = path;
		}
	}
}
