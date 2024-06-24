import React from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '@/store/store';
import { Container } from '@mui/material';

// Higher-order component to do an auth check before rendering a component
function withAuthCheck(title: string, WrappedComponent: React.ComponentType) {
    return function ProtectedComponent(props: any) {
        const isLoggedIn = useSelector((state: RootState) => state.user.isLoggedIn);

        if (!isLoggedIn) {
            return (
                <Container>
                    <h1>{title}</h1>
                    <p>Please log in to view this page</p>
                </Container>
            );
        }

        return <WrappedComponent {...props} />;
    };
}

export default withAuthCheck;