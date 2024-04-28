import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;
import expoimpl.BigDecimalMath;
import expoimpl.ExponentialMechanism;


public class LengthDistribution {
	
	//private Map<String, Double> meansMap; // mean of *additional # of cells*
	//private Map<String, Double> mediansMap; // median of *additional # of cells*
	
	private Map<String, AbstractIntegerDistribution> distributionTypeMap; //映射不同的行程到相应的整数分布类型，如均匀分布、几何分布或泊松分布。
								// uniform, geometric or poisson
	private Map<String, Integer> maxExtraLengthAllowed; //对于每个行程，记录允许的最大额外长度。
	
	//构造方法：根据输入的GridTrajectory对象、特定的网格Grid对象以及隐私预算进行初始化，并计算每一对 * 起点和终点 * 单元组合的行程长度分布。
	public LengthDistribution (List<GridTrajectory> inputDB, Grid grid, double eps) {
		List<Cell> cells = grid.getCells();
		List<GridTrajectory> trajs = new ArrayList<GridTrajectory>();
		for (GridTrajectory t : inputDB) {
			trajs.add(t);
		}
		//meansMap = new HashMap<String, Double>();
		//mediansMap = new HashMap<String, Double>();
		distributionTypeMap = new HashMap<String, AbstractIntegerDistribution>();
		maxExtraLengthAllowed = new HashMap<String, Integer>();
		//遍历输入数据库中的所有轨迹，并且针对每一对起点和终点单元组合收集额外行程长度。
		for (int i = 0; i < cells.size(); i++) {
			for (int j = 0; j < cells.size(); j++) {
				// find string of trip (currentTrip)
				Cell startCell = cells.get(i);
				Cell endCell = cells.get(j);
				String currentTrip = startCell.getName() + "->" + endCell.getName();
				// find trajectories that have this trip, store in a separate list
				List<GridTrajectory> subsetTrajs = new ArrayList<GridTrajectory>();
				List<Integer> subsetTrajCounts = new ArrayList<Integer>();
				int minCellCount = grid.findShortestLengthBetween(startCell, endCell);
				Iterator<GridTrajectory> iter = trajs.iterator();
				while (iter.hasNext()) {
					GridTrajectory t = iter.next();
					List<Cell> cellsOfTraj = t.getCells();
					if (cellsOfTraj.get(0) == startCell && 
							cellsOfTraj.get(cellsOfTraj.size()-1) == endCell) {
						subsetTrajs.add(t);
						subsetTrajCounts.add(cellsOfTraj.size()-minCellCount);
						iter.remove();
					}
				}
				//System.out.println(startCell + "->" + endCell + ";" + subsetTrajCounts);
				// find max and count 对收集到的额外长度列表进行统计，计算最大长度、总和和计数。
				int MAX = 0;
				int SUM = 0;
				int COUNT = subsetTrajCounts.size();
				for (Integer tcnt : subsetTrajCounts) {
					SUM = SUM + tcnt;
					if (tcnt > MAX)
						MAX = tcnt;
				}
				//如果有足够的数据（即行程长度集合非空或最大长度非零），则使用拉普拉斯分布添加噪音以满足差分隐私，并基于噪音数据计算经过隐私保护的平均值和中位数。
				if (subsetTrajCounts.size() == 0 || MAX == 0) {
					//meansMap.put(currentTrip, 0.0);
					//mediansMap.put(currentTrip, 0.0);
					//distributionTypeMap.put(currentTrip, NODATA);
					distributionTypeMap.put(currentTrip, new UniformIntegerDistribution(0, 0));
					maxExtraLengthAllowed.put(currentTrip, 0);
					continue;
				}
				//System.out.println("max: " + MAX +", count: " + COUNT + ", sum: " + SUM);
				// find differentially private mean of the above list (subsetTrajCounts)
				LaplaceDistribution ld = new LaplaceDistribution(0, ((double)MAX)/((double)eps)); 
				double noisySum = SUM + ld.sample();
				double dpMean = noisySum/((double)COUNT);
				if (dpMean < 0)
					dpMean = 0;
				// find differentially private median of the above list (subsetTrajCounts)
				double dpMedian = findDPmedian(subsetTrajCounts, MAX, eps);
				//System.out.println("dpmean: " + dpMean + ", dpMedian: " + dpMedian);
				// find which distribution this subset (subsetTrajCounts) fits
				//long[] observed = getObservedCountsArray(subsetTrajCounts);
				double[] observed = getObservedProbsArray(subsetTrajCounts);
				double[] uniformExpected = new double[MAX+1]; // should be counts not probs
				double[] geometricExpected = new double[MAX+1]; // counts not probs
				double[] poissonExpected = new double[MAX+1]; // counts not probs
				UniformIntegerDistribution ud = new UniformIntegerDistribution(0, MAX);
				//使用地球移动者距离（EMD）算法，对所观察到的额外行程长度与均匀、几何和泊松分布的期望进行比较，以确定最适合的分布类型。
				double geomP = -1;
				if (dpMean < 1)
					geomP = 1.0;
				else
					geomP = 1.0/dpMean;
				GeometricDistribution gd = new GeometricDistribution(geomP);
				double poissonLambda = -1;
				if (dpMean <= 0)
					poissonLambda = 0.1; // a small value (engineering solution)
				else
					poissonLambda = dpMean;
				PoissonDistribution pd = new PoissonDistribution(poissonLambda);
				for (int iterator = 0; iterator <= MAX; iterator++) {
					uniformExpected[iterator] = ud.probability(iterator);
					if (gd.probability(iterator) != 0)
						geometricExpected[iterator] = gd.probability(iterator);
					else
						geometricExpected[iterator] = 0.000001; // very small prob
					poissonExpected[iterator] = pd.probability(iterator);
					//uniformExpected[iterator] = ud.probability(iterator)*subsetTrajs.size();
					//if (gd.probability(iterator) != 0)
					//	geometricExpected[iterator] = gd.probability(iterator)*subsetTrajs.size();
					//else
					//	geometricExpected[iterator] = 0.01; // a very small value
					//poissonExpected[iterator] = pd.probability(iterator)*subsetTrajs.size();
				}
				// DEBUG-START
				//System.out.println("observed: " + Arrays.toString(observed));
				//System.out.println("uniform: " + Arrays.toString(uniformExpected));
				//System.out.println("geometric: " + Arrays.toString(geometricExpected));
				//System.out.println("poisson: " + Arrays.toString(poissonExpected));
				// DEBUG-END
				// run chi-squared tests with each, find which one is best (lower is better)
				//ChiSquareTest cs = new ChiSquareTest();
				//double uniformX2 = cs.chiSquare(uniformExpected, observed);
				//double geometricX2 = cs.chiSquare(geometricExpected, observed);
				//double poissonX2 = cs.chiSquare(poissonExpected, observed);
				EarthMoversDistance emd = new EarthMoversDistance();
				double uniformX2 = emd.compute(uniformExpected, observed);
				double geometricX2 = emd.compute(geometricExpected, observed);
				double poissonX2 = emd.compute(poissonExpected, observed);
				//int best_distr = this.NODATA;
				if (uniformX2 <= geometricX2 && uniformX2 <= poissonX2) {
					distributionTypeMap.put(currentTrip, ud);
					//System.out.println("Chose uniform.");
				}
				else if (geometricX2 <= uniformX2 && geometricX2 <= poissonX2) {
					distributionTypeMap.put(currentTrip, gd);
					//System.out.println("Chose geometric.");
				}
				else if (poissonX2 <= uniformX2 && poissonX2 <= geometricX2) {
					distributionTypeMap.put(currentTrip, pd);
					//System.out.println("Chose poisson.");
				}	
				else
					System.out.println("Unknown distribution for trip: " + currentTrip);
				maxExtraLengthAllowed.put(currentTrip, (int) Math.round(MAX*1.3));
			}
		}
	}
	
