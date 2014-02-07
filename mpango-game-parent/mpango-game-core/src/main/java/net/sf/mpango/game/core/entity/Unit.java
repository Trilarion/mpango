package net.sf.mpango.game.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import net.sf.mpango.common.entity.AbstractEntity;
import net.sf.mpango.common.utils.LocalizedMessageBuilder;
import net.sf.mpango.game.core.commands.Command;
import net.sf.mpango.game.core.commands.ConstructCommand;
import net.sf.mpango.game.core.enums.Resources;
import net.sf.mpango.game.core.events.BaseObserver;
import net.sf.mpango.game.core.events.CommandEvent;
import net.sf.mpango.game.core.events.CommandExecutedEvent;
import net.sf.mpango.game.core.events.Event;
import net.sf.mpango.game.core.events.Observer;
import net.sf.mpango.game.core.exception.CommandException;
import net.sf.mpango.game.core.exception.ConstructionAlreadyInPlaceException;
import net.sf.mpango.game.core.exception.EventNotSupportedException;
import net.sf.mpango.game.core.exception.UnknownTechnologyException;
import net.sf.mpango.game.core.exception.UselessShieldException;
import net.sf.mpango.game.core.technology.entity.ShieldTechnology;
import net.sf.mpango.game.core.technology.entity.WeaponTechnology;

