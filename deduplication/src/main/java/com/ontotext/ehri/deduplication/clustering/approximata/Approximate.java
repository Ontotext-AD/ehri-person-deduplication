package com.ontotext.ehri.deduplication.clustering.approximata;

import java.util.Arrays;

public class Approximate {
	public static final int TYPE_LEVENSHTEIN = 0;
	public static final int TYPE_TRANSPOSITION = 1;
	public static final int TYPE_MERGE_AND_SPLIT = 2;

	private IntSequence candidates;
	private int distance;
	private int type;
	private Matrix matrix;
	private Matrix matrix0;
	private MinAcyclicFSA ph;
	private MinAcyclicFSA phFwd;
	private MinAcyclicFSA phBwd;
	private CharSequence word;
	private CharSequence p1;
	private CharSequence p2;
	private int searchCase;
	private int minD;
	private char a;
	private char b;

	private static final int NO = -1;
	private static final int SEARCH_CASE_FORWARD = 0;
	private static final int SEARCH_CASE_BACKWARD = 1;

	public int[] findFwdBwd(MinAcyclicFSA phFwd, MinAcyclicFSA phBwd, String query, int distance, int type) throws Exception{
		this.phFwd = phFwd;
		this.phBwd = phBwd;
		this.distance = distance;
		this.type = type;
		candidates = new IntSequence();
		matrix = new Matrix();
		matrix0 = new Matrix();
		word = new CharSequence();
		p1 = new CharSequence();
		p2 = new CharSequence();

	    int queryLength = query.length();

	    searchCase = SEARCH_CASE_FORWARD;
	    int j = queryLength/2;
	    p1.append(query, 0, j);
	    p2.append(query, j, queryLength-j);
	    ph = phFwd;
	    minD = 0;

	    //[0][n]->
	    int qPrev = S0D(phFwd);

	    //[0][1][n-1]->
	    if( type != TYPE_LEVENSHTEIN && queryLength > 1 && qPrev != NO ){
	        if( distance == 1 ){
	            F010( query, qPrev );
	        }
	        else{
	        	S01D( qPrev, phFwd );
	        }
	    }

	    minD = 1;

	    //[1][n-1]->
	    //[2][n-2]->
	    //[3][n-3]->
	    //..........
	    if( distance > 1 ){
	    	FDD();
	    }

	    //[1][1][n-2]->
	    //[2][1][n-3]->
	    //[3][1][n-4]->
	    //.............
	    if( type != TYPE_LEVENSHTEIN && queryLength > 1 && distance > 2 ){
	    	FD1D();
	    }

	    searchCase = SEARCH_CASE_BACKWARD;
	    j = queryLength/2;
	    p2.seqStored = 0;
	    int i;
	    for( i = j; i > 0; i-- ){
	        p2.add(query.charAt(i-1));
	    }
	    p1.seqStored = 0;
	    for( i = queryLength; i > j; i-- ){
	    	p1.add(query.charAt(i-1));
	    }
	    ph = phBwd;

	    //[n][0]<-
	    qPrev = S0D( phBwd );

	    //[n-1][1][0]<-
	    if( type != TYPE_LEVENSHTEIN && distance > 1 && queryLength > 1 && qPrev != NO ){
	        S01D( qPrev, phBwd );
	    }

	    //[n-1][1]<-
	    //[n-2][2]<-
	    //[n-3][3]<-
	    //..........
	    if( distance > 2 ){
	    	BDD();
	    }

	    //[n-2][1][1]<-
	    //[n-3][1][2]<-
	    //[n-4][1][3]<-
	    //.............
	    if( type != TYPE_LEVENSHTEIN && queryLength > 1 && distance > 3 ){
	    	BD1D();
	    }

		Arrays.sort(candidates.seq, 0, candidates.seqStored);
		String[] ret = new String[candidates.seqStored];
		for (i = 0; i < candidates.seqStored; i++)
			ret[i] = phFwd.intToString(candidates.seq[i]);
        return Arrays.copyOf(candidates.seq, candidates.seqStored);
	}

	protected String[] findFwd(MinAcyclicFSA phFwd, String query, int distance, int type) throws Exception{
		this.phFwd = phFwd;
		this.distance = distance;
		this.type = type;
		candidates = new IntSequence();
		matrix = new Matrix();
		word = new CharSequence();
		matrix.reset(new CharSequence(query), type, distance);
		ph = phFwd;
		searchCase = SEARCH_CASE_FORWARD;
		findRec(0, phFwd.initialState);
		Arrays.sort(candidates.seq, 0, candidates.seqStored);
		String[] ret = new String[candidates.seqStored];
		for(int i = 0; i < candidates.seqStored; i++){
			ret[i] = phFwd.intToString(candidates.seq[i]);
		}
		return ret;
	}

