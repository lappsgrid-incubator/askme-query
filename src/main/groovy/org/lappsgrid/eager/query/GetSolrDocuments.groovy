package org.lappsgrid.eager.query

import groovy.util.logging.Slf4j
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.CloudSolrClient
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.params.MapSolrParams
import org.lappsgrid.eager.mining.api.Query

@Slf4j("logger")
class GetSolrDocuments {
    ConfigObject config

    //DOES THIS NEED TO BE PRIVATE
    public Integer answer(Query query) {
        logger.debug("Generating answer.")

        logger.trace("Creating CloudSolrClient")
        SolrClient solr = new CloudSolrClient.Builder([config.solr.host]).build()

        logger.trace("Generating query")
        Map solrParams = [:]
        solrParams.q = query.query
        solrParams.fl = 'pmid,pmc,doi,year,title,path,abstract,body'
        solrParams.rows = config.solr.rows

        MapSolrParams queryParams = new MapSolrParams(solrParams)
        String collection = config.solr.collection

        logger.trace("Sending query to Solr")
        final QueryResponse response = solr.query(collection, queryParams)
        final SolrDocumentList documents = response.getResults()

        int n = documents.size()
        logger.trace("Received {} documents", n)
        Map result = [:]
        result.query = query
        result.size = n

        logger.trace("Received {} documents", n)
        return n
    }

}
