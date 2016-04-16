package hu.elte.txtuml.export.papyrus.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import hu.elte.txtuml.export.papyrus.DiagramManagerUnitTest;
import hu.elte.txtuml.export.papyrus.PapyrusModelCreatorTest;
import hu.elte.txtuml.export.papyrus.ProjectUtilsTest;
import hu.elte.txtuml.export.papyrus.elementsarrangers.txtumllayout.tests.LayoutTransformerTest;

@RunWith(Suite.class)
@SuiteClasses({ DiagramManagerUnitTest.class, PapyrusModelCreatorTest.class,
		ProjectUtilsTest.class, LayoutTransformerTest.class })
public class UnitTests {

}
