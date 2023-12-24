// RegisterForm.jsx
import React from 'react';
import {Form, Input, Button, message} from 'antd';
import axios from 'axios';

const RegisterForm = ({onClose}) => {
    const onFinish = (values) => {
        // Здесь обработайте отправку данных на бэкенд для регистрации
        // Используйте axios или другую библиотеку для выполнения запроса к вашему API

        const endpoint = 'http://localhost:8080/student_compass/register';

        axios
            .post(endpoint, values)
            .then((response) => {
                console.log('Успешная регистрация:', response.data);
                message.success('Успешная регистрация');
                // Вызовите колбэк для закрытия модального окна
                onClose();
            })
            .catch((error) => {
                console.error('Ошибка регистрации:', error);
                message.error('Ошибка регистрации. Проверьте введенные данные.');
            });
    };

    return (
        <Form onFinish={onFinish}>
            <Form.Item name="name" rules={[{required: true, message: 'Name'}]}>
                <Input placeholder="Имя"/>
            </Form.Item>
            <Form.Item name="surname" rules={[{required: true, message: 'SecondName'}]}>
                <Input placeholder="Фамилия"/>
            </Form.Item>
            <Form.Item name="email" rules={[{required: true, message: 'Email'}]}>
                <Input placeholder="Эл. почта"/>
            </Form.Item>
            <Form.Item name="password" rules={[{required: true, message: 'Password'}]}>
                <Input.Password placeholder="Пароль"/>
            </Form.Item>

            <Form.Item>
                <Button type="primary" htmlType="submit">
                    Зарегистрироваться
                </Button>
            </Form.Item>
        </Form>
    );
};

export default RegisterForm;
