package game;

public class Ship extends Obj implements Variables{
	private Game game;

	public Ship(Game game,String ref,int x,int y) {
		super(ref,x,y);
		
		this.game = game;
	}
	public void move(long delta) {
		if ((dx < 0) && (x < 10)) {
			return;
		}
		if ((dx > 0) && (x > 750)) {
			return;
		}
		
		super.move(delta);
	}
	
	public void collidedWith(Obj other) {
		if (other instanceof Invader) {
			game.lives = 0;
			game.notifyDeath();
		}
	}
}