	//对于给定的起点和终点单元格，根据之前确定的分布类型生成一个额外行程长度的样本。
	private long[] getObservedCountsArray(List<Integer> list) {
		int max = 0;
		for (Integer elt : list) {
			if (elt > max)
				max = elt;
		}
		long[] tbr = new long[max+1];
		for (int i = 0; i <= max; i++) {
			int eltct = 0;
			for (Integer t : list) {
				if (i == t) {
					eltct++;
				}
			}
			tbr[i] = eltct;
		}
		return tbr;
	}
	
	private double[] getObservedProbsArray (List<Integer> list) {
		int max = 0;
		for (Integer elt : list) {
			if (elt > max)
				max = elt;
		}
		double[] tbr = new double[max+1];
		for (int i = 0; i <= max; i++) {
			int eltct = 0;
			for (Integer t : list) {
				if (i == t) {
					eltct++;
				}
			}
			tbr[i] = ((double)eltct)/((double)list.size());
		}
		return tbr;
	}
	
	private double findDPmedian (List<Integer> list, int max, double privBudget) {
		
		int[] counts = new int[max+1];
		for (int i = 0; i <= max; i++) {
			int count = 0;
			for (Integer t : list) {
				if (i == t) {
					count++;
				}
			}
			counts[i] = count;
		}
		double countsSum = 0.0;
		for (int i = 0; i <= max; i++) {
			countsSum = countsSum + counts[i];
		}
		//System.out.println(Arrays.toString(counts));
		//System.out.println(countsSum);
		//System.out.println(list.size());
		if (Math.abs(countsSum-list.size()) > 0.1)
			System.out.println("Something wrong with findDPmedian.");
		
		List<String> candidates = new ArrayList<String>();
		List<BigDecimal> probs = new ArrayList<BigDecimal>();
		for (int i = 0; i < max; i++) {
			candidates.add(""+i);
			double rankofI = 0.0;
			for (int citr = 0; citr <= i; citr++) {
				rankofI = rankofI + counts[citr];
			}
			//BigDecimal candProb = new BigDecimal((privBudget * (rankofI - countsSum/2.0)) / 2.0);
			double rankofMedian = countsSum/2.0;
			double scoreofElt = (-1)*Math.abs(rankofI-rankofMedian);
			BigDecimal candProb = new BigDecimal(privBudget*scoreofElt/2.0);
			candProb = BigDecimalMath.pow(new BigDecimal(Math.E), candProb);
			probs.add(candProb);
		}
		String emResult = ExponentialMechanism.getRandomCandidateBD(candidates, probs);
		if (emResult == null)
			return 0;
		else {
			return Integer.parseInt(emResult);
		}
	}
	
	public int getLengthSample (Cell startCell, Cell endCell) {
		String currentTrip = startCell.getName() + "->" + endCell.getName();
		AbstractIntegerDistribution a = distributionTypeMap.get(currentTrip);
		int len = a.sample();
		int maxAllowed = maxExtraLengthAllowed.get(currentTrip);
		if (len > maxAllowed)
			len = maxAllowed;
		return len;
	}
	
}
