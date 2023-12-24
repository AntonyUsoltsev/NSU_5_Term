// AuthComponent.jsx
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button, Modal } from 'antd';
import AuthenticationForm from "./AuthPage";

const AuthComponent = () => {
    const [isModalVisible, setIsModalVisible] = useState(false);

    const handleAuthenticationSuccess = () => {
        console.log('Успешная авторизация');
        Modal.destroyAll();
    };

    const handleRegistrationSuccess = () => {
        console.log('Успешная регистрация');
        Modal.destroyAll();
    };

    const showModal = () => {
        setIsModalVisible(true);
    };

    const handleCancel = () => {
        setIsModalVisible(false);
    };

    return (
        <div>
            <Button type="link" onClick={showModal}>
                Авторизация/Регистрация
            </Button>
            <Modal
                title="Авторизация/Регистрация"
                visible={isModalVisible}
                onCancel={handleCancel}
                footer={null}
            >
                <AuthenticationForm
                    onAuthenticationSuccess={handleAuthenticationSuccess}
                    onRegistrationSuccess={handleRegistrationSuccess}
                />
            </Modal>
            <Link to="/login">Войти</Link>
            <Link to="/register">Зарегистрироваться</Link>
        </div>
    );
};

export default AuthComponent;
