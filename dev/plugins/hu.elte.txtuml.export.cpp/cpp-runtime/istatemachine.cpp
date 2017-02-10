#include "istatemachine.hpp"
#include "threadpool.hpp"
#include "runtime.hpp"

#include <iostream>


 IStateMachine::IStateMachine(std::shared_ptr<MessageQueueType> messageQueue_)
         :_messageQueue(messageQueue_), _pool(nullptr), _inPool(false), _started(false), _initialized(false){}


void IStateMachine::init()
{
	_initialized = true;
	processInitTransition();
}

void IStateMachine::send(EventPtr e_)
{
  (*message_counter)++;
  _messageQueue->push_back(e_);
  if (_started)
  {
	if(_pool != nullptr)
  	{
    	handlePool();
  	}
  }
  

}

void IStateMachine::handlePool()
{
  std::unique_lock<std::mutex> mlock(_mutex);
  if(!_inPool)
  {
    _inPool=true;
    _pool->enqueueObject(this);
  }
}

void IStateMachine::setPooled(bool value_=true)
{
	  _inPool=value_;
	  _cond.notify_one();
}

IStateMachine::~IStateMachine()
{
	std::unique_lock<std::mutex> mlock(_mutex);
	while(_inPool)
	{
		_cond.wait(mlock);
	}
		
}
