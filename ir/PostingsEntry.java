/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;

    public PostingsEntry(int docID, double score) {
        this.docID = docID;
         this.score = score;
    }
    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
	return Double.compare( other.score, score );
    }

    //
    //  YOUR CODE HERE
    //
    public LinkedList<Integer> positions =  new LinkedList<Integer> ();

    public PostingsEntry(int docID, double score, int position) {
        this.docID = docID;
        this.score = score;
        //this.position = position;
        positions.add(position);
    }

    public PostingsEntry (int docID, double score, LinkedList<Integer> positions) {
        this.docID = docID;
        this.score = score;
        //this.position = position;
        this.positions = positions;
    }

    public void addPosition (int position) {
        //this.position = position;
        positions.add(position);
    }

    public LinkedList<Integer> getList() {
        return (LinkedList<Integer>) positions.clone();
    }

    public String toString() {
        return docID + ": " + score + " " + positions.toString()+ " ";
    }

}

    
