package com.ontotext.ehri.deduplication.clustering.approximata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Arrays;

public class MinAcyclicFSA implements Serializable{
	private static final long serialVersionUID = 1L;
	protected Alphabet alphabet;
	protected int initialState;
	protected TarjanTable tt;
	protected int[][] statesTransitionsSymbol;
	protected BitArray statesFinality;
	private boolean perfectHash;
	public int numberOfStrings;

	public static MinAcyclicFSA read(File file) throws IOException, ClassNotFoundException{
    	FileInputStream fis = null;
    	ObjectInputStream in = null;
		MinAcyclicFSA fsa;
		try{
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
			fsa = (MinAcyclicFSA) in.readObject();
		}
		finally{
			if( in != null ){
				try{ in.close(); }catch(Exception e){}
			}
			if( fis != null ){
				try{ fis.close(); }catch(Exception e){}
			}
		}
		return fsa;
	}

	public MinAcyclicFSA(String[] data, boolean perfectHash){
		this.perfectHash = perfectHash;
		alphabet = new Alphabet();
		int i, r, length;
		char ch;
		for(r = 0; r < data.length; r++){
			length = data[r].length();
			for(i = 0; i < length; i++){
				ch = data[r].charAt(i);
				alphabet.add(ch);
			}
		}
		alphabet.close();
		initialState = -1;
		BuildHelp help = new BuildHelp(alphabet.numberOfSymbols);
		tt = new TarjanTable(help.ttBuildHelp, help.statesAlloced, perfectHash);
		statesTransitionsSymbol = new int[help.statesAlloced][];
		statesFinality = new BitArray(help.statesAlloced);
		for(r = 0; r < data.length; r++){
			add(help, data, r);
		}
		close(help, data);
		int numberOfTransitions = 0;
		int numberOfFinalStates = 0;
		for(i = 0; i < statesTransitionsSymbol.length;  i++){
			if(statesFinality.get(i)){
				numberOfFinalStates++;
			}
			numberOfTransitions += statesTransitionsSymbol[i].length;
		}
		numberOfStrings = data.length;
		System.out.println("Number of states: " + statesTransitionsSymbol.length);
		System.out.println("Number of final states: " + numberOfFinalStates);
		System.out.println("Number of transitions: " + numberOfTransitions);
		System.out.println("Number of strings: " + numberOfStrings);
	}

	private void add(BuildHelp help, String[] data, int r){
		String prev;
		if(r == 0){
			prev = "";
		}
		else{
			prev = data[r-1];
		}
		String cur = data[r];
		int lcp = lcp(prev, cur);
		if(lcp == cur.length()){
			help.ts[lcp].finality = true;
			return;
		}
		int i;
		for(i = prev.length(); i > lcp; i--){
			if(perfectHash){
				help.ts[i-1].addTransition(alphabet.symbolToCode[prev.charAt(i-1)], addTempState(help, i), getMass(help.ts[i]));
			}
			else{
				help.ts[i-1].addTransition(alphabet.symbolToCode[prev.charAt(i-1)], addTempState(help, i));
			}
		}
		int curLength = cur.length();
		for( i = lcp + 1; i <= curLength; i++ ){
			help.clearTempState(i);
		}
		help.ts[curLength].finality = true;
	}

	private void close(BuildHelp help, String[] data){
    	String prev = data[data.length-1];
		for(int i = prev.length(); i > 0; i--){
			if(perfectHash){
				help.ts[i-1].addTransition(alphabet.symbolToCode[prev.charAt(i-1)], addTempState(help, i), getMass(help.ts[i]));
			}
			else{
				help.ts[i-1].addTransition(alphabet.symbolToCode[prev.charAt(i-1)], addTempState(help, i));
			}
		}
		initialState = addTempState(help, 0);
		tt.close(help.ttBuildHelp);
		statesTransitionsSymbol = Arrays.copyOf(statesTransitionsSymbol, help.statesStored);
		statesFinality.realloc(help.statesStored);
		tt.statesTransitions = Arrays.copyOf(tt.statesTransitions, help.statesStored);
    }

    private int getMass(TempState s) {
        int mass = s.finality ? 1 : 0;
        for(int i = 0; i < s.sdStored; i++){
            mass += s.sd[i].weight;
        }
        return(mass);
	}

