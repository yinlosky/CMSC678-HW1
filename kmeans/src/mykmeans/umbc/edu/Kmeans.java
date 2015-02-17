package mykmeans.umbc.edu;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;


/*
 * Author: Yin Huang
 * Date: Feb 5, 2015
 * This is my implementation of Kmeans algorithm which cluter the input digit files into K
 * clusters. 
 * 
 * To decide if my kmeans have converged or not, I use the minimum value to store the minimum distance sum for each iteration, 
 * if newest minimum value is not smaller then we know we should stop.
 * 
 * Input: 1. K, k clusters to be found for the input file
 * 		  2. Input file path
 * 		  
 * Output: {S_1, S_2,...S_k}
 */
public class Kmeans {
	private final int MAXITERATION = 10; //This is the maximum number of iterations in case the algorithm doesn't converge
	private int k;        // Num of clusters
	private ArrayList<Dataobject> vectors = new ArrayList<Dataobject>(); // vectors in an arraylist.
	private static final String DELIMITER = " ";
    private ArrayList<Cluster> Clusters = new ArrayList<Cluster>()	; // the initial k clusters with k centers and empty data vectors
	private Set<Integer> thisSet;
	private double minimum = Double.MAX_VALUE;
	/*
	 * This function will read input file and store them into vectors
	 * Assuming that each line in the input file corresponds to a vector
	 * 
	 */
	private void ReadInput(String inputFilePath){
		int index = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath)))
		{
 
			String sCurrentLine;
		
			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
				Dataobject myVector = new Dataobject();
				myVector.id = index;
				index++;
				StringTokenizer token = new StringTokenizer(sCurrentLine,DELIMITER);
				while(token.hasMoreTokens())
				{
					final int number = Integer.parseInt(token.nextToken());
					myVector.arr.add(number);
				}
				vectors.add(myVector);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	/*
	 * initializeCenter: initializes numOfCenters centers for the data we will initialize the cluster label as well
	 * Input: K the number of clusters
	 * Output: will update the centers vector
	 */
	
	private void initializeCenter(int numOfCenters){
		if(numOfCenters <= 0) return;
	    Random randomGenerator = new Random();
	    thisSet = new HashSet<Integer>();
	   
	    int dataSize = vectors.size();
	    while(thisSet.size()<numOfCenters){
	      int randomInt = randomGenerator.nextInt(dataSize);
	      if(!thisSet.contains(randomInt)) {
	    	  Dataobject oneCenter = vectors.get(randomInt);
	    	  oneCenter.label = thisSet.size()+1;
	    	  thisSet.add(randomInt);
	    	  Cluster oneCluster = new Cluster(oneCenter);
	    	  Clusters.add(oneCluster);
	      }
	     
	    }
	    System.out.println(thisSet.toString());
	}
	
	/*
	 * Help function to calculate the Euclidean distance
	 * Input: Two data objects
	 */
	
	private static double myDistance(Dataobject A, Dataobject B){
		double temp = 0;
		int i =0;
		while(i<Dataobject.Dimension){
			temp += Math.pow(A.arr.get(i) - B.arr.get(i), 2);
			i++;
		}
		return Math.sqrt(temp);
	}
	/*
	 * Assign data points to their closet center
	 * Input: the data vectors and the Cluster vectors
	 * Output: the sum of minimum distance for each data vector to its center vector, this sum will be used to determine the termination of our loop.
	 */
	
	private static double assign(ArrayList<Dataobject> datavectors, ArrayList<Cluster> clusters){
		 double localMin = 0;
		 int addTo = 0;
		 for(int k=0;k<clusters.size();k++){
			 ArrayList<Dataobject> empty = new ArrayList<Dataobject>();
			 clusters.get(k).setDatavector(empty);
		 }
		for(int i = 0; i<datavectors.size();i++){
			double tempD = Double.MAX_VALUE;
				for(int j = 0; j< clusters.size();j++){
					
				            if(tempD >= myDistance(datavectors.get(i),clusters.get(j).getCenter())){
				            	tempD = myDistance(datavectors.get(i),clusters.get(j).getCenter()); 
				            	datavectors.get(i).label = clusters.get(j).getCenter().label;
				            	addTo = j;
				            } // we assign datavector i to the centervector j if the distance is smallest among all centervectors
				            
					}
				clusters.get(addTo).getDatavector().add(datavectors.get(i));
				localMin += tempD;
				}
		return localMin;
		}
	/*
	 * Update function: will update the center location for each cluster based on the assignment
	 * The new center is the average of all data vectors in the cluster.
	 */
	
	private static void update(ArrayList<Cluster> myclusters){
		for(int i = 0;i<myclusters.size();i++){
			Cluster oneCluster = myclusters.get(i);
			ArrayList<Dataobject> datavectors = oneCluster.getDatavector();
			Dataobject newCenter = new Dataobject();
			newCenter.label = oneCluster.getCenter().getLabel();
			for(int j = 0; j<Dataobject.Dimension;j++){
				int temp = 0;
				for(int k = 0; k<datavectors.size();k++){
					temp += datavectors.get(k).arr.get(j);
				}
				newCenter.arr.add(temp/datavectors.size());
			}
			oneCluster.setCenter(newCenter);
		}
	}

	
	public static void main(String[] args) throws FileNotFoundException{
		System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("/home/yhuang9/Documents/CMSC 678/Homework 1/MNIST/console.out"))));
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String path = "/home/yhuang9/Documents/CMSC 678/Homework 1/MNIST/mnist_data.txt";
		Kmeans km = new Kmeans();
		km.k = 10;
		km.ReadInput(path);
		km.initializeCenter(km.k);
		int iterationIdx = 1;
		while(iterationIdx <= km.MAXITERATION ){
	
			Date date = new Date();
			System.out.println(dateFormat.format(date));
			
			System.out.println("Iteartion: "+iterationIdx);
			double newDistance = Kmeans.assign(km.vectors, km.Clusters);
			System.out.println("New Distance is: "+newDistance);
			if(newDistance < km.minimum)
				km.minimum = newDistance;
			else {
				System.out.println("The distance is getting bigger, so we stop!");
				break;
			}
			System.out.println("Now updating the clusters");
			Kmeans.update(km.Clusters);
			System.out.println("Updating done!");
			Date date2 = new Date();
			System.out.println(dateFormat.format(date2));
			iterationIdx++;
		}
		for(int i=0;i<km.k;i++){
			System.out.println(km.Clusters.get(i).toString());
		}
		
		/*
		Iterator<Dataobject> it = km.vectors.iterator();
		
		while(it.hasNext()){
			System.out.println(it.next().toString());
		}
		
		
		km.initializeCenter(10);
		Iterator<Dataobject> centerit = km.centers.iterator();
		while(centerit.hasNext())
			System.out.println(centerit.next().toString());
				
	}
	 */
	}
}