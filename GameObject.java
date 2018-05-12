// arrLists: death, vanish STATIC
//making the screen (1800,1000)
// gameOver
// key stuff
//velocity, team, annimation 


/*
 * Created on Nov 6, 2004
 * for BLA Computer Programming
 * (asteroids)
 */

import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;

/**
 * GameObject is really the center of this game engine. This class 
 * maintains a list of all active GameObjects, which can be removed with
 * die() or vanish(). It tells each to step() in each frame, and
 * notifies them of any collision(). It also forwards key events to
 * whichever GameObject has most recently called getKeyFocus().
 *
 * The default step() behavior is to move according to your xVelocity
 * and yVelocity, then deal with going off the side of the screen.
 * Anything that leaves the left or right of the screen wraps around to
 * the other side; if it goes off the bottom or top, offScreen() is
 * called, which calls vanish().
 */
public class GameObject {
    // Array of all existing objects
    private static ArrayList objects = new ArrayList();
    // Array of objects to kill (calling death())
    private static ArrayList toDie = new ArrayList();
    // Array of objects to be removed peacefully
    private static ArrayList toVanish = new ArrayList();
    /**
     * Largest possible x coordinate - you may change this in GameObject.java
     */
    public static final int screenWidth = 1800;
    /**
     * Largest possible y coordinate - you may change this in GameObject.java
     */
    public static final int screenHeight =1000;
    // Whether the game is over
    private static boolean gameIsOver = true;
    // Key interpreter
    private static KeyInterpreter keyInterpreter = new KeyInterpreter();
    
    private double x, y, radius;
    private double xVelocity, yVelocity;
    private int team;
    private Animation animation;
    
    /**
     * Construct a GameObject at the top left of the screen.
     */
    public GameObject() {
        this(0., 0., "invisible");
    }
    
    /**
     * Construct a GameObject at the given location. It will be unmoving,
     * with a radius of 20, on team 0 (that is, not colliding with anything).
     * @param x x coordinate of center of object
     * @param y y coordinate of center of object
     * @param spriteName Name of sprite to use
     */
    public GameObject(double x, double y, String spriteName) {
        super();
        this.x = x;
        this.y = y;
        xVelocity = 0.;
        yVelocity = 0.;
        radius = 20.;
        team = 0;
        this.animation = new Animation(spriteName);
        addObject(this);
    }
    
    /**
     * Construct a new GameObject identical to the given one, but with a
     * different sprite. Useful for making one object appear out of 
     * another.
     * @param o GameObject to duplicate
     * @param spriteName Name of sprite to use.
     */
    public GameObject(GameObject o, String spriteName) {
        super();
        this.x = o.x;
        this.y = o.y;
        this.xVelocity = o.xVelocity;
        this.yVelocity = o.yVelocity;
        this.team = o.team;
        this.radius = o.radius;
        this.animation = new Animation(spriteName);
        addObject(this);
    }
    
    /**
     * Create an exact duplicate of the given object.
     * @param o GameObject to duplicate
     */
    public GameObject(GameObject o) {
        this(o, o.animation.getName());
    }
    
    /**
     * Do whatever this object should do in one timeslice. The default 
     * behavior is to move according to your velocity, and check to see
     * if you have moved off the screen. If you override this, you
     * should almost always call super.step() somewhere in your step().
     */
    public void step() {
        x += xVelocity;
        if(x < 0) {
            offLeft();
        } else if(x > screenWidth) {
            offRight();
        }
        y += yVelocity;
        if(y < 0) {
            offBottom();
        } else if(y > screenHeight) {
            offTop();
        }
    }
    
