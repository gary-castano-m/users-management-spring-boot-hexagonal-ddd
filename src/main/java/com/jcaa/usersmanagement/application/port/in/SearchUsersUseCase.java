package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.query.SearchUsersQuery;
import com.jcaa.usersmanagement.domain.model.UserModel;
import java.util.List;

// Caso de uso: reportes de usuarios filtrados por rol y/o nombre.
public interface SearchUsersUseCase {

  List<UserModel> execute(SearchUsersQuery query);
}