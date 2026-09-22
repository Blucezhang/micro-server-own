package com.own.user.party.service;

import com.own.face.trade.TradeException;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.PersonDao;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dao.domain.Person;
import com.own.user.party.dto.BuyerRegistrationCommand;
import com.own.user.party.dto.LoginUserResponse;
import java.util.Collections;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** Creates the legacy Person and LoginUser graph nodes needed by a buyer token. */
@Service
public class BuyerRegistrationService {
    private final PersonDao people;
    private final LoginUserDao users;
    private final LoginUserService loginUsers;

    public BuyerRegistrationService(PersonDao people, LoginUserDao users, LoginUserService loginUsers) {
        this.people = people; this.users = users; this.loginUsers = loginUsers;
    }

    public synchronized LoginUserResponse register(BuyerRegistrationCommand command) {
        if (command == null) throw TradeException.unprocessable("registration payload is required");
        String loginName = required(command.getLoginName(), "loginName", 64).toLowerCase(Locale.ROOT);
        if (!loginName.matches("^[a-z0-9._-]{3,64}$")) throw TradeException.unprocessable("loginName is invalid");
        password(command.getPassword());
        if (users.findByLoginName(loginName) != null) throw TradeException.conflict("loginName is already registered");
        String name = required(command.getName(), "name", 100);
        String email = email(command.getEmail());
        String phone = phone(command.getPhone());

        Person person = new Person();
        person.setName(name); person.setEmail(email); person.setPhone(phone);
        person.setPartyTypeId(1); person.setPartyTypeName("个人用户"); person.setShipaddres(Collections.<String>emptyList());
        person = people.save(person);
        if (person.getId() == null) throw new IllegalStateException("person was not persisted");
        people.createRelationShipData(person.getId());

        LoginUser user = new LoginUser();
        user.setLoginName(loginName); user.setName(name); user.setEmail(email); user.setPhone(phone); user.setPartyId(person.getId());
        user = loginUsers.create(user, command.getPassword());
        if (user.getLoginUserId() == null) throw new IllegalStateException("login user was not persisted");
        users.createRelationShipWithLoginUser(user.getLoginUserId());
        users.createRelationShipPersonWithLoginUser(person.getId(), user.getLoginUserId());
        return LoginUserResponse.from(user);
    }

    private String required(String value, String field, int maximum) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > maximum) throw TradeException.unprocessable(field + " is invalid");
        return normalized;
    }
    private String email(String value) {
        String normalized = required(value, "email", 254);
        if (!normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw TradeException.unprocessable("email is invalid");
        return normalized;
    }
    private String phone(String value) {
        String normalized = required(value, "phone", 32);
        if (!normalized.matches("^[0-9+() -]{6,32}$")) throw TradeException.unprocessable("phone is invalid");
        return normalized;
    }
    private void password(String value) {
        if (value == null || value.length() < 8 || value.length() > 128 || value.matches(".*[\\p{Cntrl}].*")) {
            throw TradeException.unprocessable("password is invalid");
        }
    }
}
