import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '@/store/store';
import { Container } from '@mui/material';

// Higher-order component to do an auth check before rendering a component
function withAuthCheck(title: string, WrappedComponent: React.ComponentType) {
    return function ProtectedComponent(props: any) {
        const { isLoggedIn, oauthCreds, isRehydrated } = useSelector((state: RootState) => state.user);
        const [errorMessage, setErrorMessage] = useState("Initializing...");

        useEffect(() => {
            if (!isRehydrated)
                setErrorMessage("Rehydrating state...");
            if (oauthCreds && !isLoggedIn)
                setErrorMessage("Please log in to view this page.");
        }, [oauthCreds, isLoggedIn]);

        if (!isLoggedIn) {
            return (
                <Container>
                    <h1>{title}</h1>
                    <p>{errorMessage}</p>
                </Container>
            );
        }

        return <WrappedComponent {...props} />;
    };
}

export default withAuthCheck;