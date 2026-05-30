package com.velox.module.system.account.service;

import com.velox.common.result.PageResult;
import com.velox.module.system.account.dto.AccountDetailCardDTO;
import com.velox.module.system.account.dto.AccountListItemDTO;
import com.velox.module.system.account.dto.AccountQuery;
import com.velox.module.system.account.dto.AccountSaveCommand;

import java.util.List;

public interface AccountManageService {

    PageResult<AccountListItemDTO> list(AccountQuery query);

    AccountDetailCardDTO getDetailCard(String accountId);

    String create(AccountSaveCommand command);

    Boolean update(String userId, AccountSaveCommand command);

    Boolean delete(String userId);

    Boolean deleteBatch(List<String> accountIds);
}
