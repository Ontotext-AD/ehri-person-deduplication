package com.ontotext.ehri.deduplication.clustering.approximata;

import java.io.Serializable;

public class Alphabet implements Serializable{
	private static final long serialVersionUID = 1L;
	public int[] symbolToCode;
	public char[] codeToSymbol;
	public int numberOfSymbols;

	public Alphabet(){
		symbolToCode = new int[65536];
		numberOfSymbols = 1;
	}

	public void add(char ch) {
		if(ch == 0){
			return;
		}
		if(symbolToCode[ch] == 0){
			symbolToCode[ch] = 1;
			numberOfSymbols++;
		}
	}

	public void close() {
		codeToSymbol = new char[numberOfSymbols];
		codeToSymbol[0] = 0;
		numberOfSymbols = 1;
		for(int i = 0; i < symbolToCode.length; i++){
			if(symbolToCode[i] == 1){
				symbolToCode[i] = numberOfSymbols;
				numberOfSymbols++;
				codeToSymbol[symbolToCode[i]] = ((char)i);
			}
		}
	}
}
