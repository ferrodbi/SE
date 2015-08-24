/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  
package ir;
import java.lang.String;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.HashSet;
import java.util.Map.*;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.Math;


//Implements an inverted index as a Hashtable from words to PostingsLists.
 
public class HashedIndex implements Index {
    private boolean recovered = false;

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    //Reverse index (new)
    //public HashMap<String,HashSet<String>> invertedIndex = new HashMap<String,HashSet<String>>();

     //  Inserts this token in the index.
    public void insert( String token, int docID, int offset ) {
        //**This is only for Relevance Feedback**
        //Remove this for speeding-up
        /*
        HashSet<String> words = invertedIndex.get(docID+"");
        if(words!=null){
            //invertedIndex.get(docID+"").add(token);
            words.add(token);
            invertedIndex.put(docID + "'", words);
            //System.out.println("HashMap's key: " + docID + " Hashset: " + words);
        } else {
            //System.out.println(">Term was null for docID #"+docID);
            HashSet<String> hs = new HashSet<String>();
            hs.add(token);
            //System.out.println("HashMap's key: " + docID + " Hashset: " + hs);
            invertedIndex.put(docID + "", hs);
        }
        */
        //**Relevance Feedback section ends here.
        PostingsEntry postingsEntry;
        PostingsList postinglist = index.get(token);        
        if (postinglist != null) { 
            postingsEntry = postinglist.getByDocID(docID); // has to be improved, we need to look for the last element
            if (postingsEntry == null){ 
                postingsEntry = new PostingsEntry(docID, 0, offset);
            } else {
                postingsEntry.addPosition(offset);
            }
           postinglist.add(postingsEntry); // comment
        } else { 
            postinglist = new PostingsList();
            postingsEntry = new PostingsEntry(docID, 0, offset); 
            postinglist.add(postingsEntry);
        }
        index.put(token,postinglist);
    }
    //Returns all the words in the index.
    public Iterator<String> getDictionary() {
	   //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        return index.keySet().iterator(); //not sure about this implementation
    }

     // Returns the postings for a specific term, or null if the term is not in the index.
     
    public PostingsList getPostings( String token ) {
	   //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
       return index.get(token);
    }


