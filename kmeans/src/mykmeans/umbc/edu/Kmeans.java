package mykmeans.umbc.edu;


import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;

import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;


/*
 * Author: Yin Huang
 * Date: Feb 5, 2015
 * This is my implementation of Kmeans algorithm which cluter the input digit files into K
 * clusters. 
 * 
 * To determine convergence, I check the label changing of each data vector, if any one of them is changing, we consider it as unconverged.
 * Only when all the data vectors are not changing their labels, we consider it as converged.
 * As the program runs, I notice sometimes the change is very small, but it runs multiple iterations to converge, to avoid 
 * long iterations, I set a deta =300 as the threshold to converge to save running time.
 * 
 * Input: 1. K, k clusters to be found for the input file
 * 		  2. Input file path
 *        3. True label file path
 * 		  
 * Output: Clusters with statistics
 */
public class Kmeans {
	private final int MAXITERATION = Integer.MAX_VALUE; //This is the maximum number of iterations in case the algorithm doesn't converge
	private int k;        // Num of clusters
	private ArrayList<Dataobject> vectors = new ArrayList<Dataobject>(); // vectors in an arraylist.
	private static final String DELIMITER = " ";
    private ArrayList<Cluster> Clusters = new ArrayList<Cluster>()	; // the initial k clusters with k centers and empty data vectors
	private Set<Integer> thisSet;
	private double minimum = Double.MAX_VALUE;
	private static Boolean CON = false; 			 // Converge flag
	private HashMap<Integer,Integer> ClassLabels;    // stores the real labels for each data vector
	private static HashMap<Integer,ArrayList<Integer>> myMap;            // stores the data vectors within the same label cluster
	private final int NumOfClasses = 10;
	private final double delta = 300; // this is used to terminate when the change is getting too small
	
	//private final double delta = 1000; // I noticed if merely based on the change of membership, it will run around 76 iterations
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
	 * initializeCenterDes: initializes numOfCenters centers for the data we will initialize the cluster label as well
	 * with designated centers.
	 * Input: K the number of clusters
	 * Output: will update the centers vector
	 */
	
