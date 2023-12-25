import React, {useState, useEffect} from 'react';
import {List, Button, message, Spin, Form, Input} from 'antd';
import PostService from "../postService/PostService";
import ReviewList from "../reviewList/ReviewList";
import {useHistory, useParams} from "react-router-dom";
import "./MaterialStyle.css";
import axios from "axios";

const BookList = () => {
    const [data, setData]: any = useState();
    const [books, setBooks]: any = useState([]);
    const [reviews, setReviews]: any = useState([]);
    const [loading, setLoading] = useState(true);

    const {university, course, subject}: any = useParams();
    const [form] = Form.useForm();

    useEffect(() => {
        // Загрузка списка книг для выбранного предмета
        PostService.getBooks(university, course, subject).then((response: any) => {
            const inputData = response.data;
            setData(inputData);
            setBooks(inputData.materials);
            setReviews(inputData.reviews);
            setLoading(false);
        });
    }, [university, course, subject]);

    const handleDownload = (bookLink: any) => {
        const isUserAuthenticated = checkUserAuthentication();

        if (isUserAuthenticated) {
            // Выполнить скачивание книги
            handleView(bookLink);
        } else {
            // Показать предупреждение
            showWarning();
        }
    };


    const checkUserAuthentication = () => {
        const token = localStorage.getItem('token');
        if (!token) {
            return false;
        }
        return true;
    };

    const showWarning = () => {
        message.warning('Для просмотра необходимо авторизоваться, необходимо авторизоваться.');
    };

    const handleAddMaterial = (values) => {
        const {author, name, link} = values;
        const token = localStorage.getItem('token');

        // Проверка наличия токена
        if (!token) {
            message.warning('Чтобы добавить материал, необходимо авторизоваться.');
            return;
        }

        // Отправка запроса на бэкэнд с данными нового материала и токеном пользователя
        axios.post(`http://localhost:8080/auth/material/${subject}`, {
            author: author,
            name: name,
            link: link,
        }, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then(response => {
                message.success('Материал успешно добавлен.');
                // Обновление списка материалов после успешной отправки
                setBooks([...books, response.data.materials[0]]);
                // Очистка полей ввода
                form.resetFields();
            })
            .catch(error => {
                console.error('Ошибка при добавлении материала:', error);
                message.error('Ошибка при добавлении материала. Пожалуйста, попробуйте еще раз.');
            });
    };
    const handleView = (link) => {
        window.open(link, '_blank'); // Открываем ссылку в новой вкладке
    };

    return (
        <div>
            {loading ? (
                <Spin size="large"/>
            ) : (
                <>
                    <header className="subjects-header">Список книг для предмета {data.name}</header>
                    <List
                        dataSource={books}
                        renderItem={(item: any) => (
                            <List.Item>
                                {item.name} - {item.author}
                                <Button onClick={() => handleDownload(item.link)}>Посмотреть</Button>
                            </List.Item>
                        )}
                    />
                    <ReviewList selectedSubject={subject} selectedSubjectName={data.name} inputReviews={reviews}/>

                    {/* Форма для ввода нового материала */}
                    <Form
                        name="addMaterial"
                        onFinish={handleAddMaterial}
                        style={{marginTop: '20px'}}
                        form={form}
                    >
                        <Form.Item
                            label="Автор"
                            name="author"
                            rules={[{required: true, message: 'Пожалуйста, введите автора'}]}
                        >
                            <Input/>
                        </Form.Item>
                        <Form.Item
                            label="Название"
                            name="name"
                            rules={[{required: true, message: 'Пожалуйста, введите название'}]}
                        >
                            <Input/>
                        </Form.Item>
                        <Form.Item
                            label="Ссылка на книгу"
                            name="link"
                            rules={[{required: true, message: 'Пожалуйста, введите ссылку на книгу'}]}
                        >
                            <Input/>
                        </Form.Item>
                        <Form.Item>
                            <Button type="primary" htmlType="submit">
                                Добавить материал
                            </Button>
                        </Form.Item>
                    </Form>
                </>
            )}
        </div>
    );
};

export default BookList;
