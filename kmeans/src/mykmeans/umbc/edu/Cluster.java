package mykmeans.umbc.edu;

import java.util.ArrayList;

/*
 * This cluster class will contain a center and the data points that belong to it.
 */
public class Cluster {
	Dataobject center;
	ArrayList<Dataobject> datavector;
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Cluster ID: "+ center.label + " Includes the following data vectors: \n");
		for(int i=0;i<datavector.size();i++)
		{
			sb.append(datavector.get(i).id +", ");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public Cluster(){
		 center= new Dataobject();
	    datavector = new ArrayList<Dataobject>();
	}
	public Cluster(Dataobject oneCenter){
		center = oneCenter;
		datavector = new ArrayList<Dataobject>();
	}
	public Dataobject getCenter() {
		return center;
	}
	public void setCenter(Dataobject center) {
		this.center = center;
	}
	public ArrayList<Dataobject> getDatavector() {
		return datavector;
	}
	public void setDatavector(ArrayList<Dataobject> datavector) {
		this.datavector = datavector;
	}
}
