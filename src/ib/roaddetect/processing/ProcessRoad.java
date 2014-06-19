package ib.roaddetect.processing;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class ProcessRoad extends Thread {
	
	private final int CANNY_MIN_THRESH = 1;
	private final int CANNY_MAX_THRES = 100;
	//private final int BW_THRESH = 250;
	private final int HOUGH_THRESH = 65;
	private final int HOUGH_MIN_LINE_LENGHT = 20;
	private final int HOUGH_MIN_LINE_GAP = 50;
	private final int LINE_REJECT = 10;
	
	
	private Composite parent1, parent2;
	private Frame f1, f2;
	private File file;
	private MatOfByte matOfByte1, matOfByte2;
	private ImageIcon image1, image2;
	private JLabel label1, label2;
	private int width, height;
	
	private final Object GUI_INITIALIZATION_MONITOR = new Object();
	private volatile boolean running = false;
	
	public ProcessRoad(Composite p1, Composite p2, File file)
	{
		super();
		running = true;
		this.parent1 = p1;
		this.parent2 = p2;
		
		width = p1.getSize().x;
		height = p1.getSize().y;
		this.file = file;
		f1 = SWT_AWT.new_Frame(parent1);
		f2 = SWT_AWT.new_Frame(parent2);
			
		image1 = new ImageIcon();
		image2 = new ImageIcon();
		
		label1 = new JLabel();
		label2 = new JLabel();
		label1.setIcon(image1);
		label2.setIcon(image2);
		
		f1.add(label1);
		f2.add(label2);
		
		f1.setResizable(false);
		f2.setResizable(false);
		
		matOfByte1 = new MatOfByte();
		matOfByte2 = new MatOfByte();
	}
	
    public void pauseThread() throws InterruptedException {
        running = false;
    }
    public void resumeThread() {
    	synchronized (GUI_INITIALIZATION_MONITOR) {
    		running = true;
		}
    }
    private void checkForPaused() {
    	synchronized (GUI_INITIALIZATION_MONITOR) {
    		while (!running) {
    			try {
    				GUI_INITIALIZATION_MONITOR.wait();
    			} catch (Exception e) {}
    		}
		}
    }
    public boolean isRunning () {
    	return running;
    }
	
	public void run() {
		while (!Thread.currentThread().isInterrupted()) 
		{
			VideoCapture vc = new VideoCapture(file.getAbsolutePath());
	    	
	    	Mat srcMat = new Mat();
			Mat prcMat = new Mat();
			Mat edges = new Mat();
			Mat lines = new Mat();
			
	    	double rho = 1;
	    	double theta = Math.PI/180;
	    	
	    	vc.read(srcMat);

	    	double imWidht = srcMat.size().width;
	    	double imHeight = srcMat.size().height;
	    	
			while (!Thread.currentThread().isInterrupted())
			{
				checkForPaused();
				boolean succes = vc.read(srcMat);
				srcMat.copyTo(prcMat);
				
				if (!succes)
				{
					System.out.println("cannot read!");
					break;
				}
				
				// start of procecsing
				srcMat.copyTo(prcMat);
				Rect roi = new Rect(0, (int)(imHeight/2), (int)imWidht, (int)(imHeight/2));
				
				Mat temp_frame = new Mat(srcMat, roi);

				Imgproc.cvtColor(temp_frame, temp_frame, Imgproc.COLOR_BGR2GRAY);
				
		    	Mat dstGrey = Mat.zeros(temp_frame.size(), temp_frame.type() );
				Imgproc.GaussianBlur(temp_frame, temp_frame, new Size(5, 5), 1);
		    	
				prepareImage(temp_frame, dstGrey, 20);

		    	Imgproc.Canny(dstGrey, edges, CANNY_MIN_THRESH, CANNY_MAX_THRES);
		    	Imgproc.HoughLinesP(edges, lines, rho, theta, HOUGH_THRESH, HOUGH_MIN_LINE_LENGHT, HOUGH_MIN_LINE_GAP);
		    	
		    	processLanes(lines, edges, prcMat);

		    	//stop of processing
				showImages(srcMat, prcMat);
				
			}
		}
    	
	}
	
    private void processLanes(Mat lines, Mat edges, Mat dst) {
    	ArrayList<Lanes> left = new ArrayList<Lanes>();
    	ArrayList<Lanes> right = new ArrayList<Lanes>();
    	
    	double w = edges.size().width;
    	double h = edges.size().height;
    	
    	final double ROAD_HEIGHT = 0.33*h;
    	
    	for (int i = 0; i < lines.cols(); i++) {
			double[] vec = lines.get(0, i);
			if (vec != null)
			{
				double x1 = vec[0],
						y1 = vec[1],
						x2 = vec[2],
						y2 = vec[3];
				
				double dx = x2 - x1;
				double dy = y2 - y1;
				double angle = Math.atan2(dy, dx) * (180/Math.PI);
				
				if (Math.abs(angle) > LINE_REJECT)
				{
					dx = (dx == 0) ? 1 : dx;
					double k = dy/dx;
					double b = y1 - k*x1;
	
					Point start = new Point(x1, y1);
					Point end = new Point(x2, y2);
					left.add(new Lanes(start, end, angle, k, b));
				}
			}
			
		}
    	
    	for (Lanes lanes : right) {
    		lanes.p0.y += h;
    		lanes.p1.y += h;
    		Core.line(dst, lanes.p0, lanes.p1, new Scalar(255, 0, 0), 2);
		}
    	for (Lanes lanes : left) {
    		lanes.p0.y += h;
    		lanes.p1.y += h;
    		Core.line(dst, lanes.p0, lanes.p1, new Scalar(255, 0, 0), 2);
		}

    	
    }

    private void prepareImage (Mat srcGray, Mat dst, int tau)
    {
    	double aux = 0;
    	for (int i = 0; i < srcGray.rows() ; ++i) {
			for (int j = tau; j < srcGray.cols() - tau; ++j) {
				if (srcGray.get(i, j)[0] != 0)
				{
					aux = 2*srcGray.get(i, j)[0];
					aux += -srcGray.get(i, j-tau)[0];
					aux += -srcGray.get(i, j+tau)[0];
					aux += -Math.abs(srcGray.get(i, j-tau)[0] - srcGray.get(i, j+tau)[0]);
					
					aux = (aux < 0) ? 0 : aux;
					aux = (aux > 255) ? 255 : aux;
					double[] data = {aux, 0, 0};
					dst.put(i, j, data);
				}
			}
		}
    }


	private void showImages(Mat src, Mat proc) {

		Highgui.imencode(".jpg", src, matOfByte1);
		Highgui.imencode(".jpg", proc, matOfByte2);
		
		byte[] byteArray1 = matOfByte1.toArray();
		byte[] bytearray2 = matOfByte2.toArray();
		
		BufferedImage bufImage = null;
		BufferedImage bufImage2 = null;
		
		try {
			InputStream in = new ByteArrayInputStream(byteArray1);
			InputStream in2 = new ByteArrayInputStream(bytearray2);
			
			bufImage = ImageIO.read(in);
			bufImage2 = ImageIO.read(in2);
			
			image1.setImage(bufImage);
			image2.setImage(bufImage2);
			f1.pack();
			f2.pack();
			label1.updateUI();
			label2.updateUI();
			f1.setVisible(true);
			f2.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

