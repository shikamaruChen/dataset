package math;

import java.util.Random;

public class Distribution {
	private Random rand;

	public Distribution(long seed) {
		rand = new Random(seed);
	}

	public int getGeoDev(double p) {
		return 1 + (int) Math.floor(Math.log(1.0 - rand.nextDouble())
				/ Math.log(1 - p));
	}
	
	public int nextInt(int max){
		return rand.nextInt(max);
	}
	
	public static void main(String[]args){
		Distribution d = new Distribution(System.currentTimeMillis());
		System.out.println(d.getGeoDev(0.5));
	}
}
