package net.sourceforge.mpango.service;

import net.sourceforge.mpango.directory.entity.User;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: etux
 * Date: 6/13/11
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class GameServiceTest {

    private GameServiceImpl service;

    @Before
    public void setUp() {
        service = new GameServiceImpl();
    }

    @Test
    public void testJoin() {
        String name = RandomStringUtils.random(12);
        User user = new User();
        service.join(name, user);
    }
}