	private void initializeCenterDes(int numOfCenters){
		if(numOfCenters <= 0) return;
	    Random randomGenerator = new Random();
	    
	    // i is the class label, newArr.get(randomint) is the chosen vector's id
	    for(int i=0;i<numOfCenters;i++){
			ArrayList<Integer> newArr = new ArrayList<Integer>();
		    newArr =myMap.get(i);
		    
		    int dataSize = newArr.size();
		    int randomNum = randomGenerator.nextInt(dataSize);
		    
		    int myID =newArr.get(randomNum);

		    Dataobject oneCenter = vectors.get(myID);
		    oneCenter.id = myID;
		    oneCenter.label =i;
		    
		    Cluster oneCluster = new Cluster(oneCenter);
		    Clusters.add(oneCluster);
		}

	    StringBuilder sb= new StringBuilder();
	    sb.append("[");
	    for(int i =0;i<this.k;i++){
	    	sb.append(Clusters.get(i).center.id+" ");
	    }
	    sb.append("]\n");
	    sb.append("Corresponding class labels are:\n");
	    sb.append("[");
	    for(int i =0;i<this.k;i++){
	    	sb.append(Clusters.get(i).center.label+" ");
	    }
	    sb.append("]");
	    
	    System.out.println(sb.toString());
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
	    StringBuilder sb= new StringBuilder();
	    System.out.println(thisSet.toString());
	    sb.append("[");
	    for (Integer i:thisSet){
	    	sb.append(ClassLabels.get(i)+" ");
	    }
	    sb.append("]");
	    System.out.println("Corresponding class labels are:\n");
	    System.out.println(sb.toString());
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
		 int myLabel = -1;
		 for(int k=0;k<clusters.size();k++){
			 ArrayList<Dataobject> empty = new ArrayList<Dataobject>();
			 clusters.get(k).setDatavector(empty);
		 }
		 Boolean unChanged = true;       // assume the membership is not changed at all
		for(int i = 0; i<datavectors.size();i++){
			double tempD = Double.MAX_VALUE;
				for(int j = 0; j< clusters.size();j++){
					
				            if(tempD >= myDistance(datavectors.get(i),clusters.get(j).getCenter())){
				            	tempD = myDistance(datavectors.get(i),clusters.get(j).getCenter()); 
				            	//datavectors.get(i).label = clusters.get(j).getCenter().label;
				            	myLabel = clusters.get(j).getCenter().label;
				            	addTo = j;
				            } // we assign datavector i to the centervector j if the distance is smallest among all centervectors
				            // remember the cluster number in addTo and the label number in myLabel
				            
					}
				// update the label for the assignment and add the data vector to the cluster.
				int previousLabel = datavectors.get(i).label;
				if(previousLabel != myLabel) unChanged = false; // once any membership has changed, we flip the flag
				datavectors.get(i).label = myLabel;
				clusters.get(addTo).getDatavector().add(datavectors.get(i));
				localMin += tempD;
				}
		if(unChanged) CON = true; // If the flag is not flipped, then we know it has converged, so we set the flag CON to be true.
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
	
	/*
	 * readCL: read real  Class Label function will read the real label from the label file
	 * Input: path to the label file
	 * Output: updated ClassLabel hashmap
	 */
	private void readCL(String labelpath){
		ClassLabels = new HashMap<Integer,Integer>();
		myMap = new HashMap<Integer, ArrayList<Integer>>();
		
		for(int i=0;i<NumOfClasses;i++){
			ArrayList<Integer> newArr = new ArrayList<Integer>();
			myMap.put(i, newArr);
		}
		
		int index = 0;
		String sCurrentLine;
		try(BufferedReader br = new BufferedReader(new FileReader(labelpath))){
			while((sCurrentLine = br.readLine()) != null){
				int label = Integer.parseInt(sCurrentLine);
				ClassLabels.put(index, label);
				myMap.get(label).add(index);
				
				index++;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}
	/*
	 * statistics: this will calculate all the frequency for all classes
	 * Input: our clustering result, Clusters
	 * Output: statistics for our result cluster
	 * 			1. Total number of instances for the cluster
	 * 			2. classs label frequecy, will show the most common instances 
	 * 			3. Wrong instance number for each cluster
	 */
	private void statistics(ArrayList<Cluster> result){
		
		int wrongInstances = 0;
		for(int i=0;i<result.size();i++){
			StringBuilder sb = new StringBuilder();
			int[] counts = new int[NumOfClasses];
			Cluster oneCluster = result.get(i);
			int totalInstances = oneCluster.datavector.size();
			sb.append("Cluster "+oneCluster.getCenter().label+" has "+totalInstances + " instances. \n");
			for(int j=0;j<totalInstances;j++){
			    int mylab = ClassLabels.get(oneCluster.datavector.get(j).id);
				counts[mylab]++;
			}
			int max =0;
			int index =0;
			int wrong = 0;
			for(int m=0;m<NumOfClasses;m++){
				sb.append(m +" : "+counts[m] +"\t");
				if(max<counts[m])
				{
					max = counts[m];
					index = m;
				}
			}
			sb.append("\n");
			wrong = totalInstances-max;
			wrongInstances += wrong;
			sb.append("Most common digit is: " + index + " with " + max+" counts"+"\t"+ "wrong instances: "+ wrong +" counts" );
			System.out.println(sb.toString());
		}
		System.out.println("Average wrong instances in this execuation are: "+wrongInstances +" counts\n");
	}
	// Run function to find the clusters with random centers
	public void run(String path, String labelpath, int clusternum){
		
		this.k = clusternum;
		this.readCL(labelpath);
		
		// Print out all the real class labels in myMap.
		System.out.println("True class labels:\n");
		Set<Integer> labels = myMap.keySet();
		for (int label: labels) {
		    System.out.println(label + ": " + myMap.get(label));
		}
		System.out.println("**********************************************************\n");
		
		
		this.ReadInput(path);
		this.initializeCenter(this.k);
		
		int iterationIdx = 1;
		double totalT = 0;
		while(iterationIdx <= this.MAXITERATION ){
			long startTime = System.currentTimeMillis();
			System.out.println("Iteartion: "+iterationIdx);
			if(!CON){
			double newDistance = Kmeans.assign(this.vectors, this.Clusters);
			System.out.println("New Distance is: "+newDistance);
			if(newDistance < this.minimum)
				this.minimum = newDistance;
			
			else if(Math.abs(newDistance-this.minimum)< this.delta){
				System.out.println("Change is too small, so we stop!");
				System.out.println("Total running time is: " + (totalT/1000)+" s Average time is: " + (totalT/1000)/iterationIdx+ "s");
				break;
			}
		
			System.out.println("Now updating the clusters");
			Kmeans.update(this.Clusters);
			System.out.println("Updating done!");
			
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Iteration "+iterationIdx+" running time: "+totalTime);
			totalT +=totalTime;
			iterationIdx++;
			
			}
			
			else
				{
				System.out.println("We have converged! Total number of iterations: "+iterationIdx);
				System.out.println("Total running time is: " + (totalT/1000)+" s Average time is: " + (totalT/1000)/iterationIdx+ "s");
				break;
				}
		}
		if(iterationIdx == this.MAXITERATION){
			System.out.println("We have run "+ iterationIdx+" times iterations");
			System.out.println("Total running time is: " + (totalT/1000)+" s Average time is: " + (totalT/1000)/iterationIdx+ "s");
			
		}
		System.out.println("*********************My results: *******************************\n");
		for(int i=0;i<this.k;i++){
			System.out.println(this.Clusters.get(i).toString());
		}
		System.out.println("******************************************************************\n");
		System.out.println("*********************My results: statistics *******************************\n");
		this.statistics(this.Clusters);
		System.out.println("******************************************************************\n");
		
	}
	// Run with specified centers
public void runDes(String path, String labelpath, int clusternum){
		
		this.k = clusternum;
		this.readCL(labelpath);
		
		// Print out all the real class labels in myMap.
		System.out.println("True class labels:\n");
		Set<Integer> labels = myMap.keySet();
		for (int label: labels) {
		    System.out.println(label + ": " + myMap.get(label));
		}
		System.out.println("**********************************************************\n");
		
		
		this.ReadInput(path);
		this.initializeCenterDes(this.k);
		
		int iterationIdx = 1;
		double totalT = 0;
		while(iterationIdx <= this.MAXITERATION ){
			long startTime = System.currentTimeMillis();
			System.out.println("Iteartion: "+iterationIdx);
			if(!CON){
			double newDistance = Kmeans.assign(this.vectors, this.Clusters);
			System.out.println("New Distance is: "+newDistance);
			if(newDistance < this.minimum)
				this.minimum = newDistance;
			
			
			else if(Math.abs(newDistance-this.minimum)< this.delta){
				System.out.println("Change is too small, so we stop!");
				System.out.println("Total running time is: " + (totalT/1000)+" s Average time is: " + (totalT/1000)/iterationIdx+ "s");
				break;
			}
			
			System.out.println("Now updating the clusters");
			Kmeans.update(this.Clusters);
			System.out.println("Updating done!");
			
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Iteration "+iterationIdx+" running time: "+totalTime);
			totalT +=totalTime;
			iterationIdx++;
			
			}
			
			else
				{
				System.out.println("We have converged! Total number of iterations: "+iterationIdx);
				System.out.println("Total running time is: " + (totalT/1000)+" s Average time is: " + (totalT/1000)/iterationIdx+ "s");
				break;
				}
		}
		if(iterationIdx == this.MAXITERATION){
			System.out.println("We have run "+ iterationIdx+" times iterations");
			System.out.println("Total running time is: " + (totalT/1000)+" s Average time is: " + (totalT/1000)/iterationIdx+ "s");
			
		}
		System.out.println("*********************My results: *******************************\n");
		for(int i=0;i<this.k;i++){
			System.out.println(this.Clusters.get(i).toString());
		}
		System.out.println("******************************************************************\n");
		System.out.println("*********************My results: statistics *******************************\n");
		this.statistics(this.Clusters);
		System.out.println("******************************************************************\n");
		
	}
	
	public static void main(String[] args){
		//System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("/home/yhuang9/Documents/CMSC 678/Homework 1/MNIST/console.out"))));
		String dataPath = args[0];
		String labelPath = args[1];
		int k = Integer.parseInt(args[2]);
		String random = args[3];
		/* This is for test
		String random = "yes";
		String dataPath ="/home/yhuang9/Documents/CMSC 678/Homework 1/MNIST/mnist_data.txt";
		String labelPath = "/home/yhuang9/Documents/CMSC 678/Homework 1/MNIST/mnist_labels.txt";
		int k = 5;
		*/
		Kmeans km = new Kmeans();
		
		if(random.toLowerCase().equals("no"))
			km.runDes(dataPath, labelPath, k);
		else 
			km.run(dataPath, labelPath, k);

	}
}
