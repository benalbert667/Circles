package main_package;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

public class PictureEditor {
	
	private BufferedImage im;
	private int[] packedData;
	private int[][][] pixelData;
	private int h;
	private int w;
	
	public PictureEditor() {
		im = null;
		packedData = null;
		pixelData = null;
		h = 0;
		w = 0;
	}
	
	public PictureEditor(String filename) {
		this();
		
		setImg(filename);

	}
	
	public boolean setImg(String filename) {
		File f = new File(filename);
		try {
			im = ImageIO.read(new FileInputStream(f));
		} catch (IOException e) {
			System.out.println("File \"" + filename + "\" not found");
			return false;
		}
		
		h = im.getHeight();
		w = im.getWidth();
		
		packedData = im.getRGB(0, 0, w, h, null, 0, w);
		
		pixelData = new int[h][w][3];
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				pixelData[row][col][0] = (packedData[(row * w) + col] >> 16) & 0xff;
				pixelData[row][col][1] = (packedData[(row * w) + col] >> 8) & 0xff;
				pixelData[row][col][2] = (packedData[(row * w) + col]) & 0xff;
			}
		}
		
		System.out.println(h + "x" + w);
		return true;
	}
	
	public boolean saveImg(String filename) {
		packPixels();
		
		try {
			ImageIO.write(im, "png", new File(filename));
		} catch (IOException e) {
			System.out.println("File \"" + filename + "\" could not be created");
			return false;
		}
		
		return true;
	}
	
	private void packPixels() {
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				packedData[(row * w) + col] = ((255 & 0xFF) << 24) |
	            ((pixelData[row][col][0] & 0xFF) << 16) |
	            ((pixelData[row][col][1] & 0xFF) << 8)  |
	            ((pixelData[row][col][2] & 0xFF) << 0);
			}
		}
		
		im.setRGB(0, 0, w, h, packedData, 0, w);
	}
	
	public void makeBW() {
		int r, g, b, bw;
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				r = pixelData[row][col][0];
				g = pixelData[row][col][1];
				b = pixelData[row][col][2];
				
				bw = (r + g + b)/3;
				
				pixelData[row][col][0] = bw;
				pixelData[row][col][1] = bw;
				pixelData[row][col][2] = bw;
			}
		}
		
	}
	
	public void binary(Color light, Color dark, int threshold) {
		int ave, r, g, b;
		for(int row = 0; row < h; row++) {
			for(int col = 0; col < w; col++) {
				r = pixelData[row][col][0];
				g = pixelData[row][col][1];
				b = pixelData[row][col][2];
				
				ave = (r + g + b)/3;
				if(ave >= threshold) {
					pixelData[row][col][0] = light.getRed();
					pixelData[row][col][1] = light.getGreen();
					pixelData[row][col][2] = light.getBlue();
				} else {
					pixelData[row][col][0] = dark.getRed();
					pixelData[row][col][1] = dark.getGreen();
					pixelData[row][col][2] = dark.getBlue();
				}
			}
		}
		
	}
	
	public void binary() {
		this.binary(new Color(255, 255, 255), new Color(0, 0, 0), getMedianColor());
	}
	
	private int getMedianColor() {
		int[] temp = new int[h*w];
		int r, g, b;
		for(int row = 0; row < h; row++) {
			for(int col = 0; col < w; col++) {
				r = pixelData[row][col][0];
				g = pixelData[row][col][1];
				b = pixelData[row][col][2];
				temp[(w-1)*row + col] = r + g + b;
			}
		}
		
		Arrays.sort(temp);
		return temp[temp.length/2]/3;
	}
	
	public void makeCircles() {
		long t = System.currentTimeMillis();
		
		makeBW(); //make picture black and white
		
		int circles = 0;
		
		if(h*w > 4000000) {
			
			System.out.print("0%"); //print loading status
		
			circles += makeCircles(section(pixelData, 0, h/4), h/4, w); //1st 4th
		
			System.out.print("\r25%"); //print loading status
		
			circles += makeCircles(section(pixelData, h/4, h/2), h/4, w); //2nd 4th
		
			System.out.print("\r50%"); //print loading status
		
			circles += makeCircles(section(pixelData, h/2, (int)(h*0.75)), h/4, w); //3rd 4th
		
			System.out.print("\r75%"); //print loading status
		
			circles += makeCircles(section(pixelData, (int)(h*0.75), h), h/4, w); //4th 4th
		
			System.out.print("\r100%"); //print loading status
			
		} else {
			
			circles = makeCircles(pixelData, h, w); //pic is small, do not divide into 4ths
			
		}
		
		System.out.printf("\n%d total circles, time to process: %ds\n",
				circles, (System.currentTimeMillis()-t)/1000);
	}
	
	private int makeCircles(int[][][] pixelData, int h, int w) {
		//assumes picture is in black and white
		
		int minRad = 0; //minimum allowed radius of circle (>=0)
		int maxRad = (int)((this.h*this.w)/3534336.0 + (5936/767.0)); //maximum allowed radius
		boolean[][] inCircle = new boolean[h][w]; //used to keep track of where circles can be drawn
		PointHeap ph = new PointHeap(h*w); //heap used to obtain points in increasing shade order
		
		//fill heap and clear pixelData
		for(int i = 0; i < h; i++) {
			for(int j = 0; j < w; j++) {
				ph.insert(j, i, pixelData[i][j][0]);
				pixelData[i][j][0] = 255;
				pixelData[i][j][1] = 255;
				pixelData[i][j][2] = 255;
			}
		}
		
		int r = minRad, rMin, rNext, rSave; //variables used
		int circleCounter = 0; //count circles
		List<PointHeap.Point> points = new LinkedList<>(); //temporary list of points for each shade
		for(int s = 0; s <= 255; s++) {
			r = (int)((maxRad - minRad)*(s/255.0));
			if(r > maxRad) r = maxRad;
			else if(r < minRad) r = minRad;
			
			rMin = (int)(s/63.75); //rMin is 1 - 4
			if(rMin < minRad) rMin = minRad;
			
			//fill temporary list with coordinates of shade s
			points.clear();
			while(ph.hasNext(s)) {
				PointHeap.Point temp = ph.deletemin();
				if(!willOverlap(inCircle, temp.x, temp.y, rMin))
					points.add(temp);
			}
			
			boolean circlesLeft = true && !points.isEmpty();
			while(circlesLeft) {
				circlesLeft = false;
				rNext = rMin;
				rSave = r;
				for(PointHeap.Point p : points) {
					if(!willOverlap(inCircle, p.x, p.y, r)) {
						//circle can be drawn
						drawCircle(pixelData, inCircle, p.x, p.y, r);
						circleCounter++;
					} else {
						//circle is too big to be drawn
						circlesLeft = true;
						
						//get the largest possible radius with which this circle can be drawn
						r = rNext;
						while(r < rSave && !willOverlap(inCircle, p.x, p.y, r)) r++;
						r--;
						
						//set rNext to the radius of the next largest circle that can be drawn
						rNext = Math.max(r, rNext);
						r = rSave;
					}
				}
				if(r == rNext) circlesLeft = false;
				r = rNext; //set r to rNext
			}
		}
		return circleCounter;
	}
	
	private void drawCircle(int[][][] pixelData, boolean[][] arr, int h, int k, int r) {
		//draws a black circle on pixelData, midpoint at (h, k),
		//radius r (might throw an IndexOutOfBoundsException
		//if not called after a willOverlap() check)
		double x1, x2, y1, y2, sqrt;
		
		for(int x = h-r; x <= h+r; x++) {
			//Top and bottom of circle
			sqrt = Math.sqrt((-1*h*h) + (2*h*x) + (r*r) - (x*x));
			y1 = Math.round(k - sqrt);
			y2 = Math.round(k + sqrt);
			
			for(int i = 0; i < 3; i++) {
				pixelData[(int)y1][x][i] = 0;
				pixelData[(int)y2][x][i] = 0;
			}
			
			for(int i = (int)y1; i <= (int)y2; i++)
				arr[i][x] = true; //fill in boolean array
		}
		
		for(int y = k-r; y <= k+r; y++) {
			//Left and right of circle
			sqrt = Math.sqrt((-1*k*k) + (2*k*y) + (r*r) - (y*y));
			x1 = Math.round(h - sqrt);
			x2 = Math.round(h + sqrt);
			
			for(int i = 0; i < 3; i++) {
				pixelData[y][(int)x1][i] = 0;
				pixelData[y][(int)x2][i] = 0;
			}
		}
	}
	
	private boolean willOverlap(boolean[][] arr, int h, int k, int r) {
		//returns true if the circle with midpoint (h, k) and radius r will
		//overlap or contain any other circle on arr or overlap with the
		//outer bounds of the picture, else returns false
		if(h < r || k < r || arr[0].length <= r+h || arr.length <= r+k) { return true; }
		
		double y1, y2, sqrt;
		
		for(int x = h-r; x <= h+r; x++) {
			sqrt = Math.sqrt((-1*h*h) + (2*h*x) + (r*r) - (x*x));
			y1 = Math.round(k - sqrt);
			y2 = Math.round(k + sqrt);
			
			for(int y = (int)y1; y <= (int)y2; y++) {
				if(arr[y][x]) return true;
			}
		}
		
		return false;
	}
	
	private static int[][][] section(int[][][] arr, int f, int t) {
		int[][][] toReturn = new int[t-f][][];
		for(int i = f; i < t; i++) {
			toReturn[i-f] = arr[i];
		}
		return toReturn;
	}
}
