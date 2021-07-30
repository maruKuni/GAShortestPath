package ga;

public interface GAIndividual {
	public double calcFitness();
	public void mutation();
	public void setGeneCode(Object obj);
	public Object getGeneCode();
	public void setGeneRepresentation(Object obj) ;
	public Object getGeneRepresentation();
	public void setLocus(int pos,Object obj);
}
