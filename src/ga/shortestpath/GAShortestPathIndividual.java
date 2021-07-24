package ga.shortestpath;

import ga.GAIndividual;
import java.util.*;

public class GAShortestPathIndividual implements GAIndividual {
	static double[][] cost;//コスト行列
	static int numOfNodes;//訪問する頂点の数
	static double maxOfThisGeneration;//適合度計算のスケーリングウィンドウに使う．

	private int[] path;//経路を表現する．表現型
	private int[] geneCode;//遺伝型
	private double totalCost;

	public GAShortestPathIndividual() {
		totalCost = 0;
		path = new int[numOfNodes - 1];//始点＝終点の円順列になるのでnumOfNodes-1個の順列を考えればよい．
		geneCode = new int[numOfNodes - 1];
		init();//ランダムに経路を作成．
		setGeneCode(null);
	}

	@Override
	public double fitness() {
		return maxOfThisGeneration - totalCost;
	}

	@Override
	public void mutation() {
		int codePos = (int) (Math.random() * ((double) path.length));//変異する箇所
		int mutate = (int) (Math.random() * ((double) (path.length - codePos)));
		geneCode[codePos] = mutate;
		setGeneRepresentation(null);
	}

	@Override
	public void setGeneCode(Object obj) {
		ArrayList<Integer> order = new ArrayList<>();
		for (int i = 0; i < path.length; i++)
			order.add(i);
		for(int i=0;i<path.length;i++){
            int codePos=order.indexOf(path[i]);
            geneCode[i]=codePos;
            order.remove(codePos);
        }
	}

	@Override
	public int[] getGeneCode() {

		return geneCode;
	}

	@Override
	public void setGeneRepresentation(Object obj) {
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
	public Object getGeneRepresentation() {
		return path;
	}

	@Override
	public void init() {
		for (int i = 0; i < path.length; i++) {
			path[i] = i;
		}
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
		calcTotalCost();

	}

	public void calcTotalCost() {
		totalCost += cost[0][path[0]];
		for (int i = 0; i < path.length - 1; i++) {
			totalCost += cost[path[i]][path[i + 1]];
		}
		totalCost += cost[0][path[path.length - 1]];
	}

	public double getTotalCost() {
		return totalCost;
	}

	public static void updateMax(ArrayList<GAShortestPathIndividual> individuals) {
		//個体のリストを受け取って，その集団の中の最大のコストを記録する．5世代ごとくらいに行うといい？
		//コントローラから呼び出されることが前提．
		double max = 0;
		for (int i = 0; i < individuals.size(); i++) {
			if (individuals.get(i).getTotalCost() > max) {
				max = individuals.get(i).getTotalCost();
			}
		}
		maxOfThisGeneration = max;
	}
}
