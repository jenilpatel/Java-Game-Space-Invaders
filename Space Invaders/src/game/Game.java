package game;

import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Game extends Canvas implements Variables{
	private boolean waiting = true;
	private boolean leftPressed = false;
	private boolean rightPressed = false;
	private boolean firePressed = false;
	private boolean logicRequiredThisLoop = false;
	private AudioInputStream audioInputStream;
    private Clip starter = create_audio("src/game/sound/background.wav");
    private Clip game_over = create_audio("src/game/sound/GameOver.wav");
    private Clip shots = create_audio("src/game/sound/bullet.wav");
    private Clip bomb = create_audio("src/game/sound/bomb.wav");
    private int score = 0;
	private boolean already_collided = false;
	private long wait_after_death = 0;
	private long interval_after_death = 1000;
	private String score_message = "Score : " + score;
	public int lives = life;
	private String life_message = "Life : " + lives;
	private BufferStrategy strategy;
	private boolean gameRunning = true;
	private ArrayList list_of_obj = new ArrayList();
	private ArrayList removeList = new ArrayList();
	private Obj ship;
	private long lastFire = 0;
	private long lastFire_alien = 0;
	private int alienCount;	
	private String message = "";
	
	public static void main(String[] args) {
		Game spaceInvader = new Game();
		spaceInvader.gameLoop();

	}
	public Game() {
		JFrame frame = new JFrame("Space Invaders");		
		JPanel panel = (JPanel) frame.getContentPane();
		panel.setPreferredSize(new Dimension(800,600));
		panel.setLayout(null);		
		setBounds(0,0,800,600);
		panel.add(this);		
		setIgnoreRepaint(true);		
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(new KeyInputHandler());
		requestFocus();
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		GetAllObj();
	}
	private void startGame() {
		game_over.close();
		list_of_obj.clear();
		GetAllObj();	
		leftPressed = false;
		rightPressed = false;
		firePressed = false;
		score = 0;
		lives = life;
		wait_after_death = System.currentTimeMillis();
		starter = create_audio("src/game/sound/background.wav");
	    game_over = create_audio("src/game/sound/GameOver.wav");
	    bomb.stop();
		starter.start();
	}
	public void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();		
		while (gameRunning) {
			long delta = System.currentTimeMillis() - lastLoopTime;
			lastLoopTime = System.currentTimeMillis();
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.setColor(Color.black);
			g.fillRect(0,0,800,600);
			if (!waiting) {
				for (int i=0;i<list_of_obj.size();i++) {
					Obj entity = (Obj) list_of_obj.get(i);	
					entity.move(delta);
				}
			}			
			for (int i=0;i<list_of_obj.size();i++) {
				Obj entity = (Obj) list_of_obj.get(i);
					entity.draw(g);
			}
			already_collided = false;
			for (int p=0;p<list_of_obj.size();p++) {
				for (int s=p+1;s<list_of_obj.size();s++) {
					Obj me = (Obj) list_of_obj.get(p);
					Obj him = (Obj) list_of_obj.get(s);
					if (me.collidesWith(him)) {
						if(!already_collided) {
							me.collidedWith(him);
							him.collidedWith(me);
						}
						if((me instanceof Ship && him instanceof Shot) || (him instanceof Ship && me instanceof Shot)) {
							already_collided = true;
						}
					}
				}
			}	
			list_of_obj.removeAll(removeList);
			removeList.clear();
			if (logicRequiredThisLoop) {
				for (int i=0;i<list_of_obj.size();i++) {
					Obj entity = (Obj) list_of_obj.get(i);
					entity.doLogic();
				}
				
				logicRequiredThisLoop = false;
			}
			if (waiting) {
				if(message == "Game Over!") {
					bomb.close();
					starter.stop();
					game_over.start();
				}
					bomb.stop();
				g.setColor(Color.white);
				g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);
				g.drawString("Press Enter",(800-g.getFontMetrics().stringWidth("Press Enter"))/2,300);
				score_message = "Score : " + score;
				g.drawString(score_message,(600-g.getFontMetrics().stringWidth(score_message)),590);
				life_message = "Life : " + lives;
				g.drawString(life_message,(200-g.getFontMetrics().stringWidth(life_message)),590);
			}
			else {
				g.setColor(Color.white);
				score_message = "Score : " + score;
				g.drawString(score_message,(600-g.getFontMetrics().stringWidth(score_message)),590);
				life_message = "Life : " + lives;
				g.drawString(life_message,(200-g.getFontMetrics().stringWidth(life_message)),590);
			}
			g.dispose();
			strategy.show();
			ship.setHorizontalMovement(0);
			if ((leftPressed) && (!rightPressed)) {
				ship.setHorizontalMovement(-moveSpeed);
			} else if ((rightPressed) && (!leftPressed)) {
				ship.setHorizontalMovement(moveSpeed);
			}			
			if (firePressed) {
				defender_fire();
			}
			alien_fire();
			try { Thread.sleep(10); } catch (Exception e) {}
		}
	}
	Clip create_audio(String filename) {
		Clip temp;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(new File(filename).getAbsoluteFile());
			temp = AudioSystem.getClip();
			temp.open(audioInputStream);
			return temp;
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		return null;
	}
	private void GetAllObj() {
		ship = new Ship(this,"game/images/ship.gif",370,550);
		list_of_obj.add(ship);		
		alienCount = 0;
		for (int row=0;row<invader_row;row++) {
			for (int col=0;col<invader_col;col++) {
				Obj alien = new Invader(this,"game/images/invader.jpeg",100+(col*40),(40)+row*40, invader_row, row, col);
				list_of_obj.add(alien);
				alienCount++;
			}
		}
		for (int row=0;row<8;row++) {
			for (int col=0;col<24;col++) {
				Obj barrier = new Barrier("game/images/rect.png",100+(col*4),400+(4)+row*4);
				list_of_obj.add(barrier);
			}
		}
		for (int row=0;row<8;row++) {
			for (int col=0;col<24;col++) {
				Obj barrier = new Barrier("game/images/rect.png",350+(col*4),400+(4)+row*4);
				list_of_obj.add(barrier);
			}
		}
		for (int row=0;row<8;row++) {
			for (int col=0;col<24;col++) {
				Obj barrier = new Barrier("game/images/rect.png",600+(col*4),400+(4)+row*4);
				list_of_obj.add(barrier);
			}
		}
	}
	public void updateLogic() {
		logicRequiredThisLoop = true;
	}
	public void removeEntity(Obj entity) {
		removeList.add(entity);
	}
	
	public void notifyDeath() {
		message = "Game Over!";
		waiting = true;
		starter.stop();
		bomb.close();
	}
	public void notifyWin() {
		message = "You Win!!!!";
		waiting = true;
	}
	
	public void notifyAlienKilled(Obj entity) {
		alienCount--;
		score++;
		if (alienCount == 0) {
			notifyWin();
		}	
			Invader remove = (Invader) entity;
			for (int i=0;i<list_of_obj.size();i++) {
				Obj new_entity = (Obj) list_of_obj.get(i);
				if (new_entity instanceof Invader) {
					Invader ali = (Invader) new_entity;
					if((ali.row == (remove.row - 1))&&(ali.col == remove.col)) {
						ali.is_first = true;
					}
				}
			}
	}
	
	public void notifyShipDies() {
		lives--;
		if(lives == 0) {
			notifyDeath();
		}
		else {
			ship = new Ship(this,"game/images/ship.gif",370,550);
			list_of_obj.add(ship);	
			wait_after_death = System.currentTimeMillis();
		}
	}
	public void defender_fire() {
		if (System.currentTimeMillis() - lastFire < firingInterval) {
			return;
		}		
		shots.setMicrosecondPosition(0);
		lastFire = System.currentTimeMillis();
		Shot shot = new Shot(this,"game/images/shot.gif",ship.getX()+10,ship.getY()-30);
		shot.defender_shot = true;
		list_of_obj.add(shot);
		shots.start();
	}
	private void alien_fire() {
		if ((System.currentTimeMillis() - wait_after_death < interval_after_death)&&(lives != 0)) {
			return;
		}
		else {
		if (System.currentTimeMillis() - lastFire_alien < firingInterval_foralien) {
			return;
		}		
		lastFire_alien = System.currentTimeMillis();
		for (int i=0;i<list_of_obj.size();i++) {
			Obj entity = (Obj) list_of_obj.get(i);
			if (entity instanceof Invader) {
				Invader ali = (Invader) entity;
				if(ali.is_first) {
					bomb.setMicrosecondPosition(0);
					Shot shot = new Shot(this,"game/images/shot.gif",ali.getX()+12,ali.getY()+40);
					shot.invader_shot = true;
					list_of_obj.add(shot);
					bomb.start();
				}
			}
		}
		}
		
	}
	public class KeyInputHandler extends KeyAdapter {
		private int pressCount = 1;
		public void keyPressed(KeyEvent e) {
			if (waiting) {
				gameRunning = true;
				return;
			}		
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				firePressed = true;
			}
		} 
		
		public void keyReleased(KeyEvent e) {
			if (waiting) {
				gameRunning = true;
				return;
			}
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				firePressed = false;
			}
		}
		public void keyTyped(KeyEvent e) {
			if (waiting) {
				if (pressCount == 1) {
					waiting = false;
					startGame();
					pressCount = 0;
					gameRunning = true;
				} else {
					pressCount++;
				}
			}
			if (e.getKeyChar() == 27) {
				System.exit(0);
			}
		}
	}
	
}
