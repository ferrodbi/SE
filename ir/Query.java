/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */

package ir;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.HashSet;
public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();
    public static final double ALPHA=0.1;
    public static final double BETA=0.9;
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	    StringTokenizer tok = new StringTokenizer( queryString );
	    while ( tok.hasMoreTokens() ) {
	        terms.add( tok.nextToken() );
	        weights.add( new Double(1) );
	    }
        System.out.println("DEBUG Query: " +terms.size() + " terms and " + weights.size() + " weights");
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        queryCopy.terms = (LinkedList<String>) terms.clone();
        queryCopy.weights = (LinkedList<Double>) weights.clone();
        return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    /*public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
        System.out.println("What now?");
    }*/
   public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
	// results contain the ranked list from the current search
	// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
	//  YOUR CODE HERE

        // alpha-update
        System.out.println("DEBUG Query: terms size is " + terms.size()+" and weights size is " + weights.size());
        for(int i=0;i<terms.size();i++){
            weights.add(i,weights.get(i)*ALPHA);

        }
       System.out.println("DEBUG Query: Weights are " + weights);
        //positive weight
        ///normalization
        double norm = 0.0;
        for(int i=0; i<docIsRelevant.length; i++){
            if (docIsRelevant[i]) {
                norm++;//norm = norm + 1;
            }
        }
        norm = 1.0 / norm;
        ///sum
       System.out.println(indexer.index.invertedIndex.keySet() );
       if(indexer.index.invertedIndex != null) System.out.println("InvertedIndex is not null" );
       System.out.println(indexer.index.invertedIndex);
       System.out.println(indexer.index.invertedIndex.get(891+""));
       System.out.println(indexer.index.docIDs);
        for(int i=0; i<docIsRelevant.length; i++) {
            if(docIsRelevant[i]){
                int id = results.get(i).docID;
                System.out.println("DEBUG Query: Doc #"+i+":"+id);
                HashSet<String>words = indexer.index.invertedIndex.get(""+id);
                System.out.println("DEBUG Query: HashSet #"+i+":"+words);
                //LinkedList<String> terms = indexer.index.get(id+"");
                double size = indexer.index.docLengths.get(id+"");
                if (words != null) {
                    //for(int j=0;j<terms.size();j++){
                      //  String term = terms.get(j);
                    for(String word: words){
                        System.out.println("DEBUG Query: Term " + word);
                        PostingsList p = indexer.index.getPostings(word);
                        double tf_score = 0.0;
                        for(PostingsEntry pe:p.list){
                            if(pe.docID==id){
                                tf_score=pe.positions.size();
                            }
                        }
                        ///sum*norm score
                        tf_score = tf_score/size;
                        if(!weights.contains(word)){
                            weights.add(weights.size(), tf_score* BETA * norm);
                            terms.add(word);
                            //debug
                            if(!weights.contains(word)){System.out.println("DEBUG Query: ADDING TERMS HAS PROBLEMS");
                        }
                    } else {
                        weights.add(weights.indexOf(word), weights.get(weights.indexOf(word))+tf_score*BETA*norm);
                        }
                    }
                } else {System.out.println("DEBUG Query: words is null");}
            }
        }System.out.println("DEBUG Query: At the end of relevanceFeedback the query contains " + terms.size() + " terms and " + weights.size() + " weights");
       System.out.println("DEBUG Query: relevanceFeedback finished!");
    }
}

    
