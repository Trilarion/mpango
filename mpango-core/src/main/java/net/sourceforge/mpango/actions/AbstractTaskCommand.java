package net.sourceforge.mpango.actions;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.mpango.events.CommandExecutedEvent;
import net.sourceforge.mpango.events.Event;
import net.sourceforge.mpango.events.Listener;
import net.sourceforge.mpango.exception.CommandException;
import net.sourceforge.mpango.exception.EventNotSupportedException;

/**
 * <p>The purpose of this class is to execute tasks that need time to be executed.
 * The time it takes to execute the {@link Command#execute()} is determined by the {@link TaskCommand.calculateTotalTimeSlices()} method which returns milliseconds.
 * This class receives {@link Timer} as part of the constructor, a timer which will be responsible for executing the {@link Command}.
 * This class is responsible for the {@link Thread} created by the {@link Timer} creates at scheduling time.</p>
 * @author etux
 *
 */
public abstract class AbstractTaskCommand extends TimerTask implements ITaskCommand  {

	/** Default time milliseconds per time slice. */
	public static final long DEFAULT_MILLIS_PER_TIME_SLICE = 1000;
	
	/** Timer assigned to this command */
	private Timer timer;
	protected List<Listener> listeners;
	private long millisPerSlice;
	
	/**
	 * Constructor that needs to be called by all extending classes.
	 * @param timer
	 * @param listeners
	 */
	protected AbstractTaskCommand(long millisPerSlice, Timer timer, List<Listener> listeners) {
		this.timer = timer;
		this.listeners = listeners;
		this.millisPerSlice = millisPerSlice;
	}
	/**
	 * Commodity method to ease up developer from constructing the {@link List}.
	 * Uses {@link Arrays#asList()} to construct the list.
	 * @param timer
	 * @param listeners
	 */
	protected AbstractTaskCommand(long millisPerSlice, Timer timer, Listener...listeners) {
		this(millisPerSlice, timer, Arrays.asList(listeners));
	}
	/**
	 * Constructor that uses the default time milliseconds per time slice.
	 * @param timer to schedule the command.
	 */
	protected AbstractTaskCommand(Timer timer, Listener...listeners) {
		this(DEFAULT_MILLIS_PER_TIME_SLICE, timer, listeners);
	}
	/**
	 * Implementation of the execute method of the command. 
	 * This method schedules the command as a TimerTask that will be called
	 * executed in the {@link AbstractTaskCommand#calculateTotalTimeSlices()} in the future.
	 * amount of time slices needed.
	 */
	public final void execute() throws CommandException {
		evaluateExecution(); //Evaluating if the command can be executed in the current conditions.
		timer.schedule(this, calculateTotalTimeMillis(millisPerSlice)); //Scheduling the command so that its effects happen in the future.
	}
	/**
	 * Method that returns the number of milliseconds the command will take to execute.
	 * @param timeMillisPerTimeSlice factor multiplying the time slices of the command.
	 * @return total amount of time the command will take to execute.
	 */
	public long calculateTotalTimeMillis(long timeMillisPerTimeSlice) {
		return (timeMillisPerTimeSlice * calculateTotalTimeSlices()); //Basic time calculation that will fit most of the cases.
	}
	/**
	 * Implementation of the {@link Timer#run()} method which is a 
	 * Template Method for the inheriting {@link Command}(s). 
	 */
	@Override
	public void run() {
		runExecute();
		notifyListeners(new CommandExecutedEvent(this));
	}
	/**
	 * Method that notifies all listeners for the {@link Command}.
	 */
	public void notifyListeners(Event event) {
		for(Listener listener : listeners) {
			try {
				listener.receiveEvent(new CommandExecutedEvent(this));
			} catch (EventNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Method that add a listener to the {@link AbstractTaskCommand}.
	 * @param listener
	 */
	@Override
	public void addListener(Listener listener) {
		this.listeners.add(listener);
	}
	@Override
	public void removeListener(Listener listener) {
		this.listeners.remove(listener);
	}
}