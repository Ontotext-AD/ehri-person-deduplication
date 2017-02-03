package com.ontotext.ehri.deduplication.clustering.approximata;

import java.util.Arrays;



public class Matrix {
	public static final int NO = -1;
	private int type;
	public int distance;
	private IntSequence matrix;
	public CharSequence word;
	public CharSequence query;
	public int queryLength;

	public Matrix(){
		matrix = new IntSequence(128);
		word = new CharSequence();
	}

	public void reset(CharSequence query, int type, int distance){
		this.query = query;
		queryLength = query.seqStored;
		this.type = type;
		this.distance = distance;
		word.seqStored = 0;
		int b = (distance < queryLength) ? distance : queryLength;
		for(int c = 0; c <= b; c++){
			set(0, c, c);
		}
	}

	public void reset(int type, int distance) {
		reset(query, type, distance);
	}

	private void set(int r, int c, int val) {
		int pos = toPos(r, c);
		if(pos == NO){
			return;
		}
		if( matrix.seq.length <= pos){
			matrix.seq = Arrays.copyOf(matrix.seq, 2*pos);
		}
		matrix.seq[pos] = val;
	}
	
	private int get(int r, int c) {
		int pos = toPos(r, c);
		if(pos == NO){
			return NO;
		}
		return matrix.seq[pos];
	}

	private int toPos(int r, int c) {
		if( r < c ){
			if( c - r > distance ){
				return NO;
			}
			return 2*c*(distance + 1) + distance - r;
		}
		if( r - c > distance ){
			return NO;
		}
		return distance*(2*r + 1) + c;
	}

	public int stateFinality(int state) {
		int a, b;

		b = distance + state;
		if( b < 2*distance ){
			a = 0;
		}
		else{
			a = b - 2*distance;
		}
		if( a <= queryLength && queryLength <= b ){
			a = get(state, queryLength);
			if( a > distance ){
				return NO;
			}
			return a;
		}
		return NO;
	}

	public boolean delta(int state, char letter) {
		boolean defined = false;
		word.add(letter);
		state++;
	    int b = distance + state;
	    int a;
	    if( b < 2*distance ){
	        a = 0;
	    }
	    else{
	        a = b - 2*distance;
	    }
	    if( b > queryLength ){
	        b = queryLength;
	    }
	    int v, val;
	    for( int c = a; c <= b; c++ ){
	        //insertion
	        val = get( state-1, c );
	        if( val != NO ){
	            val++;
	        }
	        if( c > 0 ){
	            //deletion
	            v = get( state, c-1 );
	            if( v != NO ){
	                v++;
		            if( val == NO || v < val ){
		                val = v;
		            }
	            }
	            if( letter == query.seq[c-1] ){
	                //identity
	                v = get( state-1, c-1 );
	                if( val == NO || (v != NO && v < val) ){
	                    val = v;
	                }
	            }
	            else{
	                //substitution
                    v = get( state-1, c-1 );
                    if( v != NO ){
                        v++;
                        if( val == NO || v < val ){
                            val = v;
                        }
                    }
	            }
	        }
	        switch( type ){
	            case Approximate.TYPE_TRANSPOSITION:
	                if( state > 1 && c > 1 && query.seq[c-2] == word.seq[state-1] && query.seq[c-1] == word.seq[state-2] ){
	                    //transposition
	                    v = get( state-2, c-2 );
	                    if( v != NO ){
	                        v++;
		                    if( val == NO || v < val ){
		                        val = v;
		                    }
	                    }
	                }
	                break;

	            case Approximate.TYPE_MERGE_AND_SPLIT:
	                if( c > 1 ){
	                    //merge
	                    v = get( state-1, c-2 );
	                    if( v != NO ){
	                        v++;
		                    if( val == NO || v < val ){
		                        val = v;
		                    }
	                    }
	                }
	                if( state > 1 && c > 0 ){
	                    //split
	                    v = get( state-2, c-1 );
	                    if( v != NO ){
	                        v++;
		                    if( val == NO || v < val ){
		                        val = v;
		                    }
	                    }
	                }
	                break;
	        }
	        set( state, c, val );
	        if( val != NO && val <= distance ){
	            defined = true;
	        }
	    }
	    if( !defined ){
    		word.seqStored--;
	    }
    	return defined;
	}
}
