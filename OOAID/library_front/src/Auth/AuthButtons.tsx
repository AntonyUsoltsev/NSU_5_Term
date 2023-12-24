// AuthButtons.jsx
import React, { useState } from 'react';
import { Button, Space, Row } from 'antd';
import AuthModal from './AuthModal';
import RegisterModal from './RegisterModal';

const AuthButtons = () => {
    const [authModalVisible, setAuthModalVisible] = useState(false);
    const [registerModalVisible, setRegisterModalVisible] = useState(false);

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

    const buttonStyle = { width: '160px' }; // Задаем фиксированную ширину

    return (
        <Row justify="end" align="top" style={{ position: 'fixed', top: 10, right: 10 }}>
            <Space direction="vertical">
                <Button style={buttonStyle} onClick={showAuthModal}>
                    Авторизоваться
                </Button>
                <Button style={buttonStyle} onClick={showRegisterModal}>
                    Зарегистрироваться
                </Button>
            </Space>

            <AuthModal visible={authModalVisible} onClose={closeAuthModal} />
            <RegisterModal visible={registerModalVisible} onClose={closeRegisterModal} />
        </Row>
    );
};

export default AuthButtons;