    /**
     * Calculate how the velocity of two objects should change when they
     * collide with each other, given the mass of each. This method will
     * only do something if the two objects are moving toward one another;
     * therefore, it is safe in a collision to have both objects call
     * bounce() because it will only make the objects bounce once.
     * 
     * The mass may be something you want to store as an instance 
     * variable. Or, you could just code it in that, for example,
     * lasers have no mass and asteroids have mass 1.
     * 
     * @param other Other object to bounce against. Both my velocity and the velocity of that
     *              object will be changed.
     * @param myMass Mass of this object. The units here are arbitrary; all that really matters
     *               is the relative mass of the two.
     * @param otherMass The mass of the other object.
     */
    public void bounce(GameObject other, double myMass, double otherMass) {
        double rx = other.x - x;
        double ry = other.y - y;
        double r2 = rx * rx + ry * ry;
        
        // Find the dot product of the other's velocity with the vector to its center
        // If it is moving away from me, ignore the collision.
        if(rx * (other.xVelocity - xVelocity) + ry * (other.yVelocity - yVelocity) > 0) {
            return;
        }
        
        // Calculate the center-of-mass velocity vector.
        double vx = (getVX() * myMass + other.getVX() * otherMass) / (myMass + otherMass);
        double vy = (getVY() * myMass + other.getVY() * otherMass) / (myMass + otherMass);
        
        // Call "e" a vector perpendicular to r and of the same size.
        // velocity [dot] e
        double d1x = xVelocity * ry - yVelocity * rx;
        // (2 vcm - velocity) [dot] r
        double d1y = (2 * vx - xVelocity) * rx + (2 * vy - yVelocity) * ry;
        
        // Same thing for other
        double d2x = other.xVelocity * ry - other.yVelocity * rx;
        double d2y = (2 * vx - other.xVelocity) * rx + (2 * vy - other.yVelocity) * ry;
        
        // Final velocity is unchanged along the direction parallel to the collision, and 
        // equal to 2 vcm - vi along the other direction.
        xVelocity = (d1x * ry + d1y * rx) / r2;
        yVelocity = (-d1x * rx + d1y * ry) / r2;
        
        other.xVelocity = (d2x * ry + d2y * rx) / r2;
        other.yVelocity = (-d2x * rx + d2y * ry) / r2;
    }
    
    /**
     * Remove this object from the game. Its death() will not be called.
     */
    public void vanish() {
        vanishObject(this);
    }
    
    /**
     * Remove this object from the game, but call its death() first.
     */
    public void die() {
        removeObject(this);
    }
    
    /**
     * Do whatever this object should do when it dies (such as making an explosion).
     * The default is to do nothing.
     */
    public void death() {
    }
    
    /**
     * Return true if this object has die()'ed or vanish()'ed, false otherwise.
     * @return true if this object has die()'ed or vanish()'ed, false otherwise.
     */
    public boolean isDead() {
        return toVanish.contains(this) || toDie.contains(this) || !objects.contains(this);
    }
    
    /**
     * Called, by default, by the other off___() methods - 
     * override this if you want to change all the offscreen
     * behaviors at once. The default behavior is to do nothing.
     */
    public void offScreen() {
    	if(x < 0) {
    		x = screenWidth - (-x % screenWidth);
    	} else if(x > screenWidth) {
    		x %= screenWidth;
    	}
    	if(y < 0) {
    		y = screenHeight - (-y % screenHeight);
    	} else if(y > screenHeight) {
    		y %= screenHeight;
    	}
    }
    
    /**
     * Called when this object has gone off the top of the screen.
     * The default response is to call offScreen().
     */
    public void offTop() {
        offScreen();
    }
    
    /**
     * Called when this object has gone off the bottom of the screen.
     * The default response is to call offScreen().
     */
    public void offBottom() {
        offScreen();
    }
    
    /**
     * Called when this object has gone off the left of the screen.
     * The default response is to call offScreen().
     */
    public void offLeft() {
        offScreen();
    }
    
    /**
     * Called when this object has gone off the right of the screen.
     * The default response is to call offScreen().
     */
    public void offRight() {
        offScreen();
    }
    
    /**
     * Called when this object has collided with another object.
     * The default response is to die().
     * @param other GameObject that you collided with
     */
    public void collision(GameObject other) {
        die();
    }
    
    /**
     * Draw a circle around yourself in the given Graphics, with the
     * given radius and color. You will usually call this method from
     * your draw() method, since that is where you have a Graphics to
     * pass on.
     * @param g Graphics to draw into.
     * @param radius Radius of circle to draw.
     * @param color Color of circle to draw.
     */
    public void drawCircle(Graphics g, double radius, Color color) {
        g.setColor(color);
        g.drawOval((int)(x - radius), screenHeight - (int)(y + radius),
            (int)(2 * radius), (int)(2 * radius));
    }
    
