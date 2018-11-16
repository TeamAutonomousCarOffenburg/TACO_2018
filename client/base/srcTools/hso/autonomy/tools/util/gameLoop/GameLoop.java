package hso.autonomy.tools.util.gameLoop;

public class GameLoop implements Runnable
{
	private final IGameLoopHandler handler;

	private double updateFPS;

	private boolean paused = true;

	private boolean forceUpdate = false;

	private boolean forceDraw = false;

	private boolean stop = false;

	public GameLoop(IGameLoopHandler handler, double updateFPS)
	{
		this.handler = handler;
		this.updateFPS = updateFPS;
	}

	// http://www.java-gaming.org/topics/game-loops/24220/view
	@Override
	public void run()
	{
		final int MAX_UPDATES_BEFORE_DRAW = 5;

		double lastUpdateTime = System.nanoTime();
		double lastRenderTime = System.nanoTime();

		final double DRAW_FPS = 60;
		final double TIME_BETWEEN_DRAW = 1000000000 / DRAW_FPS;

		int lastSecondTime = (int) (lastUpdateTime / 1000000000);

		while (!stop) {
			double now = System.nanoTime();
			int updateCount = 0;

			while (now - lastUpdateTime > getTimeBetweenUpdates() && updateCount < MAX_UPDATES_BEFORE_DRAW) {
				update();
				lastUpdateTime += getTimeBetweenUpdates();
				updateCount++;
			}

			if (now - lastUpdateTime > getTimeBetweenUpdates()) {
				lastUpdateTime = now - getTimeBetweenUpdates();
			}

			draw();
			lastRenderTime = now;

			int thisSecond = (int) (lastUpdateTime / 1000000000);
			if (thisSecond > lastSecondTime) {
				lastSecondTime = thisSecond;
			}

			while (now - lastRenderTime < TIME_BETWEEN_DRAW && now - lastUpdateTime < getTimeBetweenUpdates()) {
				Thread.yield();
				try {
					Thread.sleep(1);
				} catch (Exception e) {
				}
				now = System.nanoTime();
			}
		}
	}

	private void draw()
	{
		if (!paused || forceDraw) {
			handler.draw(paused);
			forceDraw = false;
		}
	}

	private void update()
	{
		if (!paused || forceUpdate) {
			handler.update(paused);
			forceUpdate = false;
		}
	}

	private double getTimeBetweenUpdates()
	{
		return 1000000000 / updateFPS;
	}

	public void setUpdateFPS(double updateFPS)
	{
		this.updateFPS = updateFPS;
	}

	public void pause()
	{
		paused = true;
		forceDraw();
	}

	public boolean isPaused()
	{
		return paused;
	}

	public void togglePaused()
	{
		paused = !paused;
	}

	public void forceUpdate()
	{
		forceUpdate = true;
	}

	public void forceDraw()
	{
		forceDraw = true;
	}

	public void forceCycle()
	{
		forceDraw();
		forceUpdate();
	}

	public void stop()
	{
		stop = true;
	}
}
