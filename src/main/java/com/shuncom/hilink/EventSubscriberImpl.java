package com.shuncom.hilink;

import java.util.Set;

import com.huawei.hilink.event.Event;
import com.huawei.hilink.event.EventFilter;
import com.huawei.hilink.event.EventSubscriber;

public class EventSubscriberImpl implements EventSubscriber {

	private EventFilter eventFilter = new EventFilter() {

		@Override
		public boolean apply(Event event) {
			
			return true;
		}
		
	};
	
	@Override
	public EventFilter getEventFilter() {
		return eventFilter;
	}

	@Override
	public Set<String> getSubscribedEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void receive(Event event) {
		// TODO Auto-generated method stub
		
	}

}
