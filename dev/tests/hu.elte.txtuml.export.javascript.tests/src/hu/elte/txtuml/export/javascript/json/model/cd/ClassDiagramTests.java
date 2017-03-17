package hu.elte.txtuml.export.javascript.json.model.cd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.VisibilityKind;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import hu.elte.txtuml.export.diagrams.common.Rectangle;
import hu.elte.txtuml.layout.visualizer.model.AssociationType;
import hu.elte.txtuml.layout.visualizer.model.LineAssociation;
import hu.elte.txtuml.layout.visualizer.model.Point;
import hu.elte.txtuml.layout.visualizer.model.RectangleObject;
import hu.elte.txtuml.utils.Pair;

public class ClassDiagramTests {

	Model model;
	Class classA;
	Pair<List<String>, List<PrimitiveType>> primitives;

	@Before
	public void prepare() {
		model = UMLFactory.eINSTANCE.createModel();

		List<String> names = Arrays.asList("String", "Boolean", "Integer", "Real", "UnlimitedNatural");
		List<PrimitiveType> types = Arrays.asList(model.createOwnedPrimitiveType("String"),
				model.createOwnedPrimitiveType("Boolean"), model.createOwnedPrimitiveType("Integer"),
				model.createOwnedPrimitiveType("Real"), model.createOwnedPrimitiveType("UnlimitedNatural"));

		primitives = Pair.of(names, types);

		classA = model.createOwnedClass("A", false);
	}

	@Test
	public void testArgument() {
		BasicEList<String> paramNames = new BasicEList<String>(Arrays.asList("a", "b", "c", "d", "e", "f"));

		BasicEList<Type> paramTypes = new BasicEList<Type>();
		paramTypes.add(classA);
		paramTypes.addAll(primitives.getSecond());

		List<String> paramTypeNames = new ArrayList<String>();
		paramTypeNames.add("A");
		paramTypeNames.addAll(primitives.getFirst());

		Operation op = classA.createOwnedOperation("f", (EList<String>) paramNames, (EList<Type>) paramTypes);

		List<String> actualParamNames = new ArrayList<String>();
		List<String> actualTypeNames = new ArrayList<String>();
		for (Parameter par : op.getOwnedParameters()) {
			Argument arg = new Argument(par);
			actualParamNames.add(arg.getName());
			actualTypeNames.add(arg.getType());
		}

		Assert.assertArrayEquals(paramNames.toArray(), actualParamNames.toArray());
		Assert.assertArrayEquals(paramTypeNames.toArray(), actualTypeNames.toArray());

	}

	@Test
	public void testAttribute() {
		ArrayList<String> attrNames = new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f"));

		ArrayList<Type> attrTypes = new ArrayList<Type>();
		attrTypes.add(classA);
		attrTypes.addAll(primitives.getSecond());

		List<String> attrTypeNames = new ArrayList<String>();
		attrTypeNames.add("A");
		attrTypeNames.addAll(primitives.getFirst());

		ArrayList<VisibilityKind> expectedVisibilities = new ArrayList<VisibilityKind>(Arrays.asList(
				VisibilityKind.PUBLIC_LITERAL, VisibilityKind.PROTECTED_LITERAL, VisibilityKind.PRIVATE_LITERAL,
				VisibilityKind.PACKAGE_LITERAL, VisibilityKind.PUBLIC_LITERAL, VisibilityKind.PROTECTED_LITERAL));

		for (int i = 0; i < attrNames.size(); ++i) {
			Property p = classA.createOwnedAttribute(attrNames.get(i), attrTypes.get(i));
			p.setVisibility(expectedVisibilities.get(i));
			Attribute a = new Attribute(p);
			Assert.assertEquals(attrNames.get(i), a.getName());
			Assert.assertEquals(attrTypeNames.get(i), a.getType());
			Assert.assertEquals(expectedVisibilities.get(i), a.getVisibility());
		}
	}