    /**
     * Draw a string at your location in the given Graphics, with the 
     * given color. You will usually call this method from your draw() 
     * method, since that is where you have a Graphics to pass on.
     * @param g Graphics to draw into.
     * @param message String to draw.
     * @param color Color to draw with.
     */
    public void drawString(Graphics g, String message, Color color) {
        g.setColor(color);
        g.drawString(message, (int)x, screenHeight - (int)y);
    }
    
    /**
     * Draw a circle around yourself in the given Graphics, with the 
     * given color. The radius of the circle will be your radius.
     * You will usually call this method from your draw() method, 
     * since that is where you have a Graphics to pass on.
     * @param g Graphics to draw into.
     * @param color Color to draw with.
     */
    public void drawCircle(Graphics g, Color color) {
        drawCircle(g, radius, color);
    }
    
    /**
     * Draw this object in the given Graphics.
     * @param g Graphics to draw into.
     */
    public void draw(Graphics g) {
        if(animation == null) {
            vanish();
        } else {
            animation.draw(g, (int)x, screenHeight - (int)y);
        }
    }
    
    /**
     * Make this object be the one that receives key events.
     */
    public void getKeyFocus() {
        keyInterpreter.setTarget(this);
    }
    
    /**
     * Determine whether this object is the one receiving key events.
     * @return true if this object has key focus, false otherwise.
     */
    public boolean hasKeyFocus() {
        return this == keyInterpreter.getTarget();
    }
    
    /**
     * Return the x coordinate of this object.
     * @return the x coordinate of this object
     */
    public double getX() {
        return x;
    }
    
    /**
     * Return the x velocity of this object.
     * @return the x velocity of this object
     */
    public double getVX() {
        return xVelocity;
    }
    
    /**
     * Return the y coordinate of this object.
     * @return the y coordinate of this object
     */
    public double getY() {
        return y;
    }
    
    /**
     * Return the x velocity of this object.
     * @return the x velocity of this object
     */
    public double getVY() {
        return yVelocity;
    }
    
    /**
     * Return the radius of this object.
     * @return the radius of this object
     */
    public double getRadius() {
        return radius;
    }
    
    /**
     * Return the team of this object.
     * @return the team of this object
     */
    public int getTeam() {
        return team;
    }
    
    /**
     * Return the name of the sprite used by this object.
     * @return the team of this object
     */
    public String getSprite() {
        return animation.getName();
    }
    
    /**
     * Return the name of the current frame of this object's animation.
     * @return the frame name
     */
    public String getFrame() {
        return animation.getFrame();
    }
    
    /**
     * Set the frame of this object's animation to the frame of the 
     * given name, if such a frame exists in the animation
     * @param frame Name of frame to set
     */
    public void setFrame(String frame) {
        animation.setFrame(frame);
    }
    
    /**
     * Set the x coordinate of this object
     * @param x New x coordinate
     */
    public void setX(double x)
    {
        this.x = x;
    }
    
    /**
     * Set the x velocity of this object
     * @param xVelocity New x velocity
     */
    public void setVX(double xVelocity)
    {
        this.xVelocity = xVelocity;
    }
    
    /**
     * Set the y coordinate of this object
     * @param y New y coordinate
     */
    public void setY(double y)
    {
        this.y = y;
    }
    
    /**
     * Set the y velocity of this object
     * @param yVelocity New y velocity
     */
    public void setVY(double yVelocity)
    {
        this.yVelocity = yVelocity;
    }
    
