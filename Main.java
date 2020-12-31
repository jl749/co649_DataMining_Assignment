package dataMining_Assignment;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		PreProcessing pre=new PreProcessing();
		try {
			pre.load("adult.data");
			pre.removeNoise();
			pre.writeData();
			
			//pre.runGA(3,0);
			/**
			 * According to GA result [20, 63, 80, 88] 
			 * 88 is close to max 90, replace 88->90
			 * attribute age into 4 group <=20, <=63, <=80, <=90
			 * */
			//pre.runGA(2,3);
			/**
			 * Run GA on attribute education-num
			 * min = 1,	max = 16
			 * GA returns [1,2], since 1 and 2 so close just use 2
			 * <=2, <=16
			 * */
			
			//pre.runGA(4,9);
			/**69, 73, 85, 98
			 * Run GA on attribute hours-per-week
			 * min = 1, max = 99
			 * GA returned [1, 9, 82, 96], remove 1 and 96 since they are close to min and max
			 * <=9, <=82, <=99
			 * */
			int[] b1= {20,63,80,90};
			pre.convertNumeric(b1,0);
			int[] b2= {2,16};
			pre.convertNumeric(b2,3);
			int[] b3= {9,82,99};
			pre.convertNumeric(b3,9);
			pre.writeData();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
