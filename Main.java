package dataMining_Assignment;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		PreProcessing pre=new PreProcessing();
		try {
			pre.load("newAdult.test");
			//pre.removeNoise();
			//pre.writeData();
			
			//pre.runGA(2,0);
			/**
			 * According to GA result
			 * attribute age into 3 group <=24, <=88, <=90
			 * */
			//pre.runGA(3,2); 
			/**
			 * Run GA on attribute fnlwgt
			 * min = 12885, max = 1484705
			 * <=1066440, <=1098732, <=1284056, <=1484705
			 * */
			//pre.runGA(2,4);
			/**
			 * Run GA on attribute education-num
			 * min = 1,	max = 16
			 * GA returns [1,2], since 1 and 2 so close just use 2
			 * <=2, <=16
			 * */
			//int[] x= {1, 9, 82, 96};
			//pre.findClasses(x,10);
			
			//pre.runGA(4,10);
			/**69, 73, 85, 98
			 * Run GA on attribute hours-per-week
			 * min = 1, max = 99
			 * GA returned [1, 9, 82, 96], remove 1 and 96 since they are close to min and max
			 * <=9, <=82, <=99
			 * */
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
