import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;
//用于生成和评估一个圆形的空间查询。


public class Query {
	
	// CIRCULAR QUERY 

	public double centerX;
	public double centerY;
	public double radius;//分别存储圆形查询的中心点X和Y坐标、半径。
	
	//构造方法：该方法根据传入的坐标范围（minX, maxX, minY, maxY）和文件名（filename）生成查询对象。查询中心点在给定范围内随机生成，半径大小根据数据集和观察值自适应地设置。
	public Query (double minX, double maxX, double minY, double maxY, String filename) {
		// generate uniformly random query center between (minX-maxX) and (minY-maxY)
		Random r = new Random();
		this.centerX = r.nextDouble()*(maxX-minX)+minX; //<-- for uniform
		this.centerY = r.nextDouble()*(maxY-minY)+minY; //<-- for uniform
		double smallerDiff = -1.0;
		double largerDiff = -1.0;
		if (maxX-minX > maxY-minY) {
			smallerDiff = maxY-minY;
			largerDiff = maxX-minX;
		}
		else {
			smallerDiff = maxX-minX;
			largerDiff = maxY-minY;
		}
		
		// set the radius of query (dataset-dependent, based on observations)
		if (filename.contains("brinkhoff")) {
			this.radius = largerDiff/2.8;
		} else if (filename.contains("taxi")) {
			this.radius = largerDiff/3.0;
		} else if (filename.contains("geolife")) {
			this.radius = largerDiff/2.5;
		} else {
			//System.out.println("File not recognized when generating queries..");
			this.radius = largerDiff/3.0;
		}
		
	}
	
	//判断一条轨迹（Trajectory）是否与查询区域相交。这涵盖了轨迹的点是否处于查询圆内，以及轨迹的线段是否与查询圆相交。
	private boolean evaluateQueryOnTraj(Trajectory t) {
		for (int i = 0; i < t.getSize()-1; i++) {
			double p1x = t.getPoint(i).getX();
			double p2x = t.getPoint(i+1).getX();
			double p1y = t.getPoint(i).getY();
			double p2y = t.getPoint(i+1).getY();
			if (Query.eucDistance(p1x, p1y, centerX, centerY) <= radius)
				return true;
			if (Query.eucDistance(p2x, p2y, centerX, centerY) <= radius) 
				return true;
			if (p1x == p2x && p1y == p2y) { return true; }  
			else if (Query.distanceToSegment(centerX, centerY, p1x, p1y, p2x, p2y) <= radius) { 
				//System.out.println("line in circle");
				return true;
			}
		}
		return false;
	}
	
	//数据库评估方法：在轨迹数据集上执行查询，并返回符合查询条件的轨迹数量。
	public int evaluateQueryOnDatabase(List<Trajectory> dataset) {
		int tbr = 0;
		for (Trajectory t : dataset) {
			if (this.evaluateQueryOnTraj(t) == true)
				tbr++;
		}
		return tbr;
	}
	
	
	// HELPER FUNCTIONS:
	
	//计算两点间的欧几里得距离。
	public static double eucDistance (double p1x, double p1y, double p2x, double p2y) {
		return Math.sqrt((p2x-p1x)*(p2x-p1x) + (p2y-p1y)*(p2y-p1y));
	}
	
	//计算一个点到线段的最短距离，有两个重载方法，允许不同形式的参数输入。
	public static double distanceToSegment(double x3, double y3, double x1, double y1, 
			double x2, double y2) {
		final Point2D p3 = new Point2D.Double(x3, y3);
		final Point2D p1 = new Point2D.Double(x1, y1);
		final Point2D p2 = new Point2D.Double(x2, y2);
		return distanceToSegment(p1, p2, p3);
	}

	
	 public static double distanceToSegment(Point2D p1, Point2D p2, Point2D p3) {
		 final double xDelta = p2.getX() - p1.getX();
		 final double yDelta = p2.getY() - p1.getY();
		 if ((xDelta == 0) && (yDelta == 0)) {
		    throw new IllegalArgumentException("p1 and p2 cannot be the same point");
		 }
		 final double u = ((p3.getX() - p1.getX()) * xDelta + 
				 (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);
			 final Point2D closestPoint;
		 if (u < 0) {
		    closestPoint = p1;
		 } else if (u > 1) {
		    closestPoint = p2;
		 } else {
		    closestPoint = new Point2D.Double(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
		 }
		 return closestPoint.distance(p3);
	 }
	
	 
	 
	 /*
		// RECTANGULAR QUERY
		private Rectangle2D queryRegion;
		
		// Parameters to the constructor are the boundaries of dataset
		public Query(double minX, double maxX, double minY, double maxY) {
			
			Random r = new Random();
			double upperLeftX = r.nextDouble()*(maxX-minX) + minX;
			double upperLeftY = r.nextDouble()*(maxY-minY) + minY;
			
			// set query boundaries
			double xdiff = maxX-minX;
			double ydiff = maxY-minY;
			double width = xdiff/2.0;
			double height = ydiff/2.0;
			
			this.queryRegion = new Rectangle2D.Double(upperLeftX, upperLeftY, width, height);
			
			// Emre's note: This strategy of choosing the upper left corner yields to very bad
			// results, due to queries with very low counts
			
		}
		
		private boolean evaluateQueryOnTraj (Trajectory t) {
			for (int i = 0; i < t.getSize()-1; i++) {
				double p1x = t.getPoint(i).getX();
				double p2x = t.getPoint(i+1).getX();
				double p1y = t.getPoint(i).getY();
				double p2y = t.getPoint(i+1).getY();
				// if trajectory has a point in the rectangle, return true
				if (queryRegion.contains(p1x, p1y))
					return true;
				if (queryRegion.contains(p2x, p2y))
					return true;
				// if trajectory's line segment between p1-p2 passes thru rectangle, return true
				if (queryRegion.intersectsLine(p1x, p1y, p2x, p2y))
					return true;
			}
			return false;
		}
		
		public int evaluateQueryOnDatabase(List<Trajectory> dataset) {
			int tbr = 0;
			for (Trajectory t : dataset) {
				if (this.evaluateQueryOnTraj(t) == true)
					tbr++;
			}
			return tbr;
		}
	*/	
	 
}
