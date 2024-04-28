
public class Point {
	
	private double xCoord; // longitude
	private double yCoord; // latitude
	
	//构造方法：通过传入的x和y坐标值来初始化一个点。
	public Point(double xc, double yc) {
		this.xCoord = xc;
		this.yCoord = yc;
	}
	
	//获得当前点的x和y坐标
	public double getX() {
		return this.xCoord;
	}
	
	public double getY() {
		return this.yCoord;
	}
	
	//返回表示当前点坐标的字符串，格式为"(x,y)"。
	public String toString() {
		return "(" + this.xCoord + "," + this.yCoord + ")";
	}
	
	//比较当前点与另一个点的坐标是否完全相同，即是否代表同一个地理位置。
	public boolean isEqualTo(Point p2) {
		if (this.getX() != p2.getX())
			return false;
		if (this.getY() != p2.getY())
			return false;
		return true;
	}
	
	//设置当前点的x和y坐标。
	public void setX (double xc) {
		this.xCoord = xc;
	}
	
	public void setY (double yc) {
		this.yCoord = yc;
	}
	
	//计算当前点与另一个点之间的欧几里得距离。
	public double euclideanDistTo(Point p2) {
		double sum = (this.getY()-p2.getY())*(this.getY()-p2.getY()) +
				(this.getX()-p2.getX())*(this.getX()-p2.getX());
		return Math.sqrt(sum);
	}



}
