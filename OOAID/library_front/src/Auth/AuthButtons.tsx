import React, {useEffect, useState} from 'react';
import { Button, Space, Row, Avatar } from 'antd';
import AuthModal from './AuthModal';
import RegisterModal from './RegisterModal';

const AuthButtons = () => {
    const [authModalVisible, setAuthModalVisible] = useState(false);
    const [registerModalVisible, setRegisterModalVisible] = useState(false);
    const [isLoggedIn, setLoggedIn] = useState(false);
    const [userInitial, setUserInitial] = useState('');

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token){
            setLoggedIn(true);
            setUserInitial(token.charAt(0));
        }
    }, []);

    const showAuthModal = () => {
        setAuthModalVisible(true);
    };

    const showRegisterModal = () => {
        setRegisterModalVisible(true);
    };

    const closeAuthModal = () => {
        setAuthModalVisible(false);
    };

    const closeRegisterModal = () => {
        setRegisterModalVisible(false);
    };

    const handleAuthenticationSuccess = (username:any) => {
        setLoggedIn(true);
        setUserInitial(username.charAt(0));
        closeAuthModal();
    };

    const handleRegistrationSuccess = (username:any) => {
        setLoggedIn(true);
        setUserInitial(username.charAt(0));
        closeRegisterModal();
    };

    const buttonStyle = { width: '160px' };

    return (
        <Row justify="end" align="top" style={{ position: 'fixed', top: 10, right: 10 }}>
            {isLoggedIn ? (
                <Avatar>
                    {userInitial}
                </Avatar>
            ) : (
                <Space direction="vertical">
                    <Button style={buttonStyle} onClick={showAuthModal}>
                        Авторизоваться
                    </Button>
                    <Button style={buttonStyle} onClick={showRegisterModal}>
                        Зарегистрироваться
                    </Button>
                </Space>
            )}

            <AuthModal visible={authModalVisible} onClose={closeAuthModal} onAuthenticationSuccess={handleAuthenticationSuccess} />
            <RegisterModal visible={registerModalVisible} onClose={closeRegisterModal} onRegistrationSuccess={handleRegistrationSuccess} />
        </Row>
    );
};

export default AuthButtons;
