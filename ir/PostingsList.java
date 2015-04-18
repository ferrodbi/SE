/*  
*   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;
import java.util.*;
import java.util.LinkedList;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();


    /**  Number of postings in this list  */
    public int size() {
       return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
        return list.get( i );
   }

    //  YOUR CODE HERE
    
   public PostingsEntry getByDocID (int docID) { // optimize it 
       //Object[] v = list.toArray();
        //for (Object p : v ){ // 
       PostingsEntry p = list.getLast();
       if (p!=null && p.docID == docID) return p;
       else return null;/*
        for (PostingsEntry p : list){
            if (((PostingsEntry)p).docID == docID) 
                return (PostingsEntry)p;
        
        }
        //System.out.println("WARNING: getByDocID@PostingsList returns null");
        return null;
        */
   }

   public boolean contains (PostingsEntry pe) {
        return list.contains(pe);
   }

   public boolean contains (int docID){
        Object[] v = list.toArray();
        for (Object p : v ){
            if (((PostingsEntry)p).docID == docID) return true;
        }
        return false;
   }
    public void add(PostingsEntry pe) {
        if (!this.contains(pe.docID)){ //
            list.add(pe);
        } 
    }
    public PostingsEntry poll () {
        return list.poll();
    }

    public ListIterator<PostingsEntry> getIterator () {
        return list.listIterator(0);
    }

    public LinkedList<PostingsEntry> getList() {
        return (LinkedList<PostingsEntry>)list.clone();
    }
    
    public String toString() {
        return list.toString();
        //return "size: " + list.size() + ". Content: " + list.toString();
    }
    
   /* public PostingsList clone() {
        return (LinkedList<PostingsEntry>) list.clone();
    }*/

    
}
    

               
