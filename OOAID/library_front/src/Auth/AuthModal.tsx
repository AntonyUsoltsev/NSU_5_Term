// AuthModal.jsx
import React from 'react';
import { Modal } from 'antd';
import AuthForm from './AuthForm'; // Подключите компонент с формой авторизации

const AuthModal = ({ visible, onClose }) => {
    return (
        <Modal title="Авторизация" visible={visible} onCancel={onClose} footer={null}>
            <AuthForm onClose={onClose} />
        </Modal>
    );
};

export default AuthModal;
