package ga;

public interface GAController {
	public void crossover();
	public void init(int numOfSet);
	public void doSelection();
	public void setProbCrosspver(double p);
	public void setProbMutation(double p);
	public void getGeneration();
	public void doGenerationalChange();
}
