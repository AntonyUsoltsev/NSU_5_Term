// AuthForm.jsx
import React from 'react';
import {Form, Input, Button, message} from 'antd';
import axios from 'axios';

const AuthForm = ({onClose, onAuthenticationSuccess}) => {
    const onFinish = (values) => {
        // Здесь обработайте отправку данных на бэкенд для авторизации
        // Используйте axios или другую библиотеку для выполнения запроса к вашему API

        const endpoint = 'http://localhost:8080/auth/student_compass/authenticate';

        axios
            .post(endpoint, values)
            .then((response) => {
                console.log('Успешная авторизация:', response.data);
                message.success('Успешная авторизация');
                localStorage.setItem('token', response.data.token);
                const username = values.firstname;
                onAuthenticationSuccess(username);
                onClose();
            })
            .catch((error) => {
                console.error('Ошибка авторизации:', error);
                message.error('Ошибка авторизации. Проверьте введенные данные.');
            });
    };

    return (
        <Form onFinish={onFinish}>
            <Form.Item name="email" rules={[{required: true, message: 'Введите адрес эл. почты'}]}>
                <Input placeholder="Эл. почта"/>
            </Form.Item>
            <Form.Item name="password" rules={[{required: true, message: 'Введите пароль'}]}>
                <Input.Password placeholder="Пароль"/>
            </Form.Item>
            <Form.Item>
                <Button type="primary" htmlType="submit">
                    Войти
                </Button>
            </Form.Item>
        </Form>
    );
};

export default AuthForm;