    /**
     * Set the position of this object
     * @param x New x coordinate
     * @param y New y coordinate
     */
    public void setPosition(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Set the velocity vector of this object
     * @param xVelocity New x velocity
     * @param yVelocity New y velocity
     */
    public void setVelocity(double xVelocity, double yVelocity)
    {
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }
      
    /**
     * Set the radius of this object
     * @param radius New radius
     */
    public void setRadius(double radius)
    {
        this.radius = radius;
    }
      
    /**
     * Set the team of this object
     * @param team New team
     */
    public void setTeam(int team)
    {
        this.team = team;
    }
      
    /**
     * Have this object use a different sprite
     * @param spriteName Name of new sprite
     */
    public void setSprite(String spriteName)
    {
        this.animation = new Animation(spriteName);
    }
    /*
     * Add the specified object to the game (make it visible). This is 
     * automatically called by GameObject's constructor.
     */
    private static void addObject(GameObject object) {
        if(!objects.contains(object)) {
            objects.add(object);
        }
    }
    
    /*
     * Remove the specified object from the game, calling death() first.
     * This takes effect before the next step.
     */
    private static void removeObject(GameObject object) {
        if(toDie.contains(object) || toVanish.contains(object)) {
            return;
        }
        toDie.add(object);
        if(object == keyInterpreter.getTarget()) {
            keyInterpreter.setTarget(null);
        }
    }
    
    /*
     * Remove the specified object from the game, peacefully. This takes
     * effect before the next step.
     */
    private static void vanishObject(GameObject object) {
        if(toDie.contains(object)) {
            toDie.remove(object);
            toVanish.add(object);
        } else if(!toVanish.contains(object)) {
            toVanish.add(object);
            if(object == keyInterpreter.getTarget()) {
                keyInterpreter.setTarget(null);
            }
        }
    }
    
    private static void clear() {
        objects.clear();
    }
    
    /**
     * Draw all the existing objects in the given graphics context.
     * @param g Graphics context to draw into
     */
    public static void drawAll(Graphics g) {
        for(int i = 0; i < objects.size(); i++) {
            ((GameObject)objects.get(i)).draw(g);
        }
        // Sometimes it is nice o eb able to see how many objects 
        // are currently existing. This helps to identify bugs
        // where an object doesn't appear or disappear properly.
        // If you want that, uncomment these two lines.
        
        // g.setColor(Color.DARK_GRAY);
        // g.drawString("Object count: " + objects.size(), 2, 20);
    }
    
    /**
     * Called by the applet each frame. Results in each GameObject being
     * told to step(), after which collisions are determined. Finally, 
     * objects that have die()ed or vanish()ed are removed.
     */
    public static void stepAll() {      
        // Step all objects
        for(int i = 0; i < objects.size(); i++) {
            ((GameObject)objects.get(i)).step();
        }
        
        int count = objects.size();
        // Check for collisions
        for(int i = 0; i < count; i++) {
            GameObject a = (GameObject)objects.get(i);
            for(int j = i + 1; j < count; j++) {
                GameObject b = (GameObject)objects.get(j);
                // A pair of loops like this, nested in each other, is something you will
                // see very often in programming. We loop through every object in the game,
                // then for each object, loop over all the objects numbered higher than it
                // (notice how j starts out as i + 1, rather than 0).
                // The result is that we end up considering every pair of two objects.
                
                // For each pair, we want to ask the question, "Are a and b on teams that
                // can interact with each other? If so, are they close enough to touch?"
                if(a.team != 0 && b.team != 0 && (a.team != b.team || a.team == -1)) {
                    double dx = a.x - b.x, dy = a.y - b.y, r = a.radius + b.radius;
                    if(dx * dx + dy * dy < r * r) {
                        a.collision(b);
                        b.collision(a);
                    }
                }
            }
        }
        // Remove any vanished objects. Also remove them from toDie, so that if an object
        // is vanished, it will not explode, even if it simultaneously dies.
        for(int i = 0; i < toVanish.size(); i++) {
            GameObject o = (GameObject)toVanish.get(i);
            objects.remove(o);
            toDie.remove(o);
        }
        toVanish.clear();
        
        // Remove any dead objects, and call their death() method
        for(int i = 0; i < toDie.size(); i++) {
            GameObject o = (GameObject)toDie.get(i);
            objects.remove(o);
            o.death();
        }
        toDie.clear();
    }
    
    public static KeyInterpreter getKeyInterpreter() {
        return keyInterpreter;
    }
     
    /**
     * Called by Asteroids when the user presses the mouse button on 
     * game over.
     */
    public static void newGame() {
        clear();
        gameIsOver = false;
        new Game();
    }
    
    public static boolean isGameOver() {
        return gameIsOver;
        	
    }
    
    public static void gameOver() {
        gameIsOver = true;
    } 
}
