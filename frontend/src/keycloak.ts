import Keycloak from "keycloak-js";

const keycloak = Keycloak({
    url: 'http://localhost:8081/auth',
    realm: 'eventsourcing-with-axon',
    clientId: 'my-backend'
});

export default keycloak;