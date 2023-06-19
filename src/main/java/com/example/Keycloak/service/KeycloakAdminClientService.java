package com.example.Keycloak.service;


import com.example.Keycloak.request.CreateUserRequest;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class KeycloakAdminClientService {
    @Value("${keycloak.realm}")
    public String realm;

    @Value("${keycloak.resource}")
    public String clientId;

    private final Keycloak keycloak;


    public KeycloakAdminClientService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public Response createKeycloakUser(CreateUserRequest user) {
        UsersResource usersResource = keycloak.realm(realm).users();
        CredentialRepresentation credentialRepresentation = createPasswordCredentials(user.getPassword());

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(user.getEmail());
        kcUser.setCredentials(Collections.singletonList(credentialRepresentation));
        kcUser.setFirstName(user.getFirstname());
        kcUser.setLastName(user.getLastname());
        kcUser.setEmail(user.getEmail());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(false);

        Response response = usersResource.create(kcUser);

        if (response.getStatus() == 201) {
            //If you want to save the user to your other database, do it here, for example:
//            User localUser = new User();
//            localUser.setFirstName(kcUser.getFirstName());
//            localUser.setLastName(kcUser.getLastName());
//            localUser.setEmail(user.getEmail());
//            localUser.setCreatedDate(Timestamp.from(Instant.now()));
//            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
//            usersResource.get(userId).sendVerifyEmail();
//            userRepository.save(localUser);

            String userId = CreatedResponseUtil.getCreatedId(response);
            UserResource userResource = usersResource.get(userId);
            RoleRepresentation roleRepresentation = keycloak.realm(realm)
                    .clients().get(clientId).roles().get("member").toRepresentation();
            userResource.roles().clientLevel(clientId).add(Arrays.asList(roleRepresentation));
        }

        return response;

    }

    private static CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }

    private void addRole(List<String> roles){
//        ClientRepresentation client = kcProvider.getInstance().realm(realm)
//                .clients().findByClientId(clientId).get(0);
//        RoleRepresentation roleRepresentation = kcProvider.getInstance().realm(realm)
//                .clients().get(client.getId()).roles().get("member").toRepresentation();

    }

}
