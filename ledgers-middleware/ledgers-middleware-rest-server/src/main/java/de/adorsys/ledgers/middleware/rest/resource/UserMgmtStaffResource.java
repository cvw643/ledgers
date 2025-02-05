package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.PageMapper;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.INSUFFICIENT_PERMISSION;


@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping("/staff-access" + UserMgmtRestAPI.BASE_PATH)
public class UserMgmtStaffResource implements UserMgmtStaffResourceAPI {
    private final MiddlewareOnlineBankingService onlineBankingService;
    private final MiddlewareUserManagementService middlewareUserService;
    private final ScaInfoHolder scaInfoHolder;
    private final PageMapper pageMapper;

    @Override
    public ResponseEntity<UserTO> register(String branch, UserTO branchStaff) {
        // staff user can not register for the branch is already taken
        if (middlewareUserService.countUsersByBranch(branch) > 0) {
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg(USER_CANNOT_REGISTER_IN_BRANCH)
                          .build();
        }

        branchStaff.setBranch(branch);
        branchStaff.setUserRoles(Collections.singletonList(UserRoleTO.STAFF));
        UserTO user = middlewareUserService.create(branchStaff);
        user.setPin(null);

        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<UserTO> modifyUser(String branch, UserTO user) {
        return ResponseEntity.ok(middlewareUserService.updateUser(branch, user));
    }

    @Override
    public ResponseEntity<SCALoginResponseTO> login(UserCredentialsTO userCredentials) {
        return ResponseEntity.ok(onlineBankingService.authorise(userCredentials.getLogin(), userCredentials.getPin(), UserRoleTO.STAFF));
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<UserTO> createUser(UserTO user) {
        UserTO branchStaff = middlewareUserService.findById(scaInfoHolder.getScaInfo().getUserId());

        // set the same branch for the user the staff member that creates it
        user.setBranch(branchStaff.getBranch());

        // Assert that the role is neither system nor technical
        user.getUserRoles().remove(UserRoleTO.SYSTEM);
        user.getUserRoles().remove(UserRoleTO.TECHNICAL);

        UserTO newUser = middlewareUserService.create(user);
        newUser.setPin(null);

        return ResponseEntity.ok(newUser);
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<CustomPageImpl<UserTO>> getBranchUsersByRoles(List<UserRoleTO> roles, String queryParam, int page, int size) {
        CustomPageableImpl pageable = new CustomPageableImpl(page, size);
        UserTO branchStaff = middlewareUserService.findById(scaInfoHolder.getScaInfo().getUserId());
        CustomPageImpl<UserTO> users = middlewareUserService.getUsersByBranchAndRoles(branchStaff.getBranch(), roles, queryParam, pageable);
        return ResponseEntity.ok(users);
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<UserTO> getBranchUserById(String userId) {
        UserTO user = findUserForBranch(userId);
        return ResponseEntity.ok(user);
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> updateUserScaData(String userId, List<ScaUserDataTO> data) {
        UserTO user = findUserForBranch(userId);
        UserTO userWithUpdatedSca = middlewareUserService.updateScaData(user.getLogin(), data);
        URI uri = UriComponentsBuilder.fromUriString("/staff-access" + UserMgmtRestAPI.BASE_PATH + "/" + userWithUpdatedSca.getId())
                          .build().toUri();
        return ResponseEntity.created(uri).build();
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> updateAccountAccessForUser(String userId, AccountAccessTO access) {
        ScaInfoTO scaInfo = scaInfoHolder.getScaInfo();
        middlewareUserService.updateAccountAccess(scaInfo, userId, access);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private UserTO findUserForBranch(String userId) {
        UserTO branchStaff = middlewareUserService.findById(scaInfoHolder.getScaInfo().getUserId());
        UserTO user = middlewareUserService.findById(userId);

        if (!branchStaff.getBranch().equals(user.getBranch())) {
            throw MiddlewareModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg(USER_NOT_IN_BRANCH)
                          .build();
        }
        return user;
    }
}