	private int S0D(MinAcyclicFSA phToUse) throws Exception{
		Pair p = phToUse.deltaStar(phToUse.initialState, p1.seq, 0, p1.seqStored);
		if( p.state != -1 ){
			word.cpy( p1.seq, 0, p1.seqStored );
			matrix.reset( p2, type, distance );
			findRec( 0, p.state );
		}
		return p.prevState;
	}

	private void F010( String query, int qPrev ) throws Exception{
	    int queryLength = query.length();
	    int k = queryLength/2;
	    switch( type ){
	        case TYPE_TRANSPOSITION:
	        {
                word.cpy(query, 0, queryLength);
                char tmp = word.seq[k-1]; word.seq[k-1] = word.seq[k]; word.seq[k] = tmp;
                Pair q = phFwd.deltaStar(qPrev, word.seq, k-1, word.seqStored + 1 - k);
                if( q.state != NO && phFwd.statesFinality.get(q.state) ){
                	add();
                }
	        }
	        break;

	        case TYPE_MERGE_AND_SPLIT:
	        {
	        	word.cpy(query, 0, k-1);
	        	word.add(' ');
	        	int r;
                for( r = k+1; r < queryLength; r++ ){
                	word.add(query.charAt(r));
                }
                int letter;
                int tr = phFwd.tt.statesTransitions[qPrev];
                for( r = 0; r < phFwd.statesTransitionsSymbol[qPrev].length; r++ ){
                	letter = phFwd.statesTransitionsSymbol[qPrev][r];
                    word.seq[k-1] = phFwd.alphabet.codeToSymbol[letter];
                    Pair q = phFwd.deltaStar(phFwd.tt.cellsDest[tr+letter], word.seq, k, word.seqStored - k);
                    if( q.state != NO && phFwd.statesFinality.get(q.state) ){
                    	add();
                    }
                }
	        }
	        break;
	    }
	}

	private void S01D( int qPrev, MinAcyclicFSA phToUse ) throws Exception{
        int k = p1.seqStored;
        word.cpy(p1.seq, 0, k-1);
	    switch( type ){
	        case TYPE_TRANSPOSITION:
	            {
	                char[] transp = new char[2];
	                transp[0] = p2.seq[0];
	                transp[1] = p1.seq[k-1];
	                Pair q = phToUse.deltaStar( qPrev, transp, 0, 2 );
	                if( q.state == NO ){
	                    break;
	                }
	                word.append(transp, 0, 2);
	                for(k = 1; k < p2.seqStored; k++){
	                	p2.seq[k-1] = p2.seq[k];
	                }
	                p2.seqStored--;
	                matrix.reset(p2, type, distance-1);
	                findRec(0, q.state);
	                for(k = p2.seqStored-1; k > 0; k--){
	                	p2.seq[k] = p2.seq[k-1];
	                }
	                p2.seq[0] = transp[0];
	                p2.seqStored++;
	            }
	            break;

	        case TYPE_MERGE_AND_SPLIT:
	            {
	            	word.add(' ');
	            	char tmp = p2.seq[0];
	            	int i;
	                for(i = 1; i < p2.seqStored; i++){
	                	p2.seq[i-1] = p2.seq[i];
	                }
	                p2.seqStored--;
	                matrix.reset(p2, type, distance-1);
	                int letter;
	                int tr = phToUse.tt.statesTransitions[qPrev];
	                for( i = 0; i < phToUse.statesTransitionsSymbol[qPrev].length; i++ ){
	                	letter = phToUse.statesTransitionsSymbol[qPrev][i];
	                    word.seq[k-1] = phToUse.alphabet.codeToSymbol[letter];
	                    findRec( 0, phToUse.tt.cellsDest[tr+letter]);
	                }
	                for(i = p2.seqStored-1; i > 0; i--){
	                	p2.seq[i] = p2.seq[i-1];
	                }
	                p2.seq[0] = tmp;
	                p2.seqStored++;
	            }
	            break;
	    }
	}

	private void FDD() throws Exception{
	    ph = phFwd;
	    matrix0.reset(p1, type, distance/2);
	    word.seqStored = 0;
	    matrix.query= p2;
	    findDDRec( 0, phFwd.initialState );
	}

	private void BDD() throws Exception{
	    ph = phBwd;
	    if( distance % 2 == 0 ){
	    	matrix0.reset(p1, type, distance/2-1);
	    }
	    else{
	    	matrix0.reset(p1, type, distance/2);
	    }
	    word.seqStored = 0;
	    matrix.query = p2;
	    findDDRec( 0, phBwd.initialState );
	}

	private void FD1D() throws Exception{
	    a = p1.seq[p1.seqStored - 1];
	    p1.seqStored--;
	    matrix0.reset(p1, type, (distance-1)/2 );
	    b = p2.seq[0];
        for(int k = 1; k < p2.seqStored; k++){
        	p2.seq[k-1] = p2.seq[k];
        }
        p2.seqStored--;
        matrix.query = p2;
	    ph = phFwd;
	    word.seqStored = 0;
	    findD1DRec( 0, phFwd.initialState );
	}

