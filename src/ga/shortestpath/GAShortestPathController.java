package ga.shortestpath;

import ga.GAController;
import java.util.*;

public class GAShortestPathController implements GAController<GAShortestPathIndividual> {
	ArrayList<GAShortestPathIndividual> individuals;
	private int generation;//現在の世代
	private int maxGeneration;//最大の世代数
	private double generationGap;//0から1までの値をとる．子を作る親の割合．(1-generationGap)の割合でそのまま次世代に
	private double probCrossover;//交叉率
	private double probMutation;//突然変異の確率
	private int nodes;//訪問するべき頂点の数
	private int numOfInfividuals;//個体の数
	private int tournamentSize;
	public GAShortestPathController(int numOfIndividuals, int numOfNodes, double[][] cost, int maxG, double Gap,double mutation,double crossover,int tournament) {
		// 
		//個体クラスの静的変数の初期化．ここでやって大丈夫か？
		GAShortestPathIndividual.setStaticMember(cost, numOfNodes);
		individuals = new ArrayList<GAShortestPathIndividual>();
		for (int i = 0; i < numOfIndividuals; i++) {
			individuals.add(new GAShortestPathIndividual());
		}
		tournamentSize=tournament;
		//このクラスのクラス変数の初期化
		nodes = numOfNodes;
		generation = 1;
		maxGeneration = maxG;
		probMutation = mutation;//徐々に減らしてみる．
		setProbCrossover(crossover);//決め打ち．外から帰られるようにいずれコンストラクタの引数に追加するかも．
		generationGap = Gap;
		this.numOfInfividuals = numOfIndividuals;
	}

	public void mainFlow() {
		System.out.println("Start main flow");
		while (generation < maxGeneration) {
			System.out.println("generation:" + generation);
			System.out.println("aproximetry solution:");
			individuals.get(0).printPath();
			System.out.println("total cost:" + individuals.get(0).getTotalCost() + "\n");

			GAShortestPathIndividual.updateMax(individuals);
			doSelection();
			for (int i = 0; i < individuals.size(); i++) {
				individuals.get(i).update();
			}
			Collections.sort(individuals);
			doGenerationalChange();
		}
	}

	@Override
	public void doSelection() {
		/*
		 * 選択を行う．
		 * 事前条件として，個体クラスの適合度の計算はすでに終わっているものとする．
		 * ここではエリート戦略を用いる
		 * 適合度の高いnumOfInfividuals*(1-generationGap)体の個体はそのまま次世代に引き継ぐ．
		 * 選択方式はトーナメント方式を採用．
		 */
		Collections.sort(individuals);
		ArrayList<GAShortestPathIndividual> child = new ArrayList<GAShortestPathIndividual>();//子のクラス．後でindividualに上書きする．
		int elite = (int) (((double) numOfInfividuals) * (1.0 - generationGap));
		int[] tmp;
		for (int i = 0; i < elite; i++) {
			child.add(individuals.get(i).clone());
		}
		//		System.out.println("Start tournament");
		for (int i = 0; i < numOfInfividuals - elite; i++) {
			//トーナメントサイズはとりあえず4
			//あとから柔軟に変更できるようにトーナメントサイズをメンバ変数にして外からいじれるようにしたい．
			//			System.out.println("make tournament");
			tmp = getTournament(tournamentSize, elite, numOfInfividuals - 1);
			//			System.out.println("made tournament");
			int winner = 0;
			double maxFitness = 0;
			for (int j = 0; j < tmp.length; j++) {
				if (individuals.get(tmp[j]).calcFitness() > maxFitness) {
					winner = tmp[j];
					maxFitness = individuals.get(tmp[j]).calcFitness();
				}
			}
			child.add(individuals.get(winner).clone());
		}
		//		System.out.println("finish tournament");
		//子の集団生成完了．
		//GAオペレータを適用する．
		//まずは交叉

		for (int i = elite; i < numOfInfividuals; i++) {
			for (int j = i + 1; j < numOfInfividuals; j++) {
				if (Math.random() <= probCrossover) {
					crossover(child.get(i), child.get(j), child, i, j);
				}
			}
		}
		//次に突然変異．
		for (int i = elite; i < numOfInfividuals; i++) {
			if (Math.random() <= probMutation) {
				child.get(i).mutation();
			}
		}
		individuals = null;
		individuals = child;//上書き
	}

	@Override
	public void doGenerationalChange() {
		//probMutation*=0.95;
		System.out.println("generation:" + generation);
		System.out.println("aproximetry solution:");
		individuals.get(0).printPath();
		System.out.println("total cost:" + individuals.get(0).getTotalCost() + "\n");
		generation++;
	}

	@Override
	public void setGenerationGap(double p) {
		generationGap = p;
	}

	@Override
	public void setProbMutation(double p) {
		probMutation = p;
	}

	@Override
	public int getGeneration() {
		return generation;
	}

	public int getMaxGeneration() {
		return maxGeneration;
	}

	@Override
	public void crossover(GAShortestPathIndividual P1, GAShortestPathIndividual P2,
			ArrayList<GAShortestPathIndividual> child, int left, int right) {
		//親となる個体クラスを二つ受け取って子となる個体クラスを二つ生成．
		//遺伝子の交叉方式としてここでは，一様交叉を選択する．
		GAShortestPathIndividual C1 = new GAShortestPathIndividual();
		GAShortestPathIndividual C2 = new GAShortestPathIndividual();
		for (int i = 0; i < nodes - 1; i++) {
			if (Math.random() >= 0.5) {
				C1.setLocus(i, P1.getGeneCode()[i]);
				C2.setLocus(i, P2.getGeneCode()[i]);
			} else {
				C1.setLocus(i, P2.getGeneCode()[i]);
				C2.setLocus(i, P1.getGeneCode()[i]);
			}
		}
		C1.setGeneRepresentation(null);
		C2.setGeneRepresentation(null);
		child.remove(left);
		child.add(left, C2);
		child.remove(right);
		child.add(right, C1);
	}

	private int[] getTournament(int size, int min, int max) {
		int[] ret = new int[size];
		for (int i = 0; i < ret.length; i++) {
			int tmp = (int) (Math.random() * ((double) max - min)) + min;
			if (i > 0) {
				boolean dup = true;
				int j;
				while (dup) {
					for (j = 0; j < i; j++) {
						if (ret[i] == tmp) {
							tmp = (int) (Math.random() * ((double) max)) + min;
							break;
						}
					}
					if (j == i) {
						dup = false;
					}
				}
			}
			ret[i] = tmp;
		}
		return ret;
	}

	public void setProbCrossover(double probCrossover) {
		this.probCrossover = probCrossover;
	}
}
