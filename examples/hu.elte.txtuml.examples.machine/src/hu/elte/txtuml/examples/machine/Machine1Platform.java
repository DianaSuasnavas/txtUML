package hu.elte.txtuml.examples.machine;

import hu.elte.txtuml.examples.machine.model1.Machine;
import hu.elte.txtuml.examples.machine.model1.User;
import hu.elte.txtuml.api.deployment.*;

@Group(contains = { Machine.class, User.class })
@Multithreading(false)
public class Machine1Platform extends Configuration {
}
