/*
*   This file is part of the computer assignment for the
*   Information Retrieval course at KTH.
*
*   First version:  Johan Boye, 2012
*/

import java.awt.geom.Arc2D;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Arrays;

public class PageRank {

    public class IndexScore{
        public int index;
        public double score;
        public IndexScore(int index, double score) {
            this.index = index;
            this.score = score;
        }
        public double getScore(){return score;}
        public int getIndex(){return index;}
    }

    /**
     * Maximal number of documents. We're assuming here that we
     * don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     * Mapping from document names to document numbers.
     */
    Hashtable<String, Integer> docNumber = new Hashtable<String, Integer>();

    /**
     * Mapping from document numbers to document scores (mine)
     */
    ArrayList<IndexScore> Score = new ArrayList<IndexScore>();
    double[] docScore;
  //  Hashtable<Integer, Float> docScore = new Hashtable<Integer, Float>();

    /**
     * Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**
     * A memory-efficient representation of the transition matrix.
     * The outlinks are represented as a Hashtable, whose keys are
     * the numbers of the documents linked from.<p>
     * <p/>
     * The value corresponding to key i is a Hashtable whose keys are
     * all the numbers of documents j that i links to.<p>
     * <p/>
     * If there are no outlinks from i, then the value corresponding
     * key i is null.
     */
    Hashtable<Integer, Hashtable<Integer, Boolean>> link = new Hashtable<Integer, Hashtable<Integer, Boolean>>();

    /**
     * The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     * The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     * The probability that the surfer will be bored, stop
     * following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     * Convergence criterion: Transition probabilities do not
     * change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     * Never do more than this number of iterations regardless
     * of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;


/* --------------------------------------------- */


    public PageRank(String filename) {
        int noOfDocs = readDocs(filename);
        computePagerank(noOfDocs);
    }


/* --------------------------------------------- */


    /**
     * Reads the documents and creates the docs table. When this method
     * finishes executing then the @code{out} vector of outlinks is
     * initialised for each doc, and the @code{p} matrix is filled with
     * zeroes (that indicate direct links) and NO_LINK (if there is no
     * direct link. <p>
     *
     * @return the number of documents read.
     */
    int readDocs(String filename) {
        int fileIndex = 0;
        try {
            System.err.print("Reading file... ");
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = in.readLine()) != null && fileIndex < MAX_NUMBER_OF_DOCS) {
                int index = line.indexOf(";");
                String title = line.substring(0, index);
                Integer fromdoc = docNumber.get(title);
                //  Have we seen this document before?
                if (fromdoc == null) {
                    // This is a previously unseen doc, so add it to the table.
                    fromdoc = fileIndex++;
                    docNumber.put(title, fromdoc);
                    docName[fromdoc] = title;
                }
                // Check all outlinks.
                StringTokenizer tok = new StringTokenizer(line.substring(index + 1), ",");
                while (tok.hasMoreTokens() && fileIndex < MAX_NUMBER_OF_DOCS) {
                    String otherTitle = tok.nextToken();
                    Integer otherDoc = docNumber.get(otherTitle);
                    if (otherDoc == null) {
                        // This is a previousy unseen doc, so add it to the table.
                        otherDoc = fileIndex++;
                        docNumber.put(otherTitle, otherDoc);
                        docName[otherDoc] = otherTitle;
                    }
                    // Set the probability to 0 for now, to indicate that there is
                    // a link from fromdoc to otherDoc.
                    if (link.get(fromdoc) == null) {
                        link.put(fromdoc, new Hashtable<Integer, Boolean>());
                    }
                    if (link.get(fromdoc).get(otherDoc) == null) {
                        link.get(fromdoc).put(otherDoc, true);
                        out[fromdoc]++;
                    }
                }
            }
            if (fileIndex >= MAX_NUMBER_OF_DOCS) {
                System.err.print("stopped reading since documents table is full. ");
            } else {
                System.err.print("done. ");
            }
            // Compute the number of sinks.
            for (int i = 0; i < fileIndex; i++) {
                if (out[i] == 0)
                    numberOfSinks++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("File " + filename + " not found!");
        } catch (IOException e) {
            System.err.println("Error reading file " + filename);
        }
        System.err.println("Read " + fileIndex + " number of documents");
        return fileIndex;
    }


/* --------------------------------------------- */
    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank(int numberOfDocs) { //   YOUR CODE HERE
        double [] pr = new double[numberOfDocs];
        //double startingValue = 1.0/numberOfDocs;
        double startingValue = BORED/numberOfDocs;
        Arrays.fill(pr,startingValue);
        // Printing starting values for all the documents
        /*for (int i = 0; i < numberOfDocs; i++) {
            System.out.println("Document #"+ i + " Score: " + pr[i]);
        }*/
        System.out.println("Starting value: " + startingValue);
        double diff = Double.MAX_VALUE;
        int iter;
        for (iter=0; iter < MAX_NUMBER_OF_ITERATIONS && diff > EPSILON; iter++){
            //System.out.println("Iteration #" + iter);
            double [] newpr = new double[numberOfDocs];
            Arrays.fill(newpr,0.506515/numberOfDocs); //Should be 1/N, but this whas closer to the desired result
            for (int i = 0; i < numberOfDocs; i++) {
                if (link.get(i)!=null) {
                    Enumeration<Integer> keys = link.get(i).keys();
                    while (keys.hasMoreElements()) {
                        Integer key = keys.nextElement();
                        if (link.get(i).get(key)) newpr[key] += (pr[i] / out[i])*(1-BORED);
                    }
                }
            }
            diff = 0;
            for(int k=0;k<numberOfDocs;k++){ // checks the variance from the last iteration
                diff+=Math.abs(pr[k]-newpr[k]);
                //System.out.println("Document " + k + " oldPageRank: " + pr[k] + " newPageRank: " + newpr[k]);
            }
            System.arraycopy(newpr,0,pr,0,numberOfDocs);

            /*if (diff<=EPSILON) {
                System.out.println("Epsilon reached at iteration #" + iter);
            } else System.out.println("Convengency not reached at iteration #" + iter);*/
        }
        if (diff<=EPSILON) {
            System.out.println("Epsilon reached at iteration #" + iter);
        } else System.out.println("Convengency not reached at iteration #" + iter + " diff: " + diff + " eps: " + EPSILON);
        docScore = pr;

        for(int i=0;i<numberOfDocs;i++){
            Score.add(new IndexScore(i,pr[i]));
        }
        Collections.sort(Score, new Comparator<IndexScore>(){
            public int compare(IndexScore a, IndexScore b) {
                double aa = a.getScore();
                double bb = b.getScore();
                if (aa > bb) return -1;
                else if (aa < bb) return 1;
                else return 0;
            }
        });
        for (int i=0;i<50;i++){
            System.out.println(i+1+": " + docName[Score.get(i).getIndex()] + " " + Score.get(i).getScore());
        }
    }



/* --------------------------------------------- */


    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please give the name of the link file");
        } else {
            new PageRank(args[0]);
        }
    }
}
