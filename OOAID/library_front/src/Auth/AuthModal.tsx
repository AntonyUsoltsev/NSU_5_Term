// AuthModal.jsx
import React from 'react';
import { Modal } from 'antd';
import AuthForm from './AuthForm'; // Подключите компонент с формой авторизации

const AuthModal = ({ visible, onClose , onAuthenticationSuccess}) => {
    return (
        <Modal title="Авторизация" visible={visible} onCancel={onClose} footer={null}>
            <AuthForm  onAuthenticationSuccess={onAuthenticationSuccess} />
        </Modal>
    );
};

export default AuthModal;
