/*  
*   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */

package ir;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    //private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();
    public ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();

    /**  Number of postings in this list  */
    public int size() {
       return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
        return list.get(i);
   }

    //  YOUR CODE HERE
   public PostingsEntry getByDocID (int docID) { // optimize it //only checks the last one, for inserting only
       //Object[] v = list.toArray();
        //for (Object p : v ){ // 
       PostingsEntry p = //list.getLast();
       list.get(list.size()-1);
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
   /* public PostingsEntry getAllByDocID(int docID) {
        for (int i=0;i<list.size();i++) {
            PostingsEntry pe = list.get(i);
            if(pe.docID==docID)
                return pe;
        }
    }*/
    public void sort () {
        Collections.sort(list);
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
        return list.remove(0);

    }

    public ListIterator<PostingsEntry> getIterator () {
        return list.listIterator(0);
    }

    public ArrayList<PostingsEntry> getList(){
        return (ArrayList<PostingsEntry>) list.clone();
    }
    /*public LinkedList<PostingsEntry> getList() {
        return (LinkedList<PostingsEntry>)list.clone();
    }*/
    
    public String toString() {
        return list.toString();
        //return "size: " + list.size() + ". Content: " + list.toString();
    }
    
   /* public PostingsList clone() {
        return (LinkedList<PostingsEntry>) list.clone();
    }*/

    
}
    

               