	private int addTempState(BuildHelp help, int s) {
		int i;
		for(i = getHashCode(help, s); help.hash[i] != -1; i = (i + 107) % help.hash.length){
			if(statesAreEqual(help, s, help.hash[i])){
				return help.hash[i];
			}
		}
		help.hash[i] = help.statesStored;
		if(help.statesStored == help.statesAlloced){
			int mem = 2*help.statesStored;
			tt.statesTransitions = Arrays.copyOf(tt.statesTransitions, mem);
			statesTransitionsSymbol = Arrays.copyOf(statesTransitionsSymbol, mem);
			statesFinality.realloc(mem);
			help.statesAlloced = mem;
		}
		tt.statesTransitions[help.statesStored] = tt.add(help.ttBuildHelp, help.ts[s]);
		statesFinality.set(help.statesStored, help.ts[s].finality);
		statesTransitionsSymbol[help.statesStored] = new int[help.ts[s].sdStored];
		for(i = 0; i < help.ts[s].sdStored; i++){
			statesTransitionsSymbol[help.statesStored][i] = help.ts[s].sd[i].symbol;
		}
		help.statesStored++;
		if(10*help.statesStored > 9*help.hash.length){
			help.hash = Arrays.copyOf(help.hash, 2*help.hash.length+1);
			for(i = 0; i < help.hash.length; i++){
				help.hash[i] = -1;
			}
			for(int j = 0; j < help.statesStored; j++){
				for(i = getHashCode(j, help.hash.length); help.hash[i] != -1; i = (i + 107) % help.hash.length);
				help.hash[i] = j;
			}
		}
		return help.statesStored - 1;
	}

	private boolean statesAreEqual(BuildHelp help, int tempState, int state) {
		if(help.ts[tempState].sdStored != statesTransitionsSymbol[state].length){
			return false;
		}
		if(help.ts[tempState].finality){
			if(!statesFinality.get(state)){
				return false;
			}
		}
		else if(statesFinality.get(state)){
			return false;
		}
		for(int i = 0; i < statesTransitionsSymbol[state].length; i++){
			if( statesTransitionsSymbol[state][i] != help.ts[tempState].sd[i].symbol ||
				tt.cellsDest[tt.statesTransitions[state] + statesTransitionsSymbol[state][i]] != help.ts[tempState].sd[i].dest ){
				return false;
			}
		}
		return true;
	}

	private int getHashCode(int state, int hashSize) {
		int code;

		if(statesFinality.get(state)){
			code = 1;
		}
		else{
			code = 2;
		}
		for(int i = 0; i < statesTransitionsSymbol[state].length; i++){
			code = codeInt(code, statesTransitionsSymbol[state][i], hashSize);
			code = codeInt(code, tt.cellsDest[tt.statesTransitions[state] + statesTransitionsSymbol[state][i]], hashSize);
		}
		return code;
	}

	private int getHashCode(BuildHelp help, int s) {
		int code;

		if(help.ts[s].finality){
			code = 1;
		}
		else{
			code = 2;
		}
		for(int i = 0; i < help.ts[s].sdStored; i++){
			code = codeInt(code, help.ts[s].sd[i].symbol, help.hash.length);
			code = codeInt(code, help.ts[s].sd[i].dest, help.hash.length);
		}
		return code;
	}

	private int codeInt(int code, int n, int hashSize) {
		code = (code * 257 + (n & 0x000000ff)) % hashSize;
		code = (code * 257 + ((n & 0x0000ff00) >>> 8)) % hashSize;
		code = (code * 257 + ((n & 0x00ff0000) >>> 16)) % hashSize;
		code = (code * 257 + ((n & 0xff000000) >>> 24)) % hashSize;
		return code;
	}

	private int lcp(String s1, String s2) {
		int l1 = s1.length();
		int l2 = s2.length();
		int i;
		for(i = 0; i < l1 && i < l2; i++){
			if(s1.charAt(i) != s2.charAt(i)){
				break;
			}
		}
		return i;
	}

