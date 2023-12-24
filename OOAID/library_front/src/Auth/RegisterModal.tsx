// RegisterModal.jsx
import React from 'react';
import { Modal } from 'antd';
import RegisterForm from './RegisterForm'; // Подключите компонент с формой регистрации

const RegisterModal = ({ visible, onClose, onRegistrationSuccess }) => {
  return (
    <Modal title="Регистрация" visible={visible} onCancel={onClose} footer={null}>
      <RegisterForm  onRegistrationSuccess={onRegistrationSuccess} />
    </Modal>
  );
};

export default RegisterModal;