    // Searches the index for postings matching the query.
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
        int size = query.terms.size();
        PostingsList res = null;
        if (recovered) {
            loadLocal(query); // DEBUGGING: this part is killing the name recovery
        }
        if (size == 1){
            switch (queryType) {
                case Index.INTERSECTION_QUERY:
                case Index.PHRASE_QUERY:
                    res = index.get(query.terms.poll());
                    break;
                case Index.RANKED_QUERY:
                    res = index.get(query.terms.poll());
                    double queryTFScore = Math.log(index.size() / res.size()); //idft
                    for (int i = 0; i < res.size(); i++) {
                        PostingsEntry pe = res.get(i);
                        //System.out.println("Matching document " + i );
                        //System.out.println("DocID:" + pe.docID);
                        //System.out.println("Index size: " + docLengths.size());
                        //System.out.println(docLengths.get(""+12));
                        //System.out.println(pe.positions.size());
                        pe.setScore(pe.positions.size() * queryTFScore / docLengths.get("" + pe.docID));
                        //System.out.println("Matching document " + i + ": TfIdfScore: " + pe.getScore());
                    }
                    res.sort();
                    break;
                default:
                    break;
            }
        }else{ // size > 1
            PostingsList postingsLists[] = new PostingsList[size];
            switch (queryType) {
                case Index.INTERSECTION_QUERY:
                for (int i=0;i<size;i++) { 
                    postingsLists[i] = index.get(query.terms.poll());
                }
                res = postingsLists[0];
                for (int i=1;i<size;i++) {
                    res = intersect(res, postingsLists[i]);
                }
                break;
                case Index.PHRASE_QUERY:
                for (int i=0;i<size;i++) { 
                    postingsLists[i] = index.get(query.terms.poll());
                }
                res = postingsLists[0];
                for (int i=1;i<size;i++) {
                    res = positionalIntersect(res, postingsLists[i],1);
                }
                break;
                case Index.RANKED_QUERY:
                    //size = query.terms.size();
                    System.out.println("DEBUG HashedIndex: size is " + query.terms.size());
                    for (int i=0;i<size;i++) {
                        String s = query.terms.get(i);
                                //query.terms.poll();
                        postingsLists[i] = index.get(s);
                        System.out.println("DEBUG HashedIndex: postingslist #"+i+ " " + s);
                    }
                    res = postingsLists[0];
                    for (int i=1;i< size;i++){
                        res.list.addAll(postingsLists[i].list);
                    }

                    for (int i=0; i<res.size(); i++) {
                        PostingsEntry pe = res.get(i);
                        pe.setScore(0); // cleaning the previous scores
                    }
                    if(res.size()>0) {
                        double queryTFScore = Math.log(((double) index.size()) / (double) res.size()); //idft
                        for (int i=0; i<res.size(); i++){
                            PostingsEntry pe = res.get(i);
                            pe.setScore(pe.getScore() + pe.positions.size() * queryTFScore / (double) docLengths.get("" + pe.docID));
                           }
                        res.sort();
                    }
                    for (int i=0;i<res.size();i++){
                        for (int j=i+1;j<res.size();j++){
                           if (res.get(i).docID==res.get(j).docID){
                               res.list.remove(j);
                               j--;
                            }
                        }
                    }
                    //query = copy;
                    System.out.println("DEBUG HashedIndex: at the end size is " + query.terms.size());
                    break;
                default: 
                System.out.println("SEARCH RETURNED A NULL");
                break;
            }   
        } 
        return res;
    }

    public PostingsList intersect(PostingsList a, PostingsList b) {
        PostingsList res = new PostingsList();
        if (a != null && b != null) {
            ArrayList<PostingsEntry> l1 = a.getList();
            ArrayList<PostingsEntry> l2 = b.getList();
            PostingsEntry pe1 = l1.size()>0?l1.remove(0):null;
            PostingsEntry pe2 = l2.size()>0?l2.remove(0):null;
            while(pe1 != null && pe2 != null){
                if (pe1.docID == pe2.docID) {
                    res.add(pe1);
                    pe1 =l1.size()>0?l1.remove(0):null;
                    pe2 =l2.size()>0?l2.remove(0):null;
                } else if(pe1.docID < pe2.docID) {
                    pe1 = l1.size()>0?l1.remove(0):null;
                } else pe2 = l2.size()>0?l2.remove(0):null;
            }
        }
        return res;
    }
    
    public PostingsList positionalIntersect(PostingsList a, PostingsList b, int dif) {
        PostingsList res = new PostingsList();//DEBUGGING
        //System.out.println("a:: " + a.toString());
        //System.out.println("b:: " + b.toString());
        ArrayList<PostingsEntry> l1 = null;
        ArrayList<PostingsEntry> l2 = null;
        PostingsEntry pe1 = null;
        PostingsEntry pe2 = null;
        if (a != null && b != null) { 
            l1 = a.getList();
            l2 = b.getList();
            pe1 = l1.size()>0?l1.remove(0):null;
            pe2 = l2.size()>0?l2.remove(0):null;
        }
        
        /*DEBUG
        System.out.print("----Contents----");
        System.out.println("\nFile " + pe1.docID);
        for (Integer i : pe1.positions) 
        System.out.print(" " + i);
        System.out.println("\nFile " + pe2.docID);
        for (Integer i : pe2.positions) 
        System.out.print(" " + i);
        System.out.println("");
        */ //END DEBUG
        
        while(pe1 != null && pe2 != null){
            if (pe1.docID == pe2.docID) {
                LinkedList<Integer> laux =  new LinkedList<Integer> ();
                LinkedList<Integer> p1 = pe1.getList();
                LinkedList<Integer> p2 = pe2.getList();
                Integer pp1 = p1.size()>0?p1.remove(0):null;
                Integer pp2 = p2.size()>0?p2.remove(0):null;
                while (pp1 != null){
                    while( pp1 != null &&  pp2 != null) {
                        //System.out.println("Index 1 " + pp1 + " Index 2 " + pp2);
                        //if (pp1 != null) //try to remove this and the break
                            //if ((Math.abs (pp1 - pp2) )<= dif) {
                            if ((pp2 - pp1)>0 && (Math.abs (pp2 - pp1) )<= dif) {
                                laux.add(pp2);
                                //System.out.println("added"); //
                                pp2 = p2.size()>0?p2.remove(0):null;
                            } else if(pp2 > pp1) {
                                //System.out.println("p1 poll"); //
                                pp1 = p1.size()>0?p1.remove(0):null;
                                //break;
                            } else pp2 = p2.size()>0?p2.remove(0):null;
                        }
                        //pp2 = p2.poll();
                       // System.out.println("p2 poll");//
                       // System.out.println("size " + laux.size());
                        if (laux != null && pp1 != null)
                        while (laux.size()>0 && Math.abs(laux.peek() - pp1) > dif)
                            laux.remove();    
                        for(Integer p : laux)
                        res.add(new PostingsEntry(pe1.docID, 0, p)); // p was pp1
                        pp1 = p1.size()>0?p1.remove(0):null;
                }
                pe1 =l1.size()>0?l1.remove(0):null;
                pe2 =l2.size()>0?l2.remove(0):null;
            } else if (pe1.docID < pe2.docID) {
            pe1 = l1.size()>0?l1.remove(0):null;
            } else pe2 = l2.size()>0?l2.remove(0):null;
        }
    return res;
    }

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
        String path = System.getProperty("user.dir") + "/stored/";
        //System.out.println(System.getProperty("user.dir"));
        Set<Map.Entry<String, PostingsList>> set = index.entrySet();
        Iterator<Map.Entry<String, PostingsList>> iterator = set.iterator();
        while(iterator.hasNext() ){
            //System.out.println(iterator.next().toString());
            Entry<String, PostingsList> entry = iterator.next();
            String key = entry.getKey();
            try {
                PrintWriter pw = new PrintWriter(path +key,"UTF-8");
                pw.print(entry.getValue().toString());
                pw.close();
            } catch (IOException e) {
                System.err.print("cleanup " + e);
            }
        }
    }

    public void loadLocal(Query query) { // Loads the documents from the stored index
        System.out.println("Loading local ... ");
        String content = "";
        String path = System.getProperty("user.dir") + "/stored/";
        String token = "";
        // for each file in recover, add namefile as name and content as values
        File folder = new File (path);
        File[] storedIndex = folder.listFiles();
        for (String ss : query.terms) {
            try {
                token = ss;
                PostingsList pl = new PostingsList();
                content = new String(Files.readAllBytes(Paths.get(path+ss)));
                content = content.substring(1,content.length()-1);
                String[] c = content.split(" , ");
                for( String s : c){
                    String[] v = s.split(": ");
                    int id = Integer.parseInt(v[0].trim());
                    v = v[1].split("\\[");
                    float score = Float.parseFloat(v[0].trim()); 
                    v[1] = v[1].substring(0,v[1].length()-1);
                    v = v[1].split(", ");
                    LinkedList<Integer> positions = new LinkedList<Integer> ();
                    for (String s2 : v) {
                        if(s2.contains("]")){
                            s2 = s2.substring(0,s2.length()-1);
                        }
                        positions.add(Integer.parseInt(s2));
                    }
                    PostingsEntry pe = new PostingsEntry(id, score, positions);
                    pl.add(pe);  
                }
                index.put(token,pl);
            } catch (IOException e) {
                System.err.println("loadlocal " + e);
            }
        }
    }
    public void recover() {
	recovered = true;
        System.out.println("Recovering from stored files ...");
        String content = "";
        String path = System.getProperty("user.dir") + "/stored/";
        String token = "";
        // for each file in recover, add namefile as name and content as values
        File folder = new File (path);
        File[] storedIndex = folder.listFiles();
        for (File f : storedIndex) {
            try {
                token = f.getName();
                PostingsList pl = new PostingsList();
                content = new String(Files.readAllBytes(Paths.get(f.toString())));
                content = content.substring(1,content.length()-1);
                String[] c = content.split(" , ");
                for( String s : c){
                    String[] v = s.split(": ");
                    int id = Integer.parseInt(v[0].trim());
                    v = v[1].split("\\[");
                    float score = Float.parseFloat(v[0].trim()); 
                    v[1] = v[1].substring(0,v[1].length()-1);
                    v = v[1].split(", ");
                    LinkedList<Integer> positions = new LinkedList<Integer> ();
                    for (String s2 : v) {
                        if(s2.contains("]")){
                            s2 = s2.substring(0,s2.length()-1);
                        }
                        positions.add(Integer.parseInt(s2));
                    }
                    PostingsEntry pe = new PostingsEntry(id, score, positions);
                    pl.add(pe);  
                }
                index.put(token,pl);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }
}
