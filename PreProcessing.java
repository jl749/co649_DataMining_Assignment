package dataMining_Assignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class PreProcessing {

	private String[][] data = new String[0][0];
	
	private Random rnd=new Random();
	private static final int POPULATION_SIZE=50; 
	private static final int MAX_GEN=20;
	private static final double MUTATION_PROB=0.99;
	private static final int T_SIZE=10;
	
	DecimalFormat df=new DecimalFormat("###.##"); //to round decimal
	
	public void load(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        ArrayList<String[]> lines = new ArrayList<>();
        String line = null;
        
        while ((line = reader.readLine()) != null) {
        	if(line.trim().isEmpty())
        		continue;
        	String[] tmp=(line.replaceAll(" ", "")).split(","); //0~14
        	
        	//remove 10,11 Capital-Gain, Capital-Loss
        	int index=0;
        	String[] values=new String[12];
        	for(int i=0;i<15;i++) {
        		if(i==10 || i==11 || i==2) {
        			continue;
        		}
        		values[index++]=tmp[i];
        	}
        	//System.out.println(Arrays.toString(values).replaceAll("(\\[|\\])",""));
        	
            lines.add(values);
        }
        data = lines.toArray(data);
    }
	
	public void writeData() throws IOException {
		FileWriter fstream = new FileWriter("newAdult.data");
		for(int i=0;i<data.length;i++) 
			fstream.write(Arrays.deepToString(data[i]).replaceAll("(\\[|\\])","")+"\n");
		fstream.close();
	}
	
	public void convertNumeric(int[] boundaries,int col) { //0 2 4 10
		for(int i=0;i<data.length;i++) 
			for(int j=0;j<boundaries.length;j++) 
				if(Integer.parseInt(data[i][col])<=boundaries[j]) { 
					if(j==0)
						data[i][col]="~"+boundaries[j];
					else
						data[i][col]=boundaries[j-1]+"~"+boundaries[j];
					break;
				}
	}
	/**
	 * noise in age, fnlwgt columns -> median of attribute
	 * noise in the other columns -> mode of attribute
	 * */
	public void removeNoise() {
		List<String> findMode=new ArrayList<>();
		List<Integer> count=new ArrayList<>();
		
		List<String> mode=new ArrayList<>();
		List<String> medianA=new ArrayList<>();
		List<String> medianF=new ArrayList<>();
		int incIndex=-1;
		for(int i=0;i<data[0].length;i++) { //for each column
			for(int j=0;j<data.length;j++) { //for every row
				String val = data[j][i];
				if(i==0) //for "age" and "fnlwgt" columns only
					medianA.add(val);
				else if(i==2) 
					medianF.add(val);
				else {
					if((incIndex=findMode.indexOf(val)) == -1) { //when not on list
						findMode.add(val);
						count.add(1);
					}else 
						count.set(incIndex, count.get(incIndex)+1); //when on list
				}
			}
			if(i==0 || i==2) {
				mode.add(null);
				continue;
			}
			int index=count.indexOf(Collections.max(count));
			mode.add(findMode.get(index));
			findMode.clear();
			count.clear();
		}
		
		Collections.sort(medianA);
		Collections.sort(medianF);
		for(int i=0;i<data.length;i++) {
			List<String> line=Arrays.asList(data[i]);
			while(line.contains("?")) {
				int index = line.indexOf("?");
				if(index==0)
					line.set(index, medianA.get(medianA.size()/2));
				else if(index==2)
					line.set(index, medianF.get(medianF.size()/2));
				else
					line.set(index, mode.get(index));
			}
			data[i]=line.toArray(data[i]);
			//System.out.println(line);
		}
	}
	
	private int[] findThreshold(int index){
		int[] threshold=new int[2];
		int max=Integer.parseInt(data[0][index]); int min=Integer.parseInt(data[0][index]);
		for(int i=1;i<data.length;i++) {
			if(max < Integer.parseInt(data[i][index]))
				max=Integer.parseInt(data[i][index]);
			if(min > Integer.parseInt(data[i][index]))
				min=Integer.parseInt(data[i][index]);
		}
		threshold[0]=max;	threshold[1]=min;
		return threshold;
	}
	
	public void runGA(int size,int whichColumn) {
		int[][] pop=new int[POPULATION_SIZE][size];
		int[] threshold=findThreshold(whichColumn); //threshold contains max and min value of an attribute
		for(int i=0;i<POPULATION_SIZE;i++) 
			pop[i]=initialise(size,threshold[0],threshold[1]);
		
		
		double[] fitness=evaluate(pop,whichColumn);
		
		int[][] newpop=new int[POPULATION_SIZE][size];
		int[] tmp=new int[size];
		int bestIndex=-1;
		for(int g=0;g<MAX_GEN;g++) {
			System.out.print("<GEN "+g+"> ");
			bestIndex=0;
			
            for(int i=1;i<POPULATION_SIZE;i++) 
            	if(fitness[bestIndex]<fitness[i])
            		bestIndex=i;
            
            System.out.println("best fitness= "+fitness[bestIndex]);
            //elitism
            tmp=pop[bestIndex].clone();
            
            for(int i=1;i<POPULATION_SIZE;i++) {
        		int[] offspring=mutation(pop[select(fitness)],size,threshold[0],threshold[1]);
        		newpop[i]=offspring;
        	}
            
            pop=newpop;
            
            pop[0]=tmp.clone();
            //System.out.println("bestIndex = "+bestIndex);
            System.out.println(fitness[bestIndex]+Arrays.toString(fitness));
            fitness=evaluate(pop,whichColumn); //update fitness -> update select()
		}
		bestIndex=0;
		for(int i=1;i<POPULATION_SIZE;i++) 
        	if(fitness[bestIndex]<fitness[i])
        		bestIndex=i;
		System.out.println(Arrays.toString(pop[bestIndex]));
	}
	
	private int[] mutation(int[] slctP,int size,int max,int min) {
		int[] offspring=slctP;
		int newNum=rnd.nextInt(max-min)+min;
		int index=rnd.nextInt(size);
		while(contains(offspring,newNum)) 
			newNum=rnd.nextInt(max-min)+min;

		offspring[index]=newNum;
		Arrays.sort(offspring);
		return offspring;
	}
	
	public int[][] findClasses(int[] individual,int col){//0
		int[][] count=new int[individual.length+1][2]; //[range][50k>, 50k<]
		for(int i=0;i<data.length;i++) { //use every data
			boolean flag=false;
			for(int j=0;j<individual.length;j++) { //find matching range
				if(Integer.parseInt(data[i][col])<individual[j]) {
					if(data[i][11].equals("<=50K")) {
						count[j][0]++;	flag=true;
						break;
					}else if(data[i][11].equals(">50K")) {
						count[j][1]++;	flag=true;
						break;
					}
				}
			}
			if(!flag) 
				if(data[i][11].equals("<=50K")) 
					count[individual.length][0]++;
				else if(data[i][11].equals(">50K")) 
					count[individual.length][1]++;
		}
		return count;
	}
	private double[] evaluate(int[][] pop,int whichColumn) {
		double[] fitness=new double[POPULATION_SIZE];
		
		int[][] att=findClasses(pop[0],whichColumn);
		int sumP=0; int sumN=0; //each represent >=50K, <50K
		for(int k=0;k<att.length;k++) {
			sumP+=att[k][0];
			sumN+=att[k][1];
		}
		int[] classes= {sumP,sumN};
		for(int i=0;i<pop.length;i++) {
			att=findClasses(pop[i],whichColumn);
			double splitI=Double.parseDouble(df.format(100*splitInfo(att)));
			double infoGain=Double.parseDouble(df.format(100*entropy(classes)-infoneeded(att)));
			fitness[i]=Double.parseDouble(df.format(infoGain/splitI));
		}
		return fitness;
	}
	
	private int[] initialise(int size,int max,int min) {
		int[] arr=new int[size];
		Set<Integer> set=new HashSet<>();
		while(set.size()<size) 
			set.add(rnd.nextInt(max-min)+min);
		int index=0;
		for(Integer i:set) {
			arr[index++]=i;
		}
		Arrays.sort(arr);
		return arr;
	}
	
	private int select(double fitness[]) {
	    	Set<Integer> arena=new HashSet<Integer>();
	            		
	    	while(arena.size()<T_SIZE) {
	    		int index=(int)(rnd.nextDouble()*POPULATION_SIZE);
	    		arena.add(index);
	    	}
	    	
	    	int winner=arena.iterator().next();
	    	for(int a:arena) 
	    		if(fitness[winner]<fitness[a])
	    			winner=a;
	    	
	    	return winner;
	}

	private double entropy(int[] input) {
		double result=0,total=0;
		for(int i=0;i<input.length;i++)
			total+=input[i];
		for(int a:input) {
			if(a!=0)
				result+=(a/total)*(Math.log(a/total)/Math.log(2));
		}
		return -result;
	}
	private double infoneeded(int[][] input) {
		double result=0;	int total=0;
		for(int i=0;i<input.length;i++)
			for(int j=0;j<input[i].length;j++)
				total+=input[i][j];
		for(int i=0;i<input.length;i++) {
			int sum=0;
			for(int j=0;j<input[i].length;j++) 
				sum+=input[i][j];
			result+=((double)sum/total)*entropy(input[i]);
		}
		return result;
	}
	private double splitInfo(int[][] input) {
		double result=0;	int total=0;
		for(int i=0;i<input.length;i++)
			for(int j=0;j<input[i].length;j++)
				total+=input[i][j];
		for(int i=0;i<input.length;i++) {
			int sum=0;
			for(int j=0;j<input[i].length;j++) 
				sum+=input[i][j];
			if(sum==0)
				continue;
			result+=((double)sum/total)*(Math.log((double)sum/total)/Math.log(2));
		}
		return -result;
	}
	private boolean contains(int[] arr,int val) {
		for(int i=0;i<arr.length;i++) 
			if(arr[i]==val) 
				return true;
		return false;
	}
}
