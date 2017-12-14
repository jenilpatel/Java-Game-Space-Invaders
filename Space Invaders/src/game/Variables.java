package game;

public interface Variables {
	public int life = 5; //no of lives for defender
	public static final int invader_row = 6; //rows of invaders
	public static final int invader_col = 6; //columns of invaders
	public static final double moveSpeed = 600; //rate at which user can move
	public static final double alien_moveSpeed = 200; //rate invaders go back and forth
	public static final long firingInterval = 500; //rate defender fire
	public static final long firingInterval_foralien = 2000; //rate invaders fire
	public static final int rate_going_down = 80; // rate invader drop down
}
