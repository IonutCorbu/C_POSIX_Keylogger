import React, { useEffect, useState } from 'react';
import keycloak from './keycloak';
import { ReactKeycloakProvider } from '@react-keycloak/web';

const KeycloakProviderWithInit = ({ children }) => {
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    keycloak.init({ onLoad: 'login-required' }).then(authenticated => {
      if (authenticated) setInitialized(true);
    });
  }, []);

  if (!initialized) return <p>Loading authentication...</p>;

  return children;
};

export default KeycloakProviderWithInit;
