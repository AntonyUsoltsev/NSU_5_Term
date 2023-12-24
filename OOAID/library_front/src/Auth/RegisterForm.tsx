// RegisterForm.jsx
import React from 'react';
import {Form, Input, Button, message} from 'antd';
import axios from 'axios';

const RegisterForm = ({onRegistrationSuccess}) => {
    const onFinish = (values: any) => {
        // Здесь обработайте отправку данных на бэкенд для регистрации
        // Используйте axios или другую библиотеку для выполнения запроса к вашему API

        const endpoint = 'http://localhost:8080/auth/student_compass/register';

        axios
            .post(endpoint, values)
            .then((response) => {
                console.log('Успешная регистрация:', response.data);
                message.success('Успешная регистрация');
                localStorage.setItem('token', response.data.token);
                // Вызовите колбэк для закрытия модального окна
                const username = values.firstname;
                onRegistrationSuccess(username);
            })
            .catch((error) => {
                console.error('Ошибка регистрации:', error);
                message.error('Ошибка регистрации. Проверьте введенные данные.');
            });
    };

    return (
        <Form onFinish={onFinish}>
            <Form.Item name="firstname" rules={[{required: true, message: 'Введите Имя'}]}>
                <Input placeholder="Имя"/>
            </Form.Item>
            <Form.Item name="secondname" rules={[{required: true, message: 'Введите Фамилию'}]}>
                <Input placeholder="Фамилия"/>
            </Form.Item>
            <Form.Item name="email" rules={[{required: true, message: 'Введите адрес эл. почты'}]}>
                <Input placeholder="Эл. почта"/>
            </Form.Item>
            <Form.Item name="password" rules={[{required: true, message: 'Введите пароль'}]}>
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
