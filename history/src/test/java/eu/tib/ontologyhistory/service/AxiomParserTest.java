package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.Axiom;
import eu.tib.ontologyhistory.utils.ParserUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AxiomParserTest {

    @Test
    public void testParseAddedAxioms() {
        List<String> diff = new ArrayList<>(Arrays.asList("5 axioms in left ontology but not in right ontology:\n",
                "- Annotation(<http://purl.org/dc/terms/modified> \"2022-02-13\"^^xsd:date)\n",
                "- Annotation(owl:priorVersion <https://schema.coypu.org/global/2.1>)\n",
                "- AnnotationAssertion(rdfs:label <https://schema.coypu.org/global#RemoteExplosiveOrLandmineOrIED> \"Remote explosive or landmine\"@en)\n",
                "- Declaration(Class(<https://schema.coypu.org/global#RemoteExplosiveOrLandmineOrIED>))\n",
                "- SubClassOf(<https://schema.coypu.org/global#RemoteExplosiveOrLandmineOrIED> <https://schema.coypu.org/global#ExplosionOrRemoteViolence>)\n",
                "\n",
                "9 axioms in right ontology but not in left ontology:\n",
                "+ Annotation(<http://purl.org/dc/terms/modified> \"2023-02-13\"^^xsd:date)\n",
                "+ Annotation(owl:priorVersion <https://schema.coypu.org/global/2.2>)\n",
                "+ AnnotationAssertion(rdfs:label <https://schema.coypu.org/global#RemoteExplosiveOrLandmineOrIed> \"Remote explosive or landmine or IED\"@en)\n",
                "+ Declaration(Class(<https://schema.coypu.org/global#RemoteExplosiveOrLandmineOrIed>))\n",
                "+ Declaration(ObjectProperty(<https://schema.coypu.org/global#memberOf>))\n",
                "+ EquivalentClasses(<https://schema.coypu.org/vtf#ExgrDva> ObjectSomeValuesFrom(<https://schema.coypu.org/vtf#hasTrade> <https://schema.coypu.org/vtf#Trade>))\n",
                "+ EquivalentClasses(<https://schema.coypu.org/vtf#Trade> ObjectHasSelf(<https://schema.coypu.org/vtf#isTrade>))\n",
                "+ ObjectPropertyRange(<https://schema.coypu.org/global#memberOf> <https://schema.coypu.org/global#InternationalOrganization>)\n",
                "+ SubClassOf(<https://schema.coypu.org/global#RemoteExplosiveOrLandmineOrIed> <https://schema.coypu.org/global#ExplosionOrRemoteViolence>)"));

        Map<String, List<Axiom>> axioms = ParserUtils.parseAxioms(diff);
        for (Axiom axiom : axioms.get("added")) {
            System.out.println(axiom.getAxiomURI());
        }
//        assertEquals(14, axioms.get("added").size() + axioms.get("removed").size());
        assertEquals("<http://purl.org/dc/terms/modified>", axioms.get("removed").get(0).getAxiomURI());
    }
}
