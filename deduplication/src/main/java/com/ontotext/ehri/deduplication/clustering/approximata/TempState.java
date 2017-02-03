package com.ontotext.ehri.deduplication.clustering.approximata;

import java.util.Arrays;

public class TempState {
	public SymbolDest[] sd;
	public int sdStored;
	public boolean finality;

	public TempState(){
		sd = new SymbolDest[1];
		sd[0] = new SymbolDest();
		finality = false;
	}

	public void addTransition(int symbol, int dest){
		if(sdStored == sd.length){
			sd = Arrays.copyOf(sd, 2*sdStored);
			for(int i = sdStored; i < sd.length; i++){
				sd[i] = new SymbolDest();
			}
		}
		sd[sdStored].symbol = symbol;
		sd[sdStored].dest = dest;
		sdStored++;
	}

	public void addTransition(int symbol, int dest, int mass) {
		addTransition(symbol, dest);
		sd[sdStored-1].weight = mass;
	}
}
