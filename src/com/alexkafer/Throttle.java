package com.alexkafer;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class Throttle {
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat original = new Mat();
		
		MatWindow window = new MatWindow("Camera");
		MatWindow threshWindow = new MatWindow("Thresh");
		
		VideoCapture camera = new VideoCapture(0);
		
		JFrame jFrame = new JFrame("Options");
		jFrame.setSize(200, 200);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLayout(new FlowLayout());

		JPanel panel = new JPanel();

		JSlider hueSlider = new JSlider(0, 255, 195);
		panel.add(hueSlider);

		JSlider satSlider = new JSlider(0, 255, 186);
		panel.add(satSlider);

		JSlider valSlider = new JSlider(10, 255, 255);
		panel.add(valSlider);

		JSlider tolSlider = new JSlider(0, 255, 74);
		panel.add(tolSlider);

		jFrame.setContentPane(panel);
		jFrame.setVisible(true);
		
while (true) {
			
			if (!camera.read(original))
				continue;
			
			Mat threshImage = new Mat();

			Imgproc.cvtColor(original, threshImage, Imgproc.COLOR_RGB2HSV);

			int hue = hueSlider.getValue();
			int satu = satSlider.getValue();
			int valu = valSlider.getValue();
			int tol = tolSlider.getValue();
			
			System.out.println("Hue: " + hue + " Sat: " + satu + " Value "
					+ valu + " Tol: " + tol);

			Core.inRange(
					threshImage,
					new Scalar(Math.max(hue - tol, 0), Math.max(satu - tol, 0),
							Math.max(valu - tol, 0)),
					new Scalar(Math.min(hue + tol, 179), Math.min(satu + tol,
							255), Math.min(valu + tol, 255)), threshImage);

			threshWindow.setImage(threshImage);
			

			List<MatOfPoint> particles = new ArrayList<MatOfPoint>();
			List<MatOfPoint2f> matrix = new ArrayList<MatOfPoint2f>();

			Imgproc.findContours(threshImage, particles, new Mat(),
					Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
			
			
			for(int i = 0; i < particles.size(); i++){
			    MatOfPoint2f myPt = new MatOfPoint2f();
			    particles.get(i).convertTo(myPt, CvType.CV_32FC2);
			    matrix.add(myPt);
			}
			
			double biggestY = 0.0;
			double biggestArea = 0.0;
			
			for (int i = particles.size()-1; i > 0; i--) {
				MatOfPoint contour = particles.get(i);
				
				MatOfPoint2f myPt = new MatOfPoint2f();
			    particles.get(i).convertTo(myPt, CvType.CV_32FC2);
			    
				RotatedRect rect = Imgproc.minAreaRect(myPt);
				double area = rect.boundingRect().area();
				
				if (area > 100) {
					Core.rectangle(original, rect.boundingRect().tl(), rect.boundingRect().br(), new Scalar(0, 255, 255));
					
					rect.size = new Size(rect.size.width, rect.size.height+100);
					
					Point points[] = new Point[4];
					rect.points(points);
					
					
					
				    for(int j=0; j<4; ++j){
				        Core.line(original, points[j], points[(j+1)%4], new Scalar(255,255,255));
				    }
				    
				    if (area > biggestArea) {
				    	biggestArea = area;
				    	biggestY = rect.center.y;
				    }
				    
				    Core.fillConvexPoly(original, new MatOfPoint(points), new Scalar(255, 0, 0));
				    
				} else {
					particles.remove(i);
				}
				
			}
			
			double throttle = -1 * (((2.0 * biggestY) / original.height()) - 1);
			
			
			
			
			String dist = "Throttle: " + throttle;
			
			Core.putText(original, dist, new Point(300, 10), Core.FONT_HERSHEY_PLAIN,
					1, new Scalar(0, 255, 0));
			
			// Blue line
			Core.line(original, new Point(0, original.height() / 2), 
					new Point(original.width(), original.height() / 2), 
					new Scalar(255, 0, 0));

			// Update the image on the window
			window.setImage(original);

		}
	}
}
