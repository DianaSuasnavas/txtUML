/*
 * generated by Xtext
 */
package hu.elte.txtuml.xtxtuml.parser.antlr;

import java.io.InputStream;
import org.eclipse.xtext.parser.antlr.IAntlrTokenFileProvider;

public class XtxtUMLAntlrTokenFileProvider implements IAntlrTokenFileProvider {
	
	@Override
	public InputStream getAntlrTokenFile() {
		ClassLoader classLoader = getClass().getClassLoader();
    	return classLoader.getResourceAsStream("hu/elte/txtuml/xtxtuml/parser/antlr/internal/InternalXtxtUML.tokens");
	}
}
