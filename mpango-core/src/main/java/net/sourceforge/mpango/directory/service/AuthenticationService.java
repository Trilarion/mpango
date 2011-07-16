package net.sourceforge.mpango.directory.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

import net.sourceforge.mpango.directory.dao.UserDAO;
import net.sourceforge.mpango.directory.entity.User;
import net.sourceforge.mpango.entity.Player;
import net.sourceforge.mpango.enums.StateEnum;
import net.sourceforge.mpango.exception.PlayerAlreadyExistsException;

/**
 * Basic Authentication Service implementation.
 * 
 * @author etux
 * 
 */
public class AuthenticationService implements IAuthenticationService {

	private UserDAO userDAO;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.mpango.service.IAuthenticationService#load(java.lang.
	 * String)
	 */
	@Override
	public User load(String email) {
		return userDAO.load(email);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.mpango.service.IAuthenticationService#save(net.sourceforge
	 * .mpango.entity.User)
	 */
	@Override
	public User register(User user) {
		String nonce = generateNonce();
		user.setNonceToken(nonce);
		user.setState(StateEnum.CREATED);
		return userDAO.save(user);
	}

	public String generateNonce() {
		// Create two secure number generators with the same seed
		int seedByteCount = 24;
		byte[] seed = new byte[seedByteCount];
		try {
			// Create a secure random number generator
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			// Get 1024 random bits
			byte[] bytes = new byte[1024 / 8];
			sr.nextBytes(bytes);
			seed = sr.generateSeed(seedByteCount);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return new String(seed);
	}

	public Player createPlayer(User user, Player player)
			throws PlayerAlreadyExistsException {
		List<Player> players = userDAO.findPlayersByUser(user);

		if (null != players && players.size() > 0) {
			throw new PlayerAlreadyExistsException();
		}
		player.setUser(user);
		return userDAO.save(player);
	}

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	@Override
	public List<User> list() {
		return userDAO.list();
	}

	@Override
	public void delete(Player player) {
		player.setState(StateEnum.DELETED);
		userDAO.save(player);

	}

	@Override
	public String generateResetKey(String email) {
		String resetKey = String.valueOf(UUID.randomUUID());
		User user = getUserDAO().load(email);
		user.setResetKey(resetKey);
		getUserDAO().save(user);
		return resetKey;
	}

}