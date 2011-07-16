/**
 * 
 */
package net.sourceforge.mpango.directory.service;

import java.util.Calendar;

import net.sourceforge.mpango.BaseSpringTest;
import net.sourceforge.mpango.directory.dao.UserDAO;
import net.sourceforge.mpango.directory.entity.User;
import net.sourceforge.mpango.entity.Player;
import net.sourceforge.mpango.exception.PlayerAlreadyExistsException;
import net.sourceforge.mpango.TestUtils;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author aplause
 * 
 */
public class AuthenticationServiceTest extends BaseSpringTest {

	@Autowired
	AuthenticationService authService;

	@Test
	public void authenticationTest() {
		User user = new User();
		user.setDateOfBirth(Calendar.getInstance().getTime());
		user.setEmail("User@company.com");
		user.setGender("male");
		user.setPassword("pwd");
		user.setUsername("user");
		user = authService.register(user);
		Assert.assertNotNull(user);
		Assert.assertNotNull(user.getIdentifier());

		User userFound = authService.load("nonexisted@email.com");
		Assert.assertNull(userFound);
		userFound = authService.load("User@company.com");
		Assert.assertNotNull(userFound);
		Assert.assertEquals(userFound.getUsername(), user.getUsername());

	}

	@Test(expected = PlayerAlreadyExistsException.class)
	public void createPlayerTest() throws PlayerAlreadyExistsException {
		User user = new User();
		user.setDateOfBirth(Calendar.getInstance().getTime());
		user.setEmail("User@company.com");
		user.setGender("male");
		user.setPassword("pwd");
		user.setUsername("user");
		user = authService.register(user);

		Player player = TestUtils.getPlayer();
		player = authService.createPlayer(user, player);
		Assert.assertNotNull(player.getIdentifier());

		authService.delete(player);
		
		player = TestUtils.getPlayer();
		player = authService.createPlayer(user, player);
		
		Player player2 = TestUtils.getPlayer();
		// here exception should be raised 
		player2 = authService.createPlayer(user, player2);
	}
	
	@Test
	public void testGenerateResetKey() {
		String email = "email@domain.com";
		User user = EasyMock.createMock(User.class);
		UserDAO userDAO = EasyMock.createMock(UserDAO.class);
		authService.setUserDAO(userDAO);
		EasyMock.expect(userDAO.load(email)).andReturn(user);
		user.setResetKey(EasyMock.isA(String.class));
		userDAO.update(user);
		EasyMock.replay(userDAO);
		String resetKey = authService.generateResetKey(email);
		EasyMock.verify(userDAO);
		Assert.assertNotNull(resetKey);
		
	}

}
