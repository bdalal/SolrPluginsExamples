// In this example demonstrating the use of a custom update processor, I'll simply be adding a timestamp field to all the docs which will hold the current timestamp.
// Make sure that the schema contains all fields that are going to be added to document using an update processor else an exception will be thrown during indexing.
// Documentation for all the operations that can be performed: https://lucene.apache.org/solr/5_4_1/solr-solrj/org/apache/solr/common/SolrInputDocument.html

package updatehandler;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

public class UpdateProcessorFactory extends UpdateRequestProcessorFactory {

	private static Date date;

	@SuppressWarnings("rawtypes")
	@Override
	public void init(NamedList params) {
		// do something; similar to the init methods used in the request handler
		// and component examples
		date = new Date();
		super.init(params);
	}

	@Override
	public UpdateRequestProcessor getInstance(SolrQueryRequest arg0, SolrQueryResponse arg1, UpdateRequestProcessor arg2) {
		return new UpdateProcessor(arg2);
	}

	public class UpdateProcessor extends UpdateRequestProcessor {

		public UpdateProcessor(UpdateRequestProcessor next) {
			super(next);
		}

		@Override
		public void processAdd(AddUpdateCommand cmd) throws IOException {
			// do something
			SolrInputDocument thisDoc = cmd.getSolrInputDocument();
			thisDoc.addField("Timestamp", new Timestamp(date.getTime()));
			super.processAdd(cmd);
		}
	}
}