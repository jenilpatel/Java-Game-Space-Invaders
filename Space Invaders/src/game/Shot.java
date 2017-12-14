package game;

public class Shot extends Obj implements Variables{
	private double moveSpeed = -300;
	private Game game;
	public boolean defender_shot = false;
	public boolean invader_shot = false;

	public Shot(Game game,String sprite,int x,int y) {
		super(sprite,x,y);
		
		this.game = game;
		
		dy = moveSpeed;
	}

	public void move(long delta) {
		super.move(delta);
		if(defender_shot) {
			if (y < -100) {
				game.removeEntity(this);
			}
		}
		if(invader_shot) {
			dx = -dx;
			y += 10;
			if (y > 550) {
				game.removeEntity(this);
			}
		}
	}
	public void collidedWith(Obj other) {
		if (other instanceof Invader) {
			if(this.defender_shot) {
			game.removeEntity(this);
			game.notifyAlienKilled(other);
			game.removeEntity(other);
			}
		}
		if(other instanceof Ship) {
			game.removeEntity(this);
			game.notifyShipDies();
			game.removeEntity(other);
		}
		if(other instanceof Barrier) {
			if(this.invader_shot) {
				game.removeEntity(this);
				game.removeEntity(other);
			}
		}
	}
}