	@Test
	public void testAssociationEnd() {
		Class classB = model.createOwnedClass("B", false);
		List<String> names = Arrays.asList("a", "b", "c", "d");
		List<Boolean> compositions = Arrays.asList(true, false, false, true);
		List<Boolean> navigabilities = Arrays.asList(true, true, false, false);
		List<String> expectedMultiplicites = Arrays.asList("*", "2..*", "1..3", "2");
		List<VisibilityKind> expectedVisibilities = Arrays.asList(VisibilityKind.PUBLIC_LITERAL,
				VisibilityKind.PRIVATE_LITERAL, VisibilityKind.PROTECTED_LITERAL, VisibilityKind.PACKAGE_LITERAL);

		Association a = classA.createAssociation(true, AggregationKind.NONE_LITERAL, "b", 2, -1, classB, true,
				AggregationKind.COMPOSITE_LITERAL, "a", 0, -1);
		Association b = classB.createAssociation(false, AggregationKind.COMPOSITE_LITERAL, "d", 2, 2, classA, false,
				AggregationKind.NONE_LITERAL, "c", 1, 3);

		a.getMemberEnd("a", classA).setVisibility(VisibilityKind.PUBLIC_LITERAL);
		a.getMemberEnd("b", classB).setVisibility(VisibilityKind.PRIVATE_LITERAL);
		b.getMemberEnd("c", classB).setVisibility(VisibilityKind.PROTECTED_LITERAL);
		b.getMemberEnd("d", classA).setVisibility(VisibilityKind.PACKAGE_LITERAL);

		List<Property> props = Arrays.asList(a.getMemberEnd("a", classA), a.getMemberEnd("b", classB),
				b.getMemberEnd("c", classB), b.getMemberEnd("d", classA));

		for (int i = 0; i < props.size(); ++i) {
			AssociationEnd ae = new AssociationEnd(props.get(i));
			Assert.assertEquals(names.get(i), ae.getName());
			Assert.assertEquals(compositions.get(i), ae.isComposition());
			Assert.assertEquals(navigabilities.get(i), ae.isNavigable());
			Assert.assertEquals(expectedMultiplicites.get(i), ae.getMultiplicity());
			Assert.assertEquals(expectedVisibilities.get(i), ae.getVisibility());
		}

	}

	@Test
	public void testClassAttributeLink() throws UnexpectedEndException {
		Class classB = model.createOwnedClass("B", false);
		LineAssociation lab = new LineAssociation("package.AB", "package.A", "package.B");
		LineAssociation laa = new LineAssociation("package.AA", "package.A", "package.A");

		Association ab = classA.createAssociation(true, AggregationKind.NONE_LITERAL, "b", 1, 1, classB, true,
				AggregationKind.NONE_LITERAL, "a", 2, 2);
		ab.setName("AB");

		Association aa = classA.createAssociation(true, AggregationKind.NONE_LITERAL, "a2", 3, 3, classA, true,
				AggregationKind.NONE_LITERAL, "a1", 4, 4);

		aa.setName("AA");

		ClassAttributeLink calab = new ClassAttributeLink(lab, ab, classA, classB);
		ClassAttributeLink calaa = new ClassAttributeLink(laa, aa, classA, classA);

		Assert.assertEquals("package.AB", calab.getId());
		Assert.assertEquals("package.AA", calaa.getId());

		Assert.assertEquals("AB", calab.getName());
		Assert.assertEquals("AA", calaa.getName());

		Assert.assertEquals(AssociationType.normal, calab.getType());
		Assert.assertEquals(AssociationType.normal, calaa.getType());

		Assert.assertEquals("package.A", calab.getFromID());
		Assert.assertEquals("package.A", calaa.getFromID());

		Assert.assertEquals("package.B", calab.getToID());
		Assert.assertEquals("package.A", calaa.getToID());

		Assert.assertEquals("a", calab.getFrom().getName());
		Assert.assertEquals("a1", calaa.getFrom().getName());

		Assert.assertEquals("b", calab.getTo().getName());
		Assert.assertEquals("a2", calaa.getTo().getName());

	}

	@Test
	public void testClassLink() {
		LineAssociation la = new LineAssociation("package.AB", "package.A", "package.B");
		la.setType(AssociationType.generalization);

		ClassLink cl = new ClassLink(la);

		Assert.assertEquals("package.AB", cl.getId());
		Assert.assertEquals("package.A", cl.getFromID());
		Assert.assertEquals("package.B", cl.getToID());
		Assert.assertEquals(AssociationType.generalization, cl.getType());

	}

