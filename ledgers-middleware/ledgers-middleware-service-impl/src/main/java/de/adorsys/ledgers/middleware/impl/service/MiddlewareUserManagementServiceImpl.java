package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MiddlewareUserManagementServiceImpl implements MiddlewareUserManagementService {
    private final UserService userService;
    private final DepositAccountService depositAccountService;
    private final UserMapper userTOMapper = Mappers.getMapper(UserMapper.class);

    @Override
    public UserTO create(UserTO user) {
        UserBO userBO = userTOMapper.toUserBO(user);
            return userTOMapper.toUserTO(userService.create(userBO));
    }

    @Override
    public UserTO findById(String id) {
            return userTOMapper.toUserTO(userService.findById(id));
    }

    @Override
    public UserTO findByUserLogin(String userLogin) {
            return userTOMapper.toUserTO(userService.findByLogin(userLogin));
    }

    @Override
    public UserTO updateScaData(String userLogin, List<ScaUserDataTO> scaDataList){
            UserBO userBO = userService.updateScaData(userTOMapper.toScaUserDataListBO(scaDataList), userLogin);
            return userTOMapper.toUserTO(userBO);
    }

    @Override
    public UserTO updateAccountAccess(String userLogin, List<AccountAccessTO> accounts) throws UserNotFoundMiddlewareException {
        try {
            // check if accounts exist in ledgers deposit account
            for (AccountAccessTO account : accounts) {
                depositAccountService.getDepositAccountByIban(account.getIban(), LocalDateTime.now(), false);
            }

            UserBO userBO = userService.updateAccountAccess(userLogin, userTOMapper.toAccountAccessListBO(accounts));
            return userTOMapper.toUserTO(userBO);
        } catch (DepositAccountNotFoundException e) {
            log.error(e.getMessage());
            throw new UserNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public List<UserTO> listUsers(int page, int size) {
        return userTOMapper.toUserTOList(userService.listUsers(page, size));
    }

    @Override
    public List<UserTO> getUsersByBranchAndRoles(String branch, List<UserRoleTO> roles) {
        List<UserBO> users = userService.findByBranchAndUserRolesIn(branch, userTOMapper.toUserRoleBO(roles));
        return userTOMapper.toUserTOList(users);
    }

    @Override
    public int countUsersByBranch(String branch) {
        return userService.countUsersByBranch(branch);
    }
}
