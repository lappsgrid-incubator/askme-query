package org.lappsgrid.askme.query

import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.eager.mining.api.Query

@Ignore
class SimpleQueryProcessorTest {

    @Test
    void removeStopWords(){
        String[] q1 = "Don't process this query?".trim().toLowerCase().split('\\W+')
        String[] q2 = "Who what where when why?".trim().toLowerCase().split('\\W+')
        String[] q3 = "nothin\"g to see +here or there or @nywhere'".trim().toLowerCase().split('\\W+')

        SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor()

        /*
        Questions:
        1) don't should be removed, not split into don and t?
        2) nothing becomes nothin g, not nothing?
         */

        assert ["process", "query"] == queryProcessor.removeStopWords(q1)
        assert [] == queryProcessor.removeStopWords(q2)
        //assert ["nothin", "g", "see", "nywhere"] == queryProcessor.removeStopWords(q3)
        assert ["nothin", "g", "see", "@nywh3r3"] == queryProcessor.removeStopWords(q3)


    }

    @Test
    void transform(){
        String q1 = "How do I search?"
        String q2 = "Get me some documents!!!"
        String q3 = "Don't fail me now!"

        SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor()
        Query query1 = queryProcessor.transform(q1)
        Query query2 = queryProcessor.transform(q2)
        Query query3 = queryProcessor.transform(q3)

        assert q1 == query1.question
        assert ["search"] == query1.terms
        assert "body:search" == query1.query

        assert q2 == query2.question
        assert ["get", "documents"] == query2.terms
        assert "body:get AND body:documents" == query2.query

        assert q3 == query3.question
        //assert ["don", "t", "fail", "now"] == query3.terms
        //assert "body:don AND body:t AND body:fail AND body:now" == query3.query
        assert ["fail", "now"] == query3.terms
        assert "body:fail AND body:now" == query3.query

    }

}
