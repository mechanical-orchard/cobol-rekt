package org.smojol.interpreter.structure;

import org.eclipse.lsp.cobol.core.CobolParser;
import org.smojol.common.vm.strategy.UnresolvedReferenceStrategy;
import org.smojol.common.vm.structure.*;

public class DefaultFormat1DataStructureBuilder implements Format1DataStructureBuilder {
    @Override
    public Format1DataStructure build(CobolParser.DataDescriptionEntryFormat1Context format1Structure, UnresolvedReferenceStrategy strategy) {
        if (format1Structure.dataOccursClause().isEmpty()) {
            return new Format1DataStructure(format1Structure, strategy);
        }
        int numOccurrences = Integer.parseInt(format1Structure.dataOccursClause().getFirst().integerLiteral().getText());
        return new TableDataStructure(format1Structure, numOccurrences, strategy);
    }
}
