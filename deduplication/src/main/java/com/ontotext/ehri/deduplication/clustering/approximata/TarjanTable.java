package com.ontotext.ehri.deduplication.clustering.approximata;

import java.io.Serializable;
import java.util.Arrays;

public class TarjanTable implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final int NUM_OF_POS_TO_LOOK_BACK_IN_TARJAN_TABLE = 10000;
	public int[] statesTransitions;
	public int[] cellsSymbol;
	public int[] cellsDest;
	public int[] cellsWeight;
	private boolean weights;

	public TarjanTable(TarjanTableBuildHelp help, int statesAlloced, boolean weights){
		this.weights = weights;
		cellsSymbol = new int[help.cellsAlloced];
		cellsDest = new int[help.cellsAlloced];
		if(weights){
			cellsWeight = new int[help.cellsAlloced];
		}
		statesTransitions = new int[statesAlloced];
	}

	public int add(TarjanTableBuildHelp help, TempState ts){
		SymbolDest[] sd = ts.sd;
		int sdStored = ts.sdStored;
	    if(sdStored == 0){
	    	return 0;
	    }
	    int i;
	    if(NUM_OF_POS_TO_LOOK_BACK_IN_TARJAN_TABLE < 0 || help.lastPos - 1 < NUM_OF_POS_TO_LOOK_BACK_IN_TARJAN_TABLE){
	        i = 1;
	    }
	    else{
	        i = help.lastPos - NUM_OF_POS_TO_LOOK_BACK_IN_TARJAN_TABLE;
	    }
	    boolean posFound = false;
	    int j;
	    for( ; i + help.alphabetLength < help.cellsAlloced; i++){
	        for(j = 0; j < sdStored; j++){
	            if(cellsSymbol[i + sd[j].symbol] != 0){
	                break;
	            }
	        }
	        if(j < sdStored){
	            continue;
	        }
	        for(j = 1; j < help.alphabetLength; j++){
	            if(cellsSymbol[i + j] == j){
	                break;
	            }
	        }
	        if(j == help.alphabetLength){
	            posFound = true;
	            break;
	        }
	    }
	    if(!posFound){
	        i = help.lastPos + help.alphabetLength;
	        i += (i/4);
	        cellsSymbol = Arrays.copyOf(cellsSymbol, i);
	        cellsDest = Arrays.copyOf(cellsDest, i);
	        if(weights){
	        	cellsWeight = Arrays.copyOf(cellsWeight, i);
	        }
	        help.cellsAlloced = i;
	        i = help.lastPos;
	    }
	    int mass = 0;
	    if(weights){
	    	mass = ts.finality ? 1 : 0;
	    }
	    for(j = 0; j < sdStored; j++){
	    	cellsSymbol[i + sd[j].symbol] = sd[j].symbol;
	    	cellsDest[i + sd[j].symbol] = sd[j].dest;
	        if(weights){
	        	cellsWeight[i + sd[j].symbol] = mass;
	        	mass += sd[j].weight;
	        }
	    }
	    if(help.cellsStored < i + help.alphabetLength){
	    	help.cellsStored = i + help.alphabetLength;
	    }
	    if(help.lastPos < i + sd[sdStored-1].symbol){
	    	help.lastPos = i + sd[sdStored-1].symbol + 1;
	    }
	    return i;
	}

	public void close(TarjanTableBuildHelp help){
		cellsSymbol = Arrays.copyOf(cellsSymbol, help.cellsStored);
		cellsDest = Arrays.copyOf(cellsDest, help.cellsStored);
		if(weights){
			cellsWeight = Arrays.copyOf(cellsWeight, help.cellsStored);
		}
	}
}
