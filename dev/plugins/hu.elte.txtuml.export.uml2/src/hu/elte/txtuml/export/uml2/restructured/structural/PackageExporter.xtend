package hu.elte.txtuml.export.uml2.restructured.structural

import hu.elte.txtuml.export.uml2.restructured.Exporter
import hu.elte.txtuml.utils.jdt.ElementTypeTeller
import hu.elte.txtuml.utils.jdt.SharedUtils
import java.io.File
import java.util.regex.Pattern
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.Package
import org.eclipse.uml2.uml.PackageableElement
import org.eclipse.uml2.uml.Type

abstract class AbstractPackageExporter<T extends Package> extends Exporter<IPackageFragment, IPackageFragment, T> {

	new() {
	}

	new(Exporter<?, ?, ?> parent) {
		super(parent);
	}

	def exportCompUnit(ICompilationUnit compUnit) {
		val unit = parseCompUnit(compUnit)
		unit.types.forEach[exportType]
	}

	def exportType(TypeDeclaration decl) {
		switch decl {
			case ElementTypeTeller.isModelClass(decl): exportClass(decl)
			case ElementTypeTeller.isAssociation(decl): exportAssociation(decl)
			// default: throw new IllegalArgumentException(decl.toString)
		}
	}

	protected def exportPackageContents(IPackageFragment packageFragment) {
		packageFragment.children.map[it as ICompilationUnit].forEach[exportCompUnit]
		(packageFragment.parent as IPackageFragmentRoot).children.map[it as IPackageFragment].filter [
			elementName.startsWith(packageFragment.elementName + ".")
		].forEach[exportPackage]
	}

	def parseCompUnit(ICompilationUnit compUnit) {
		SharedUtils.parseJavaSource(new File(compUnit.resource.locationURI), compUnit.javaProject)
	}

	override tryStore(Element contained) {
		switch contained {
			Type: result.ownedTypes.add(contained)
			Package: result.nestedPackages.add(contained)
			PackageableElement: result.packagedElements.add(contained)
			default: return false
		}
		return true
	}
}

class PackageExporter extends AbstractPackageExporter<Package> {

	new(Exporter<?, ?, ?> parent) {
		super(parent)
	}

	override create(IPackageFragment pf) { factory.createPackage }

	override exportContents(IPackageFragment s) {
		result.name = s.elementName.split(Pattern.quote(".")).last
		exportPackageContents(s)
	}
}