/**
 * <p>Abstract class that contains the basic attributes and methods for all units.</p>
 * <p>Every kind of units has certain amount of attack points(AP) and hit points(HP), if the unit's HP reaches zero, it dies / blows up</p>
 * <p>Every unit is created using different technologies. So the attributes of the unit vary depending on the technologies that are used at creation time.</p>
 * @author edvera
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Unit<T extends Event> extends AbstractEntity<Long> implements Runnable, Damageable, Observer<T>, Serializable {

    static final int DEFAULT_THREAD_NUMBER = 1;

    static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(DEFAULT_THREAD_NUMBER);

    static final int DEFAULT_UNIT_SLEEP_TIME = 100;

    private float maximumHitPoints;
    private float attackPoints;
    private float hitPoints;
    private Shield shield;
    private Weapon weapon;
    private List<Technology> technologies;
    private float constructionSkills;
    private float collectionSkills;
    private BlockingQueue<FutureTask<? extends CommandEvent>> futureCommands;
    private Timer timer;
    private City city;
    private BaseObserver baseObserver;

    private Object lock = new Object();

    /**
	 * At the beginning of the game the Unit can not belong to a city.
	 */
	public Unit() {}

	/**
	 * <p>Every time a unit is created it is done by assigning it a city.
	 * All units in the game belong to a city, to and from which resources flow.
	 * The only exception to this are the initial units, which don't belong to a city until the first city is founded.</p>
	 * @param city where the unit is born or has moved.
	 */
	protected Unit(final City city) {
		this (city, new ArrayList<Technology>(), 0f, 0f);
	}

	/**
	 * Convenient constructor for test classes.
	 * @param city
	 * @param attackPoints
	 * @param maximumHitPoints
	 */
	protected Unit (final City city,
                    final List<Technology> technologies,
                    final float attackPoints,
                    final float maximumHitPoints) {
		this.city = city;
		this.futureCommands = new LinkedBlockingQueue<>();
		this.setTechnologies(technologies);
		this.hitPoints = maximumHitPoints;
		this.maximumHitPoints = maximumHitPoints;
		this.attackPoints = attackPoints;
		this.timer = new Timer();
        this.baseObserver = new BaseObserver();
	}

	/**
	 * Method that serves to apply a shield technology.
	 * @param technology
	 */
	private void applyTechnology(final ShieldTechnology technology) {
		this.shield = technology.createShield();
	}

	/**
	 * Method that serves to aply a weapon technology.
	 * @param technology
	 */
	private void applyTechnology(final WeaponTechnology technology) {
		this.weapon = technology.createWeapon();
	}

	/**
	 * Method that switches WeaponTechnology or ShieldTechnology.
	 * @param technology
	 * @throws UnknownTechnologyException in case the technology to apply is aplicable to the unit.
	 */
	private void applyTechnology(final Technology technology) throws UnknownTechnologyException {
		if (technology instanceof WeaponTechnology) {
			applyTechnology((WeaponTechnology) technology);
		} else if (technology instanceof ShieldTechnology) {
			applyTechnology((ShieldTechnology) technology);
		} else {
			throw new UnknownTechnologyException(technology);
		}
	}

	/**
	 * This method calculates the effective attack points the unit has.
	 * The three factors that are important are:
	 * - attackBonus from the weapon.
	 * - health of the unit.
	 * - attack points of the unit.
	 * @return
	 */
	@Transient
	public float getEffectiveAttackPoints() {
		return attackPoints * weapon.getAttackBonus() * this.getHealth();
	}

	/**
	 * Method called when the unit is attacked. It first directs the attack to the possible shield and then, in case of resting hit points, it will
	 * target them to the unit.
	 * @param attacker the attacker that is causing the damage
     * @param attackBonus to use for the total attack points.
     * @param defenseBonus to use for the total defense points.
	 */
	@Transient
	public void receiveDamage(final Unit attacker, final float attackBonus, final float defenseBonus) {
        float attackPoints = attacker.getEffectiveAttackPoints() * (attackBonus / defenseBonus);
		float remainingDamage = attackPoints;
		if (this.shield != null) {
			try {
				remainingDamage = shield.receiveDamage(attackPoints);
			} catch (final UselessShieldException e) {
				this.shield = null;
			}
		}
		this.hitPoints -= remainingDamage;
	}


	/**
	 * Determines if the unit is dead.
	 * @return true if the unit is dead.
	 */
	@Transient
	public synchronized boolean isDead() {
		return (this.hitPoints <= 0);
	}

	/**
	 * <p>Health is determined by the relation between maximum hit points and the actual hit points.
	 * Health is always less than 1 and more than 0;</p>
	 * @return
	 */
	@Transient
	public float getHealth() {
		float hitPoints = this.hitPoints;
		float maximumHitPoints = this.maximumHitPoints;
		float realHealth = hitPoints / maximumHitPoints;
		return realHealth > 0 ? realHealth : 0;
	}

    /**
	 * <p>Action the Unit can perform in order to settle in a cell resulting in a new city construction.</p>
	 * @return
	 * @throws ConstructionAlreadyInPlaceException
	 */
	public Future<CommandEvent> settle(final Cell cell) throws CommandException {
		return construct(cell, new City());
	}

    /**
	 * <p>Action to to a put to work in a specific construction. The unit will be busy for a certain time until the construction is finished.</p>
	 * @param cell
	 * @param construction
	 * @return
	 * @throws ConstructionAlreadyInPlaceException
	 */
	protected Future<CommandEvent> construct(final Cell cell, final Construction construction) throws CommandException {
		final ConstructCommand command = new ConstructCommand(Unit.EXECUTOR_SERVICE, this, construction, cell);
		Future<CommandEvent> futureEvent = this.addCommand(command);
        return futureEvent;
	}
	/**
	 * <p>Method that adds a command to the command queue. If it is the first command, it also executes it after having it added.</p>
	 * @param command Command to be added and possibly executed.
	 * @throws CommandException In case there is a problem with the execution of the command.
	 */
	<T extends CommandEvent> Future<T> addCommand(final Command<T> command) throws CommandException {
        command.addListener(this);
        final FutureTask<T> futureCommand = new FutureTask<>(command);
        futureCommands.add(futureCommand);
        LOGGER.log(Level.INFO, String.format("Command %s added to the list of commands to execute.", command.getClass().getName()));
        return futureCommand;
	}

	@Transient
	public float repair() {
		return 0;
	}

	@Transient
	public void putResources(Resources resource, int foodCollected) {
		city.addResources(resource, foodCollected);
	}

    public float getHitPoints() {
		return this.hitPoints;
	}

    public void setHitPoints(float hitPoints) {
		this.hitPoints = hitPoints;
	}
    @Column
	public float getMaximumHitPoints() {
		return maximumHitPoints;
	}
    public void setMaximumHitPoints(float maximumHitPoints) {
        this.maximumHitPoints = maximumHitPoints;
    }
	@OneToOne
	public Weapon getWeapon() {
		return this.weapon;
	}

    public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
	}

    @OneToOne
	public Shield getShield() {
		return this.shield;
	}
    public void setShield(Shield shield) {
		this.shield = shield;
	}
    @OneToMany
	public List<Technology> getTechnologies() {
		return technologies;
	}
    public void setTechnologies(final List<Technology> technologies) {
		final Iterator<Technology> iterator = technologies.iterator();
		while (iterator.hasNext()) {
			try {
				applyTechnology(iterator.next());
			} catch (final UnknownTechnologyException e) {
				LOGGER.log(Level.FINE, LocalizedMessageBuilder.getSystemMessage(this, MessageConstants.UNIT_UNKNOWN_TECHNOLOGY, e.getUnknownTechnology()));
			}
		}
		this.technologies = technologies;
	}
    @Override
	public void observe(final T event) throws EventNotSupportedException {
        if (event instanceof CommandExecutedEvent) {
            LOGGER.log(Level.INFO, "Command executed event observed");
            observe((CommandExecutedEvent) event);
        } else {
            LOGGER.log(Level.INFO, LocalizedMessageBuilder.getSystemMessage(this, MessageConstants.UNIT_OBSERVER_DEFAULT_OBSERVER));
            baseObserver.observe(event);
        }
    }

    public void observe(final CommandEvent event) {
        final Command command = event.getCommand();
        if (command instanceof ConstructCommand) {
            LOGGER.log(Level.INFO, "Construct command observed");
            final ConstructCommand constructCommand = (ConstructCommand) command;
            final Unit constructingUnit = constructCommand.getUnit();
            final Construction construction = constructCommand.getConstruction();
            if (this.equals(constructingUnit) && construction.getClass().equals(City.class)) {
                LOGGER.log(Level.INFO,
                        LocalizedMessageBuilder.getSystemMessage(
                                this,
                                MessageConstants.UNIT_USED_FOR_CONSTRUCTION)
                );
                this.die();
            }
        }
    }

    public float getConstructionSkills() {
		return this.constructionSkills;
	}

    public void setConstructionSkills(float constructionSkills) {
		this.constructionSkills = constructionSkills;
	}
    public void improveConstructionSkills(float skillsUpgrade) {
		this.constructionSkills+=skillsUpgrade;
	}
    @Transient
	public Timer getTimer() {
		if (timer == null) {
			timer = new Timer();
		}
		return timer;
	}
    //For testing purposes.
	protected void setTimer(Timer timer) {
		this.timer = timer;
	}
    public float getCollectionSkills() {
		return this.collectionSkills;
	}
    public void setCollectionSkills(float collectionSkills) {
		this.collectionSkills = collectionSkills;
	}
    public void improveCollectionSkills(float skillsUpgrade) {
		this.collectionSkills += skillsUpgrade;
	}
    public City getCity() {
   		return city;
   	}
    public void setCity(City city) {
		this.city = city;
	}
    /**
	 * Method that kills the unit.
	 */
	private synchronized void die() {
        this.timer.cancel();
        this.city = null;
        this.hitPoints = 0;
	}

    @Override
    public void run() {
        while (!isDead()) {
            LOGGER.log(Level.FINEST,
                    LocalizedMessageBuilder.getSystemMessage(
                            this,
                            MessageConstants.UNIT_IS_ALIVE,
                            this.getId()));
            final FutureTask<? extends CommandEvent> commandToExecute = futureCommands.poll();
            try {
                if (commandToExecute != null) {
                    LOGGER.log(Level.INFO, "Command to be executed " + commandToExecute.getClass().getSimpleName());
                    EXECUTOR_SERVICE.submit(commandToExecute);
                } else {
                    LOGGER.log(Level.INFO, "No command to executed, sleeping");
                    Thread.currentThread().sleep(getSleepTime());
                }
            } catch (final InterruptedException e) {
                LOGGER.log(Level.WARNING,
                        LocalizedMessageBuilder.getSystemMessage(
                                this,
                                MessageConstants.UNIT_THREAD_INTERRUPTED),
                        e);
            }
        }
    }

    public void kill() {
        LOGGER.log(Level.FINEST,
                LocalizedMessageBuilder.getSystemMessage(
                        this,
                        MessageConstants.UNIT_KILLED,
                        Thread.currentThread().getStackTrace()[2].getClassName()));
        synchronized(this.lock) {
            if (!isDead()) {
                synchronized(this.lock) {
                    this.die();
                }
            }
        }
    }

    public long getSleepTime() {
        return DEFAULT_UNIT_SLEEP_TIME;
    }
    private static final Logger LOGGER = Logger.getLogger(Unit.class.getName());

    private static final long serialVersionUID = -3825620941652893699L;
}