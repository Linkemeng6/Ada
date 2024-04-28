import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Grid {
	
	//成员变量
	Cell[][] topLevelCells; // NxN matrix of cells 一个二维数组，代表一个NxN的单元格矩阵。
	
	private HashMap<Cell, Integer> posInListForm;  // for efficiency 一个HashMap，用于存储单元格在列表中的位置，这样可以提高查找效率。
	
	//构造方法：根据传入的单元格数量cellCount和边界坐标创建网格，并分配给每个单元格一个唯一的名称。
	public Grid (int cellCount, double minX, double maxX, double minY, double maxY) {
		// initialize 
		topLevelCells = new Cell[cellCount][cellCount];
		
		// fill with cells
		double xIncrement = (maxX-minX)/((double) cellCount);
		double yIncrement = (maxY-minY)/((double) cellCount);
		for (int i = 0; i < cellCount; i++) {
			for (int j = 0; j < cellCount; j++) {
				topLevelCells[i][j] = new Cell(minX+xIncrement*i, minY+yIncrement*j,
						xIncrement, yIncrement, ""+i+","+j);
			}
		}
		
		// for efficiency
		posInListForm = new HashMap<Cell, Integer>();
		for (int i = 0; i < this.getCells().size(); i++) {
			posInListForm.put(this.getCells().get(i), i);
		}
	}
	
	//网格操作方法：返回网格每一边上的单元格数量。
	public int getN() {
		return this.topLevelCells.length;
	}
	
	//网格操作方法：分别返回网格中指定单元格的X和Y坐标值。
	public double getXofCell(Cell c1) {
		for (int i = 0; i < topLevelCells.length; i++) {
			for (int j = 0; j < topLevelCells.length; j++) {
				if (topLevelCells[i][j] == c1) {
					return i + 0.0;
				}
			}
		}
		return -1;
	}
	
	public double getYofCell(Cell c1) {
		for (int i = 0; i < topLevelCells.length; i++) {
			for (int j = 0; j < topLevelCells.length; j++) {
				if (topLevelCells[i][j] == c1) {
					return j + 0.0;
				}
			}
		}
		return -1;
	}
	
	//网格操作方法：以列表形式返回所有顶层单元格。
	public List<Cell> getCells() {
		List<Cell> tbR = new ArrayList<Cell>();
		for (int i = 0; i < this.getN(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				tbR.add(topLevelCells[i][j]);
			}
		}
		return tbR;
	}
	
	//网格操作方法：返回指定单元格在列表表示中的位置。
	public int getPosInListForm(Cell c1) {
		return this.posInListForm.get(c1);
	}
	
	//网格操作方法：返回包含所有顶层单元格的二维数组。
	public Cell[][] getCellMatrix() {
		return this.topLevelCells;
	}
	
	//相邻性判定与获取方法：判断两个单元格是否相邻。
	public boolean areAdjacent (Cell c1, Cell c2) {
		int c1x = -1, c1y = -1, c2x = -1, c2y = 1;
		boolean c1found = false, c2found = false;
		// locate cells
		for (int i = 0; i < this.getN(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				if (c1 == topLevelCells[i][j]) {
					c1x = i;
					c1y = j;
					c1found = true;
				}
				if (c2 == topLevelCells[i][j]) {
					c2x = i;
					c2y = j;
					c2found = true;
				}
			}
		}
		if (c1found == false || c2found == false) {
			System.out.println("cells not found, cannot compute adjacency");
			System.out.println("c1: " + c1.getName() + ", c2: " + c2.getName());
			return false;
		}
		// check adjacency conditions: c1 to c2
		if (c2x == c1x+1 && c2y == c1y) 
			return true; // c2 is east of c1
		if (c2x == c1x+1 && c2y == c1y+1)
			return true; // c2 is northeast of c1
		if (c2x == c1x && c2y == c1y+1)
			return true; // c2 is north of c1
		if (c2x == c1x-1 && c2y == c1y+1)
			return true; // c2 is northwest of c1
		if (c2x == c1x-1 && c2y == c1y) 
			return true; // c2 is west of c1
		if (c2x == c1x-1 && c2y == c1y-1)
			return true; // c2 is southwest of c1
		if (c2x == c1x && c2y == c1y-1)
			return true; // c2 is south of c1
		if (c2x == c1x+1 && c2y == c1y-1)
			return true; // c2 is southeast of c1
		// check adjacency conditions: c2 to c1 - symmetric to above
		if (c1x == c2x+1 && c1y == c2y) 
			return true; 
		if (c1x == c2x+1 && c1y == c2y+1)
			return true; 
		if (c1x == c2x && c1y == c2y+1)
			return true; 
		if (c1x == c2x-1 && c1y == c2y+1)
			return true; 
		if (c1x == c2x-1 && c1y == c2y) 
			return true; 
		if (c1x == c2x-1 && c1y == c2y-1)
			return true; 
		if (c1x == c2x && c1y == c2y-1)
			return true; 
		if (c1x == c2x+1 && c1y == c2y-1)
			return true; 
		// no adjacency found?
		return false;		
	}
	
	//相邻性判定与获取方法：返回与指定单元格相邻的所有单元格的列表。
	public List<Cell> getAdjacentCells (Cell c1) {
		List<Cell> cells = this.getCells();
		List<Cell> tbr = new ArrayList<Cell>();
		for (int i = 0; i < cells.size(); i++) {
			if (this.areAdjacent(cells.get(i), c1))
				tbr.add(cells.get(i));
		}
		return tbr;
	}
	
	// 路径规划方法：返回从起点单元格到终点单元格之间的插值路径或者最短路径的单元格列表
	public List<Cell> giveInterpolatedRoute (Cell start, Cell end) {
		// locate cells
		int startx = -1, starty = -1, endx = -1, endy = -1;
		boolean startfound = false, endfound = false;
		for (int i = 0; i < this.getN(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				if (start == topLevelCells[i][j]) {
					startx = i;
					starty = j;
					startfound = true;
				}
				if (end == topLevelCells[i][j]) {
					endx = i;
					endy = j;
					endfound = true;
				}
			}
		}
		if (startfound == false || endfound == false) {
			System.out.println("cells not found, cannot interpolate");
			return null;
		}
		// compute shortest path between them 
		List<Cell> tbr = new ArrayList<Cell>();
		int currx = startx;
		int curry = starty;
		while (true) {
			tbr.add(this.topLevelCells[currx][curry]); // adds startcell, never the endcell
			if (endx > currx) {
				currx = currx+1;
			} else if (endx < currx) {
				currx = currx-1;
			} else {
				currx = currx;
			}
			if (endy > curry) {
				curry = curry + 1;
			} else if (endy < curry) {
				curry = curry - 1;
			} else {
				curry = curry;
			}
			
			if (currx == endx && curry == endy) 
				break;
		}
		return tbr;
	}
	
	//路径规划方法：找到并返回启始单元格和终点单元格之间最短路径的长度。
	public int findShortestLengthBetween (Cell start, Cell end) {
		List<Cell> shortestPath = this.giveInterpolatedRoute(start, end);
		shortestPath.add(end); // because giveInterpolatedRoute doesnt add end cell
		return shortestPath.size();
	}

}
