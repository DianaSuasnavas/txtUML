package hu.elte.txtuml.xtxtuml.scoping

import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.scoping.XImportSectionNamespaceScopeProvider

class XtxtUMLXImportSectionNamespaceScopeProvider extends XImportSectionNamespaceScopeProvider {

	static final QualifiedName TXTUML_LIBRARY = QualifiedName::create("hu", "elte", "txtuml", "xtxtuml", "lib")

	override protected getImplicitImports(boolean ignoreCase) {
		#[
			doCreateImportNormalizer(JAVA_LANG, true, false),
			doCreateImportNormalizer(TXTUML_LIBRARY, true, false)
		]
	}

	override protected doCreateImportNormalizer(QualifiedName importedNamespace, boolean wildcard, boolean ignoreCase) {
		return new XtxtUMLImportNormalizer(importedNamespace, wildcard, ignoreCase);
	}

}
