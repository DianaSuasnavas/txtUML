package hu.elte.txtuml.examples.train;

import hu.elte.txtuml.api.deployment.Configuration;
import hu.elte.txtuml.api.deployment.Group;
import hu.elte.txtuml.api.deployment.Multithreading;

import hu.elte.txtuml.examples.train.model.*;

@Group(contains = { Engine.class, Gearbox.class }, max = 5, constant = 2)
@Multithreading(false)
public class TrainConfiguration extends Configuration {

}
