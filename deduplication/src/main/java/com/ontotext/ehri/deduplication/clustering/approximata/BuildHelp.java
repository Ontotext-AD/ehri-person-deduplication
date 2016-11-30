package com.ontotext.ehri.deduplication.clustering.approximata;

import java.util.Arrays;

public class BuildHelp {
	public int[] hash;
	public TempState[] ts;
	public TarjanTableBuildHelp ttBuildHelp;
	public int statesStored;
	public int statesAlloced;

	public BuildHelp(int alphabetLength){
		ts = new TempState[1024];

		int i;
		for(i = 0; i < ts.length; i++){
			ts[i] = new TempState();
		}
		ttBuildHelp = new TarjanTableBuildHelp(alphabetLength);
		statesAlloced = 1024;
		hash = new int[511];
		for(i = 0; i < hash.length; i++){
			hash[i] = -1;
		}
	}

	public void clearTempState(int st) {
		if(ts.length <= st){
			int prevLength = ts.length;
			ts = Arrays.copyOf(ts, 2*(st + 1));
			for(int i = prevLength; i < ts.length; i++){
				ts[i] = new TempState();
			}
		}
		ts[st].sdStored = 0;
		ts[st].finality = false;
	}
}
