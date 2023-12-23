// AuthenticationForm.jsx
import React, { useState } from 'react';
import { Form, Input, Button, message } from 'antd';
import axios from 'axios';

const AuthenticationForm = ({ onAuthenticationSuccess, onRegistrationSuccess }) => {
    const [form] = Form.useForm();
    const [isRegistration, setIsRegistration] = useState(false);

    const handleAuthentication = (values) => {
        // Здесь обработайте отправку данных на бэкенд для авторизации/регистрации
        // Используйте axios или другую библиотеку для выполнения запроса к вашему API

        const endpoint = isRegistration
            ? 'http://localhost:8080/student_compass/register'
            : 'http://localhost:8080/student_compass/authenticate';

        axios.post(endpoint, values)
            .then((response) => {
                // В случае успешной авторизации/регистрации
                // Вызвать соответствующий колбэк
                if (isRegistration) {
                    onRegistrationSuccess();
                } else {
                    onAuthenticationSuccess();
                }

                console.log('Успешная авторизация/регистрация:', response.data);
                message.success('Успешная авторизация/регистрация');
            })
            .catch((error) => {
                // В случае ошибки отобразите сообщение об ошибке
                console.error('Ошибка авторизации/регистрации:', error);
                message.error('Ошибка авторизации/регистрации. Проверьте введенные данные.');
            });
    };

    return (
        <Form form={form} onFinish={handleAuthentication}>
            <Form.Item label="Имя" name="username" rules={[{ required: true, message: 'Введите ваше имя' }]}>
                <Input />
            </Form.Item>
            <Form.Item label="Фамилия" name="surname" rules={[{ required: true, message: 'Введите вашу фамилию' }]}>
                <Input />
            </Form.Item>
            {isRegistration && (
                <>
                    <Form.Item label="Пароль" name="password" rules={[{ required: true, message: 'Введите пароль' }]}>
                        <Input.Password />
                    </Form.Item>
                    <Form.Item
                        label="Подтвердите пароль"
                        name="confirmPassword"
                        dependencies={['password']}
                        rules={[
                            { required: true, message: 'Подтвердите пароль' },
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue('password') === value) {
                                        return Promise.resolve();
                                    }
                                    return Promise.reject('Пароли не совпадают');
                                },
                            }),
                        ]}
                    >
                        <Input.Password />
                    </Form.Item>
                </>
            )}
            <Form.Item>
                <Button type="primary" htmlType="submit">
                    {isRegistration ? 'Зарегистрироваться' : 'Войти'}
                </Button>
                {!isRegistration && (
                    <Button type="link" onClick={() => setIsRegistration(true)}>
                        Регистрация
                    </Button>
                )}
            </Form.Item>
        </Form>
    );
};

export default AuthenticationForm;
