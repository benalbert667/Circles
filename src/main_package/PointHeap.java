package main_package;

public class PointHeap {
	
	public class Point {
		 int x;
		 int y;
		 int s; //shade
		 
		 public Point(int x, int y, int s) {
			 this.x = x;
			 this.y = y;
			 this.s = s;
		 }
		 
		 public Point copy() {
			 return new Point(x, y, s);
		 }
	}
	
	private Point[] h;
	private int nextRB;
	
	public PointHeap(int n) {
		h = new Point[n];
		nextRB = 0;
	}
	
	public void insert(int x, int y, int s) {
		Point p = new Point(x,y,s);
		
		h[nextRB] = p;
		percup(nextRB);
		nextRB++;
	}
	
	public Point deletemin() {
		nextRB--;
		exchange(0, nextRB);
		
		Point toReturn = h[nextRB].copy();
		h[nextRB] = null;
		
		percdown(0);
		
		return toReturn;
	}
	
	public boolean hasNext(int s) {
		return h[0] != null && h[0].s == s;
	}
	
	private void percup(int i) {
		while(i>0 && h[i].s < h[parent(i)].s) {
			exchange(i, parent(i));
			i = parent(i);
		}
	}
	
	private void percdown(int i) {
		while(!isLeaf(i) && isGreaterThanChild(i)) {
			int j;
			if(h[lChild(i)] == null) {
				j = rChild(i);
			} else if(h[rChild(i)] == null) {
				j = lChild(i);
			} else if (h[lChild(i)].s < h[rChild(i)].s){
				j = lChild(i);
			} else {
				j = rChild(i);
			}
			
			exchange(i, j);
			i = j;
		}
	}
	
	private void exchange(int a, int b) {
		Point save = h[a].copy();
		h[a] = h[b];
		h[b] = save;
	}
	
	private static int parent(int i) {
		return (i-1)/2;
	}
	
	private static int lChild(int i) {
		return i*2 + 1;
	}
	
	private static int rChild(int i) {
		return i*2 + 2;
	}
	
	private boolean isLeaf(int i) {
		if(lChild(i) >= h.length || rChild(i) >= h.length)
			return true;
		else
			return h[lChild(i)] == null && h[rChild(i)] == null;
	}
	
	private boolean isGreaterThanChild(int i) {
		if(h[lChild(i)] == null && h[rChild(i)] != null) {
			return h[rChild(i)].s < h[i].s;
		} else if (h[lChild(i)] != null && h[rChild(i)] == null) {
			return h[lChild(i)].s < h[i].s;
		} else {
			return h[lChild(i)].s < h[i].s || h[rChild(i)].s < h[i].s;
		}
	}
}
