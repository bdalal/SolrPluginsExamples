// Use the following code with this presentation here for a detailed understanding: 
// http://www.slideshare.net/searchbox-com/tutorial-on-developin-a-solr-search-component-plugin

package component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentDemo extends SearchComponent {

	private static Logger LOGGER = LoggerFactory.getLogger(ComponentDemo.class);
	volatile long numRequests;
	volatile long totalRequestsTime;
	private String defaultField;
	private List<String> words;
	
	// There are additional functions that can be overriden. For the complete list look here:
	// https://lucene.apache.org/solr/5_4_1/solr-core/org/apache/solr/handler/component/SearchComponent.html

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {
		// Called before the process() method for every request. Use this to initialize request independent variables.
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void process(ResponseBuilder rb) throws IOException {
		
		// Process the response received from the preceding search component.

		numRequests++;

		SolrParams params = rb.req.getParams();
		long lstartTime = System.currentTimeMillis();
		SolrIndexSearcher searcher = rb.req.getSearcher();

		NamedList response = new SimpleOrderedMap();

		String queryField = params.get("field");
		String field = null;
		
		if(queryField != null){
			field = queryField;
		} else if(defaultField != null){
			field = defaultField;
		} else{
			LOGGER.error("Fields aren't defined, not performing counting.");
			return;
		}

		DocList docs = rb.getResults().docList;
		if (docs == null || docs.size() == 0) {
			LOGGER.info("No results");
			return;
		}
		LOGGER.info("Doing these many docs: " + docs.size());

		Set<String> fieldSet = new HashSet<String>();

		SchemaField keyField = rb.req.getCore().getLatestSchema().getUniqueKeyField();
		if (null != keyField) {
			fieldSet.add(keyField.getName());
		}

		fieldSet.add(field);
		DocIterator iterator = docs.iterator();
		for (int i = 0; i < docs.size(); i++) {
			try {
				int docId = iterator.nextDoc();
				HashMap<String, Double> counts = new HashMap<String, Double>();

				Document doc = searcher.doc(docId, fieldSet);
				IndexableField[] multifield = doc.getFields(field);
				for (IndexableField singlefield : multifield) {
					for (String string : singlefield.stringValue().split(" ")) {
						if (words.contains(string)) {
							Double oldcount = counts.containsKey(string) ? counts.get(string) : 0;
							counts.put(string, oldcount + 1);
						}
					}
				}

				String id = doc.getField(keyField.getName()).stringValue();

				NamedList<Double> docresults = new NamedList<Double>();
				for (String word : words) {
					docresults.add(word, counts.get(word));
				}

				response.add(id, docresults);
			} catch (IOException ex) {

				java.util.logging.Logger.getLogger(ComponentDemo.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		rb.rsp.add("demoSearchComponent", response);
		totalRequestsTime += System.currentTimeMillis() - lstartTime;
	}

	@Override
	public String getDescription() {
		return "Some description regarding the component";
	}

	@Override
	public String getSource() {
		return "Source of the component";
	}
	
	@Override
	public NamedList<Object> getStatistics(){
		
		// Return statistics related to the component. In this case, no. of requests and time taken by component for processing.
		
		NamedList<Object> stats = new SimpleOrderedMap<Object>();
		stats.add("Number of requests", numRequests);
		stats.add("Time Taken", totalRequestsTime);
		return stats;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init(NamedList args) {
		
		// Called once when the component is first loaded. Use to load data structures etc. that need to be in memory, for e.g., for lookups like HashMaps etc.
		// args contains the xml entries made under the search component declaration in solrconfig.xml
		
		super.init(args);
		defaultField = (String) args.get("field");
		if (defaultField == null) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Need to specify the default for analysis");
		}
		words = ((NamedList) args.get("words")).getAll("word");
		if (words.isEmpty()) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
					"Need to specify at least one word in searchComponent config!");
		}
	}

}