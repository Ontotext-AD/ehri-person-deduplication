package com.ontotext.ehri.deduplication.clustering.approximata;

import java.util.Arrays;

public class IntSequence {
	public int[] seq;
	public int seqStored;

	public IntSequence(){
		seq = new int[64];
	}

	public IntSequence(int seqAlloced){
		seq = new int[seqAlloced];
	}

	public void add(int n){
		if(seqStored == seq.length){
			seq = Arrays.copyOf(seq, 2*seq.length);
		}
		seq[seqStored] = n;
		seqStored++;
	}

	public boolean contains(int n) {
		for(int i = 0; i < seqStored; i++){
			if(seq[i] == n){
				return true;
			}
		}
		return false;
	}
}
