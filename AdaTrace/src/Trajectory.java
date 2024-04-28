import java.util.ArrayList;
import java.util.List;
//用于表示和操作一条轨迹，它由多个点（Point对象）组成。


//构造方法：无参构造方法：创建一个无点的空轨迹。
public class Trajectory {
	
	private List<Point> points;
	
	
	//无参构造方法：创建一个无点的空轨迹。
	public Trajectory() {
		this.points = new ArrayList<Point>();
	}
	
	//重载构造方法：接受两个List<Double>类型的列表，分别代表X和Y坐标，用来创建轨迹。如果列表长度不匹配，则构造方法中有检查机制并将导致失败创建的提示信息。
	public Trajectory(List<Double> xs, List<Double> ys) {
		if (xs.size() != ys.size()) {
			System.out.println("Couldnt create trajectory since xcoordinates size != y.");
			return;
		}
		this.points = new ArrayList<Point>();
		for (int i = 0; i < xs.size(); i++) {
			Point p = new Point(xs.get(i), ys.get(i));
			this.points.add(p);
		}
	}
	
	//添加一个坐标对到轨迹中，通过给定的x和y坐标创建一个新的Point并添加到轨迹中。
	public void addCoordinates(double xc, double yc) {
		Point p = new Point(xc, yc);
		this.points.add(p);
	}
	
	//直接向轨迹中添加一个Point对象。
	public void addPoint (Point inpP) {
		this.points.add(inpP);
	}
	
	//返回轨迹中的点数。
	public int getSize() {
		return points.size();
	}
	
	//返回指定位置点的字符串表示。
	public String getPointString(int pos) {
		return this.points.get(pos).toString();
	}
	
	//获得轨迹中指定位置的点。
	public Point getPoint (int pos) {
		return this.points.get(pos);
	}
	
	//获得坐标
	public double getMinXCoord() {
		double currMin = this.points.get(0).getX();
		for (Point p : this.points) {
			if (p.getX() < currMin) {
				currMin = p.getX();
			}
		}
		return currMin;
	}
	
	public double getMinYCoord() {
		double currMin = this.points.get(0).getY();
		for (Point p : this.points) {
			if (p.getY() < currMin) {
				currMin = p.getY();
			}
		}
		return currMin;
	}
	
	public double getMaxXCoord() {
		double currMax = this.points.get(0).getX();
		for (Point p : this.points) {
			if (p.getX() > currMax) {
				currMax = p.getX();
			}
		}
		return currMax;
	}
	
	public double getMaxYCoord() {
		double currMax = this.points.get(0).getY();
		for (Point p : this.points) {
			if (p.getY() > currMax) {
				currMax = p.getY();
			}
		}
		return currMax;
	}
	
	//计算轨迹上任意两点间的最大距离。
	public double getDiameter() {
		double maxDist = 0.0;
		for (int i = 0; i < this.getSize(); i++) {
			for (int j = i+1; j < this.getSize(); j++) {
				Point p1 = this.points.get(i);
				Point p2 = this.points.get(j);
				double dist = p1.euclideanDistTo(p2);
				if (dist > maxDist)
					maxDist = dist;
			}
		}
		return maxDist;
	}
	
	//计算轨迹上连续点之间的总欧几里得距离。
	public double getDistanceTravelled() {
		double tbr = 0.0;
		for (int i = 0; i < this.points.size()-1; i++) {
			Point thisPoint = this.points.get(i);
			Point nextPoint = this.points.get(i+1);
			tbr = tbr + thisPoint.euclideanDistTo(nextPoint);
		}
		return tbr;
	}
	
	//找出轨迹上在给定的Cell区域（探测区域）中的所有点。
	public List<Point> getSniffedPoints (Cell sniffZone) {
		List<Point> tbr = new ArrayList<Point>();
		int pos = 0;
		while (pos < this.getSize()) {
			if (sniffZone.inCell(this.points.get(pos))) {
				// in the sniff zone now
				int sniffpos = pos;
				while (sniffpos < this.getSize() && 
						sniffZone.inCell(this.points.get(sniffpos))) {
					tbr.add(this.points.get(sniffpos));
					sniffpos++;
				}
				return tbr;
			}
			pos++;
		}
		return tbr;
	}
	
	//计算当前轨迹与另一条轨迹的交点。
	public List<Point> calcIntersectionWith (Trajectory t2) {
		List<Point> intersected = new ArrayList<Point>();
		for (Point p : t2.points) {
			for (int i = 0; i < this.points.size(); i++) {
				Point cand = this.points.get(i);
				if (p.euclideanDistTo(cand) < 0.001) {
					intersected.add(p);
					break;
				}
			}
		}
		return intersected;
	}
	
	//计算当前轨迹与另一轨迹的交点数目。
	public int calcIntersectionCount (Trajectory t2) {
		return this.calcIntersectionWith(t2).size();
	}

	//计算当前轨迹与另一轨迹之间的动态时间弯曲（DTW）距离，这通常用于评估两个轨迹的相似度。
	public double calculateDTWto(Trajectory t2) {
		return Evaluation.calculateDTW(this.points, t2.points);
	}
	
}
