package org.lappsgrid.askme.query

import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.askme.core.api.Query
import org.lappsgrid.serialization.Serializer


class SimpleQueryProcessorTest {

    @Test
    void removeStopWords(){
        String[] q1 = "Don't process this query".trim().toLowerCase().split('\\s+')
        String[] q2 = "Who what where when why".trim().toLowerCase().split('\\s+')

        SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor()

        /*
        Questions:
        1) don't should be removed, not split into don and t?
        2) nothing becomes nothin g, not nothing? (Fixed)
         */

        assert ["process", "query"] == queryProcessor.removeStopWords(q1)
        assert [] == queryProcessor.removeStopWords(q2)
    }

    @Test
    void transform(){
        String q1 = "How do I search?"
        String q2 = "Get me some documents"
        String q3 = "Feet don't fail me now"

        SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor()
        Query query1 = queryProcessor.transform(new Query(q1))
        Query query2 = queryProcessor.transform(new Query(q2))
        Query query3 = queryProcessor.transform(new Query(q3))

        assert q1 == query1.question
        assert ["search"] == query1.terms
        assert "body:search" == query1.query

        assert q2 == query2.question
        assert ["get", "documents"] == query2.terms
        assert "body:get AND body:documents" == query2.query

        assert q3 == query3.question
        //assert ["don", "t", "fail", "now"] == query3.terms
        //assert "body:don AND body:t AND body:fail AND body:now" == query3.query
        assert ["feet", "fail", "now"] == query3.terms
        assert "body:feet AND body:fail AND body:now" == query3.query

    }

    @Ignore
    void getQuery() {
        SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor()
        Query query = queryProcessor.transform(new Query("What is the effect of chloroquine on SARS-Cov-2 replication?"))
        println Serializer.toPrettyJson(query)

    }
}
