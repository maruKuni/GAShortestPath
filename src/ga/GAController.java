package ga;

import java.util.*;
public interface GAController <T>{
	public void crossover(T P1,T P2,ArrayList<T> child,int left,int right);
	public void doSelection();
	public void setGenerationGap(double p);
	public void setProbMutation(double p);
	public int getGeneration();
	public void doGenerationalChange();
	
}
