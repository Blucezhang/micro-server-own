package com.own.user.party.service;

import com.own.face.trade.TradeException;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.PersonDao;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dao.domain.Person;
import com.own.user.party.dto.BuyerRegistrationCommand;
import com.own.user.party.dto.LoginUserResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BuyerRegistrationServiceTest {
    private PersonDao people;
    private LoginUserDao users;
    private LoginUserService loginUsers;
    private BuyerRegistrationService registrations;

    @Before
    public void setUp() {
        people = mock(PersonDao.class); users = mock(LoginUserDao.class); loginUsers = mock(LoginUserService.class);
        registrations = new BuyerRegistrationService(people, users, loginUsers);
    }

    @Test
    public void createsOnlyBuyerCompatiblePersonAndLoginUser() {
        when(people.save(any(Person.class))).thenAnswer(invocation -> { Person person = (Person) invocation.getArguments()[0]; person.setId(91L); return person; });
        when(loginUsers.create(any(LoginUser.class), org.mockito.Matchers.eq("password-123"))).thenAnswer(invocation -> { LoginUser user = (LoginUser) invocation.getArguments()[0]; user.setLoginUserId(92L); return user; });

        LoginUserResponse response = registrations.register(command());

        assertEquals(Long.valueOf(91L), response.getPartyId());
        assertEquals("buyer01", response.getLoginName());
        verify(people).createRelationShipData(91L);
        verify(users).createRelationShipPersonWithLoginUser(91L, 92L);
    }

    @Test
    public void rejectsExistingLoginNameBeforeCreatingPerson() {
        LoginUser existing = new LoginUser();
        when(users.findByLoginName("buyer01")).thenReturn(existing);
        try {
            registrations.register(command());
            fail("duplicate login name must be rejected");
        } catch (TradeException expected) {
            assertEquals(409, expected.getStatus());
        }
    }

    @Test
    public void rejectsMalformedPublicLoginNameBeforeCreatingGraphNodes() {
        BuyerRegistrationCommand command = command(); command.setLoginName("bad user name");
        try {
            registrations.register(command);
            fail("unsafe login name must be rejected");
        } catch (TradeException expected) {
            assertEquals(422, expected.getStatus());
        }
    }

    @Test
    public void rejectsWeakPasswordBeforeCreatingGraphNodes() {
        BuyerRegistrationCommand command = command(); command.setPassword("short");
        try {
            registrations.register(command);
            fail("weak password must be rejected");
        } catch (TradeException expected) {
            assertEquals(422, expected.getStatus());
        }
    }

    private BuyerRegistrationCommand command() {
        BuyerRegistrationCommand command = new BuyerRegistrationCommand();
        command.setLoginName("Buyer01"); command.setPassword("password-123"); command.setName("Buyer");
        command.setEmail("buyer@example.test"); command.setPhone("13800138000"); return command;
    }
}