	@Test
	public void testClassNode() {
		BasicEList<String> emptyStrings = new BasicEList<String>();
		BasicEList<Type> emptyTypes = new BasicEList<Type>();

		List<String> expectedAttrNames = Arrays.asList("attr1", "attr2");
		for (String attr : expectedAttrNames) {
			classA.createOwnedAttribute(attr, primitives.getSecond().get(0));
		}

		List<String> expectedOpNames = Arrays.asList("op1", "op2");
		for (String op : expectedOpNames) {
			classA.createOwnedOperation(op, emptyStrings, emptyTypes);
		}

		Class classB = model.createOwnedClass("B", true);
		RectangleObject rectA = new RectangleObject("package.A");
		rectA.setPosition(new Point(1, 2));
		rectA.setWidth(3);
		rectA.setHeight(4);

		RectangleObject rectB = new RectangleObject("package.B");
		rectB.setPosition(new Point(5, 6));
		rectB.setWidth(7);
		rectB.setHeight(8);

		ClassNode cnA = new ClassNode(classA, rectA.getName());
		ClassNode cnB = new ClassNode(classB, rectB.getName());

		List<MemberOperation> ops = cnA.getOperations();
		List<Attribute> attrs = cnA.getAttributes();

		List<String> opNames = new ArrayList<String>();
		for (MemberOperation op : ops) {
			opNames.add(op.getName());
		}

		List<String> attrNames = new ArrayList<String>();
		for (Attribute attr : attrs) {
			attrNames.add(attr.getName());
		}

		Assert.assertEquals("package.A", cnA.getId());
		Assert.assertEquals("package.B", cnB.getId());

		Assert.assertEquals("A", cnA.getName());
		Assert.assertEquals("B", cnB.getName());

		Assert.assertEquals(CDNodeType.CLASS, cnA.getType());
		Assert.assertEquals(CDNodeType.ABSTRACT_CLASS, cnB.getType());

		Rectangle layoutTest = new Rectangle(1, 2, 3, 4);
		cnA.setLayout(layoutTest);

		Assert.assertEquals(1, cnA.getPosition().getX());
		Assert.assertEquals(2, cnA.getPosition().getY());

		Assert.assertEquals((Integer) 3, cnA.getWidth());
		Assert.assertEquals((Integer) 4, cnA.getHeight());

		Assert.assertTrue(cnB.getAttributes().isEmpty());
		Assert.assertTrue(cnB.getOperations().isEmpty());

		Assert.assertArrayEquals(expectedAttrNames.toArray(), attrNames.toArray());
		Assert.assertArrayEquals(expectedOpNames.toArray(), opNames.toArray());

	}

	@Test
	public void testMemberOperation() {
		ArrayList<String> opNames = new ArrayList<String>(Arrays.asList("a", "b", "c", "d"));
		ArrayList<VisibilityKind> expectedVisibilities = new ArrayList<VisibilityKind>(
				Arrays.asList(VisibilityKind.PUBLIC_LITERAL, VisibilityKind.PROTECTED_LITERAL,
						VisibilityKind.PRIVATE_LITERAL, VisibilityKind.PACKAGE_LITERAL));
		ArrayList<Type> returnTypes = new ArrayList<Type>(
				Arrays.asList(null, classA, primitives.getSecond().get(0), null));
		ArrayList<String> returnTypeNames = new ArrayList<String>(
				Arrays.asList(null, "A", primitives.getFirst().get(0), null));

		for (int i = 0; i < opNames.size(); ++i) {
			BasicEList<Type> argTypes = new BasicEList<Type>();
			primitives.getSecond().subList(0, i).stream().forEach((PrimitiveType pt) -> argTypes.add(pt));

			List<String> argTypeNames = primitives.getFirst().subList(0, i);
			List<String> argNames = opNames.subList(0, i);

			Operation op = classA.createOwnedOperation(opNames.get(i), new BasicEList<String>(argNames), argTypes);
			op.setVisibility(expectedVisibilities.get(i));

			if (returnTypes.get(i) != null) {
				Parameter ret = op.createOwnedParameter("return", returnTypes.get(i));
				ret.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			MemberOperation mop = new MemberOperation(op);

			Assert.assertEquals(opNames.get(i), mop.getName());
			Assert.assertEquals(expectedVisibilities.get(i), mop.getVisibility());
			Assert.assertEquals(returnTypeNames.get(i), mop.getReturnType());

			ArrayList<String> actualArgNames = new ArrayList<String>();
			ArrayList<String> actualArgTypeNames = new ArrayList<String>();
			for (Argument a : mop.getArgs()) {
				actualArgNames.add(a.getName());
				actualArgTypeNames.add(a.getType());
			}

			Assert.assertArrayEquals(actualArgNames.toArray(), argNames.toArray());
			Assert.assertArrayEquals(actualArgTypeNames.toArray(), argTypeNames.toArray());

		}
	}
}
