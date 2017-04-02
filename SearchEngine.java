import java.util.*;
import java.io.*;

// This class implements a google-like search engine
public class SearchEngine {

    public HashMap<String, LinkedList<String> > wordIndex;                  // this will contain a set of pairs (String, LinkedList of Strings)	
    public DirectedGraph internet;             // this is our internet graph
    
    
    
    // Constructor initializes everything to empty data structures
    // It also sets the location of the internet files
    SearchEngine() {
	// Below is the directory that contains all the internet files
	HtmlParsing.internetFilesLocation = "internetFiles";
	wordIndex = new HashMap<String, LinkedList<String> > ();		
	internet = new DirectedGraph();				
    } // end of constructor//2017
    
    
    // Returns a String description of a searchEngine
    public String toString () {
	return "wordIndex:\n" + wordIndex + "\ninternet:\n" + internet;
    }
    
    
    // This does a graph traversal of the internet, starting at the given url.
    // For each new vertex seen, it updates the wordIndex, the internet graph,
    // and the set of visited vertices.
    
    void traverseInternet(String url) throws Exception {
    	//We will begin our graph traversal. We will use a Recursive Depth-First
    	//Search Algorithm. 
    	Stack<String> search = new Stack<String>();
    	//Add the first vertex to the graph.
    	internet.addVertex(url);
    	internet.setVisited(url, true);
    	search.push(url);
    	while (!search.empty()) {
    		String client = search.pop();
	    	//Create two LinkedLists: one to store the content and one to save the links. 
	    	//The links are needed to establish the vertices; neighbors. 
	    	LinkedList<String> links = HtmlParsing.getLinks(client);
	    	Iterator<String> traverseLinks = links.iterator();
	    	while (traverseLinks.hasNext()) {
	    		String page = traverseLinks.next();
	    		if (internet.getVertices().contains(page)) {
	    			internet.addEdge(client, page);
	    		}
	    		else {
	    			internet.addVertex(page);
	    			internet.addEdge(client, page);
	    		}
	    	}
	    	
	    	LinkedList<String> content = HtmlParsing.getContent(client);
	    	Iterator<String> i = content.iterator();
	    	//For each String Token: add it to the wordindex. If the word is already in the 
	    	//word index, then add only the link where it was encountered again.
	    	while (i.hasNext()) {
	    		String token = i.next();
	    		if (wordIndex.containsKey(token)) {
	    			wordIndex.get(token).addLast(client);
	    		}
	    		else {
	    			LinkedList<String> toToken = new LinkedList<String>();
	    			toToken.addLast(client);
	    			wordIndex.put(token, toToken);
	    		}
	    	}
	    	//We proceed with the graph traversal and perform the operations on all vertices. 
	    	Iterator<String> vertexCheck = internet.getNeighbors(client).iterator();
	    	while (vertexCheck.hasNext()) {
	    		String toCheck = vertexCheck.next();
	    		if (!internet.getVisited(toCheck)) {
	    			internet.setVisited(toCheck, true);
	    			search.push(toCheck);
	    		}
	    	}
    	}	
    } // end of traverseInternet
    
    
    /* This computes the pageRanks for every vertex in the internet graph.
       It will only be called after the internet graph has been constructed using 
       traverseInternet.
       Use the iterative procedure described in the text of the assignment to
       compute the pageRanks for every vertices in the graph. 
       
       This method will probably fit in about 30 lines.
    */
    void computePageRanks() {
    	//We will need to iterate through all the vertices of the graph: set up iterator. 
    	Iterator<String> i = internet.getVertices().iterator();
    	//We set the Page Rank of each to 1.
    	while (i.hasNext()) {
    		String page = i.next();
    		internet.setPageRank(page, 1);
    	}
    	//We will perform 100 iterations just to be sure of a convergence.
    	for (int k = 0; k < 100; k++) {
    		//Iterate through each vertex of the graph.
    		Iterator<String> checkPR = internet.getVertices().iterator();
    		while(checkPR.hasNext()) {
    			String currentToken = checkPR.next();
    			double prCurrentToken;
    			double sumPrOfEdges = 0;
    			//Iterate through all of the current Vertex's neighbors, get sum the ratio of their page rank and out degree, and store it.
    			Iterator<String> edgesIn = internet.getEdgesInto(currentToken).iterator();
    			while (edgesIn.hasNext()) {
    				String currentEdge = edgesIn.next();
    				sumPrOfEdges += (internet.getPageRank(currentEdge)/internet.getOutDegree(currentEdge));
    			}
    			//Final computation to get the page rank and assign it to the vertex. 
    			prCurrentToken = (sumPrOfEdges * 0.5) + 0.5;
    			internet.setPageRank(currentToken, prCurrentToken);
    		}
    	}
    	
    } // end of computePageRanks
    
	
    /* Returns the URL of the page with the high page-rank containing the query word
       Returns the String "" if no web site contains the query.
       This method can only be called after the computePageRanks method has been executed.
       Start by obtaining the list of URLs containing the query word. Then return the URL 
       with the highest pageRank.
       This method should take about 25 lines of code.
    */
    String getBestURL(String query) {
    	//If the wordIndex contains the query, then we look at the links where the query 
    	//can be found.
    	if (wordIndex.containsKey(query)) {
    		//Go through the linked list of links, find, and store the highest Page Rank and 
    		// the URL to which it belongs.
    		LinkedList<String> linksToQuery = wordIndex.get(query);
    		Iterator<String> findPr = linksToQuery.iterator();
    		double maxPr = 0;
    		String maxURL = "";
    		while (findPr.hasNext()) {
    			String currentPage = findPr.next();
    			if (internet.getPageRank(currentPage) > maxPr) {
    				maxPr = internet.getPageRank(currentPage);
    				maxURL = currentPage;
    			}
    		}
    		//Print the pr like in the example.
    		System.out.println("pr = " + maxPr);
    		return maxURL;
    	}
    	//If the word index does not contain the query, return nothing.
    	else {
    		return "";
    	}
    } // end of getBestURL
    
    
	
    public static void main(String args[]) throws Exception{		
	SearchEngine mySearchEngine = new SearchEngine();
	// to debug your program, start with.
	//mySearchEngine.traverseInternet("http://www.cs.mcgill.ca/~blanchem/250/a.html");
	// When your program is working on the small example, move on to
	mySearchEngine.traverseInternet("http://www.cs.mcgill.ca");
	
	
	
	mySearchEngine.computePageRanks();
	// this is just for debugging purposes. REMOVE THIS BEFORE SUBMITTING
	//System.out.println(mySearchEngine);
	
	BufferedReader stndin = new BufferedReader(new InputStreamReader(System.in));
	String query;
	do {
	    System.out.print("Enter query: ");
	    query = stndin.readLine();
	    if ( query != null && query.length() > 0 ) {
		System.out.println("Best site = " + mySearchEngine.getBestURL(query));
	    }
	} while (query!=null && query.length()>0);		
    } // end of main
}