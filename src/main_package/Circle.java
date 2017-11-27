package main_package;

//POJO
public class Circle {
	
	private int r; //radius
	private int x; //x of midpoint
	private int y; //y of midpoint
	
	public Circle(int r, int x, int y) {
		this.r = r;
		this.x = x;
		this.y = y;
	}
	
	public Circle(int x, int y) {
		this(-1, x, y);
	}
	
	public int getY() { return y; }
	public int getX() { return x; }
	public int getR() { return r; }
	
	public void setR(int r) { this.r = r; }
}
