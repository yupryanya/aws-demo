package com.task10.service;

import com.task10.models.SignUp;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

public class CognitoService {
    private static final String USER_POOL = System.getenv("booking_userpool");
    private static final Region REGION = Region.of(System.getenv("region"));

    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;
    private final String clientId;

    public CognitoService() {
        this.cognitoClient = initCognitoClient();
        this.userPoolId = getUserPoolId();
        this.clientId = getClientId();
    }

    public AdminInitiateAuthResponse cognitoSignIn(String email, String password) {
        Map<String, String> authParams = Map.of(
                "USERNAME", email,
                "PASSWORD", password
        );
        return cognitoClient.adminInitiateAuth(AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParams)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .build());
    }

    public AdminCreateUserResponse cognitoSignUp(SignUp signUp) {
        return cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(signUp.getEmail())
                .temporaryPassword(signUp.getPassword())
                .userAttributes(
                        AttributeType.builder()
                                .name("given_name")
                                .value(signUp.getFirstName())
                                .build(),
                        AttributeType.builder()
                                .name("family_name")
                                .value(signUp.getLastName())
                                .build(),
                        AttributeType.builder()
                                .name("email")
                                .value(signUp.getEmail())
                                .build(),
                        AttributeType.builder()
                                .name("email_verified")
                                .value("true")
                                .build())
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .messageAction("SUPPRESS")
                .forceAliasCreation(Boolean.FALSE)
                .build()
        );
    }

    public AdminRespondToAuthChallengeResponse confirmSignUp(SignUp signUp) {
        AdminInitiateAuthResponse adminInitiateAuthResponse = cognitoSignIn(signUp.getEmail(), signUp.getPassword());
        if (!ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(adminInitiateAuthResponse.challengeNameAsString())) {
            throw new RuntimeException("unexpected challenge: " + adminInitiateAuthResponse.challengeNameAsString());
        }
        Map<String, String> challengeResponses = Map.of(
                "USERNAME", signUp.getEmail(),
                "PASSWORD", signUp.getPassword(),
                "NEW_PASSWORD", signUp.getPassword()
        );
        return cognitoClient.adminRespondToAuthChallenge(AdminRespondToAuthChallengeRequest.builder()
                .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                .challengeResponses(challengeResponses)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .session(adminInitiateAuthResponse.session())
                .build());
    }

    private CognitoIdentityProviderClient initCognitoClient() {
        return CognitoIdentityProviderClient.builder()
                .region(REGION)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private String getUserPoolId() {
        ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder().build();
        ListUserPoolsResponse listUserPoolsResponse = cognitoClient.listUserPools(listUserPoolsRequest);
        return listUserPoolsResponse.userPools().stream()
                .filter(pool -> pool.name().contains(USER_POOL))
                .findAny()
                .orElseThrow(() -> new RuntimeException(String.format("User pool %s not found.", USER_POOL)))
                .id();
    }

    private String getClientId() {
        String userPoolId = getUserPoolId();
        ListUserPoolClientsRequest listUserPoolClientsRequest = ListUserPoolClientsRequest.builder()
                .userPoolId(userPoolId)
                .maxResults(1)
                .build();
        ListUserPoolClientsResponse listUserPoolClientsResponse = cognitoClient.listUserPoolClients(listUserPoolClientsRequest);
        return listUserPoolClientsResponse.userPoolClients().stream()
                .filter(client -> client.clientName().contains("client-app"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Client 'client-app' not found."))
                .clientId();
    }
}
