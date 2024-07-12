import ch.qos.logback.classic.BasicConfigurator;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.jena.atlas.web.auth.PreemptiveBasicAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * The code runs a federated query against TIVA and BACI sparql endpoints.
 */
public class FederatedIQuery {

    public static void main(String[] argv) throws IOException {

        BasicConfigurator.configure();

        Logger.getRootLogger().setLevel(Level.DEBUG);

        org.apache.jena.atlas.web.auth.HttpAuthenticator authenticator = new PreemptiveBasicAuthenticator(new SimpleAuthenticator("user-name", "pass".toCharArray()), true);

        try(QueryExecution q = QueryExecutionFactory.sparqlService("https://skynet.coypu.org/coypu-internal/query",query(),authenticator)){

        ResultSet results = q.execSelect();

        int i =1;

        while(results.hasNext()){

            QuerySolution soln = results.next() ;

            /**
             * BACI variable
             */

             RDFNode animport = soln.get("import") ;
             RDFNode export = soln.get("export");
             RDFNode productMatch = soln.get("productMatch");
             Literal productLabel = soln.getLiteral("productLabel");
             RDFNode amountYear = soln.get("amountYear");
             Literal amountValue = soln.getLiteral("amountValue");
             Literal value = soln.getLiteral("value");


            /**
             * TiVA variable
             */

            Literal vaoImportValue = soln.getLiteral("vaoImportValue");
            RDFNode vaoImportYear = soln.get("vaoImportYear");
            RDFNode exIndustryCode = soln.get("exIndustryCode");
            RDFNode exTradeLocation = soln.get("exTradeLocation");
            RDFNode importLocation = soln.get("importLocation");


            System.out.println(i++ + ".  " +
                    " | imgr bsci export location: " + exTradeLocation.asNode().getLocalName() +
                    " | exgr dva export location: " + export.asNode().getLocalName() +
                    " | imgr export industry code: " + exIndustryCode +
                    " | imgr bsci import location: " + importLocation.asNode().getLocalName() +
                     " | exgr dva import location: " + animport.asNode().getLocalName() +
                    " | exgr dva product match (code): " + productMatch +
                    " | exgr dva product name: " + productLabel.toString() +
                     " | exgr dva amount year: " + amountYear.asLiteral().getValue() +
                     " | exgr  dva amount value: " + amountValue.asLiteral().getValue() +
                    " | exgr dva trade value: " + value.asLiteral().getValue()+
                    " imgr bsci value : "+ vaoImportValue.asLiteral().getValue() +
                    " | imgr bsci year: "+ vaoImportYear.asLiteral().getValue());

        }

        }catch(Exception e) {

            e.printStackTrace();
        };

//  ResultSetFormatter.out(System.out, results);

    }

    public static String query(){

    String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "SELECT DISTINCT ?vaoImportValue ?vaoImportYear ?exTradeLocation  ?importLocation " +
            "?import ?export ?exIndustryCode  ?productMatch ?productLabel ?amountYear ?amountValue ?value  ?sum" +
            "WHERE " +
            "{ " +
            "GRAPH <https://data.coypu.org/trade/baci/> " +
            "{ " +
            "?exgrdva rdf:type <https://schema.coypu.org/vtf#ExgrDva> . " +
            "?exgrdva <https://schema.coypu.org/vtf#hasImport> ?import . " +
            "?exgrdva <https://schema.coypu.org/vtf#hasExport> ?export ." +
            "?exgrdva <https://schema.coypu.org/vtf#hasTrade> ?trade . " +
            "?trade <https://schema.coypu.org/vtf#hasTradeProduct> ?product . " +
            "?product <http://www.w3.org/2000/01/rdf-schema#label> ?productLabel . " +
            "?product <http://www.w3.org/2004/02/skos/core#exactMatch> ?productMatch . " +
            "?trade <https://schema.coypu.org/vtf#hasTradeAmount> ?tradeAmount . " +
            "?tradeAmount <https://schema.coypu.org/global#hasYear> ?amountYear . " +
            "?tradeAmount <https://schema.coypu.org/global#hasValue> ?amountValue ." +
            "?trade <https://schema.coypu.org/vtf#hasTradeValue> ?tradeValue . " +
            "?tradevalue <https://schema.coypu.org/global#hasValue> ?value . " +
            "FILTER(str(?amountYear)='2018') . " +

            "} " +
            "SERVICE <https://tiva.coypu.org/tiva> { " +
            " {" +
            "SELECT ?vaoImportValue ?vaoImportYear ?exIndustryCode ?exTradeLocation ?importLocation  " +
            "WHERE { " +
            "?imgrbsci a <https://schema.coypu.org/vtf#ImgrBsci> . " +
            "?imgrbsci <https://schema.coypu.org/global#hasValue> ?vaoImportValue . " +
            "?imgrbsci <https://schema.coypu.org/global#hasYear> ?vaoImportYear . " +
            "?imgrbsci <https://schema.coypu.org/vtf#hasExport> ?ex . " +
            "?ex <https://schema.coypu.org/vtf#hasIndustryCode> ?exIndustryCode . " +
            "?imgrbsci <https://schema.coypu.org/vtf#hasImport> ?imgrImport . " +
            "?imgrImport <https://schema.coypu.org/vtf#hasTradeLocation> ?importLocation . " +
            "?ex rdf:type <https://schema.coypu.org/vtf#Export> . " +
            "?ex <https://schema.coypu.org/vtf#hasTradeLocation> ?exTradeLocation  . " +
            "}" +
            "} " +
            "} " +
          "FILTER(str(?import)=str(?importLocation) && str(?export)=str(?exTradeLocation)) . " +
            "} "+
            "LIMIT 30";

        return q;
}

}