package com.ontotext.ehri.deduplication.clustering.approximata;

import java.io.Serializable;
import java.util.Arrays;

public class BitArray implements Serializable{
	private static final long serialVersionUID = 1L;
	private int[] array;

	public BitArray(int alloced){
		array = new int[bitsToInts(alloced)];
	}

	private int bitsToInts(int bits) {
		if(bits % 32 == 0){
			return bits/32;
		}
		return bits/32 + 1;
	}

	public void realloc(int alloced) {
		array = Arrays.copyOf(array, bitsToInts(alloced));
	}

	public boolean get(int pos) {
		int v = 1;
		v <<= (pos % 32);
		return (array[pos/32] & v) != 0;
	}
	
	public void set(int pos, boolean value) {
		int v = 1;
		v <<= (pos % 32);
		if(value){
			array[pos/32] |= v;
		}
		else{
			array[pos/32] &= (~v);
		}
	}
}
