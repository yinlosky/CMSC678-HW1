package mykmeans.umbc.edu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/*
 * This class is the implementation of the data object for our analysis
 * Each data object is a row vector with the size of Dimension and a cluster label
 * Author: Yin Huang
 * Date: Feb,5 2015
 */
public class Dataobject {
	static int Dimension = 28*28; // total number of integers for each row/vector
	int id;						  // Row id for each object to differentiate each other
	int label;                    // class label 
	ArrayList<Integer> arr; 	  // the vector
	public Dataobject(){
		this.arr = new ArrayList<Integer>(Dimension);
		this.label = -1;
		id = 0;
	}
	public Dataobject(int myid, int mylabel){
		this.id = myid;
		this.arr = new ArrayList<Integer>(Dimension);
		this.label = mylabel;
	}
	
	public String toString(){
		StringBuffer myString =new StringBuffer();
		myString.append("Row: " + this.id +": ");
		Iterator<Integer> it = arr.iterator();
		while(it.hasNext())
			myString.append(it.next().toString()+" ");
		myString.append("LABEL: "+this.label);
		return myString.toString();
	}
	public static void main(String[] args){
		Dataobject ob = new Dataobject();
		  Random randomGenerator = new Random();
		for(int i=0;i<Dimension;i++)
			ob.arr.add(randomGenerator.nextInt(255));
		System.out.println(ob.toString());
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getLabel() {
		return label;
	}


	public void setLabel(int label) {
		this.label = label;
	}

	
}
