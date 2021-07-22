package ga;

public interface GAIndividual {
	public double fitness();
	public void mutation();
	public void setGeneCode(Object obj);
	public Object getGeneCode();
	public void setGeneRepresentation(Object obj) ;
	public Object getGeneRepresentation();
	public void init();
}
