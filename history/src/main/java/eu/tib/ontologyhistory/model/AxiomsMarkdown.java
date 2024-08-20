package eu.tib.ontologyhistory.model;

import java.util.List;

public record AxiomsMarkdown (
        List<String> plainOutput,

        String markdownOutput
) {}
