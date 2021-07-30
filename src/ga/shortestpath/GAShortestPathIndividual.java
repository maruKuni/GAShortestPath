package ga.shortestpath;

import ga.GAIndividual;
import java.util.*;

public class GAShortestPathIndividual implements GAIndividual, Comparable<GAShortestPathIndividual>, Cloneable {
	static double[][] cost;//コスト行列
	static int numOfNodes;//訪問する頂点の数
	static ArrayList<Double> maxOfThisGeneration = new ArrayList<Double>();//適合度計算のスケーリングウィンドウに使う．世代ごとの最大経路を格納．ここでは最大5世代
	static double scalingWindow;
	static int sizeOfScaleWindow = 5;
	static char[] nodes;
	static boolean print=false;
	private int[] path;//経路を表現する．表現型
	private int[] geneCode;//遺伝型
	private double totalCost;
	private double fitness;

	public GAShortestPathIndividual() {
		totalCost = 0;

		path = new int[numOfNodes - 1];//始点＝終点の円順列になるのでnumOfNodes-1個の順列を考えればよい．
		geneCode = new int[numOfNodes - 1];
		for (int i = 0; i < path.length; i++) {
			path[i] = i;
		}
		//乱数で経路をでたらめにする．
		for (int i = 0; i < 20; i++) {
			int rnd1 = (int) (Math.random() * ((double) path.length));
			int rnd2 = (int) (Math.random() * ((double) path.length));
			while (rnd1 == rnd2) {
				rnd1 = (int) (Math.random() * ((double) path.length));
				rnd2 = (int) (Math.random() * ((double) path.length));
			}
			int tmp = path[rnd1];
			path[rnd1] = path[rnd2];
			path[rnd2] = tmp;
		}
		//経路長を計算して，遺伝型をセットする．
		calcTotalCost();
		setGeneCode(null);
	}

	public static void setStaticMember(double[][] cost, int numOfNodes) {
		nodes = new char[numOfNodes];
		GAShortestPathIndividual.cost = cost;
		GAShortestPathIndividual.numOfNodes = numOfNodes;
		for (int i = 0; i < numOfNodes - 1; i++) {
			nodes[i] = (char) ((int) 'A' + i);
		}
	}

	public void update() {
		setGeneRepresentation(null);
		calcTotalCost();
		calcFitness();
	}
	@Override
	public GAShortestPathIndividual clone() {
		GAShortestPathIndividual child = new GAShortestPathIndividual();
		for (int i = 0; i < child.getGeneCode().length; i++) {
			child.geneCode[i] = this.geneCode[i];
		}
		child.setGeneRepresentation(null);
		child.totalCost=this.totalCost;

		return child;
	}

	@Override
	public int compareTo(GAShortestPathIndividual o) {
		return -1 * (int) (this.fitness - o.fitness);
	}

	@Override
	public double calcFitness() {
		//適合度を計算する．
		//過去５世代の最長経路をスケーリングウィンドウに用いる．
		fitness = scalingWindow - totalCost;
		return scalingWindow - totalCost;
	}

	@Override
	public void mutation() {
		//突然変異を起こす．
		//表現型も更新する．
		int codePos = (int) (Math.random() * ((double) path.length));//変異する箇所
		int mutate = (int) (Math.random() * ((double) (path.length - codePos)));
		geneCode[codePos] = mutate;
		setGeneRepresentation(null);
	}

	@Override
	public void setGeneCode(Object obj) {
		//表現型から遺伝型を求める
		ArrayList<Integer> order = new ArrayList<>();
		for (int i = 0; i < path.length; i++)
			order.add(i);
		for (int i = 0; i < path.length; i++) {
			int codePos = order.indexOf(path[i]);
			geneCode[i] = codePos;
			order.remove(codePos);
		}
	}

	@Override
	public int[] getGeneCode() {
		//遺伝型を返す．使うか？これ
		return geneCode;
	}

	@Override
	public void setGeneRepresentation(Object obj) {
		//遺伝型から表現型を作る．
		ArrayList<Integer> order = new ArrayList<Integer>();
		for (int i = 0; i < path.length; i++) {
			order.add(i);
		}
		for (int i = 0; i < path.length; i++) {
			path[i] = order.get(geneCode[i]);
			order.remove(geneCode[i]);
		}
	}

	@Override
	public int[] getGeneRepresentation() {
		//経路の表現型を返す．
		return path;
	}

	public void calcTotalCost() {
		totalCost = 0;
		if(print) {
			System.out.println("totalcost="+totalCost);
		}
		totalCost += cost[0][path[0]+1];
		for (int i = 0; i < path.length - 1; i++) {
			totalCost += cost[path[i]+1][path[i + 1]+1];
			if(print) {
				System.out.println("totalcost="+totalCost);
			}
		}
		totalCost += cost[0][path[path.length - 1]+1];
		if(print) {
			System.out.println("totalcost="+totalCost);
		}
	}

	public double getTotalCost() {
		return totalCost;
	}

	public static void updateMax(ArrayList<GAShortestPathIndividual> individuals) {
		//現在の世代における経路長の最大値を記録．
		//過去5世代における最大値をそれぞれ記録する．
		//コントローラから呼び出されることが前提．
		double max = 0;
		for (int i = 0; i < individuals.size(); i++) {
			if (individuals.get(i).getTotalCost() > max) {
				max = individuals.get(i).getTotalCost();
			}
		}
		maxOfThisGeneration.add(max);
		//記録した最大値の数を５にする．
		if (maxOfThisGeneration.size() > sizeOfScaleWindow) {
			maxOfThisGeneration.remove(0);
		}
		max = 0;
		for (double tmp : maxOfThisGeneration) {
			if (tmp > max) {
				max = tmp;
			}
		}
		scalingWindow = max;
	}

	@Override
	public void setLocus(int pos, Object code) {
		//指定した遺伝子座を書き換える．
		//ここでは遺伝型がint画なのでintにキャストしている
		geneCode[pos] = (int) code;
	}

	public int getHamming(GAShortestPathIndividual indiv) {
		//遺伝型の距離を計算する．
		//ここではハミング距離を用いる．
		int ret = 0;
		int[] tmp = indiv.getGeneCode();
		for (int i = 0; i < tmp.length; i++) {
			if (this.geneCode[i] != tmp[i]) {
				ret++;
			}
		}

		return ret;
	}

	public void printPath() {
		setGeneRepresentation(null);
		for(int i=0;i<path.length;i++) {
			System.out.printf(path[i]+"-");
		}
		System.out.println();
		System.out.printf("SP-");
		for (int i = 0; i < path.length; i++) {
			System.out.printf("%c", nodes[path[i]]);

			System.out.printf("-");
		}
		System.out.println("SP");
	}
}
