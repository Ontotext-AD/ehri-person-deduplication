package com.ontotext.ehri.deduplication.clustering.approximata;

import java.util.Arrays;

public class CharSequence {
	public char[] seq;
	public int seqStored;

	public CharSequence(){
		seq = new char[64];
	}

	public CharSequence(int seqAlloced){
		seq = new char[seqAlloced];
	}

	public CharSequence(String str) {
		seq = new char[str.length()];
		for(int i = 0; i < seq.length; i++){
			seq[i] = str.charAt(i);
		}
		seqStored = seq.length;
	}

	public void add(char c){
		if(seqStored == seq.length){
			int mem = 2*seq.length;

			if(mem == seqStored){
				mem++;
			}
			seq = Arrays.copyOf(seq, mem);
		}
		seq[seqStored] = c;
		seqStored++;
	}

	public void append(String str, int pos, int length) {
		for(int i = 0; i < length; i++){
			add(str.charAt(pos+i));
		}
	}

	public void append(char[] str, int pos, int length) {
		for(int i = 0; i < length; i++){
			add(str[pos+i]);
		}
	}

	public void cpy(char[] str, int pos, int length) {
		seqStored = 0;
		for(int i = 0; i < length; i++){
			add(str[pos+i]);
		}
	}

	public void cpy(String str, int pos, int length) {
		seqStored = 0;
		for(int i = 0; i < length; i++){
			add(str.charAt(pos+i));
		}
	}
}
