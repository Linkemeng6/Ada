import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.LaplaceDistribution;


public class TripDistribution {
	
	/* NOTE:
	 * Two versions of trip distribution: 
	 * (1) DP-Star version where we find the trip distribution only wrt a single level of grid
	 * (2) AdaTrace CCS version where we divide eps_3 into two portions (alpha*eps_3 and (1-alpha)*eps3),
	 * find the trip distribution wrt first level of grid, find the trip distribution wrt second level of grid,
	 * and finally use OLS to correct the impact of error.
	 * This class implements version #1, as we empirically found it to be better on 2 datasets   
	 */
	
	private List<Cell> cellList;
	private double[][] tripProbabilities;  // cellList x cellList
	
	//构造方法:接受一组GridTrajectory对象（即数据集中的网格轨迹）、Grid对象以及隐私预算，构造方法计算网格单元间的起点和终点间行程概率。
	public TripDistribution(List<GridTrajectory> trajs, Grid g, double privBudget) {
		this.cellList = g.getCells();
		this.tripProbabilities = new double[cellList.size()][cellList.size()];
		
		//行程计数与概率计算:
		//初始化行程计数矩阵tripCounts用于记录每一对起始单元和结束单元间的行程次数，然后为这个计数添加拉普拉斯噪音来满足差分隐私要求，最终这些计数被转换为行程概率tripProbabilities。
		int[][] tripCounts = new int[cellList.size()][cellList.size()];
		for (int i = 0; i < cellList.size(); i++) {
			for (int j = 0; j < cellList.size(); j++) {
				tripCounts[i][j] = 0;
			}
		}
		for (GridTrajectory traj : trajs) {
			Cell startCell = traj.getCells().get(0);
			Cell endCell = traj.getCells().get(traj.getCells().size()-1);
			// find position of startCell and endCell
			int startpos = -1;
			int endpos = -1;
			boolean startfound = false, endfound = false;
			for (int i = 0; i < this.cellList.size(); i++) {
				if (cellList.get(i) == startCell) {
					startpos = i;
					startfound = true;
				}
				if (cellList.get(i) == endCell) {
					endpos = i;
					endfound = true;
				}
				if (startfound && endfound) {
					break;
				}
			}
			if (startfound == false || endfound == false) {
				System.out.println("Unknown cell in a trajectory, couldnt build trip distr.");
				return;
			}
			// increment tripCounts
			tripCounts[startpos][endpos] = tripCounts[startpos][endpos] + 1;
		}
		
		// add noise to tripCounts to satisfy differential privacy
		for (int i = 0; i < cellList.size(); i++) {
			for (int j = 0; j < cellList.size(); j++) {
				int noiseFreeCount = tripCounts[i][j];
				LaplaceDistribution ld = new LaplaceDistribution(0, 1.0/((double)privBudget));
				double noisyCount = noiseFreeCount + ld.sample();
				if (noisyCount < 0)
					noisyCount = 0;
				tripCounts[i][j] = (int) Math.round(noisyCount); // put the noisy value back in
			}
		}
		
		// convert from tripCounts to tripProbabilities
		int noisyNoTraj = 0;
		for (int i = 0; i < cellList.size(); i++) {
			for (int j = 0; j < cellList.size(); j++) {
				noisyNoTraj = noisyNoTraj + tripCounts[i][j];
			}
		}
		//System.out.println("(From trip distn) Noisy # of trajs: " + noisyNoTraj);
		for (int i = 0; i < cellList.size(); i++) {
			for (int j = 0; j < cellList.size(); j++) {
				tripProbabilities[i][j] = ((double) tripCounts[i][j])/((double)noisyNoTraj);
			}
		}
		
		// DEBUG-START
		//Main.printMatrix(tripCounts);
		//Main.printMatrix(tripProbabilities);
		double sum = 0;
		for (int i = 0; i < cellList.size(); i++) {
			for (int j = 0; j < cellList.size(); j++) {
				sum = sum + tripProbabilities[i][j];
			}
		}
		//System.out.println(sum);
		// DEBUG-END	
	}
	
	//行程概率获取方法:返回行程概率矩阵。
	public double[][] getTripProbabilities() {
		return this.tripProbabilities;
	}
	
	//行程概率获取方法:将行程概率矩阵转换为排序后的列表（降序）。
	public List<Double> getTripProbabilitiesAsSortedList() {
		// non-ascending order
		List<Double> sortedList = new ArrayList<Double>();
		for (int i = 0; i < this.tripProbabilities.length; i++) {
			for (int j = 0; j < this.tripProbabilities.length; j++) {
				sortedList.add(tripProbabilities[i][j]);
			}
		}
		Collections.sort(sortedList);
		return sortedList;
	}
	
	//行程抽样方法:随机抽样一对起始和结束网格单元，基于行程概率分布。
	public Cell[] sampleStartEndCells() {
		Cell[] tbr = new Cell[2]; // pos 0 is start cell, pos 1 is end cell
		double randomVal = new Random().nextDouble();
		double totalSoFar = 0;
		for (int i = 0; i < tripProbabilities.length; i++) {
			for (int j = 0; j < tripProbabilities.length; j++) {
				totalSoFar = totalSoFar + tripProbabilities[i][j];
				if (totalSoFar >= randomVal) {
					tbr[0] = cellList.get(i);
					tbr[1] = cellList.get(j);
					return tbr;
				}
			}
		}
		return tbr;
	}
	
	//行程概率列表获取方法:返回一个包含所有行程概率的列表。
	// This function is useful in Evaluation.java
	public List<Double> getTripProbsAsList() {
		List<Double> tbr = new ArrayList<Double>();
		for (int i = 0; i < tripProbabilities.length; i++) {
			for (int j = 0; j < tripProbabilities.length; j++) {
				tbr.add(tripProbabilities[i][j]);
			}
		}
		return tbr;
	}

}
