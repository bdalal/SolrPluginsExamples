// Use the following code with this presentation here for a detailed understanding: 
// http://www.slideshare.net/searchbox-com/develop-a-solr-request-handler-plugin

package requesthandler;

import java.util.HashMap;
import java.util.List;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

public class RequestHandler extends RequestHandlerBase {

	volatile long numRequests;
	volatile long totalRequestsTime;
	private List<String> words;

	@Override
	public String getDescription() {
		return "Some description regarding the component";
	}

	@Override
	public void handleRequestBody(SolrQueryRequest arg0, SolrQueryResponse arg1) throws Exception {
		// Process the incoming request

		long starttime = System.currentTimeMillis();

		++numRequests;

		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		SolrParams params = arg0.getParams();
		String query = params.get(CommonParams.Q);
		for (String string : query.split(" ")) {
			if (words.contains(string)) {
				Integer count = counts.containsKey(string) ? counts.get(string) : 0;
				counts.put(string, ++count);
			}
		}
		arg1.add("Results", counts);
		totalRequestsTime += System.currentTimeMillis() - starttime;
	}

	@Override
	public String getSource() {
		return "Source of the component";
	}

	@Override
	public NamedList<Object> getStatistics() {

		// Return statistics related to the component. In this case, no. of
		// requests and time taken by the request handler for processing.

		NamedList<Object> stats = new SimpleOrderedMap<Object>();
		stats.add("Number of requests", numRequests);
		stats.add("Time Taken", totalRequestsTime);
		return stats;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init(NamedList args) {

		// Called once when the component is first loaded. Use to load data
		// structures etc. that need to be in memory, for e.g., for lookups like
		// HashMaps etc.
		// args contains the xml entries made under the search component
		// declaration in solrconfig.xml

		words = ((NamedList) args.get("words")).getAll("word");
		if (words.isEmpty()) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Need to specify at least one word in searchComponent config!");
		}
		super.init(args);
	}

}
