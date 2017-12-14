package game;

public class Invader extends Obj implements Variables {
	private Game game;
	public boolean is_first = false;
	public int row = -1;
	public int col = 0;
	public boolean passing = true;
	
	public Invader(Game game,String ref,int x,int y,int fir, int row, int col) {
		super(ref,x,y);
		if(row == (fir - 1)) {
			this.is_first = true;
		}
		this.row = row;
		this.col = col;
		this.game = game;
		dx = -alien_moveSpeed;
	}

	public void move(long delta) {
		if ((dx < 0) && (x < 10)) {
			game.updateLogic();
		}
		if ((dx > 0) && (x > 750)) {
			game.updateLogic();
		}
		super.move(delta);
	}
	
	public void doLogic() {
		dx = -dx;
		y += rate_going_down;
		if (y > 570) {
			game.notifyDeath();
		}
	}
	public void collidedWith(Obj other) {
	}
}