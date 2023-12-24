// RegisterModal.jsx
import React from 'react';
import { Modal } from 'antd';
import RegisterForm from './RegisterForm'; // Подключите компонент с формой регистрации

const RegisterModal = ({ visible, onClose }) => {
  return (
    <Modal title="Регистрация" visible={visible} onCancel={onClose} footer={null}>
      <RegisterForm onClose={onClose} />
    </Modal>
  );
};

export default RegisterModal;
