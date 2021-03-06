package net.sf.mpango.game.core.events;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import net.sf.mpango.game.core.exception.EventNotSupportedException;

/**
 * Class that receives an event, processes it, and propagates it to all the subscribed listeners.
 * @author edvera
 */
public class ObservableBaseListener extends BaseListener implements Observable {
	
	protected List<Listener> listeners;
	private Listener myself;
	
	public ObservableBaseListener(Listener yourself) {
		this.myself = yourself;
		listeners = new ArrayList<Listener>();
	}
	
	@Override
	public final void receive(Event event) throws EventNotSupportedException {
		try {
			if (myself != null) {
				myself.receive (event);
			}
		} catch (EventNotSupportedException e) {
			
		} finally {
			//We make sure the chain is not broken because of the exception
			notifyListeners(event);
		}
	}
	
	@Override
	 public void notifyListeners (Event event) {
		for (Listener listener : listeners) {
			try {
				listener.receive(event);
			} catch (EventNotSupportedException e) {}
		}
	}
	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	@Transient
	protected List<Listener> getListeners() {
		return listeners;
	}
	@Transient
	public Listener getMyself() {
		return myself;
	}
}