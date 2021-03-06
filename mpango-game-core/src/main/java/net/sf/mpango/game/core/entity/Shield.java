package net.sf.mpango.game.core.entity;

import javax.persistence.Embeddable;

import net.sf.mpango.common.entity.AbstractPersistable;
import net.sf.mpango.game.core.exception.UselessShieldException;

/**
 * This class represents the shield a unit might have.
 * @author etux
 *
 */
@Embeddable
public class Shield extends AbstractPersistable<Long> {
	
	public static final float REPAIR_COST_PER_HITPOINT = 0.1f;
	private float maximumHitPoints;
	private float hitPoints;

    public Shield() {}

	/**
	 * This constructor gives us a brand new shield with all hitPoints available.
	 * @param maximumHitPoints
	 */
	public Shield(float maximumHitPoints) {
		this.maximumHitPoints = maximumHitPoints;
		this.hitPoints = maximumHitPoints;
	}

	/**
	 * When a unit that owns the shield is attacked, the first one to receive the damage is the shield.
	 * @param attackPoints represents the strength of the attack.
	 * @return in case the shield could not take all the attack, returns the attackPoints that the shield could not take. In other case, returns 0.
	 */
	public Float receiveDamage(float attackPoints) throws UselessShieldException {
		if (this.hitPoints <= 0) {
			//This shield has been destroyed
			throw new UselessShieldException();
		}
		this.hitPoints -= attackPoints;
		if (this.hitPoints < 0) { //Shield can not take more damage
			return -this.hitPoints;
		} else { // Shield has taken all the damage
			return 0f;
		}
	}
	
	/**
	 * This method will restore the hitPoints the shield can take to the maximum.
	 * @return the cost of the repair.
	 */
	public float repair() {
		Float cost = (maximumHitPoints - hitPoints) * REPAIR_COST_PER_HITPOINT;
		this.hitPoints = maximumHitPoints;
		return cost;
	}

	/**
	 * Returns the remaining hit points the shield can still get.
	 * @return
	 */
	public float getRemainingHitPoints() {
		return hitPoints;
	}
	public void setRemainingHitPoints(float hitPoints) {
		this.hitPoints = hitPoints;
	}

	public float getMaximumHitPoints() {
		return maximumHitPoints;
	}
	
	public void setMaximumHitPoints(float maximumHitPoints) {
        this.maximumHitPoints = maximumHitPoints;
    }
}
