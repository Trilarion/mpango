package net.sourceforge.mpango.exception;

import net.sourceforge.mpango.entity.Construction;

public class ConstructionAlreadyInPlaceException extends Exception {

	/** generated serial version uid */
	private static final long serialVersionUID = -403216765336288386L;
	private Construction construction;
	
	public ConstructionAlreadyInPlaceException (Construction construction) {
		super ();
		this.construction = construction;
	}

	public Construction getConstruction () {
		return this.construction;
	}
}