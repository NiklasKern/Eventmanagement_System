package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.domain.exceptions.EventManagementException;

public interface SecuritySetupService {
    void ensureBuiltInRoles();
    void ensureAdminAccount(String username, String password) throws EventManagementException;
}
