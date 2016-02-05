package hu.elte.txtuml.examples.printer;


import hu.elte.txtuml.api.platform.Configuration;
import hu.elte.txtuml.api.platform.Group;
import hu.elte.txtuml.api.platform.Multithreading;

import hu.elte.txtuml.examples.printer.model.*;

@Group(contains = {Human.class}) // the human will be a separate thread and everything else will be an other
@Multithreading(true)
public class PrinterConfiguration extends Configuration{

}
