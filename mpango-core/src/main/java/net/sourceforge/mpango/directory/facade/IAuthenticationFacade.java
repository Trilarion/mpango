package net.sourceforge.mpango.directory.facade;


import net.sourceforge.mpango.directory.dto.UserDTO;

public interface IAuthenticationFacade {

	/**
	 * finds {@link UserDTO} by email
	 * 
	 * @param email
	 * @return {@link UserDTO}
	 */
	public UserDTO load(String email);

	/**
	 * persists {@link UserDTO} in database
	 * 
	 * @param user
	 * @return {@link UserDTO}
	 */
	public UserDTO register(UserDTO user);
}