	public void generateLanguage(File file, String outputCharSet) throws IOException{
    	FileOutputStream fos = null;
    	OutputStreamWriter osw = null;
    	try{
    		fos = new FileOutputStream(file);
    		osw = new OutputStreamWriter(fos, outputCharSet);

    		if(perfectHash){
    			for(int n = 0; n < numberOfStrings; n++ ){
    				osw.write(intToString(n));
    				osw.write("\n");
    				osw.flush();    				
    			}
    		}
    		else{
    			StringBuilder sb = new StringBuilder();
    			generateLanguage(initialState, sb, osw);
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

	private void generateLanguage(int state, StringBuilder sb, OutputStreamWriter osw) throws IOException {
		if(statesFinality.get(state)){
			osw.write(sb.toString());
			osw.write("\n");
			osw.flush();
		}
		int length = sb.length();
		for(int j = 0; j < statesTransitionsSymbol[state].length; j++){
			sb.append(alphabet.codeToSymbol[statesTransitionsSymbol[state][j]]);
			generateLanguage(tt.cellsDest[tt.statesTransitions[state]+statesTransitionsSymbol[state][j]], sb, osw);
			sb.deleteCharAt(length);
		}
	}

    public int stringToInt(String str){
    	int st = initialState;
    	int tr, letter, n, length;

    	length = str.length();
    	n = 0;
        for(int i = 0; i < length; i++){
        	letter = alphabet.symbolToCode[str.charAt(i)];
        	if(letter == 0){
        		return -1;
        	}
        	tr = tt.statesTransitions[st]+letter;
			if(tt.cellsSymbol[tr] != letter){
				return -1;
			}
            n += tt.cellsWeight[tr];
            st = tt.cellsDest[tr];
        }
        if(statesFinality.get(st)){
        	return n;
        }
        return -1;
    }

    public String intToString(int n){
		if(n < 0 || numberOfStrings <= n){
		    return null;
		}
		int i, m, tr, st, letter;
		StringBuilder sb = new StringBuilder();
		i = 0;
		st = initialState;
		while(i != n || !statesFinality.get(st)){
		    for(m = 1; m < statesTransitionsSymbol[st].length; m++){
		    	letter = statesTransitionsSymbol[st][m];
		        tr = tt.statesTransitions[st] + letter;
		        if( i + tt.cellsWeight[tr] > n ){
		            break;
		        }
		    }
	    	letter = statesTransitionsSymbol[st][m-1];
	        tr = tt.statesTransitions[st] + letter;
	        i += tt.cellsWeight[tr];
		    st = tt.cellsDest[tr];
		    sb.append(alphabet.codeToSymbol[letter]);
		}
		return sb.toString();
    }

    public int charSequenceToInt(CharSequence str){
    	int st = initialState;
    	int tr, letter, n;

    	n = 0;
        for(int i = 0; i < str.seqStored; i++){
        	letter = alphabet.symbolToCode[str.seq[i]];
        	if(letter == 0){
        		return -1;
        	}
        	tr = tt.statesTransitions[st]+letter;
			if(tt.cellsSymbol[tr] != letter){
				return -1;
			}
            n += tt.cellsWeight[tr];
            st = tt.cellsDest[tr];
        }
        if(statesFinality.get(st)){
        	return n;
        }
        return -1;
    }
    
    public int reverseCharSequenceToInt(CharSequence str){
    	int st = initialState;
    	int tr, letter, n;

    	n = 0;
        for(int i = str.seqStored-1; i >= 0; i--){
        	letter = alphabet.symbolToCode[str.seq[i]];
        	if(letter == 0){
        		return -1;
        	}
        	tr = tt.statesTransitions[st]+letter;
			if(tt.cellsSymbol[tr] != letter){
				return -1;
			}
            n += tt.cellsWeight[tr];
            st = tt.cellsDest[tr];
        }
        if(statesFinality.get(st)){
        	return n;
        }
        return -1;    	
    }

    public int getNumberOfStrings(){
    	return numberOfStrings;
    }

	private int delta(int state, char symbol) throws Exception{
        int letter = alphabet.symbolToCode[symbol];
        if(letter == 0){
            return -1;
        }
        int tr = tt.statesTransitions[state]+letter;
		if(tt.cellsSymbol[tr] != letter){
			return -1;
		}
		return tt.cellsDest[tr];
	}

	public Pair deltaStar(int state, char[] seq, int pos, int length) throws Exception {
		Pair p = new Pair();
	    p.prevState = -1;
	    p.state = state;
	    for( int i = 0; i < length; i++ ){
	        p.prevState = p.state;
	        p.state = delta( p.state, seq[pos+i] );
	        if( p.state == -1 ){
	            if( i != length-1 ){
	                p.prevState = -1;
	            }
	            return p;
	        }
	    }
		return p;
	}
}
