package com.ontotext.ehri.deduplication.clustering.approximata;

public class TarjanTableBuildHelp {
	public int alphabetLength;
	public int lastPos;
	public int cellsStored;
	public int cellsAlloced;

	public TarjanTableBuildHelp(int alphabetLength){
		this.alphabetLength = alphabetLength;
		cellsAlloced = 4*alphabetLength;
		lastPos = 1;
	}
}
