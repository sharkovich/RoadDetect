package ib.roaddetect.processing;

import org.opencv.core.Point;

public class Lanes {
	public Point p0, p1;
	public int votes;
	public boolean visited, found;
	public double angle, k, b;
	
	public Lanes(Point a, Point b, double angle, double kl, double bl) {
		this.p0 = a;
		this.p1 = b;
		this.angle = angle;
		this.k = kl;
		this.b = bl;
		this.votes = 0;
		this.visited = false;
		this.found = false;	
	}

}