	private void BD1D() throws Exception{
	    a = p1.seq[p1.seqStored - 1];
	    p1.seqStored--;
	    if(distance % 2 == 1){
	    	matrix0.reset(p1, type, (distance-1)/2 - 1);
	    }
	    else{
	    	matrix0.reset(p1, type, (distance-1)/2);
	    }
	    b = p2.seq[0];
        for(int k = 1; k < p2.seqStored; k++){
        	p2.seq[k-1] = p2.seq[k];
        }
        p2.seqStored--;
        matrix.query = p2;
        ph = phBwd;
	    word.seqStored = 0;
	    findD1DRec( 0, phBwd.initialState );
	}

	private void findD1DRec( int mState, int phState ) throws Exception{
	    int sf = matrix0.stateFinality(mState);
	    int tr, i;
	    if( 0 < sf && sf != Matrix.NO ){
	        switch( type ){
	            case TYPE_TRANSPOSITION:
	                {
	                    char[] transp = new char[2];

	                    transp[0] = b;
	                    transp[1] = a;
	                    Pair q = ph.deltaStar( phState, transp, 0, 2 );
	                    if( q.state == NO ){
	                        break;
	                    }
	                    word.append(transp, 0, 2);
	                    matrix.reset(type, distance - 1 - sf);
	                    findRec( 0, q.state );
	                    word.seqStored -= 2;
	                }
	                break;

	            case TYPE_MERGE_AND_SPLIT:
	                {
	                    int k = word.seqStored;
	                    word.add(' ');
	                    matrix.reset(type, distance - 1 - sf);
	                    int letter;
	                    tr = ph.tt.statesTransitions[phState];
	                    for( i = 0; i < ph.statesTransitionsSymbol[phState].length; i++ ){
	                    	letter = ph.statesTransitionsSymbol[phState][i];
	                        word.seq[k] = ph.alphabet.codeToSymbol[letter];
	                        findRec( 0, ph.tt.cellsDest[tr+letter] );
	                    }
	                    word.seqStored--;
	                }
	                break;
	        }
	    }
	    if( matrix0.queryLength + matrix0.distance <= mState ){
	        return;
	    }
        tr = ph.tt.statesTransitions[phState];
        char c;
        int letter;
        for( i = 0; i < ph.statesTransitionsSymbol[phState].length; i++ ){
        	letter = ph.statesTransitionsSymbol[phState][i];
        	c = ph.alphabet.codeToSymbol[letter];
        	if( !matrix0.delta(mState, c) ){
        		continue;
        	}
        	word.add(c);
	        findD1DRec( mState + 1, ph.tt.cellsDest[tr+letter] );
	        word.seqStored--;
	        matrix0.word.seqStored--;
        }
	}

	private void findDDRec( int mState, int phState ) throws Exception{
	    int sf = matrix0.stateFinality(mState);
	    if( 0 < sf && sf != NO ){
	    	matrix.reset(type, distance - sf);
	    	findRec( 0, phState );
	    }
	    if( matrix0.queryLength + matrix0.distance <= mState ){
	        return;
	    }
	    int tr = ph.tt.statesTransitions[phState];
	    int letter;
	    char c;
	    for( int i = 0; i < ph.statesTransitionsSymbol[phState].length; i++ ){
	        letter = ph.statesTransitionsSymbol[phState][i];
	        c = ph.alphabet.codeToSymbol[letter];
	        if( !matrix0.delta(mState, c) ){
	            continue;
	        }
	        word.add(c);
	        findDDRec( mState + 1, ph.tt.cellsDest[tr+letter] );
	        word.seqStored--;
	        matrix0.word.seqStored--;
	    }
	}

	private void findRec(int mState, int phState) throws Exception {
	    int sf = matrix.stateFinality(mState);
	    if( minD <= sf && sf != Matrix.NO && ph.statesFinality.get(phState) ){
	        add();
	    }
	    if( matrix.queryLength + matrix.distance <= mState ){
	        return;
	    }
		int tr = ph.tt.statesTransitions[phState];
		int letter;
		char c;
		for( int i = 0; i < ph.statesTransitionsSymbol[phState].length; i++ ){
			letter = ph.statesTransitionsSymbol[phState][i];
			c = ph.alphabet.codeToSymbol[letter];
			if( !matrix.delta(mState, c) ){
				continue;
			}
			word.add(c);
			findRec( mState + 1, ph.tt.cellsDest[tr+letter] );
			word.seqStored--;
			matrix.word.seqStored--;
		}
	}

	private void add() throws Exception {
		int w;
		if(searchCase == SEARCH_CASE_FORWARD){
			w = phFwd.charSequenceToInt(word);
		}
		else{
			w = phFwd.reverseCharSequenceToInt(word);
		}
		if(candidates.contains(w)){
			return;
		}
		candidates.add(w);
	}
}
