package com.ontotext.ehri.deduplication.clustering.approximata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Arrays;

public class StringToStrings implements Serializable{
	private static final long serialVersionUID = 1L;
	private MinAcyclicFSA domain;
	private MinAcyclicFSA range;
	private int[][] mapping;

	public StringToStrings(String[] data){
		String[] d = new String[data.length];
		String[] r = new String[data.length];
		int i, tab;
		for(i = 0; i < data.length; i++){
			if((tab = data[i].indexOf('\t')) == -1){
				d[i] = data[i];
				r[i] = "";
			}
			else{
				d[i] = data[i].substring(0,tab);
				r[i] = data[i].substring(tab+1);
			}
		}
		domain = new MinAcyclicFSA(normalize(d), true);
		range = new MinAcyclicFSA(normalize(r), true);
		mapping = new int[domain.getNumberOfStrings()][];
		for(i = 0; i < mapping.length; i++){
			mapping[i] = new int[0];
		}
		d = null;
		r = null;
		int xId, yId;
		for(i = 0; i < data.length; i++){
			if((tab = data[i].indexOf('\t')) == -1){
				xId = domain.stringToInt(data[i]);
				yId = range.stringToInt("");
			}
			else{
				xId = domain.stringToInt(data[i].substring(0,tab));
				yId = range.stringToInt(data[i].substring(tab+1));
			}
			mapping[xId] = Arrays.copyOf(mapping[xId], mapping[xId].length + 1);
			mapping[xId][mapping[xId].length - 1] = yId;
		}
		for(i = 0; i < mapping.length; i++){
			Arrays.sort(mapping[i]);
		}
	}

	public String[] get(String x){
		int xId = domain.stringToInt(x);
		if(xId == -1){
			return new String[0];
		}
		String[] ys = new String[mapping[xId].length];
		for(int i = 0; i < ys.length; i++){
			ys[i] = range.intToString(mapping[xId][i]);
		}
		return ys;
	}

	public MinAcyclicFSA getDomain(){
		return domain;
	}

	public void generateLanguage(File file, String outputCharSet) throws IOException{
    	FileOutputStream fos = null;
    	OutputStreamWriter osw = null;
    	try{
    		fos = new FileOutputStream(file);
    		osw = new OutputStreamWriter(fos, outputCharSet);

    		String x, y;
    		int j;
    		for(int i = 0; i < mapping.length; i++){
    			x = domain.intToString(i);
    			for(j = 0; j < mapping[i].length; j++){
    				y = range.intToString(mapping[i][j]);
    				osw.write(x); osw.write("\t"); osw.write(y); osw.write("\n");
    				osw.flush();
    			}
    		}
    	}
    	finally{
    		if( fos != null ){
    			try{ fos.close(); }catch(Exception e){}
    		}
    		if( osw != null ){
    			try{ osw.close(); }catch(Exception e){}
    		}
    	}
	}

	private String[] normalize(String[] a) {
		Arrays.sort(a);
		if(a.length < 2){
			return a;
		}
		int j = 0;
		for(int i = 1; i < a.length; i++){
			if(a[j].equals(a[i])){
				continue;
			}
			j++;
			if(j != i){
				a[j] = a[i];
			}
		}
		j++;
		if(j < a.length){
			a = Arrays.copyOf(a, j);
		}
		return a;
	}
}
