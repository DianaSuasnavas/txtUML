#include "threadpoolmanager.hpp"

ThreadPoolManager::ThreadPoolManager() : configuration(nullptr) {}

void ThreadPoolManager::recalculateThreads(int id,int n)
{
	LinearFunction function = *(configuration->getFunction(id)]);
	int max = configuration->getMax(id);
	if (function(n) < max) {
		configuration->getThreadPool(id)->modifiedThreads(function(n));
	}
}

void ThreadPoolManager::enqueObject(StateMachineI* sm)
{
	id_type object_id = sm->getPoolId();
	id_matching_map[object_id]->enqueObject(sm);
}

int ThreadPoolManager::getNumberOfConfigurations()
{
	return ((int)configuration->getNumberOfConfigurations());
}

ThreadPoolManager::~ThreadPoolManager()
{
	delete configuration;
}

void ThreadPoolManager::setConfiguration(ThreadConfiguration* configuration)
{
	this->configuration = configuration;
}

bool ThreadPoolManager::isConfigurated()
{
	return configuration != nullptr;
}

